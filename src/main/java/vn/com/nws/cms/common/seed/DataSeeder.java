package vn.com.nws.cms.common.seed;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import vn.com.nws.cms.domain.enums.RoleType;
import vn.com.nws.cms.modules.academic.api.dto.EnrollmentCreateRequest;
import vn.com.nws.cms.modules.academic.domain.enums.AttendanceStatus;
import vn.com.nws.cms.modules.academic.domain.enums.CohortLifecycleStatus;
import vn.com.nws.cms.modules.academic.domain.enums.EnrollmentStatus;
import vn.com.nws.cms.modules.academic.domain.model.*;
import vn.com.nws.cms.modules.academic.domain.repository.*;
import vn.com.nws.cms.modules.academic.application.EnrollmentService;
import vn.com.nws.cms.modules.auth.domain.model.User;
import vn.com.nws.cms.modules.auth.domain.repository.UserRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

@Component
@RequiredArgsConstructor
@Slf4j
@Profile("dev")
@ConditionalOnProperty(name = "cms.seed.enabled", havingValue = "true")
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final AdminClassRepository adminClassRepository;
    private final TeacherRepository teacherRepository;
    private final StudentRepository studentRepository;
    private final SemesterRepository semesterRepository;
    private final ClassRepository classRepository;
    private final CohortRepository cohortRepository;
    private final CohortTimeSlotRepository cohortTimeSlotRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final AttendanceSessionRepository attendanceSessionRepository;
    private final AttendanceRecordRepository attendanceRecordRepository;
    private final EnrollmentService enrollmentService;
    private final PasswordEncoder passwordEncoder;

    /**
     * @Transactional phải đặt tại run() — không phải tại từng seedXxx() method.
     *
     * Lý do: Các method nội bộ (seedXxx()) được gọi qua `this`, không qua Spring proxy,
     * nên @Transactional trên chúng hoàn toàn vô hiệu → LazyInitializationException.
     * Chỉ method được gọi qua proxy (run()) mới áp dụng được @Transactional.
     *
     * Tại sao không bị UnexpectedRollbackException như lần đầu?
     * → enrollmentService.enrollStudent() dùng REQUIRES_NEW:
     *   - Suspend TX của run(), tạo TX con độc lập
     *   - Exception trong enrollStudent() chỉ rollback TX con đó
     *   - TX của run() KHÔNG bị mark rollback-only
     *   - enrollIfNotExists() catch exception → run() tiếp tục và commit bình thường
     */
    @Override
    @Transactional
    public void run(String... args) {
        log.info("=== Starting Data Seeder ===");

        seedUsers();
        seedDepartments();
        seedAdminClasses();
        seedTeacherProfiles();
        seedStudentProfiles();
        seedSemesters();
        seedClasses();
        seedCohorts();
        seedCohortTimeSlots();
        seedEnrollments();
        seedEnrollmentGrades();
        seedAttendance();

        log.info("=== Data Seeder completed successfully ===");
    }

    // -------------------------------------------------------------------------
    // Users
    // -------------------------------------------------------------------------

    private void seedUsers() {
        seedUserIfNotExists("nguyen.quang.huy", "manager@nws.com.vn",   "manager123",   Set.of(RoleType.TEACHER, RoleType.ADMIN));
        seedUserIfNotExists("le.minh.anh",      "admin@nws.com.vn",     "admin123",     Set.of(RoleType.ADMIN));
        seedUserIfNotExists("pham.thi.hoa",     "teacher@nws.com.vn",   "teacher123",   Set.of(RoleType.TEACHER));
        seedUserIfNotExists("do.duc.long",      "teacher2@nws.com.vn",  "teacher123",   Set.of(RoleType.TEACHER));
        seedUserIfNotExists("nguyen.van.an",    "student@nws.com.vn",   "student123",   Set.of(RoleType.STUDENT));
        seedUserIfNotExists("tran.thi.binh",    "student2@nws.com.vn",  "student123",   Set.of(RoleType.STUDENT));
        seedUserIfNotExists("vu.quoc.khanh",    "assistant@nws.com.vn", "assistant123", Set.of(RoleType.TEACHER, RoleType.STUDENT));
        log.info("Users seeding done.");
    }

    private void seedUserIfNotExists(String username, String email, String rawPassword, Set<RoleType> roles) {
        userRepository.findByEmail(email).ifPresentOrElse(existing -> {
            existing.setUsername(username);
            existing.setPassword(passwordEncoder.encode(rawPassword));
            existing.setRoles(roles);
            userRepository.save(existing);
            log.info("Updated seed user [{}]", email);
        }, () -> {
            userRepository.save(User.builder()
                    .username(username).email(email)
                    .password(passwordEncoder.encode(rawPassword))
                    .roles(roles).build());
            log.info("Seeded user [{}]", email);
        });
    }

    // -------------------------------------------------------------------------
    // Departments
    // -------------------------------------------------------------------------

    private void seedDepartments() {
        seedDepartmentIfNotExists("CNTT",   "Công nghệ thông tin", "Khoa Công nghệ thông tin", true);
        seedDepartmentIfNotExists("QTKD",   "Quản trị kinh doanh", "Khoa Quản trị kinh doanh", true);
        seedDepartmentIfNotExists("KETOAN", "Kế toán",             "Khoa Kế toán",              true);
        log.info("Departments seeding done.");
    }

    private void seedDepartmentIfNotExists(String code, String name, String description, boolean active) {
        boolean exists = departmentRepository.findAll().stream()
                .anyMatch(d -> code.equalsIgnoreCase(d.getCode()));
        if (exists) return;
        departmentRepository.save(Department.builder()
                .code(code).name(name).description(description).active(active).build());
    }

    private Department getDepartmentByCode(String code) {
        return departmentRepository.findAll().stream()
                .filter(d -> code.equalsIgnoreCase(d.getCode()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Department not found: " + code));
    }

    // -------------------------------------------------------------------------
    // Admin Classes (lớp hành chính)
    // -------------------------------------------------------------------------

    private void seedAdminClasses() {
        Department cntt = getDepartmentByCode("CNTT");
        Department qtkd = getDepartmentByCode("QTKD");

        upsertAdminClass("CNTT-K25-01", "CNTT K25 - Lớp 01", cntt, 2025, "Đại học chính quy", true);
        upsertAdminClass("CNTT-K25-02", "CNTT K25 - Lớp 02", cntt, 2025, "Đại học chính quy", true);
        upsertAdminClass("QTKD-K25-01", "QTKD K25 - Lớp 01", qtkd, 2025, "Đại học chính quy", true);
        log.info("Admin classes seeding done.");
    }

    private void upsertAdminClass(String code, String name, Department department, Integer intakeYear, String program, boolean active) {
        adminClassRepository.findByCode(code).ifPresentOrElse(existing -> {
            existing.setName(name);
            existing.setDepartment(department);
            existing.setIntakeYear(intakeYear);
            existing.setProgram(program);
            existing.setActive(active);
            adminClassRepository.save(existing);
        }, () -> adminClassRepository.save(AdminClass.builder()
                .code(code)
                .name(name)
                .department(department)
                .intakeYear(intakeYear)
                .program(program)
                .active(active)
                .build()));
    }

    // -------------------------------------------------------------------------
    // Teachers
    // -------------------------------------------------------------------------

    private void seedTeacherProfiles() {
        Department cntt = getDepartmentByCode("CNTT");
        Department qtkd = getDepartmentByCode("QTKD");

        upsertTeacherProfile("pham.thi.hoa",  "GV0001", cntt, "Java/Spring Boot", "Giảng viên",    true);
        upsertTeacherProfile("do.duc.long",   "GV0002", qtkd, "Quản trị",         "Giảng viên",    true);
        upsertTeacherProfile("nguyen.quang.huy", "GV0003", cntt, "Quản lý đào tạo",  "Trưởng bộ môn", true);
        upsertTeacherProfile("vu.quoc.khanh", "GV0004", cntt, "Fullstack",        "Trợ giảng",     true);
        log.info("Teacher profiles seeding done.");
    }

    private void upsertTeacherProfile(String username, String employeeCode, Department department,
                                      String specialization, String title, boolean active) {
        if (!userRepository.existsByUsername(username)) return;
        User user = userRepository.findByUsername(username).orElseThrow();
        teacherRepository.findByUserId(user.getId()).ifPresentOrElse(existing -> {
            existing.setEmployeeCode(employeeCode);
            existing.setDepartment(department);
            existing.setSpecialization(specialization);
            existing.setTitle(title);
            existing.setActive(active);
            teacherRepository.save(existing);
        }, () -> teacherRepository.save(Teacher.builder()
                .user(user).employeeCode(employeeCode).department(department)
                .specialization(specialization).title(title)
                .bio("Demo teacher profile").officeLocation("Tầng 3 - Phòng 301")
                .officeHours("T2-T6 9:00-11:00").phone("0900000000").active(active)
                .build()));
    }

    // -------------------------------------------------------------------------
    // Students
    // -------------------------------------------------------------------------

    private void seedStudentProfiles() {
        Department cntt   = getDepartmentByCode("CNTT");
        Department ketoan = getDepartmentByCode("KETOAN");

        upsertStudentProfile("nguyen.van.an", "SV0001", cntt,   "CNTT-K25-01", "0911111111", true);
        upsertStudentProfile("tran.thi.binh", "SV0002", ketoan, null,          "0922222222", true);
        upsertStudentProfile("vu.quoc.khanh", "SV0003", cntt,   "CNTT-K25-02", "0933333333", true);
        log.info("Student profiles seeding done.");
    }

    private void upsertStudentProfile(String username, String studentCode, Department department, String adminClassCode,
                                      String phone, boolean active) {
        if (!userRepository.existsByUsername(username)) return;
        User user = userRepository.findByUsername(username).orElseThrow();
        AdminClass adminClass = adminClassCode == null ? null : adminClassRepository.findByCode(adminClassCode).orElse(null);
        studentRepository.findByUserId(user.getId()).ifPresentOrElse(existing -> {
            existing.setStudentCode(studentCode);
            existing.setDepartment(department);
            existing.setAdminClass(adminClass);
            existing.setPhone(phone);
            existing.setActive(active);
            studentRepository.save(existing);
        }, () -> studentRepository.save(Student.builder()
                .user(user).studentCode(studentCode).department(department)
                .adminClass(adminClass)
                .phone(phone).active(active).build()));
    }

    // -------------------------------------------------------------------------
    // Semesters
    // -------------------------------------------------------------------------

    private void seedSemesters() {
        upsertSemester("HK2_2526", "Học kỳ 2 2025-2026",
                LocalDate.of(2026, 1, 10), LocalDate.of(2026, 6, 15), true, false);
        upsertSemester("HK1_2526", "Học kỳ 1 2025-2026",
                LocalDate.of(2025, 9, 1), LocalDate.of(2026, 1, 5), false, false);
        upsertSemester("HK_HE_2526", "Học kỳ hè 2025-2026",
                LocalDate.of(2026, 6, 20), LocalDate.of(2026, 8, 5), false, false);
        upsertSemester("HK_PHU_2526", "Kỳ học phụ 2025-2026",
                LocalDate.of(2026, 3, 1), LocalDate.of(2026, 4, 15), false, true);
        log.info("Semesters seeding done.");
    }

    private void upsertSemester(String code, String name, LocalDate start, LocalDate end, boolean active, boolean secondaryActive) {
        semesterRepository.findByCode(code).ifPresentOrElse(existing -> {
            existing.setName(name);
            existing.setStartDate(start);
            existing.setEndDate(end);
            existing.setActive(active);
            existing.setSecondaryActive(secondaryActive);
            semesterRepository.save(existing);
        }, () -> semesterRepository.save(Semester.builder()
                .code(code).name(name).startDate(start).endDate(end).active(active).secondaryActive(secondaryActive).build()));
    }

    // -------------------------------------------------------------------------
    // Classes
    // -------------------------------------------------------------------------

    private void seedClasses() {
        seedClassIfNotExists("JAVA001", "Lập trình Java căn bản",       3, "Môn học cung cấp kiến thức nền tảng về ngôn ngữ lập trình Java.");
        seedClassIfNotExists("WEB002",  "Lập trình Web với Spring Boot", 4, "Xây dựng ứng dụng web hiện đại sử dụng Spring Boot framework.");
        seedClassIfNotExists("DB003",   "Cơ sở dữ liệu",                 3, "Kiến thức về thiết kế và quản trị cơ sở dữ liệu quan hệ.");
        log.info("Classes seeding done.");
    }

    private void seedClassIfNotExists(String code, String name, int credits, String description) {
        if (classRepository.existsByCode(code)) return;
        classRepository.save(CourseClass.builder()
                .code(code).name(name).credits(credits).description(description)
                .active(true).theoryHours(30).practiceHours(15)
                .processWeight((short) 40).examWeight((short) 60)
                .build());
        log.info("Seeded class [{}]", code);
    }

    // -------------------------------------------------------------------------
    // Cohorts
    // -------------------------------------------------------------------------

    private void seedCohorts() {
        Semester semester = semesterRepository.findByCode("HK2_2526")
                .orElseThrow(() -> new IllegalStateException("Semester HK2_2526 not found"));
        Semester shortTerm = semesterRepository.findByCode("HK_PHU_2526")
                .orElseThrow(() -> new IllegalStateException("Semester HK_PHU_2526 not found"));

        User t1 = userRepository.findByEmail("teacher@nws.com.vn").orElseThrow();
        User t2 = userRepository.findByEmail("teacher2@nws.com.vn").orElseThrow();

        LocalDate now = LocalDate.now();

        seedCourseIfNotExists("JAVA001_HK2_01", "Lớp Java 01 - HK2", 60, 0, true, now.minusDays(5), now.plusDays(20), semester, "JAVA001", t1);
        seedCourseIfNotExists("JAVA001_HK2_02", "Lớp Java 02 - HK2", 60, 0, true, now.minusDays(5), now.plusDays(20), semester, "JAVA001", t2);
        seedCourseIfNotExists("WEB002_HK2_01", "Lớp Web 01 - HK2", 50, 0, true, now.minusDays(3), now.plusDays(18), semester, "WEB002", t1);
        seedCourseIfNotExists("DB003_HK2_01", "Lớp CSDL 01 - HK2", 70, 0, true, now.minusDays(3), now.plusDays(18), semester, "DB003", t2);
        seedCourseIfNotExists("WEB002_PHU_01", "Lớp Web - Kỳ phụ", 45, 0, true, now.minusDays(10), now.plusDays(7), shortTerm, "WEB002", t2);

        log.info("Cohorts seeding done.");
    }

    private void seedCourseIfNotExists(String code, String name, int maxStudents, int currentStudents,
                                       boolean active, LocalDate enrollStart, LocalDate enrollEnd,
                                       Semester semester, String subjectCode, User teacher) {
        if (cohortRepository.findByCode(code).isPresent()) return;
        CourseClass clazz = classRepository.findByCode(subjectCode)
                .orElseThrow(() -> new IllegalStateException("Class not found: " + subjectCode));
        cohortRepository.save(Cohort.builder()
                .name(name).code(code).maxStudents(maxStudents).minStudents(0)
                .currentStudents(currentStudents).active(active).status(CohortLifecycleStatus.OPEN)
                .enrollmentStartDate(enrollStart).enrollmentEndDate(enrollEnd)
                .registrationEnabled(true)
                .semester(semester).clazz(clazz).teacher(teacher).build());
    }

    // -------------------------------------------------------------------------
    // Cohort Time Slots
    // -------------------------------------------------------------------------

    private void seedCohortTimeSlots() {
        upsertCohortTimeSlots("JAVA001_HK2_01", List.of(slot(1, "08:00", "10:30"), slot(3, "08:00", "10:30")));
        upsertCohortTimeSlots("JAVA001_HK2_02", List.of(slot(2, "10:00", "12:30"), slot(4, "10:00", "12:30")));
        upsertCohortTimeSlots("WEB002_HK2_01", List.of(slot(1, "13:00", "15:30"), slot(3, "13:00", "15:30"), slot(5, "13:00", "15:30")));
        upsertCohortTimeSlots("DB003_HK2_01", List.of(slot(2, "13:00", "15:30"), slot(6, "08:00", "10:30")));
        upsertCohortTimeSlots("WEB002_PHU_01", List.of(slot(6, "10:00", "12:30"), slot(7, "10:00", "12:30")));
        log.info("Cohort time slots seeding done.");
    }

    private CohortTimeSlot slot(int isoDayOfWeek, String start, String end) {
        return CohortTimeSlot.builder()
                .dayOfWeek((short) isoDayOfWeek)
                .startTime(LocalTime.parse(start))
                .endTime(LocalTime.parse(end))
                .build();
    }

    private void upsertCohortTimeSlots(String cohortCode, List<CohortTimeSlot> slots) {
        Cohort cohort = cohortRepository.findByCode(cohortCode).orElseThrow();
        slots.forEach(s -> s.setCohortId(cohort.getId()));
        cohortTimeSlotRepository.replaceCohortTimeSlots(cohort.getId(), slots);
    }

    // -------------------------------------------------------------------------
    // Enrollments
    // -------------------------------------------------------------------------

    private void seedEnrollments() {
        Cohort java = cohortRepository.findByCode("JAVA001_HK2_01").orElseThrow();
        Cohort web  = cohortRepository.findByCode("WEB002_HK2_01").orElseThrow();
        Cohort db   = cohortRepository.findByCode("DB003_HK2_01").orElseThrow();

        // Lấy studentProfileId (student.getId()) — KHÔNG phải userId
        // EnrollmentCreateRequest.studentId = student profile id, khớp với EnrollmentEntity.student (StudentEntity)
        Student s1 = studentRepository.findByUserId(userRepository.findByEmail("student@nws.com.vn").orElseThrow().getId()).orElseThrow();
        Student s2 = studentRepository.findByUserId(userRepository.findByEmail("student2@nws.com.vn").orElseThrow().getId()).orElseThrow();
        Student s3 = studentRepository.findByUserId(userRepository.findByEmail("assistant@nws.com.vn").orElseThrow().getId()).orElseThrow();

        enrollIfNotExists(java.getId(), s1.getId());
        enrollIfNotExists(web.getId(),  s2.getId());
        enrollIfNotExists(db.getId(),   s3.getId());
        enrollIfNotExists(db.getId(),   s1.getId());

        log.info("Enrollments seeding done.");
    }

    private void seedEnrollmentGrades() {
        Cohort javaCourse = cohortRepository.findByCode("JAVA001_HK2_01").orElseThrow();
        Cohort web  = cohortRepository.findByCode("WEB002_HK2_01").orElseThrow();
        Cohort db   = cohortRepository.findByCode("DB003_HK2_01").orElseThrow();

        seedCourseGradesIfEmpty(javaCourse.getId());
        seedCourseGradesIfEmpty(web.getId());
        seedCourseGradesIfEmpty(db.getId());

        log.info("Grades seeding done.");
    }

    private void seedAttendance() {
        Cohort javaCourse = cohortRepository.findByCode("JAVA001_HK2_01").orElseThrow();
        List<Enrollment> enrollments = enrollmentRepository.findByCourseId(javaCourse.getId()).stream()
                .filter(e -> e.getStatus() == EnrollmentStatus.ENROLLED)
                .toList();
        if (enrollments.isEmpty()) return;

        for (int i = 1; i <= 6; i++) {
            final int dayIndex = i;
            LocalDate d = LocalDate.now().minusDays(i);
            AttendanceSession session = attendanceSessionRepository.findByCourseIdAndSessionDate(javaCourse.getId(), d)
                    .orElseGet(() -> attendanceSessionRepository.save(AttendanceSession.builder()
                            .cohortId(javaCourse.getId())
                            .sessionDate(d)
                            .periods((short) 3)
                            .createdByUserId(javaCourse.getTeacher() != null ? javaCourse.getTeacher().getId() : null)
                            .build()));

            List<AttendanceRecord> existing = attendanceRecordRepository.findBySessionId(session.getId());
            Set<Long> existingEnrollmentIds = existing.stream().map(AttendanceRecord::getEnrollmentId).filter(Objects::nonNull).collect(java.util.stream.Collectors.toSet());
            List<AttendanceRecord> toCreate = enrollments.stream()
                    .filter(e -> e.getId() != null && !existingEnrollmentIds.contains(e.getId()))
                    .map(e -> AttendanceRecord.builder()
                            .sessionId(session.getId())
                            .enrollmentId(e.getId())
                            .studentId(e.getStudent() != null ? e.getStudent().getId() : null)
                            .status(sampleAttendanceStatus(e.getId(), dayIndex))
                            .markedAt(LocalDateTime.now())
                            .build())
                    .filter(r -> r.getStudentId() != null)
                    .toList();
            if (!toCreate.isEmpty()) {
                attendanceRecordRepository.saveAll(toCreate);
            }
        }
        log.info("Attendance seeding done.");
    }

    private AttendanceStatus sampleAttendanceStatus(Long enrollmentId, int dayIndex) {
        long v = (enrollmentId + dayIndex) % 10;
        if (v == 0) return AttendanceStatus.EXCUSED;
        if (v <= 2) return AttendanceStatus.ABSENT;
        if (v <= 5) return AttendanceStatus.LATE;
        return AttendanceStatus.PRESENT;
    }

    private void seedCourseGradesIfEmpty(Long courseId) {
        List<Enrollment> enrollments = enrollmentRepository.findByCourseId(courseId);
        for (Enrollment e : enrollments) {
            if (e.getProcessScore() != null || e.getExamScore() != null || e.getFinalScore() != null) continue;
            double base = 6.5 + ((e.getId() % 7) * 0.4);
            BigDecimal process = BigDecimal.valueOf(Math.min(10, Math.max(0, base))).setScale(2, java.math.RoundingMode.HALF_UP);
            BigDecimal exam = BigDecimal.valueOf(Math.min(10, Math.max(0, base + 0.8))).setScale(2, java.math.RoundingMode.HALF_UP);
            BigDecimal finalScore = process.multiply(BigDecimal.valueOf(40)).add(exam.multiply(BigDecimal.valueOf(60)))
                    .divide(BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP);
            e.setProcessScore(process);
            e.setExamScore(exam);
            e.setFinalScore(finalScore);
            e.setScoreLocked(true);
            e.setScoredAt(LocalDateTime.now());
            enrollmentRepository.save(e);
        }
    }

    /**
     * TX của run() bị suspend bởi REQUIRES_NEW trong enrollmentService.enrollStudent().
     * Exception chỉ rollback TX con → TX của run() tiếp tục bình thường.
     */
    private boolean enrollIfNotExists(Long courseId, Long studentProfileId) {
        if (enrollmentRepository.existsByCourseIdAndStudentId(courseId, studentProfileId)) {
            return false;
        }
        EnrollmentCreateRequest req = new EnrollmentCreateRequest();
        req.setCourseId(courseId);
        req.setStudentId(studentProfileId);
        try {
            enrollmentService.enrollStudent(req);
            return true;
        } catch (Exception e) {
            log.warn("Seed enrollment skipped: {}", e.getMessage());
            return false;
        }
    }
}
