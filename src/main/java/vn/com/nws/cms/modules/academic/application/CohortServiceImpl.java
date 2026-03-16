package vn.com.nws.cms.modules.academic.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.com.nws.cms.common.dto.PageResponse;
import vn.com.nws.cms.common.exception.BusinessException;
import vn.com.nws.cms.modules.academic.api.dto.*;
import vn.com.nws.cms.modules.academic.domain.enums.CohortLifecycleStatus;
import vn.com.nws.cms.modules.academic.domain.model.*;
import vn.com.nws.cms.modules.academic.domain.repository.*;
import vn.com.nws.cms.modules.auth.domain.model.User;
import vn.com.nws.cms.modules.auth.domain.repository.UserRepository;
import vn.com.nws.cms.modules.iam.api.dto.UserResponse;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CohortServiceImpl implements CohortService {

    private final CohortRepository cohortRepository;
    private final ClassRepository classRepository;
    private final SemesterRepository semesterRepository;
    private final UserRepository userRepository;
    private final CohortTimeSlotRepository cohortTimeSlotRepository;
    private final EnrollmentRepository enrollmentRepository;

    @Override
    public PageResponse<CohortResponse> getCohorts(CohortFilterRequest request) {
        List<Cohort> cohorts = cohortRepository.search(
                request.getKeyword(),
                request.getSemesterId(),
                request.getClassId(),
                request.getTeacherId(),
                request.getActive(),
                request.getStatus(),
                request.getPage(),
                request.getSize());

        long totalElements = cohortRepository.count(
                request.getKeyword(),
                request.getSemesterId(),
                request.getClassId(),
                request.getTeacherId(),
                request.getActive(),
                request.getStatus());

        int totalPages = (int) Math.ceil((double) totalElements / request.getSize());

        List<CohortResponse> responses = cohorts.stream().map(this::toResponse).collect(Collectors.toList());

        return PageResponse.<CohortResponse>builder()
                .page(request.getPage())
                .size(request.getSize())
                .totalElements(totalElements)
                .totalPages(totalPages)
                .data(responses)
                .build();
    }

    @Override
    public CohortResponse getCohortById(Long id) {
        Cohort cohort = cohortRepository.findById(id).orElseThrow(() -> new BusinessException("Cohort not found"));
        return toResponse(cohort);
    }

    @Override
    @Transactional
    public CohortResponse createCohort(CohortCreateRequest request) {
        if (cohortRepository.existsByCode(request.getCode())) {
            throw new BusinessException("Cohort code already exists");
        }

        CourseClass clazz = classRepository.findById(request.getClassId())
                .orElseThrow(() -> new BusinessException("Class not found"));

        Semester semester = semesterRepository.findById(request.getSemesterId())
                .orElseThrow(() -> new BusinessException("Semester not found"));

        User teacher = null;
        if (request.getTeacherId() != null) {
            teacher = userRepository.findById(request.getTeacherId())
                    .orElseThrow(() -> new BusinessException("Teacher not found"));
        }

        Cohort cohort = Cohort.builder()
                .name(request.getName())
                .code(request.getCode())
                .maxStudents(request.getMaxStudents())
                .minStudents(request.getMinStudents() != null ? request.getMinStudents() : 0)
                .currentStudents(0)
                .active(request.isActive())
                .status(request.getStatus() != null ? request.getStatus() : CohortLifecycleStatus.OPEN)
                .clazz(clazz)
                .semester(semester)
                .teacher(teacher)
                .enrollmentStartDate(request.getEnrollmentStartDate() != null ? request.getEnrollmentStartDate() : LocalDate.now())
                .enrollmentEndDate(request.getEnrollmentEndDate() != null ? request.getEnrollmentEndDate() : LocalDate.now().plusDays(365))
                .registrationEnabled(request.isRegistrationEnabled())
                .build();

        validateEnrollmentWindow(cohort.getEnrollmentStartDate(), cohort.getEnrollmentEndDate());
        cohort = cohortRepository.save(cohort);
        return toResponse(cohort);
    }

    @Override
    @Transactional
    public CohortResponse updateCohort(Long id, CohortUpdateRequest request) {
        Cohort cohort = cohortRepository.findById(id).orElseThrow(() -> new BusinessException("Cohort not found"));

        if (request.getCode() != null && !request.getCode().equals(cohort.getCode())) {
            if (cohortRepository.existsByCode(request.getCode())) {
                throw new BusinessException("Cohort code already exists");
            }
            cohort.setCode(request.getCode());
        }

        if (request.getName() != null) cohort.setName(request.getName());
        if (request.getMaxStudents() != null) cohort.setMaxStudents(request.getMaxStudents());
        if (request.getMinStudents() != null) cohort.setMinStudents(request.getMinStudents());
        if (request.getActive() != null) cohort.setActive(request.getActive());
        if (request.getStatus() != null) cohort.setStatus(request.getStatus());
        if (request.getCanceledReason() != null) cohort.setCanceledReason(request.getCanceledReason());

        if (request.getClassId() != null) {
            CourseClass clazz = classRepository.findById(request.getClassId())
                    .orElseThrow(() -> new BusinessException("Class not found"));
            cohort.setClazz(clazz);
        }

        if (request.getSemesterId() != null) {
            Semester semester = semesterRepository.findById(request.getSemesterId())
                    .orElseThrow(() -> new BusinessException("Semester not found"));
            cohort.setSemester(semester);
        }

        if (request.getTeacherId() != null) {
            User teacher = userRepository.findById(request.getTeacherId())
                    .orElseThrow(() -> new BusinessException("Teacher not found"));
            cohort.setTeacher(teacher);
        }

        if (request.getEnrollmentStartDate() != null) {
            cohort.setEnrollmentStartDate(request.getEnrollmentStartDate());
        }
        if (request.getEnrollmentEndDate() != null) {
            cohort.setEnrollmentEndDate(request.getEnrollmentEndDate());
        }
        if (request.getRegistrationEnabled() != null) {
            cohort.setRegistrationEnabled(request.getRegistrationEnabled());
        }
        validateEnrollmentWindow(cohort.getEnrollmentStartDate(), cohort.getEnrollmentEndDate());

        cohort = cohortRepository.save(cohort);
        return toResponse(cohort);
    }

    @Override
    @Transactional
    public CohortResponse cancelCohort(Long id, CohortCancelRequest request) {
        Cohort cohort = cohortRepository.findById(id).orElseThrow(() -> new BusinessException("Cohort not found"));

        cohort.setStatus(CohortLifecycleStatus.CANCELED);
        cohort.setActive(false);
        cohort.setCanceledAt(LocalDateTime.now());
        if (request != null && request.getReason() != null && !request.getReason().isBlank()) {
            cohort.setCanceledReason(request.getReason());
        }

        enrollmentRepository.findByCourseId(id).forEach(e -> enrollmentRepository.deleteById(e.getId()));
        cohort.setCurrentStudents(0);

        cohort = cohortRepository.save(cohort);
        return toResponse(cohort);
    }

    @Override
    @Transactional
    public CohortResponse mergeCohorts(CohortMergeRequest request) {
        if (request.getTargetCohortId() == null) {
            throw new BusinessException("Target cohort is required");
        }
        if (request.getSourceCohortIds() == null || request.getSourceCohortIds().isEmpty()) {
            throw new BusinessException("Source cohorts are required");
        }

        Set<Long> sourceIds = new HashSet<>(request.getSourceCohortIds());
        sourceIds.remove(request.getTargetCohortId());
        if (sourceIds.isEmpty()) {
            throw new BusinessException("Source cohorts are required");
        }

        Cohort target = cohortRepository.findById(request.getTargetCohortId())
                .orElseThrow(() -> new BusinessException("Target cohort not found"));
        if (target.getStatus() != CohortLifecycleStatus.OPEN && target.getStatus() != CohortLifecycleStatus.CLOSED) {
            throw new BusinessException("Target cohort status is not mergeable");
        }

        var targetSlots = cohortTimeSlotRepository.findByCohortId(target.getId());

        int capacity = Math.max(0, target.getMaxStudents() - target.getCurrentStudents());
        int toMove = 0;

        for (Long srcId : sourceIds) {
            Cohort src = cohortRepository.findById(srcId)
                    .orElseThrow(() -> new BusinessException("Source cohort not found: " + srcId));

            if (!src.getSemester().getId().equals(target.getSemester().getId())) {
                throw new BusinessException("Chỉ được dồn lớp trong cùng học kỳ");
            }
            if (!src.getClazz().getId().equals(target.getClazz().getId())) {
                throw new BusinessException("Chỉ được dồn lớp trong cùng môn");
            }
            if (src.getStatus() == CohortLifecycleStatus.CANCELED || src.getStatus() == CohortLifecycleStatus.MERGED) {
                throw new BusinessException("Source cohort status is not mergeable: " + src.getCode());
            }

            var srcSlots = cohortTimeSlotRepository.findByCohortId(src.getId());
            if (!sameSchedule(srcSlots, targetSlots)) {
                throw new BusinessException("Chỉ hỗ trợ dồn lớp khi lịch học giống nhau");
            }

            for (var e : enrollmentRepository.findByCourseId(src.getId())) {
                Long studentProfileId = e.getStudent() != null ? e.getStudent().getId() : null;
                if (studentProfileId == null) {
                    continue;
                }
                boolean already = enrollmentRepository.existsByCourseIdAndStudentId(target.getId(), studentProfileId);
                if (!already) {
                    toMove++;
                }
            }
        }

        if (toMove > capacity) {
            throw new BusinessException("Lớp đích không đủ chỗ để dồn");
        }

        for (Long srcId : sourceIds) {
            Cohort src = cohortRepository.findById(srcId).orElseThrow();
            for (var e : enrollmentRepository.findByCourseId(src.getId())) {
                Long studentProfileId = e.getStudent() != null ? e.getStudent().getId() : null;
                if (studentProfileId == null) {
                    enrollmentRepository.deleteById(e.getId());
                    continue;
                }
                boolean already = enrollmentRepository.existsByCourseIdAndStudentId(target.getId(), studentProfileId);
                if (already) {
                    enrollmentRepository.deleteById(e.getId());
                } else {
                    e.setCohort(target);
                    enrollmentRepository.save(e);
                }
            }

            src.setStatus(CohortLifecycleStatus.MERGED);
            src.setActive(false);
            src.setMergedIntoCohortId(target.getId());
            src.setCanceledAt(LocalDateTime.now());
            if (request.getReason() != null && !request.getReason().isBlank()) {
                src.setCanceledReason(request.getReason());
            }
            src.setCurrentStudents((int) enrollmentRepository.countByCourseId(src.getId()));
            cohortRepository.save(src);
        }

        target.setCurrentStudents((int) enrollmentRepository.countByCourseId(target.getId()));
        target = cohortRepository.save(target);
        return toResponse(target);
    }

    @Override
    @Transactional
    public void deleteCohort(Long id) {
        if (cohortRepository.findById(id).isEmpty()) {
            throw new BusinessException("Cohort not found");
        }
        cohortRepository.deleteById(id);
    }

    @Override
    public List<CohortTimeSlotResponse> getCohortTimeSlots(Long cohortId) {
        return cohortTimeSlotRepository.findByCohortId(cohortId).stream()
                .map(CohortTimeSlotResponse::fromDomain)
                .toList();
    }

    @Override
    @Transactional
    public void replaceCohortTimeSlots(Long cohortId, List<CohortTimeSlotRequest> slots) {
        if (slots == null) {
            cohortTimeSlotRepository.replaceCohortTimeSlots(cohortId, List.of());
            return;
        }
        List<CohortTimeSlot> mapped = slots.stream().map(r -> CohortTimeSlot.builder()
                .cohortId(cohortId)
                .dayOfWeek(r.getDayOfWeek())
                .startTime(r.getStartTime())
                .endTime(r.getEndTime())
                .build()).toList();
        cohortTimeSlotRepository.replaceCohortTimeSlots(cohortId, mapped);
    }

    private CohortResponse toResponse(Cohort cohort) {
        List<CohortTimeSlotResponse> timeSlots = getCohortTimeSlots(cohort.getId());
        return CohortResponse.builder()
                .id(cohort.getId())
                .name(cohort.getName())
                .code(cohort.getCode())
                .maxStudents(cohort.getMaxStudents())
                .currentStudents(cohort.getCurrentStudents())
                .active(cohort.isActive())
                .status(cohort.getStatus())
                .minStudents(cohort.getMinStudents())
                .canceledAt(cohort.getCanceledAt())
                .canceledReason(cohort.getCanceledReason())
                .mergedIntoCohortId(cohort.getMergedIntoCohortId())
                .clazz(ClassResponse.builder()
                        .id(cohort.getClazz().getId())
                        .name(cohort.getClazz().getName())
                        .code(cohort.getClazz().getCode())
                        .credit(cohort.getClazz().getCredits())
                        .build())
                .semester(SemesterResponse.builder()
                        .id(cohort.getSemester().getId())
                        .name(cohort.getSemester().getName())
                        .code(cohort.getSemester().getCode())
                        .build())
                .teacher(cohort.getTeacher() != null ? UserResponse.builder()
                        .id(cohort.getTeacher().getId())
                        .username(cohort.getTeacher().getUsername())
                        .email(cohort.getTeacher().getEmail())
                        .role(cohort.getTeacher().getRoles().stream().map(Enum::name).collect(Collectors.joining(",")))
                        .build() : null)
                .enrollmentStartDate(cohort.getEnrollmentStartDate())
                .enrollmentEndDate(cohort.getEnrollmentEndDate())
                .registrationEnabled(cohort.isRegistrationEnabled())
                .timeSlots(timeSlots)
                .createdAt(cohort.getCreatedAt())
                .updatedAt(cohort.getUpdatedAt())
                .build();
    }

    private void validateEnrollmentWindow(LocalDate start, LocalDate end) {
        if (start == null || end == null) {
            throw new BusinessException("Khoảng thời gian mở đăng ký là bắt buộc");
        }
        if (start.isAfter(end)) {
            throw new BusinessException("Thời gian mở đăng ký không hợp lệ");
        }
    }

    private boolean sameSchedule(List<CohortTimeSlot> a, List<CohortTimeSlot> b) {
        if (a == null || a.isEmpty()) return b == null || b.isEmpty();
        if (b == null || b.isEmpty()) return false;
        if (a.size() != b.size()) return false;
        for (int i = 0; i < a.size(); i++) {
            CohortTimeSlot sa = a.get(i);
            CohortTimeSlot sb = b.get(i);
            if (sa.getDayOfWeek() != sb.getDayOfWeek()) return false;
            if (!sa.getStartTime().equals(sb.getStartTime())) return false;
            if (!sa.getEndTime().equals(sb.getEndTime())) return false;
        }
        return true;
    }
}
