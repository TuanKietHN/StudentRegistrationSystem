package vn.com.nws.cms.modules.academic.application.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import vn.com.nws.cms.common.exception.BusinessException;
import vn.com.nws.cms.modules.academic.api.dto.*;
import vn.com.nws.cms.modules.academic.application.SectionGradeService;
import vn.com.nws.cms.modules.academic.domain.enums.EnrollmentStatus;
import vn.com.nws.cms.modules.academic.domain.model.Enrollment;
import vn.com.nws.cms.modules.academic.domain.model.Section;
import vn.com.nws.cms.modules.academic.domain.repository.EnrollmentRepository;
import vn.com.nws.cms.modules.academic.domain.repository.SectionRepository;
import vn.com.nws.cms.modules.academic.domain.repository.SectionTimeSlotRepository;
import vn.com.nws.cms.modules.academic.domain.repository.TeacherRepository;
import vn.com.nws.cms.modules.auth.domain.model.User;
import vn.com.nws.cms.modules.auth.domain.repository.UserRepository;
import vn.com.nws.cms.modules.iam.api.dto.UserResponse;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SectionGradeServiceImpl implements SectionGradeService {

    private final EnrollmentRepository enrollmentRepository;
    private final SectionRepository sectionRepository;
    private final UserRepository userRepository;
    private final TeacherRepository teacherRepository;
    private final SectionTimeSlotRepository sectionTimeSlotRepository;

    @Override
    @Transactional(readOnly = true)
    public List<SectionGradeResponse> getSectionGrades(Long sectionId, String username, boolean isAdmin, boolean isTeacher) {
        Section section = sectionRepository.findById(sectionId).orElseThrow(() -> new BusinessException("Section not found"));

        if (isTeacher && !isAdmin) {
            assertTeacherOwnsSection(username, section);
        }

        return enrollmentRepository.findBySectionId(sectionId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public SectionGradeResponse updateGrade(Long enrollmentId, String username, boolean isAdmin, boolean isTeacher, SectionGradeUpdateRequest request) {
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new BusinessException("Enrollment not found"));

        Section section = enrollment.getSection();
        if (isTeacher) {
            assertTeacherOwnsSection(username, section);
        }

        if (request.getStatus() != null) {
            if (request.getStatus() == EnrollmentStatus.DROPPED
                    && enrollment.getStatus() != EnrollmentStatus.DROPPED) {
                section.setCurrentStudents(Math.max(0, section.getCurrentStudents() - 1));
                sectionRepository.save(section);
            }
            enrollment.setStatus(request.getStatus());
        }

        boolean hasComponentScores = request.getProcessScore() != null || request.getExamScore() != null;
        if (hasComponentScores) {
            if (!isAdmin && !isTeacher) {
                throw new BusinessException("Không có quyền nhập điểm");
            }
            if (isTeacher && enrollment.isScoreLocked()) {
                throw new BusinessException("Điểm đã được nhập. Vui lòng liên hệ Admin để thay đổi");
            }

            BigDecimal processScore = request.getProcessScore() == null ? enrollment.getProcessScore() : BigDecimal.valueOf(request.getProcessScore());
            BigDecimal examScore = request.getExamScore() == null ? enrollment.getExamScore() : BigDecimal.valueOf(request.getExamScore());
            if (processScore == null || examScore == null) {
                throw new BusinessException("Cần nhập đủ điểm quá trình và điểm thi");
            }

            BigDecimal finalScore = calculateFinalScore(section, processScore, examScore);

            if (isAdmin && enrollment.isScoreLocked()) {
                if (!enrollment.isScoreOverridden()) {
                    enrollment.setProcessScoreBeforeOverride(enrollment.getProcessScore());
                    enrollment.setExamScoreBeforeOverride(enrollment.getExamScore());
                    enrollment.setFinalScoreBeforeOverride(enrollment.getFinalScore());
                }
                enrollment.setScoreOverridden(true);
                enrollment.setScoreOverrideReason(request.getOverrideReason());
                enrollment.setScoreOverriddenAt(LocalDateTime.now());
            }

            enrollment.setProcessScore(processScore);
            enrollment.setExamScore(examScore);
            enrollment.setFinalScore(finalScore);
            if (!enrollment.isScoreLocked()) {
                enrollment.setScoredAt(LocalDateTime.now());
            }
            enrollment.setScoreLocked(true);
        }

        enrollment = enrollmentRepository.save(enrollment);
        return toResponse(enrollment);
    }

    @Override
    @Transactional
    public GradesImportResultResponse importSectionGrades(Long sectionId, String username, boolean isAdmin, boolean isTeacher, MultipartFile file) {
        if (!isAdmin && !isTeacher) {
            throw new BusinessException("Không có quyền import điểm");
        }
        Section section = sectionRepository.findById(sectionId).orElseThrow(() -> new BusinessException("Section not found"));
        if (isTeacher && !isAdmin) { // only assert if strictly teacher
            assertTeacherOwnsSection(username, section);
        }

        if (file == null || file.isEmpty()) {
            throw new BusinessException("File không hợp lệ");
        }

        List<Enrollment> enrollments = enrollmentRepository.findBySectionId(sectionId);
        Map<String, Enrollment> byStudentCode = new HashMap<>();
        Map<String, Enrollment> byUsername = new HashMap<>();
        for (Enrollment e : enrollments) {
            if (e.getStudent() == null || e.getStudent().getUser() == null) continue;
            if (e.getStudent().getStudentCode() != null) {
                byStudentCode.put(e.getStudent().getStudentCode().trim().toUpperCase(), e);
            }
            if (e.getStudent().getUser().getUsername() != null) {
                byUsername.put(e.getStudent().getUser().getUsername().trim().toLowerCase(), e);
            }
        }

        int totalRows = 0;
        int imported = 0;
        int skippedLocked = 0;
        int skippedNotFound = 0;
        int skippedInvalid = 0;
        List<String> errors = new ArrayList<>();

        try (var is = file.getInputStream();
             var wb = new org.apache.poi.xssf.usermodel.XSSFWorkbook(is)) {
            var sheet = wb.getNumberOfSheets() > 0 ? wb.getSheetAt(0) : null;
            if (sheet == null) {
                throw new BusinessException("File Excel không có sheet");
            }

            int firstRow = sheet.getFirstRowNum();
            var headerRow = sheet.getRow(firstRow);
            if (headerRow == null) {
                throw new BusinessException("File Excel thiếu header");
            }

            Map<String, Integer> headerIndex = new HashMap<>();
            for (int c = headerRow.getFirstCellNum(); c < headerRow.getLastCellNum(); c++) {
                var cell = headerRow.getCell(c);
                String v = cell == null ? null : cell.getStringCellValue();
                if (v == null) continue;
                headerIndex.put(v.trim().toLowerCase(), c);
            }

            Integer colStudentCode = findFirstHeader(headerIndex, "studentcode", "mssv", "ma sv", "mã sv", "student_code");
            Integer colUsername = findFirstHeader(headerIndex, "username", "tai khoan", "tài khoản", "user");
            Integer colProcess = findFirstHeader(headerIndex, "process", "diem qua trinh", "điểm quá trình", "qt");
            Integer colExam = findFirstHeader(headerIndex, "exam", "diem thi", "điểm thi", "thi");

            if ((colStudentCode == null && colUsername == null) || colProcess == null || colExam == null) {
                throw new BusinessException("Header cần có: studentCode/username, processScore, examScore");
            }

            for (int r = firstRow + 1; r <= sheet.getLastRowNum(); r++) {
                var row = sheet.getRow(r);
                if (row == null) continue;
                totalRows++;

                String keyStudentCode = colStudentCode == null ? null : readCellAsString(row.getCell(colStudentCode));
                String keyUsername = colUsername == null ? null : readCellAsString(row.getCell(colUsername));
                Enrollment enrollment = null;
                if (keyStudentCode != null && !keyStudentCode.isBlank()) {
                    enrollment = byStudentCode.get(keyStudentCode.trim().toUpperCase());
                }
                if (enrollment == null && keyUsername != null && !keyUsername.isBlank()) {
                    enrollment = byUsername.get(keyUsername.trim().toLowerCase());
                }
                if (enrollment == null) {
                    skippedNotFound++;
                    continue;
                }

                if (!isAdmin && enrollment.isScoreLocked()) {
                    skippedLocked++;
                    continue;
                }

                Double p = readCellAsDouble(row.getCell(colProcess));
                Double e = readCellAsDouble(row.getCell(colExam));
                if (p == null || e == null) {
                    skippedInvalid++;
                    continue;
                }

                SectionGradeUpdateRequest req = new SectionGradeUpdateRequest();
                req.setProcessScore(p);
                req.setExamScore(e);
                if (isAdmin && enrollment.isScoreLocked()) {
                    req.setOverrideReason("Excel import by admin");
                }

                SectionGradeResponse updated = updateGrade(enrollment.getId(), username, isAdmin, isTeacher, req);
                if (updated != null) imported++;
            }
        } catch (BusinessException be) {
            throw be;
        } catch (Exception ex) {
            throw new BusinessException("Không đọc được file Excel");
        }

        return GradesImportResultResponse.builder()
                .totalRows(totalRows)
                .imported(imported)
                .skippedLocked(skippedLocked)
                .skippedNotFound(skippedNotFound)
                .skippedInvalid(skippedInvalid)
                .errors(errors)
                .build();
    }

    private SectionGradeResponse toResponse(Enrollment enrollment) {
        Section section = enrollment.getSection();
        List<SectionTimeSlotResponse> timeSlots = sectionTimeSlotRepository.findBySectionId(section.getId())
                .stream().map(SectionTimeSlotResponse::fromDomain).toList();

        return SectionGradeResponse.builder()
                .enrollmentId(enrollment.getId())
                .id(enrollment.getId()) // Added `id` mapping so `e.id` on Vue frontend keeps working
                .status(enrollment.getStatus())
                .processScore(enrollment.getProcessScore())
                .examScore(enrollment.getExamScore())
                .finalScore(enrollment.getFinalScore())
                .scoreLocked(enrollment.isScoreLocked())
                .scoreOverridden(enrollment.isScoreOverridden())
                .createdAt(enrollment.getCreatedAt())
                .updatedAt(enrollment.getUpdatedAt())
                .section(SectionResponse.builder()
                        .id(section.getId())
                        .name(section.getName())
                        .code(section.getCode())
                        .maxStudents(section.getMaxStudents())
                        .currentStudents(section.getCurrentStudents())
                        .active(section.isActive())
                        .status(section.getStatus())
                        .minStudents(section.getMinStudents())
                        .canceledAt(section.getCanceledAt())
                        .canceledReason(section.getCanceledReason())
                        .mergedIntoSectionId(section.getMergedIntoSectionId())
                        .enrollmentStartDate(section.getEnrollmentStartDate())
                        .enrollmentEndDate(section.getEnrollmentEndDate())
                        .registrationEnabled(section.isRegistrationEnabled())
                        .semester(section.getSemester() != null
                                ? SemesterResponse.builder()
                                .id(section.getSemester().getId())
                                .name(section.getSemester().getName())
                                .code(section.getSemester().getCode())
                                .build()
                                : null)
                        .subject(section.getSubject() != null
                                ? SubjectResponse.builder()
                                .id(section.getSubject().getId())
                                .name(section.getSubject().getName())
                                .code(section.getSubject().getCode())
                                .credit(section.getSubject().getCredits())
                                .processWeight(section.getSubject().getProcessWeight())
                                .examWeight(section.getSubject().getExamWeight())
                                .build()
                                : null)
                        .teacher(section.getTeacher() != null
                                ? UserResponse.builder()
                                .id(section.getTeacher().getId())
                                .username(section.getTeacher().getUsername())
                                .email(section.getTeacher().getEmail())
                                .role(section.getTeacher().getRoles().stream().map(Enum::name).collect(Collectors.joining(",")))
                                .build()
                                : null)
                        .timeSlots(timeSlots)
                        .build())
                .student(enrollment.getStudent() != null
                        && enrollment.getStudent().getUser() != null
                        ? UserResponse.builder()
                        .id(enrollment.getStudent().getUser().getId())
                        .username(enrollment.getStudent().getUser().getUsername())
                        .email(enrollment.getStudent().getUser().getEmail())
                        .build()
                        : null)
                .studentCode(enrollment.getStudent() != null ? enrollment.getStudent().getStudentCode() : null)
                .studentPhone(enrollment.getStudent() != null ? enrollment.getStudent().getPhone() : null)
                .studentActive(enrollment.getStudent() != null ? enrollment.getStudent().isActive() : null)
                .studentDepartmentCode(enrollment.getStudent() != null && enrollment.getStudent().getDepartment() != null
                        ? enrollment.getStudent().getDepartment().getCode()
                        : null)
                .studentDepartmentName(enrollment.getStudent() != null && enrollment.getStudent().getDepartment() != null
                        ? enrollment.getStudent().getDepartment().getName()
                        : null)
                .studentClassCode(enrollment.getStudent() != null && enrollment.getStudent().getStudentClass() != null
                        ? enrollment.getStudent().getStudentClass().getCode()
                        : null)
                .studentClassName(enrollment.getStudent() != null && enrollment.getStudent().getStudentClass() != null
                        ? enrollment.getStudent().getStudentClass().getName()
                        : null)
                .build();
    }

    private void assertTeacherOwnsSection(String username, Section section) {
        User currentUser = userRepository.findByUsername(username)
                .or(() -> userRepository.findByEmail(username))
                .orElseThrow(() -> new BusinessException("User not found"));

        teacherRepository.findByUserId(currentUser.getId())
                .orElseThrow(() -> new BusinessException("Teacher profile not found"));

        if (section.getTeacher() == null
                || section.getTeacher().getId() == null
                || !section.getTeacher().getId().equals(currentUser.getId())) {
            throw new BusinessException("Bạn không có quyền thao tác lớp học phần này");
        }
    }

    private BigDecimal calculateFinalScore(Section section, BigDecimal processScore, BigDecimal examScore) {
        short processWeight = 40;
        short examWeight = 60;
        if (section.getSubject() != null) {
            if (section.getSubject().getProcessWeight() != null) processWeight = section.getSubject().getProcessWeight();
            if (section.getSubject().getExamWeight() != null) examWeight = section.getSubject().getExamWeight();
        }
        BigDecimal hundred = BigDecimal.valueOf(100);
        BigDecimal p = processScore.multiply(BigDecimal.valueOf(processWeight)).divide(hundred);
        BigDecimal e = examScore.multiply(BigDecimal.valueOf(examWeight)).divide(hundred);
        return p.add(e).setScale(2, java.math.RoundingMode.HALF_UP);
    }

    private Integer findFirstHeader(Map<String, Integer> headerIndex, String... keys) {
        for (String k : keys) {
            Integer idx = headerIndex.get(k);
            if (idx != null) return idx;
        }
        return null;
    }

    private String readCellAsString(org.apache.poi.ss.usermodel.Cell cell) {
        if (cell == null) return null;
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> {
                double v = cell.getNumericCellValue();
                long lv = (long) v;
                yield String.valueOf(lv);
            }
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> cell.getCellFormula();
            default -> null;
        };
    }

    private Double readCellAsDouble(org.apache.poi.ss.usermodel.Cell cell) {
        if (cell == null) return null;
        try {
            return switch (cell.getCellType()) {
                case NUMERIC -> cell.getNumericCellValue();
                case STRING -> {
                    String s = cell.getStringCellValue();
                    if (s == null || s.isBlank()) yield null;
                    yield Double.parseDouble(s.trim());
                }
                default -> null;
            };
        } catch (Exception e) {
            return null;
        }
    }
}
