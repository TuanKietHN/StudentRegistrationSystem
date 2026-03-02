package vn.com.nws.cms.modules.academic.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.com.nws.cms.common.exception.BusinessException;
import vn.com.nws.cms.modules.academic.api.dto.*;
import vn.com.nws.cms.modules.academic.domain.enums.AttendanceStatus;
import vn.com.nws.cms.modules.academic.domain.enums.EnrollmentStatus;
import vn.com.nws.cms.modules.academic.domain.model.AttendanceRecord;
import vn.com.nws.cms.modules.academic.domain.model.AttendanceSession;
import vn.com.nws.cms.modules.academic.domain.model.Cohort;
import vn.com.nws.cms.modules.academic.domain.model.Enrollment;
import vn.com.nws.cms.modules.academic.domain.model.Student;
import vn.com.nws.cms.modules.academic.domain.repository.AttendanceRecordRepository;
import vn.com.nws.cms.modules.academic.domain.repository.AttendanceSessionRepository;
import vn.com.nws.cms.modules.academic.domain.repository.CohortRepository;
import vn.com.nws.cms.modules.academic.domain.repository.EnrollmentRepository;
import vn.com.nws.cms.modules.academic.infrastructure.persistence.entity.AttendanceRecordEntity;
import vn.com.nws.cms.modules.academic.infrastructure.persistence.repository.AttendanceRecordJpaRepository;
import vn.com.nws.cms.modules.academic.infrastructure.persistence.repository.AttendanceSessionJpaRepository;
import vn.com.nws.cms.modules.academic.infrastructure.persistence.repository.JpaEnrollmentRepository;
import vn.com.nws.cms.modules.auth.domain.model.User;
import vn.com.nws.cms.modules.auth.domain.repository.UserRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class AttendanceServiceImpl implements AttendanceService {

    private final CohortRepository cohortRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final AttendanceSessionRepository sessionRepository;
    private final AttendanceRecordRepository recordRepository;
    private final AttendanceRecordJpaRepository recordJpaRepository;
    private final AttendanceSessionJpaRepository sessionJpaRepository;
    private final JpaEnrollmentRepository enrollmentJpaRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public AttendanceSessionResponse openSession(String username, boolean isAdmin, boolean isTeacher, AttendanceSessionOpenRequest request) {
        Cohort cohort = cohortRepository.findById(request.getCohortId())
                .orElseThrow(() -> new BusinessException("Cohort not found"));
        if (isTeacher) {
            assertTeacherOwnsCohort(username, cohort);
        }

        LocalDate sessionDate = request.getSessionDate() != null ? request.getSessionDate() : LocalDate.now();
        short periods = request.getPeriods() != null ? request.getPeriods().shortValue() : (short) 3;
        if (periods < 1) periods = 1;

        User currentUser = resolveUser(username);

        AttendanceSession session = sessionRepository.findByCourseIdAndSessionDate(cohort.getId(), sessionDate)
                .orElseGet(() -> AttendanceSession.builder()
                        .cohortId(cohort.getId())
                        .sessionDate(sessionDate)
                        .build());

        LocalDateTime now = LocalDateTime.now();
        session.setPeriods(periods);
        session.setOpenedAt(now);
        session.setClosesAt(now.plusMinutes(15));
        session.setCreatedByUserId(currentUser.getId());
        session = sessionRepository.save(session);

        ensureRecords(session.getId(), cohort.getId());
        return toSessionResponse(session);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AttendanceSessionResponse> listCohortSessions(Long cohortId, String username, boolean isAdmin, boolean isTeacher) {
        Cohort cohort = cohortRepository.findById(cohortId).orElseThrow(() -> new BusinessException("Cohort not found"));
        if (isTeacher) {
            assertTeacherOwnsCohort(username, cohort);
        }
        return sessionRepository.findByCourseId(cohortId).stream().map(this::toSessionResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public AttendanceSessionRosterResponse getSessionRoster(Long sessionId, String username, boolean isAdmin, boolean isTeacher) {
        AttendanceSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new BusinessException("Không tìm thấy buổi điểm danh"));
        Cohort cohort = cohortRepository.findById(session.getCohortId())
                .orElseThrow(() -> new BusinessException("Cohort not found"));
        if (isTeacher) {
            assertTeacherOwnsCohort(username, cohort);
        }

        List<Enrollment> enrollments = enrollmentRepository.findByCourseId(cohort.getId()).stream()
                .filter(e -> e.getStatus() == EnrollmentStatus.ENROLLED)
                .toList();

        Map<Long, AttendanceRecordEntity> recordByEnrollment = new HashMap<>();
        for (AttendanceRecordEntity r : recordJpaRepository.findBySessionId(sessionId)) {
            if (r.getEnrollment() != null && r.getEnrollment().getId() != null) {
                recordByEnrollment.put(r.getEnrollment().getId(), r);
            }
        }

        Map<Long, BigDecimal> absentEquivalentPeriodsByEnrollment = computeAbsentEquivalentPeriods(cohort.getId());
        BigDecimal totalPeriods = totalPeriods(cohort);
        BigDecimal limit = totalPeriods.multiply(BigDecimal.valueOf(0.2)).setScale(2, RoundingMode.HALF_UP);

        List<AttendanceRosterRowResponse> rows = enrollments.stream().map(en -> {
            Student s = en.getStudent();
            AttendanceRecordEntity rec = recordByEnrollment.get(en.getId());
            AttendanceStatus st = rec != null ? rec.getStatus() : AttendanceStatus.ABSENT;
            BigDecimal absentEq = absentEquivalentPeriodsByEnrollment.getOrDefault(en.getId(), BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);
            boolean banned = absentEq.compareTo(limit) > 0;
            return AttendanceRosterRowResponse.builder()
                    .enrollmentId(en.getId())
                    .studentId(s != null ? s.getId() : null)
                    .studentCode(s != null ? s.getStudentCode() : null)
                    .username(s != null && s.getUser() != null ? s.getUser().getUsername() : null)
                    .email(s != null && s.getUser() != null ? s.getUser().getEmail() : null)
                    .phone(s != null ? s.getPhone() : null)
                    .departmentCode(s != null && s.getDepartment() != null ? s.getDepartment().getCode() : null)
                    .departmentName(s != null && s.getDepartment() != null ? s.getDepartment().getName() : null)
                    .adminClassCode(s != null && s.getAdminClass() != null ? s.getAdminClass().getCode() : null)
                    .adminClassName(s != null && s.getAdminClass() != null ? s.getAdminClass().getName() : null)
                    .attendanceStatus(st)
                    .markedAt(rec != null ? rec.getMarkedAt() : null)
                    .note(rec != null ? rec.getNote() : null)
                    .absentEquivalentPeriods(absentEq)
                    .absentLimitPeriods(limit)
                    .bannedExam(banned)
                    .build();
        }).toList();

        return AttendanceSessionRosterResponse.builder()
                .session(toSessionResponse(session))
                .students(rows)
                .build();
    }

    @Override
    @Transactional
    public AttendanceSessionRosterResponse markAttendance(Long sessionId, Long enrollmentId, String username, boolean isAdmin, boolean isTeacher, AttendanceMarkRequest request) {
        AttendanceSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new BusinessException("Không tìm thấy buổi điểm danh"));
        Cohort cohort = cohortRepository.findById(session.getCohortId())
                .orElseThrow(() -> new BusinessException("Cohort not found"));
        if (isTeacher) {
            assertTeacherOwnsCohort(username, cohort);
        }

        if (request.getStatus() == AttendanceStatus.PRESENT) {
            LocalDateTime now = LocalDateTime.now();
            if (session.getClosesAt() != null && now.isAfter(session.getClosesAt())) {
                throw new BusinessException("Đã quá 15 phút, không thể điểm danh có mặt");
            }
        }

        AttendanceRecordEntity entity = recordJpaRepository.findBySessionIdAndEnrollmentId(sessionId, enrollmentId)
                .orElseGet(() -> {
                    var sess = sessionJpaRepository.findById(sessionId).orElseThrow();
                    var enr = enrollmentJpaRepository.findById(enrollmentId).orElseThrow();
                    var stu = enr.getStudent();
                    AttendanceRecordEntity r = new AttendanceRecordEntity();
                    r.setSession(sess);
                    r.setEnrollment(enr);
                    r.setStudent(stu);
                    r.setStatus(AttendanceStatus.ABSENT);
                    return r;
                });

        entity.setStatus(request.getStatus());
        entity.setNote(request.getNote());
        entity.setMarkedAt(LocalDateTime.now());
        recordJpaRepository.save(entity);

        return getSessionRoster(sessionId, username, isAdmin, isTeacher);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isExamBanned(Long cohortId, Long enrollmentId) {
        Cohort cohort = cohortRepository.findById(cohortId).orElseThrow(() -> new BusinessException("Cohort not found"));
        BigDecimal totalPeriods = totalPeriods(cohort);
        BigDecimal limit = totalPeriods.multiply(BigDecimal.valueOf(0.2));
        BigDecimal absentEq = computeAbsentEquivalentPeriods(cohortId).getOrDefault(enrollmentId, BigDecimal.ZERO);
        return absentEq.compareTo(limit) > 0;
    }

    private void ensureRecords(Long sessionId, Long courseId) {
        List<Long> existing = recordRepository.findBySessionId(sessionId).stream()
                .map(AttendanceRecord::getEnrollmentId)
                .filter(Objects::nonNull)
                .toList();

        List<Enrollment> enrollments = enrollmentRepository.findByCourseId(courseId).stream()
                .filter(e -> e.getStatus() == EnrollmentStatus.ENROLLED)
                .toList();

        List<AttendanceRecord> toCreate = enrollments.stream()
                .filter(e -> e.getId() != null && !existing.contains(e.getId()))
                .map(e -> AttendanceRecord.builder()
                        .sessionId(sessionId)
                        .enrollmentId(e.getId())
                        .studentId(e.getStudent() != null ? e.getStudent().getId() : null)
                        .status(AttendanceStatus.ABSENT)
                        .build())
                .filter(r -> r.getStudentId() != null)
                .toList();

        if (!toCreate.isEmpty()) {
            recordRepository.saveAll(toCreate);
        }
    }

    private Map<Long, BigDecimal> computeAbsentEquivalentPeriods(Long cohortId) {
        Map<Long, BigDecimal> map = new HashMap<>();
        for (AttendanceRecordEntity r : recordJpaRepository.findByCohortId(cohortId)) {
            if (r.getEnrollment() == null || r.getEnrollment().getId() == null) continue;
            long enrollmentId = r.getEnrollment().getId();
            short periods = r.getSession() != null ? r.getSession().getPeriods() : 0;
            BigDecimal add = BigDecimal.ZERO;
            if (r.getStatus() == AttendanceStatus.ABSENT || r.getStatus() == AttendanceStatus.EXCUSED) {
                add = BigDecimal.valueOf(periods);
            } else if (r.getStatus() == AttendanceStatus.LATE) {
                add = BigDecimal.valueOf(periods).multiply(BigDecimal.valueOf(0.5));
            }
            map.put(enrollmentId, map.getOrDefault(enrollmentId, BigDecimal.ZERO).add(add));
        }
        return map;
    }

    private BigDecimal totalPeriods(Cohort cohort) {
        if (cohort.getClazz() == null || cohort.getClazz().getCredits() == null) return BigDecimal.ZERO;
        return BigDecimal.valueOf(cohort.getClazz().getCredits()).multiply(BigDecimal.valueOf(15));
    }

    private AttendanceSessionResponse toSessionResponse(AttendanceSession s) {
        return AttendanceSessionResponse.builder()
                .id(s.getId())
                .cohortId(s.getCohortId())
                .sessionDate(s.getSessionDate())
                .periods(s.getPeriods())
                .openedAt(s.getOpenedAt())
                .closesAt(s.getClosesAt())
                .createdAt(s.getCreatedAt())
                .updatedAt(s.getUpdatedAt())
                .build();
    }

    private User resolveUser(String username) {
        return userRepository.findByUsername(username)
                .or(() -> userRepository.findByEmail(username))
                .orElseThrow(() -> new BusinessException("User not found"));
    }

    private void assertTeacherOwnsCohort(String username, Cohort cohort) {
        User currentUser = resolveUser(username);
        if (cohort.getTeacher() == null || cohort.getTeacher().getId() == null || !cohort.getTeacher().getId().equals(currentUser.getId())) {
            throw new BusinessException("Bạn không có quyền thao tác lớp học phần này");
        }
    }
}
