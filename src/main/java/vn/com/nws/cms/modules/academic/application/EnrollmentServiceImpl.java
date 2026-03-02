package vn.com.nws.cms.modules.academic.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import vn.com.nws.cms.common.exception.BusinessException;
import vn.com.nws.cms.modules.academic.api.dto.*;
import vn.com.nws.cms.modules.academic.domain.enums.CourseLifecycleStatus;
import vn.com.nws.cms.modules.academic.domain.enums.EnrollmentStatus;
import vn.com.nws.cms.modules.academic.domain.model.Course;
import vn.com.nws.cms.modules.academic.domain.model.Enrollment;
import vn.com.nws.cms.modules.academic.domain.model.Student;
import vn.com.nws.cms.modules.academic.domain.repository.CourseRepository;
import vn.com.nws.cms.modules.academic.domain.repository.CourseTimeSlotRepository;
import vn.com.nws.cms.modules.academic.domain.repository.EnrollmentRepository;
import vn.com.nws.cms.modules.academic.domain.repository.StudentRepository;
import vn.com.nws.cms.modules.auth.domain.model.User;
import vn.com.nws.cms.modules.auth.domain.repository.UserRepository;
import vn.com.nws.cms.modules.iam.api.dto.UserResponse;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EnrollmentServiceImpl implements EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final CourseTimeSlotRepository courseTimeSlotRepository;

    /**
     * REQUIRES_NEW: Luôn tạo transaction độc lập, suspend TX cha nếu có.
     *
     * Quan trọng: Method này nhận studentId = student PROFILE id (không phải userId).
     * Lý do: EnrollmentEntity.student là StudentEntity (profile), không phải UserEntity.
     * EnrollmentCreateRequest.studentId phải là Student.id (profile).
     *
     * REQUIRES_NEW đảm bảo khi throw BusinessException:
     * - Chỉ TX con của method này rollback
     * - TX cha (nếu có, ví dụ DataSeeder.run()) KHÔNG bị mark rollback-only
     * - Caller catch exception → tiếp tục bình thường
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public EnrollmentResponse enrollStudent(EnrollmentCreateRequest request) {
        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new BusinessException("Course not found"));

        // request.getStudentId() = student profile id
        Student studentProfile = studentRepository.findById(request.getStudentId())
                .orElseThrow(() -> new BusinessException("Không tìm thấy hồ sơ sinh viên"));

        return enrollInternal(course, studentProfile);
    }

    /**
     * enrollSelf gọi từ API trực tiếp — không cần REQUIRES_NEW.
     * REQUIRED là đủ: không có TX cha nào bị ảnh hưởng.
     */
    @Override
    @Transactional
    public EnrollmentResponse enrollSelf(String username, EnrollmentSelfRequest request) {
        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new BusinessException("Course not found"));
        Student studentProfile = resolveStudentByUsername(username);
        return enrollInternal(course, studentProfile);
    }

    private EnrollmentResponse enrollInternal(Course course, Student studentProfile) {
        // --- Business rule checks ---
        if (!course.isActive()) {
            throw new BusinessException("Course is not active");
        }

        if (course.getStatus() != null && course.getStatus() != CourseLifecycleStatus.OPEN) {
            throw new BusinessException("Course is not open for enrollment");
        }

        assertEnrollmentWindowOpen(course);

        if (!studentProfile.isActive()) {
            throw new BusinessException("Hồ sơ sinh viên không hoạt động");
        }

        // Check trùng enrollment theo studentProfileId — khớp với EnrollmentEntity.student (StudentEntity)
        if (enrollmentRepository.existsByCourseIdAndStudentId(course.getId(), studentProfile.getId())) {
            throw new BusinessException("Student already enrolled in this course");
        }

        if (course.getCurrentStudents() >= course.getMaxStudents()) {
            throw new BusinessException("Course is full");
        }

        // --- Schedule conflict check ---
        var slots = courseTimeSlotRepository.findByCourseId(course.getId());
        if (!slots.isEmpty()) {
            for (var slot : slots) {
                boolean conflict = courseTimeSlotRepository.existsStudentScheduleConflict(
                        studentProfile.getId(),
                        course.getSemester().getId(),
                        course.getId(),
                        slot.getDayOfWeek(),
                        slot.getStartTime(),
                        slot.getEndTime()
                );
                if (conflict) {
                    throw new BusinessException("Trùng lịch với lớp học phần đã đăng ký");
                }
            }
        }

        // --- Persist ---
        course.setCurrentStudents(course.getCurrentStudents() + 1);
        courseRepository.save(course);

        Enrollment enrollment = Enrollment.builder()
                .course(course)
                .student(studentProfile)
                .status(EnrollmentStatus.ENROLLED)
                .build();

        enrollment = enrollmentRepository.save(enrollment);
        return toResponse(enrollment);
    }

    @Override
    @Transactional
    public EnrollmentResponse updateEnrollment(Long id, String username, boolean isAdmin, boolean isTeacher, EnrollmentUpdateRequest request) {
        Enrollment enrollment = enrollmentRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Enrollment not found"));

        Course course = enrollment.getCourse();
        if (isTeacher) {
            assertTeacherOwnsCourse(username, course);
        }

        if (request.getStatus() != null) {
            if (request.getStatus() == EnrollmentStatus.DROPPED
                    && enrollment.getStatus() != EnrollmentStatus.DROPPED) {
                course.setCurrentStudents(Math.max(0, course.getCurrentStudents() - 1));
                courseRepository.save(course);
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

            BigDecimal finalScore = calculateFinalScore(course, processScore, examScore);

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
            enrollment.setGrade(finalScore.doubleValue());
            if (!enrollment.isScoreLocked()) {
                enrollment.setScoredAt(LocalDateTime.now());
            }
            enrollment.setScoreLocked(true);
        } else if (request.getGrade() != null) {
            if (isTeacher && enrollment.isScoreLocked()) {
                throw new BusinessException("Điểm đã được nhập. Vui lòng liên hệ Admin để thay đổi");
            }
            enrollment.setGrade(request.getGrade());
        }

        enrollment = enrollmentRepository.save(enrollment);
        return toResponse(enrollment);
    }

    @Override
    @Transactional
    public void cancelEnrollment(Long id, String username, boolean isAdmin) {
        Enrollment enrollment = enrollmentRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Enrollment not found"));

        Course course = enrollment.getCourse();
        assertEnrollmentWindowOpen(course);

        if (!isAdmin) {
            User currentUser = userRepository.findByUsername(username)
                    .or(() -> userRepository.findByEmail(username))
                    .orElseThrow(() -> new BusinessException("User not found"));

            if (enrollment.getStudent() == null
                    || enrollment.getStudent().getUser() == null
                    || enrollment.getStudent().getUser().getId() == null
                    || !enrollment.getStudent().getUser().getId().equals(currentUser.getId())) {
                throw new BusinessException("Bạn không có quyền hủy đăng ký này");
            }
        }

        course.setCurrentStudents(Math.max(0, course.getCurrentStudents() - 1));
        courseRepository.save(course);
        enrollmentRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EnrollmentResponse> getStudentEnrollments(Long studentId) {
        return enrollmentRepository.findByStudentId(studentId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<EnrollmentResponse> getMyEnrollments(String username) {
        Student student = resolveStudentByUsername(username);
        return enrollmentRepository.findByStudentId(student.getId()).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<EnrollmentResponse> getCourseEnrollments(Long courseId, String username, boolean isAdmin, boolean isTeacher) {
        Course course = courseRepository.findById(courseId).orElseThrow(() -> new BusinessException("Course not found"));
        if (isTeacher) {
            assertTeacherOwnsCourse(username, course);
        }
        return enrollmentRepository.findByCourseId(courseId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public GradesImportResultResponse importCourseGrades(Long courseId, String username, boolean isAdmin, boolean isTeacher, org.springframework.web.multipart.MultipartFile file) {
        if (!isAdmin && !isTeacher) {
            throw new BusinessException("Không có quyền import điểm");
        }
        Course course = courseRepository.findById(courseId).orElseThrow(() -> new BusinessException("Course not found"));
        if (isTeacher) {
            assertTeacherOwnsCourse(username, course);
        }

        if (file == null || file.isEmpty()) {
            throw new BusinessException("File không hợp lệ");
        }

        List<Enrollment> enrollments = enrollmentRepository.findByCourseId(courseId);
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

                EnrollmentUpdateRequest req = new EnrollmentUpdateRequest();
                req.setProcessScore(p);
                req.setExamScore(e);
                if (isAdmin && enrollment.isScoreLocked()) {
                    req.setOverrideReason("Excel import by admin");
                }

                EnrollmentResponse updated = updateEnrollment(enrollment.getId(), username, isAdmin, isTeacher, req);
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

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private EnrollmentResponse toResponse(Enrollment enrollment) {
        Course course    = enrollment.getCourse();
        List<CourseTimeSlotResponse> timeSlots = courseTimeSlotRepository.findByCourseId(course.getId())
                .stream().map(CourseTimeSlotResponse::fromDomain).toList();

        return EnrollmentResponse.builder()
                .id(enrollment.getId())
                .status(enrollment.getStatus())
                .grade(enrollment.getFinalScore() != null ? enrollment.getFinalScore().doubleValue() : enrollment.getGrade())
                .processScore(enrollment.getProcessScore())
                .examScore(enrollment.getExamScore())
                .finalScore(enrollment.getFinalScore())
                .scoreLocked(enrollment.isScoreLocked())
                .scoreOverridden(enrollment.isScoreOverridden())
                .createdAt(enrollment.getCreatedAt())
                .updatedAt(enrollment.getUpdatedAt())
                .course(CourseResponse.builder()
                        .id(course.getId())
                        .name(course.getName())
                        .code(course.getCode())
                        .maxStudents(course.getMaxStudents())
                        .currentStudents(course.getCurrentStudents())
                        .active(course.isActive())
                        .enrollmentStartDate(course.getEnrollmentStartDate())
                        .enrollmentEndDate(course.getEnrollmentEndDate())
                        .semester(course.getSemester() != null
                                ? SemesterResponse.builder()
                                .id(course.getSemester().getId())
                                .name(course.getSemester().getName())
                                .code(course.getSemester().getCode())
                                .build()
                                : null)
                        .subject(course.getSubject() != null
                                ? SubjectResponse.builder()
                                .id(course.getSubject().getId())
                                .name(course.getSubject().getName())
                                .code(course.getSubject().getCode())
                                .credit(course.getSubject().getCredits())
                                .processWeight(course.getSubject().getProcessWeight())
                                .examWeight(course.getSubject().getExamWeight())
                                .build()
                                : null)
                        .teacher(course.getTeacher() != null
                                ? UserResponse.builder()
                                .id(course.getTeacher().getId())
                                .username(course.getTeacher().getUsername())
                                .email(course.getTeacher().getEmail())
                                .role(course.getTeacher().getRoles().stream()
                                        .map(Enum::name)
                                        .collect(Collectors.joining(",")))
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
                .build();
    }

    private void assertTeacherOwnsCourse(String username, Course course) {
        User currentUser = userRepository.findByUsername(username)
                .or(() -> userRepository.findByEmail(username))
                .orElseThrow(() -> new BusinessException("User not found"));
        if (course.getTeacher() == null || course.getTeacher().getId() == null || !course.getTeacher().getId().equals(currentUser.getId())) {
            throw new BusinessException("Bạn không có quyền thao tác lớp học phần này");
        }
    }

    private BigDecimal calculateFinalScore(Course course, BigDecimal processScore, BigDecimal examScore) {
        short processWeight = 40;
        short examWeight = 60;
        if (course.getSubject() != null) {
            if (course.getSubject().getProcessWeight() != null) processWeight = course.getSubject().getProcessWeight();
            if (course.getSubject().getExamWeight() != null) examWeight = course.getSubject().getExamWeight();
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

    private Student resolveStudentByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .or(() -> userRepository.findByEmail(username))
                .orElseThrow(() -> new BusinessException("User not found"));
        return studentRepository.findByUserId(user.getId()).orElseGet(() -> studentRepository.save(Student.builder()
                .user(user)
                .studentCode("SV" + user.getId())
                .active(true)
                .build()));
    }

    private void assertEnrollmentWindowOpen(Course course) {
        LocalDate start = course.getEnrollmentStartDate();
        LocalDate end   = course.getEnrollmentEndDate();
        if (start == null || end == null) {
            throw new BusinessException("Ngoài thời gian mở đăng ký");
        }
        LocalDate now = LocalDate.now();
        if (now.isBefore(start) || now.isAfter(end)) {
            throw new BusinessException("Ngoài thời gian mở đăng ký");
        }
    }
}
