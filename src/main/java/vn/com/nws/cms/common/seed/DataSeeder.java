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

    @Value("${cms.seed.dense.enabled:true}")
    private boolean denseEnabled;

    @Value("${cms.seed.dense.students:120}")
    private int denseStudents;

    @Value("${cms.seed.dense.teachers:12}")
    private int denseTeachers;

    @Value("${cms.seed.dense.subjects:12}")
    private int denseSubjects;

    @Value("${cms.seed.dense.courses:60}")
    private int denseCourses;

    @Value("${cms.seed.dense.enrollments-per-student:3}")
    private int denseEnrollmentsPerStudent;

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

        if (denseEnabled) {
            seedDenseData();
        }

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
        upsertSemester("HK1_2425", "Học kỳ 1 2024-2025",
                LocalDate.of(2024, 9, 1), LocalDate.of(2025, 1, 15), false);
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

        User t1 = userRepository.findByUsername("teacher").orElseThrow();
        User t2 = userRepository.findByUsername("teacher2").orElseThrow();

        LocalDate now = LocalDate.now();

        seedCourseIfNotExists("JAVA001_HK2_01",  "Lớp Java 01 - HK2",           2, 0, true,  now.minusDays(5),  now.plusDays(30),  semester, "JAVA001", t1);
        seedCourseIfNotExists("WEB002_HK2_01",   "Lớp Web 01 - HK2",            2, 0, true,  now.minusDays(5),  now.plusDays(30),  semester, "WEB002",  t1);
        seedCourseIfNotExists("DB003_HK2_01",    "Lớp CSDL 01 - HK2",           1, 0, true,  now.minusDays(5),  now.plusDays(30),  semester, "DB003",   t2);
        seedCourseIfNotExists("JAVA001_EXPIRED", "Lớp Java (hết hạn đăng ký)", 30, 0, true,  now.minusDays(60), now.minusDays(30), semester, "JAVA001", t1);
        seedCourseIfNotExists("WEB002_FUTURE",   "Lớp Web (chưa mở đăng ký)", 30, 0, true,  now.plusDays(10),  now.plusDays(40),  semester, "WEB002",  t2);
        seedCourseIfNotExists("DB003_INACTIVE",  "Lớp CSDL (ngưng)",          30, 0, false, now.minusDays(5),  now.plusDays(30),  semester, "DB003",   t2);

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
        upsertCourseTimeSlots("JAVA001_HK2_01",  List.of(slot(2, "08:00", "10:00"), slot(4, "08:00", "10:00")));
        upsertCourseTimeSlots("WEB002_HK2_01",   List.of(slot(2, "09:00", "11:00"), slot(5, "13:00", "15:00")));
        upsertCourseTimeSlots("DB003_HK2_01",    List.of(slot(3, "08:00", "10:00")));
        upsertCourseTimeSlots("JAVA001_EXPIRED", List.of(slot(6, "08:00", "10:00")));
        upsertCourseTimeSlots("WEB002_FUTURE",   List.of(slot(3, "13:00", "15:00")));
        upsertCourseTimeSlots("DB003_INACTIVE",  List.of(slot(4, "13:00", "15:00")));
        log.info("Course time slots seeding done.");
    }

    private CourseTimeSlot slot(int dayOfWeek, String start, String end) {
        return CourseTimeSlot.builder()
                .dayOfWeek((short) dayOfWeek)
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
    // Dense data
    // -------------------------------------------------------------------------

    private void seedDenseData() {
        int teachers = Math.max(0, denseTeachers);
        int students = Math.max(0, denseStudents);
        int subjects = Math.max(0, denseSubjects);
        int courses  = Math.max(0, denseCourses);

        if (teachers == 0 && students == 0 && subjects == 0 && courses == 0) return;

        seedDenseTeacherUsersAndProfiles(teachers);
        seedDenseStudentUsersAndProfiles(students);
        seedDenseSubjects(subjects);
        seedDenseCoursesAndTimeSlots(courses);
        seedDenseEnrollments(students, Math.max(0, denseEnrollmentsPerStudent), courses);
    }

    private void seedDenseTeacherUsersAndProfiles(int count) {
        if (count <= 0) return;
        List<Department> departments = departmentRepository.findAll();
        if (departments.isEmpty()) return;
        for (int i = 1; i <= count; i++) {
            String username = String.format("t_demo%02d", i);
            seedUserIfNotExists(username, username + "@nws.com.vn", "teacher123", Set.of(RoleType.TEACHER));
            upsertTeacherProfile(username,
                    String.format("GV%04d", 100 + i),
                    departments.get((i - 1) % departments.size()),
                    "Demo specialization", "Giảng viên", true);
        }
    }

    private void seedDenseStudentUsersAndProfiles(int count) {
        if (count <= 0) return;
        List<Department> departments = departmentRepository.findAll();
        if (departments.isEmpty()) return;
        for (int i = 1; i <= count; i++) {
            String username = String.format("s_demo%03d", i);
            seedUserIfNotExists(username, username + "@nws.com.vn", "student123", Set.of(RoleType.STUDENT));
            upsertStudentProfile(username,
                    String.format("SV%04d", 100 + i),
                    departments.get((i - 1) % departments.size()),
                    String.format("09%08d", 10000000 + i), true);
        }
    }

    private void seedDenseSubjects(int count) {
        if (count <= 0) return;
        Long departmentId = departmentRepository.findAll().stream()
                .findFirst().map(Department::getId).orElse(null);
        if (departmentId == null) return;
        for (int i = 1; i <= count; i++) {
            String code = String.format("DEMO%03d", i);
            if (subjectRepository.existsByCode(code)) continue;
            subjectRepository.save(Subject.builder()
                    .code(code).name(String.format("Môn Demo %03d", i))
                    .credits(2 + (i % 3)).description("Demo subject")
                    .active(true).departmentId(departmentId)
                    .theoryHours(30).practiceHours(15).build());
        }
    }

    private void seedDenseCoursesAndTimeSlots(int count) {
        if (count <= 0) return;
        Semester semester = semesterRepository.findActiveSemester()
                .or(() -> semesterRepository.findByCode("HK2_2526"))
                .orElseThrow(() -> new IllegalStateException("Active semester not found"));

        List<String> teacherUsernames = new ArrayList<>(List.of("teacher", "teacher2", "manager", "assistant"));
        for (int i = 1; i <= Math.max(0, denseTeachers); i++) {
            teacherUsernames.add(String.format("t_demo%02d", i));
        }
        List<User> teachers = new ArrayList<>();
        teacherUsernames.forEach(u -> userRepository.findByUsername(u).ifPresent(teachers::add));
        if (teachers.isEmpty()) return;

        List<String> subjectCodes = new ArrayList<>(List.of("JAVA001", "WEB002", "DB003"));
        for (int i = 1; i <= Math.max(0, denseSubjects); i++) {
            subjectCodes.add(String.format("DEMO%03d", i));
        }

        LocalDate now = LocalDate.now();
        for (int i = 1; i <= count; i++) {
            String  subjectCode = subjectCodes.get((i - 1) % subjectCodes.size());
            Subject subject     = subjectRepository.findByCode(subjectCode).orElse(null);
            if (subject == null) continue;

            String code = String.format("DEMO_%s_%03d", subjectCode, i);
            if (courseRepository.findByCode(code).isPresent()) continue;

            Course course = courseRepository.save(Course.builder()
                    .name(String.format("Lớp %s Demo %03d", subjectCode, i))
                    .code(code).maxStudents(20 + (i % 30)).minStudents(0)
                    .currentStudents(0).active(true).status(CourseLifecycleStatus.OPEN)
                    .enrollmentStartDate(now.minusDays(2)).enrollmentEndDate(now.plusDays(21))
                    .semester(semester).subject(subject)
                    .teacher(teachers.get((i - 1) % teachers.size()))
                    .build());

            LocalTime start = switch ((i - 1) % 4) {
                case 0  -> LocalTime.of(8, 0);
                case 1  -> LocalTime.of(10, 0);
                case 2  -> LocalTime.of(13, 0);
                default -> LocalTime.of(15, 0);
            };
            courseTimeSlotRepository.replaceCourseTimeSlots(course.getId(), List.of(
                    CourseTimeSlot.builder()
                            .courseId(course.getId())
                            .dayOfWeek((short) (2 + ((i - 1) % 5)))
                            .startTime(start).endTime(start.plusHours(2))
                            .build()));
        }
    }

    private void seedDenseEnrollments(int studentCount, int perStudent, int courseCount) {
        if (studentCount <= 0 || perStudent <= 0 || courseCount <= 0) return;

        List<String> courseCodes = new ArrayList<>();
        for (int i = 1; i <= courseCount; i++) {
            courseCodes.add(String.format("DEMO_JAVA001_%03d", i));
            courseCodes.add(String.format("DEMO_WEB002_%03d", i));
            courseCodes.add(String.format("DEMO_DB003_%03d", i));
            for (int s = 1; s <= Math.max(0, denseSubjects); s++) {
                courseCodes.add(String.format("DEMO_DEMO%03d_%03d", s, i));
            }
        }

        for (int i = 1; i <= studentCount; i++) {
            String  username = String.format("s_demo%03d", i);
            Student student  = userRepository.findByUsername(username)
                    .flatMap(u -> studentRepository.findByUserId(u.getId()))
                    .orElse(null);
            if (student == null) continue;

            int enrolled = 0, tries = 0;
            while (enrolled < perStudent && tries < 30) {
                int    idx    = Math.floorMod(i * 13 + tries * 7, courseCodes.size());
                Course course = courseRepository.findByCode(courseCodes.get(idx)).orElse(null);
                tries++;
                if (course == null) continue;
                // existsByCourseIdAndStudentId nhận studentProfileId (student.getId())
                if (enrollIfNotExists(course.getId(), student.getId())) enrolled++;
            }
        }
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