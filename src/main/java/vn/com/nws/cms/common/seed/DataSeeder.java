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
import vn.com.nws.cms.modules.academic.domain.enums.CourseLifecycleStatus;
import vn.com.nws.cms.modules.academic.domain.model.*;
import vn.com.nws.cms.modules.academic.domain.repository.*;
import vn.com.nws.cms.modules.academic.application.EnrollmentService;
import vn.com.nws.cms.modules.auth.domain.model.User;
import vn.com.nws.cms.modules.auth.domain.repository.UserRepository;

import java.time.LocalDate;
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
    private final TeacherRepository teacherRepository;
    private final StudentRepository studentRepository;
    private final SemesterRepository semesterRepository;
    private final SubjectRepository subjectRepository;
    private final CourseRepository courseRepository;
    private final CourseTimeSlotRepository courseTimeSlotRepository;
    private final EnrollmentRepository enrollmentRepository;
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
        seedTeacherProfiles();
        seedStudentProfiles();
        seedSemesters();
        seedSubjects();
        seedCourses();
        seedCourseTimeSlots();
        seedEnrollments();

        log.info("=== Data Seeder completed successfully ===");
    }

    // -------------------------------------------------------------------------
    // Users
    // -------------------------------------------------------------------------

    private void seedUsers() {
        seedUserIfNotExists("manager",   "manager@nws.com.vn",   "manager123",   Set.of(RoleType.TEACHER, RoleType.ADMIN));
        seedUserIfNotExists("admin",     "admin@nws.com.vn",     "admin123",     Set.of(RoleType.ADMIN));
        seedUserIfNotExists("teacher",   "teacher@nws.com.vn",   "teacher123",   Set.of(RoleType.TEACHER));
        seedUserIfNotExists("teacher2",  "teacher2@nws.com.vn",  "teacher123",   Set.of(RoleType.TEACHER));
        seedUserIfNotExists("student",   "student@nws.com.vn",   "student123",   Set.of(RoleType.STUDENT));
        seedUserIfNotExists("student2",  "student2@nws.com.vn",  "student123",   Set.of(RoleType.STUDENT));
        seedUserIfNotExists("assistant", "assistant@nws.com.vn", "assistant123", Set.of(RoleType.TEACHER, RoleType.STUDENT));
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
    // Teachers
    // -------------------------------------------------------------------------

    private void seedTeacherProfiles() {
        Department cntt = getDepartmentByCode("CNTT");
        Department qtkd = getDepartmentByCode("QTKD");

        upsertTeacherProfile("teacher",   "GV0001", cntt, "Java/Spring Boot", "Giảng viên",    true);
        upsertTeacherProfile("teacher2",  "GV0002", qtkd, "Quản trị",         "Giảng viên",    true);
        upsertTeacherProfile("manager",   "GV0003", cntt, "Quản lý đào tạo",  "Trưởng bộ môn", true);
        upsertTeacherProfile("assistant", "GV0004", cntt, "Fullstack",        "Trợ giảng",     true);
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

        upsertStudentProfile("student",   "SV0001", cntt,   "0911111111", true);
        upsertStudentProfile("student2",  "SV0002", ketoan, "0922222222", true);
        upsertStudentProfile("assistant", "SV0003", cntt,   "0933333333", true);
        log.info("Student profiles seeding done.");
    }

    private void upsertStudentProfile(String username, String studentCode, Department department,
                                      String phone, boolean active) {
        if (!userRepository.existsByUsername(username)) return;
        User user = userRepository.findByUsername(username).orElseThrow();
        studentRepository.findByUserId(user.getId()).ifPresentOrElse(existing -> {
            existing.setStudentCode(studentCode);
            existing.setDepartment(department);
            existing.setPhone(phone);
            existing.setActive(active);
            studentRepository.save(existing);
        }, () -> studentRepository.save(Student.builder()
                .user(user).studentCode(studentCode).department(department)
                .phone(phone).active(active).build()));
    }

    // -------------------------------------------------------------------------
    // Semesters
    // -------------------------------------------------------------------------

    private void seedSemesters() {
        upsertSemester("HK2_2526", "Học kỳ 2 2025-2026",
                LocalDate.of(2026, 1, 10), LocalDate.of(2026, 6, 15), true);
        upsertSemester("HK1_2526", "Học kỳ 1 2025-2026",
                LocalDate.of(2025, 9, 1), LocalDate.of(2026, 1, 5), false);
        upsertSemester("HK_HE_2526", "Học kỳ hè 2025-2026",
                LocalDate.of(2026, 6, 20), LocalDate.of(2026, 8, 5), false);
        upsertSemester("HK_PHU_2526", "Kỳ học phụ 2025-2026",
                LocalDate.of(2026, 3, 1), LocalDate.of(2026, 4, 15), false);
        log.info("Semesters seeding done.");
    }

    private void upsertSemester(String code, String name, LocalDate start, LocalDate end, boolean active) {
        semesterRepository.findByCode(code).ifPresentOrElse(existing -> {
            existing.setName(name);
            existing.setStartDate(start);
            existing.setEndDate(end);
            existing.setActive(active);
            semesterRepository.save(existing);
        }, () -> semesterRepository.save(Semester.builder()
                .code(code).name(name).startDate(start).endDate(end).active(active).build()));
    }

    // -------------------------------------------------------------------------
    // Subjects
    // -------------------------------------------------------------------------

    private void seedSubjects() {
        seedSubjectIfNotExists("JAVA001", "Lập trình Java căn bản",       3, "Môn học cung cấp kiến thức nền tảng về ngôn ngữ lập trình Java.");
        seedSubjectIfNotExists("WEB002",  "Lập trình Web với Spring Boot", 4, "Xây dựng ứng dụng web hiện đại sử dụng Spring Boot framework.");
        seedSubjectIfNotExists("DB003",   "Cơ sở dữ liệu",                 3, "Kiến thức về thiết kế và quản trị cơ sở dữ liệu quan hệ.");
        log.info("Subjects seeding done.");
    }

    private void seedSubjectIfNotExists(String code, String name, int credits, String description) {
        if (subjectRepository.existsByCode(code)) return;
        subjectRepository.save(Subject.builder()
                .code(code).name(name).credits(credits).description(description)
                .active(true).theoryHours(30).practiceHours(15).build());
        log.info("Seeded subject [{}]", code);
    }

    // -------------------------------------------------------------------------
    // Courses
    // -------------------------------------------------------------------------

    private void seedCourses() {
        Semester semester = semesterRepository.findByCode("HK2_2526")
                .orElseThrow(() -> new IllegalStateException("Semester HK2_2526 not found"));
        Semester shortTerm = semesterRepository.findByCode("HK_PHU_2526")
                .orElseThrow(() -> new IllegalStateException("Semester HK_PHU_2526 not found"));

        User t1 = userRepository.findByUsername("teacher").orElseThrow();
        User t2 = userRepository.findByUsername("teacher2").orElseThrow();

        LocalDate now = LocalDate.now();

        seedCourseIfNotExists("JAVA001_HK2_01", "Lớp Java 01 - HK2", 60, 0, true, now.minusDays(5), now.plusDays(20), semester, "JAVA001", t1);
        seedCourseIfNotExists("JAVA001_HK2_02", "Lớp Java 02 - HK2", 60, 0, true, now.minusDays(5), now.plusDays(20), semester, "JAVA001", t2);
        seedCourseIfNotExists("WEB002_HK2_01", "Lớp Web 01 - HK2", 50, 0, true, now.minusDays(3), now.plusDays(18), semester, "WEB002", t1);
        seedCourseIfNotExists("DB003_HK2_01", "Lớp CSDL 01 - HK2", 70, 0, true, now.minusDays(3), now.plusDays(18), semester, "DB003", t2);
        seedCourseIfNotExists("WEB002_PHU_01", "Lớp Web - Kỳ phụ", 45, 0, true, now.minusDays(10), now.plusDays(7), shortTerm, "WEB002", t2);

        log.info("Courses seeding done.");
    }

    private void seedCourseIfNotExists(String code, String name, int maxStudents, int currentStudents,
                                       boolean active, LocalDate enrollStart, LocalDate enrollEnd,
                                       Semester semester, String subjectCode, User teacher) {
        if (courseRepository.findByCode(code).isPresent()) return;
        Subject subject = subjectRepository.findByCode(subjectCode)
                .orElseThrow(() -> new IllegalStateException("Subject not found: " + subjectCode));
        courseRepository.save(Course.builder()
                .name(name).code(code).maxStudents(maxStudents).minStudents(0)
                .currentStudents(currentStudents).active(active).status(CourseLifecycleStatus.OPEN)
                .enrollmentStartDate(enrollStart).enrollmentEndDate(enrollEnd)
                .semester(semester).subject(subject).teacher(teacher).build());
    }

    // -------------------------------------------------------------------------
    // Course Time Slots
    // -------------------------------------------------------------------------

    private void seedCourseTimeSlots() {
        upsertCourseTimeSlots("JAVA001_HK2_01", List.of(slot(1, "08:00", "10:00"), slot(3, "08:00", "10:00")));
        upsertCourseTimeSlots("JAVA001_HK2_02", List.of(slot(2, "10:00", "12:00"), slot(4, "10:00", "12:00")));
        upsertCourseTimeSlots("WEB002_HK2_01", List.of(slot(1, "13:00", "15:00"), slot(3, "13:00", "15:00"), slot(5, "13:00", "15:00")));
        upsertCourseTimeSlots("DB003_HK2_01", List.of(slot(2, "13:00", "15:00"), slot(6, "08:00", "10:00")));
        upsertCourseTimeSlots("WEB002_PHU_01", List.of(slot(6, "10:00", "12:00"), slot(7, "10:00", "12:00")));
        log.info("Course time slots seeding done.");
    }

    private CourseTimeSlot slot(int isoDayOfWeek, String start, String end) {
        return CourseTimeSlot.builder()
                .dayOfWeek((short) isoDayOfWeek)
                .startTime(LocalTime.parse(start))
                .endTime(LocalTime.parse(end))
                .build();
    }

    private void upsertCourseTimeSlots(String courseCode, List<CourseTimeSlot> slots) {
        Course course = courseRepository.findByCode(courseCode).orElseThrow();
        slots.forEach(s -> s.setCourseId(course.getId()));
        courseTimeSlotRepository.replaceCourseTimeSlots(course.getId(), slots);
    }

    // -------------------------------------------------------------------------
    // Enrollments
    // -------------------------------------------------------------------------

    private void seedEnrollments() {
        Course java = courseRepository.findByCode("JAVA001_HK2_01").orElseThrow();
        Course web  = courseRepository.findByCode("WEB002_HK2_01").orElseThrow();
        Course db   = courseRepository.findByCode("DB003_HK2_01").orElseThrow();

        // Lấy studentProfileId (student.getId()) — KHÔNG phải userId
        // EnrollmentCreateRequest.studentId = student profile id, khớp với EnrollmentEntity.student (StudentEntity)
        Student s1 = studentRepository.findByUserId(userRepository.findByUsername("student").orElseThrow().getId()).orElseThrow();
        Student s2 = studentRepository.findByUserId(userRepository.findByUsername("student2").orElseThrow().getId()).orElseThrow();
        Student s3 = studentRepository.findByUserId(userRepository.findByUsername("assistant").orElseThrow().getId()).orElseThrow();

        enrollIfNotExists(java.getId(), s1.getId());
        enrollIfNotExists(web.getId(),  s2.getId());
        enrollIfNotExists(db.getId(),   s3.getId());
        enrollIfNotExists(db.getId(),   s1.getId());

        log.info("Enrollments seeding done.");
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
