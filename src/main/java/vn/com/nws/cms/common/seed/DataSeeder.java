package vn.com.nws.cms.common.seed;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import vn.com.nws.cms.domain.enums.RoleType;
import vn.com.nws.cms.modules.academic.api.dto.EnrollmentCreateRequest;
import vn.com.nws.cms.modules.academic.api.dto.EnrollmentUpdateRequest;
import vn.com.nws.cms.modules.academic.domain.enums.EnrollmentStatus;
import vn.com.nws.cms.modules.academic.domain.enums.SectionLifecycleStatus;
import vn.com.nws.cms.modules.academic.domain.model.*;
import vn.com.nws.cms.modules.academic.domain.repository.*;
import vn.com.nws.cms.modules.academic.application.EnrollmentService;
import vn.com.nws.cms.modules.auth.domain.model.User;
import vn.com.nws.cms.modules.auth.domain.repository.UserRepository;
import vn.com.nws.cms.modules.auth.infrastructure.persistence.entity.PermissionEntity;
import vn.com.nws.cms.modules.auth.infrastructure.persistence.entity.RoleEntity;
import vn.com.nws.cms.modules.auth.infrastructure.persistence.entity.RolePermissionEntity;
import vn.com.nws.cms.modules.auth.infrastructure.persistence.entity.RolePermissionId;
import vn.com.nws.cms.modules.auth.infrastructure.persistence.repository.JpaPermissionRepository;
import vn.com.nws.cms.modules.auth.infrastructure.persistence.repository.JpaRolePermissionRepository;
import vn.com.nws.cms.modules.auth.infrastructure.persistence.repository.JpaRoleRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * DataSeeder - Khởi tạo dữ liệu mẫu tiếng Việt cho môi trường dev/test.
 *
 * Kích hoạt: spring.profiles.active=dev + cms.seed.enabled=true
 *
 * Tài khoản được tạo:
 *  - admin@nws.com.vn       / Admin@123      (ROLE_ADMIN)
 *  - giaovu@nws.com.vn      / Teacher@123    (ROLE_ADMIN + ROLE_TEACHER)
 *  - phamthihoa@nws.com.vn  / Teacher@123    (ROLE_TEACHER) — dạy CNTT
 *  - doduchung@nws.com.vn   / Teacher@123    (ROLE_TEACHER) — dạy QTKD
 *  - nguyenhuuphuc@nws.com.vn / Teacher@123  (ROLE_TEACHER) — dạy CNTT
 *  - student@nws.com.vn     / Student@123    (ROLE_STUDENT)
 *  - + 9 sinh viên mẫu khác
 */
@Component
@RequiredArgsConstructor
@Slf4j
@Profile("dev")
@ConditionalOnProperty(name = "cms.seed.enabled", havingValue = "true")
public class DataSeeder implements CommandLineRunner {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final JpaRoleRepository jpaRoleRepository;
    private final JpaPermissionRepository jpaPermissionRepository;
    private final JpaRolePermissionRepository jpaRolePermissionRepository;

    private final DepartmentRepository departmentRepository;
    private final TeacherRepository teacherRepository;
    private final StudentRepository studentRepository;

    private final SemesterRepository semesterRepository;
    private final SubjectRepository subjectRepository;
    private final SectionRepository sectionRepository;
    private final SectionTimeSlotRepository sectionTimeSlotRepository;

    private final CohortRepository cohortRepository;
    private final StudentClassRepository studentClassRepository;

    private final EnrollmentService enrollmentService;
    private final EnrollmentRepository enrollmentRepository;
    private final AcademicProgramRepository academicProgramRepository;
    private final ProgramSubjectRepository programSubjectRepository;
    private final PlatformTransactionManager transactionManager;

    // =========================================================================
    //  ENTRY POINT
    // =========================================================================

    @Override
    public void run(String... args) {
        log.info("========== [DataSeeder] Bắt đầu khởi tạo dữ liệu mẫu ==========");

        TransactionTemplate tx = new TransactionTemplate(transactionManager);
        tx.executeWithoutResult(s -> seedRbac());
        tx.executeWithoutResult(s -> seedUsers());
        tx.executeWithoutResult(s -> seedDepartments());
        tx.executeWithoutResult(s -> seedSubjects()); // Moved up for AcademicProgram dependency
        tx.executeWithoutResult(s -> seedTeacherProfiles());
        tx.executeWithoutResult(s -> seedCohorts());
        tx.executeWithoutResult(s -> seedAcademicPrograms()); // Phase 1: Academic Programs
        tx.executeWithoutResult(s -> seedStudentClasses());
        tx.executeWithoutResult(s -> seedStudentProfiles());
        tx.executeWithoutResult(s -> seedSemesters());
        // tx.executeWithoutResult(s -> seedSubjects()); // Moved up
        tx.executeWithoutResult(s -> seedSections());
        tx.executeWithoutResult(s -> seedSectionTimeSlots());
        tx.executeWithoutResult(s -> seedEnrollments());
        tx.executeWithoutResult(s -> seedEnrollmentGrades());

        log.info("========== [DataSeeder] Hoàn thành khởi tạo dữ liệu mẫu ==========");
    }

    // =========================================================================
    //  0. RBAC
    // =========================================================================

    private record PermissionSeed(String name, String resource, String action, String description) {
    }

    private void seedRbac() {
        log.info("[Seeder] Khởi tạo RBAC (roles/permissions/role_permissions)...");

        Map<String, RoleEntity> roles = new HashMap<>();
        for (RoleType roleType : RoleType.values()) {
            String roleName = roleType.authority();
            RoleEntity role = jpaRoleRepository.findByName(roleName)
                    .orElseGet(() -> jpaRoleRepository.save(RoleEntity.builder()
                            .name(roleName)
                            .description(roleName)
                            .build()));
            roles.put(roleName, role);
        }

        List<PermissionSeed> permissionSeeds = List.of(
                new PermissionSeed("ACADEMIC_PROGRAM:READ", "ACADEMIC_PROGRAM", "READ", "Read academic program"),
                new PermissionSeed("ACADEMIC_PROGRAM:CREATE", "ACADEMIC_PROGRAM", "CREATE", "Create academic program"),
                new PermissionSeed("ACADEMIC_PROGRAM:UPDATE", "ACADEMIC_PROGRAM", "UPDATE", "Update academic program"),
                new PermissionSeed("ACADEMIC_PROGRAM:DELETE", "ACADEMIC_PROGRAM", "DELETE", "Delete academic program"),

                new PermissionSeed("STUDENT_PROGRESS:READ_ALL", "STUDENT_PROGRESS", "READ_ALL", "Read all student progress"),
                new PermissionSeed("STUDENT_PROGRESS:READ_CLASS", "STUDENT_PROGRESS", "READ_CLASS", "Read class student progress"),
                new PermissionSeed("STUDENT_PROGRESS:READ_SELF", "STUDENT_PROGRESS", "READ_SELF", "Read self progress"),

                new PermissionSeed("USER:CREATE", "USER", "CREATE", "Create user"),
                new PermissionSeed("USER:READ", "USER", "READ", "Read user"),
                new PermissionSeed("USER:UPDATE", "USER", "UPDATE", "Update user"),
                new PermissionSeed("USER:DELETE", "USER", "DELETE", "Delete user"),

                new PermissionSeed("DEPARTMENT:CREATE", "DEPARTMENT", "CREATE", "Create department"),
                new PermissionSeed("DEPARTMENT:READ", "DEPARTMENT", "READ", "Read department"),
                new PermissionSeed("DEPARTMENT:UPDATE", "DEPARTMENT", "UPDATE", "Update department"),
                new PermissionSeed("DEPARTMENT:DELETE", "DEPARTMENT", "DELETE", "Delete department"),

                new PermissionSeed("SUBJECT:CREATE", "SUBJECT", "CREATE", "Create subject"),
                new PermissionSeed("SUBJECT:READ", "SUBJECT", "READ", "Read subject"),
                new PermissionSeed("SUBJECT:UPDATE", "SUBJECT", "UPDATE", "Update subject"),
                new PermissionSeed("SUBJECT:DELETE", "SUBJECT", "DELETE", "Delete subject"),

                new PermissionSeed("SEMESTER:CREATE", "SEMESTER", "CREATE", "Create semester"),
                new PermissionSeed("SEMESTER:READ", "SEMESTER", "READ", "Read semester"),
                new PermissionSeed("SEMESTER:UPDATE", "SEMESTER", "UPDATE", "Update semester"),
                new PermissionSeed("SEMESTER:DELETE", "SEMESTER", "DELETE", "Delete semester"),

                new PermissionSeed("COHORT:CREATE", "COHORT", "CREATE", "Create cohort"),
                new PermissionSeed("COHORT:READ", "COHORT", "READ", "Read cohort"),
                new PermissionSeed("COHORT:UPDATE", "COHORT", "UPDATE", "Update cohort"),
                new PermissionSeed("COHORT:DELETE", "COHORT", "DELETE", "Delete cohort"),

                new PermissionSeed("SECTION:CREATE", "SECTION", "CREATE", "Create section"),
                new PermissionSeed("SECTION:READ", "SECTION", "READ", "Read section"),
                new PermissionSeed("SECTION:UPDATE", "SECTION", "UPDATE", "Update section"),
                new PermissionSeed("SECTION:DELETE", "SECTION", "DELETE", "Delete section"),

                new PermissionSeed("TEACHER:CREATE", "TEACHER", "CREATE", "Create teacher"),
                new PermissionSeed("TEACHER:READ", "TEACHER", "READ", "Read teacher"),
                new PermissionSeed("TEACHER:UPDATE", "TEACHER", "UPDATE", "Update teacher"),
                new PermissionSeed("TEACHER:DELETE", "TEACHER", "DELETE", "Delete teacher"),

                new PermissionSeed("STUDENT:CREATE", "STUDENT", "CREATE", "Create student"),
                new PermissionSeed("STUDENT:READ", "STUDENT", "READ", "Read student"),
                new PermissionSeed("STUDENT:UPDATE", "STUDENT", "UPDATE", "Update student"),
                new PermissionSeed("STUDENT:DELETE", "STUDENT", "DELETE", "Delete student"),

                new PermissionSeed("STUDENT_CLASS:CREATE", "STUDENT_CLASS", "CREATE", "Create student class"),
                new PermissionSeed("STUDENT_CLASS:READ", "STUDENT_CLASS", "READ", "Read student class"),
                new PermissionSeed("STUDENT_CLASS:UPDATE", "STUDENT_CLASS", "UPDATE", "Update student class"),
                new PermissionSeed("STUDENT_CLASS:DELETE", "STUDENT_CLASS", "DELETE", "Delete student class"),

                new PermissionSeed("ENROLLMENT:CREATE", "ENROLLMENT", "CREATE", "Create enrollment"),
                new PermissionSeed("ENROLLMENT:READ", "ENROLLMENT", "READ", "Read enrollment"),
                new PermissionSeed("ENROLLMENT:UPDATE", "ENROLLMENT", "UPDATE", "Update enrollment"),
                new PermissionSeed("ENROLLMENT:DELETE", "ENROLLMENT", "DELETE", "Delete enrollment")
        );

        Map<String, PermissionEntity> permissionsByName = new HashMap<>();
        for (PermissionSeed seed : permissionSeeds) {
            PermissionEntity permission = jpaPermissionRepository.findByName(seed.name())
                    .map(existing -> {
                        existing.setResource(seed.resource());
                        existing.setAction(seed.action());
                        existing.setDescription(seed.description());
                        return existing;
                    })
                    .orElseGet(() -> PermissionEntity.builder()
                            .name(seed.name())
                            .resource(seed.resource())
                            .action(seed.action())
                            .description(seed.description())
                            .build());
            permission = jpaPermissionRepository.save(permission);
            permissionsByName.put(permission.getName(), permission);
        }

        List<String> allPermissionNames = permissionSeeds.stream().map(PermissionSeed::name).toList();

        Map<String, List<String>> roleToPermissions = Map.of(
                RoleType.ADMIN.authority(), allPermissionNames,
                RoleType.TEACHER.authority(), List.of(
                        "DEPARTMENT:READ",
                        "SUBJECT:READ",
                        "SEMESTER:READ",
                        "COHORT:READ",
                        "SECTION:READ",
                        "STUDENT_CLASS:READ",
                        "TEACHER:READ",
                        "ENROLLMENT:READ",
                        "ENROLLMENT:UPDATE",
                        "ACADEMIC_PROGRAM:READ",
                        "STUDENT_PROGRESS:READ_CLASS"
                ),
                RoleType.STUDENT.authority(), List.of(
                        "SUBJECT:READ",
                        "SEMESTER:READ",
                        "SECTION:READ",
                        "ENROLLMENT:READ",
                        "ENROLLMENT:CREATE",
                        "ENROLLMENT:DELETE",
                        "ACADEMIC_PROGRAM:READ",
                        "STUDENT_PROGRESS:READ_SELF"
                )
        );

        for (Map.Entry<String, List<String>> entry : roleToPermissions.entrySet()) {
            RoleEntity role = roles.get(entry.getKey());
            if (role == null) {
                continue;
            }
            for (String permissionName : entry.getValue()) {
                PermissionEntity permission = permissionsByName.get(permissionName);
                if (permission == null) {
                    continue;
                }
                RolePermissionId id = new RolePermissionId(role.getId(), permission.getId());
                if (jpaRolePermissionRepository.existsById(id)) {
                    continue;
                }
                jpaRolePermissionRepository.save(RolePermissionEntity.builder()
                        .id(id)
                        .role(role)
                        .permission(permission)
                        .build());
            }
        }
    }

    // =========================================================================
    //  1. USERS
    // =========================================================================

    private void seedUsers() {
        log.info("[Seeder] Tạo tài khoản người dùng...");

        // Quản trị viên
        upsertUser("le.minh.anh",       "admin@nws.com.vn",         "Admin@123",    Set.of(RoleType.ADMIN));

        // Giáo vụ (admin + teacher)
        upsertUser("nguyen.quang.huy",   "giaovu@nws.com.vn",        "Teacher@123",  Set.of(RoleType.ADMIN, RoleType.TEACHER));

        // Giảng viên CNTT
        upsertUser("pham.thi.hoa",       "phamthihoa@nws.com.vn",    "Teacher@123",  Set.of(RoleType.TEACHER));
        upsertUser("nguyen.huu.phuc",    "nguyenhuuphuc@nws.com.vn", "Teacher@123",  Set.of(RoleType.TEACHER));
        upsertUser("tran.van.duc",       "tranvanduc@nws.com.vn",    "Teacher@123",  Set.of(RoleType.TEACHER));

        // Giảng viên QTKD
        upsertUser("do.duc.hung",        "doduchung@nws.com.vn",     "Teacher@123",  Set.of(RoleType.TEACHER));
        upsertUser("vo.thi.lan",         "vothilan@nws.com.vn",      "Teacher@123",  Set.of(RoleType.TEACHER));

        // Sinh viên
        upsertUser("nguyen.van.an",      "student@nws.com.vn",       "Student@123",  Set.of(RoleType.STUDENT));
        upsertUser("tran.thi.binh",      "tranthihinh@nws.com.vn",   "Student@123",  Set.of(RoleType.STUDENT));
        upsertUser("le.quoc.cuong",      "lequoccuong@nws.com.vn",   "Student@123",  Set.of(RoleType.STUDENT));
        upsertUser("pham.ngoc.diem",     "phamngocidiem@nws.com.vn", "Student@123",  Set.of(RoleType.STUDENT));
        upsertUser("hoang.van.em",       "hoangvanem@nws.com.vn",    "Student@123",  Set.of(RoleType.STUDENT));
        upsertUser("nguyen.thi.phuong",  "nguyenthiphuong@nws.com.vn","Student@123", Set.of(RoleType.STUDENT));
        upsertUser("bui.duc.giang",      "buiducgiang@nws.com.vn",   "Student@123",  Set.of(RoleType.STUDENT));
        upsertUser("do.thi.huyen",       "dothihuyen@nws.com.vn",    "Student@123",  Set.of(RoleType.STUDENT));
        upsertUser("cao.van.ien",        "caovanien@nws.com.vn",     "Student@123",  Set.of(RoleType.STUDENT));
        upsertUser("mai.thi.khanh",      "maithikhanh@nws.com.vn",   "Student@123",  Set.of(RoleType.STUDENT));
    }

    private void upsertUser(String username, String email, String rawPassword, Set<RoleType> roles) {
        userRepository.findByEmail(email)
                .or(() -> userRepository.findByUsername(username))
                .ifPresentOrElse(existing -> {
            existing.setUsername(username);
            existing.setEmail(email);
            existing.setPassword(passwordEncoder.encode(rawPassword));
            existing.setRoles(roles);
            userRepository.save(existing);
            log.debug("  [User] Cập nhật: {}", email);
        }, () -> {
            userRepository.save(User.builder()
                    .username(username)
                    .email(email)
                    .password(passwordEncoder.encode(rawPassword))
                    .roles(roles)
                    .build());
            log.debug("  [User] Tạo mới: {}", email);
        });
    }

    // =========================================================================
    //  2. DEPARTMENTS
    // =========================================================================

    private void seedDepartments() {
        log.info("[Seeder] Tạo khoa/bộ môn...");
        upsertDepartment("CNTT",   "Công nghệ Thông tin",  "Đào tạo lập trình, mạng máy tính, AI, an ninh mạng");
        upsertDepartment("QTKD",   "Quản trị Kinh doanh",  "Đào tạo quản trị, marketing, kinh doanh quốc tế");
        upsertDepartment("KETOAN", "Kế toán - Kiểm toán",  "Đào tạo kế toán doanh nghiệp, kiểm toán, tài chính");
        upsertDepartment("NGOAINGU","Ngoại ngữ",            "Đào tạo tiếng Anh, tiếng Nhật, tiếng Hàn, tiếng Trung");
        upsertDepartment("DIENTEN","Điện tử - Viễn thông",  "Đào tạo kỹ thuật điện tử, viễn thông, IoT");
    }

    private void upsertDepartment(String code, String name, String description) {
        boolean exists = departmentRepository.findAll().stream()
                .anyMatch(d -> code.equalsIgnoreCase(d.getCode()));
        if (exists) {
            log.debug("  [Department] Đã tồn tại: {}", code);
            return;
        }
        departmentRepository.save(Department.builder()
                .code(code)
                .name(name)
                .description(description)
                .active(true)
                .build());
        log.debug("  [Department] Tạo mới: {}", code);
    }

    private Department departmentByCode(String code) {
        return departmentRepository.findAll().stream()
                .filter(d -> code.equalsIgnoreCase(d.getCode()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Không tìm thấy khoa: " + code));
    }

    // =========================================================================
    //  3. TEACHER PROFILES
    // =========================================================================

    private void seedTeacherProfiles() {
        log.info("[Seeder] Tạo hồ sơ giảng viên...");

        Department cntt    = departmentByCode("CNTT");
        Department qtkd    = departmentByCode("QTKD");
        Department ketoan  = departmentByCode("KETOAN");

        upsertTeacher("phamthihoa@nws.com.vn",     "GV0001", cntt,   "Java, Spring Boot, Microservices",   "Thạc sĩ - Giảng viên",          "Phòng 301 - Tòa A", "T2,T4: 8:00-11:00",  "0901000001");
        upsertTeacher("nguyenhuuphuc@nws.com.vn",  "GV0002", cntt,   "Python, Machine Learning, AI",       "Tiến sĩ - Giảng viên chính",     "Phòng 302 - Tòa A", "T3,T5: 9:00-12:00",  "0901000002");
        upsertTeacher("tranvanduc@nws.com.vn",     "GV0003", cntt,   "Cơ sở dữ liệu, Data Engineering",   "Thạc sĩ - Giảng viên",          "Phòng 303 - Tòa A", "T2,T6: 14:00-17:00", "0901000003");
        upsertTeacher("doduchung@nws.com.vn",      "GV0004", qtkd,   "Quản trị chiến lược, Marketing",     "Thạc sĩ - Giảng viên",          "Phòng 201 - Tòa B", "T3,T5: 8:00-11:00",  "0901000004");
        upsertTeacher("vothilan@nws.com.vn",       "GV0005", ketoan, "Kế toán doanh nghiệp, Kiểm toán",    "Thạc sĩ - Giảng viên",          "Phòng 202 - Tòa B", "T2,T4: 13:00-16:00", "0901000005");
        upsertTeacher("giaovu@nws.com.vn",         "GV0006", cntt,   "Quản lý đào tạo, Giám sát học vụ",   "Thạc sĩ - Trưởng bộ môn",       "Phòng 101 - Tòa A", "T2-T6: 8:00-12:00",  "0901000006");
    }

    private void upsertTeacher(String email, String employeeCode, Department department,
                               String specialization, String title, String officeLocation,
                               String officeHours, String phone) {
        User user = userByEmail(email);
        teacherRepository.findByUserId(user.getId())
                .or(() -> teacherRepository.findByEmployeeCode(employeeCode))
                .ifPresentOrElse(existing -> {
            existing.setUser(user);
            existing.setEmployeeCode(employeeCode);
            existing.setDepartment(department);
            existing.setSpecialization(specialization);
            existing.setTitle(title);
            existing.setOfficeLocation(officeLocation);
            existing.setOfficeHours(officeHours);
            existing.setPhone(phone);
            existing.setActive(true);
            teacherRepository.save(existing);
            log.debug("  [Teacher] Cập nhật: {} - {}", employeeCode, email);
        }, () -> {
            teacherRepository.save(Teacher.builder()
                    .user(user)
                    .employeeCode(employeeCode)
                    .department(department)
                    .specialization(specialization)
                    .title(title)
                    .bio("Giảng viên " + department.getName())
                    .officeLocation(officeLocation)
                    .officeHours(officeHours)
                    .phone(phone)
                    .active(true)
                    .build());
            log.debug("  [Teacher] Tạo mới: {} - {}", employeeCode, email);
        });
    }

    private Teacher teacherByEmail(String email) {
        User user = userByEmail(email);
        return teacherRepository.findByUserId(user.getId())
                .orElseThrow(() -> new IllegalStateException("Không tìm thấy hồ sơ giảng viên: " + email));
    }

    // =========================================================================
    //  4. COHORTS (niên khóa)
    // =========================================================================

    private void seedCohorts() {
        log.info("[Seeder] Tạo niên khóa...");
        upsertCohort("COHORT_2022", "Niên khóa 2022-2026", 2022, 2026, true);
        upsertCohort("COHORT_2023", "Niên khóa 2023-2027", 2023, 2027, true);
        upsertCohort("COHORT_2024", "Niên khóa 2024-2028", 2024, 2028, true);
        upsertCohort("COHORT_2025", "Niên khóa 2025-2029", 2025, 2029, true);
    }

    private void upsertCohort(String code, String name, int startYear, int endYear, boolean active) {
        if (cohortRepository.findByCode(code).isPresent()) {
            log.debug("  [Cohort] Đã tồn tại: {}", code);
            return;
        }
        cohortRepository.save(Cohort.builder()
                .code(code)
                .name(name)
                .startYear(startYear)
                .endYear(endYear)
                .active(active)
                .build());
        log.debug("  [Cohort] Tạo mới: {}", code);
    }

    private Cohort cohortByCode(String code) {
        return cohortRepository.findByCode(code)
                .orElseThrow(() -> new IllegalStateException("Không tìm thấy niên khóa: " + code));
    }

    // =========================================================================
    //  4.5. ACADEMIC PROGRAMS (Phase 1)
    // =========================================================================

    private void seedAcademicPrograms() {
        log.info("[Seeder] Tạo chương trình đào tạo...");
        Department cntt = departmentByCode("CNTT");
        Department qtkd = departmentByCode("QTKD");
        Department ketoan = departmentByCode("KETOAN");

        AcademicProgram cnttProg = upsertAcademicProgram("CNTT_K25", "Kỹ thuật phần mềm K25", cntt, 130, "Chương trình chuẩn Kỹ thuật phần mềm K25");
        AcademicProgram qtkdProg = upsertAcademicProgram("QTKD_K25", "Quản trị kinh doanh K25", qtkd, 120, "Chương trình chuẩn QTKD K25");
        AcademicProgram ketoanProg = upsertAcademicProgram("KETOAN_K25", "Kế toán K25", ketoan, 125, "Chương trình chuẩn Kế toán K25");

        // Gán môn học vào chương trình
        upsertProgramSubject(cnttProg, "JAVA001", 1, ProgramSubject.TYPE_COMPULSORY, 4.0);
        upsertProgramSubject(cnttProg, "JAVA002", 2, ProgramSubject.TYPE_COMPULSORY, 4.0);
        upsertProgramSubject(cnttProg, "WEB001", 3, ProgramSubject.TYPE_COMPULSORY, 4.0);
        upsertProgramSubject(cnttProg, "DB001", 1, ProgramSubject.TYPE_COMPULSORY, 4.0);
        upsertProgramSubject(cnttProg, "DB002", 2, ProgramSubject.TYPE_ELECTIVE, 4.0);
        upsertProgramSubject(cnttProg, "AI001", 4, ProgramSubject.TYPE_ELECTIVE, 4.0);

        upsertProgramSubject(qtkdProg, "MKT001", 1, ProgramSubject.TYPE_COMPULSORY, 4.0);
        upsertProgramSubject(qtkdProg, "MGMT001", 2, ProgramSubject.TYPE_COMPULSORY, 4.0);
        upsertProgramSubject(qtkdProg, "FIN001", 3, ProgramSubject.TYPE_COMPULSORY, 4.0);

        upsertProgramSubject(ketoanProg, "ACC001", 1, ProgramSubject.TYPE_COMPULSORY, 4.0);
        upsertProgramSubject(ketoanProg, "ACC002", 2, ProgramSubject.TYPE_COMPULSORY, 4.0);
    }

    private AcademicProgram upsertAcademicProgram(String code, String name, Department dept, int credits, String description) {
        return academicProgramRepository.findByCode(code)
                .map(existing -> {
                    existing.setName(name);
                    existing.setDepartment(dept);
                    existing.setTotalCredits(credits);
                    existing.setDescription(description);
                    existing.setActive(true);
                    return academicProgramRepository.save(existing);
                })
                .orElseGet(() -> {
                    AcademicProgram p = AcademicProgram.builder()
                            .code(code)
                            .name(name)
                            .department(dept)
                            .totalCredits(credits)
                            .description(description)
                            .active(true)
                            .build();
                    log.debug("  [Program] Tạo mới: {}", code);
                    return academicProgramRepository.save(p);
                });
    }

    private void upsertProgramSubject(AcademicProgram prog, String subjectCode, int semester, String type, double passScore) {
        try {
            Subject subject = subjectByCode(subjectCode);
            boolean exists = programSubjectRepository.findByProgramId(prog.getId()).stream()
                    .anyMatch(ps -> ps.getSubject().getId().equals(subject.getId()));

            if (!exists) {
                ProgramSubject ps = ProgramSubject.builder()
                        .programId(prog.getId())
                        .subject(subject)
                        .semester(semester)
                        .subjectType(type)
                        .passScore(passScore)
                        .build();
                programSubjectRepository.save(ps);
                log.debug("  [ProgramSubject] Thêm {} vào {}", subjectCode, prog.getCode());
            }
        } catch (Exception e) {
            log.warn("  [ProgramSubject] Lỗi thêm môn {}: {}", subjectCode, e.getMessage());
        }
    }

    // =========================================================================
    //  5. STUDENT CLASSES (lớp hành chính)
    // =========================================================================

    private void seedStudentClasses() {
        log.info("[Seeder] Tạo lớp hành chính...");

        Department cntt    = departmentByCode("CNTT");
        Department qtkd    = departmentByCode("QTKD");
        Department ketoan  = departmentByCode("KETOAN");

        Teacher advisorCntt = teacherByEmail("phamthihoa@nws.com.vn");
        Teacher advisorQtkd = teacherByEmail("doduchung@nws.com.vn");
        Teacher advisorKetoan = teacherByEmail("vothilan@nws.com.vn");

        Cohort k22 = cohortByCode("COHORT_2022");
        Cohort k23 = cohortByCode("COHORT_2023");
        Cohort k24 = cohortByCode("COHORT_2024");
        Cohort k25 = cohortByCode("COHORT_2025");

        // CNTT
        upsertStudentClass("CNTT-K22-01", "CNTT K22 - Lớp 01", cntt, k22, advisorCntt, 2022, "Đại học chính quy");
        upsertStudentClass("CNTT-K22-02", "CNTT K22 - Lớp 02", cntt, k22, advisorCntt, 2022, "Đại học chính quy");
        upsertStudentClass("CNTT-K23-01", "CNTT K23 - Lớp 01", cntt, k23, advisorCntt, 2023, "Đại học chính quy");
        upsertStudentClass("CNTT-K23-02", "CNTT K23 - Lớp 02", cntt, k23, advisorCntt, 2023, "Đại học chính quy");
        upsertStudentClass("CNTT-K24-01", "CNTT K24 - Lớp 01", cntt, k24, advisorCntt, 2024, "Đại học chính quy");
        upsertStudentClass("CNTT-K25-01", "CNTT K25 - Lớp 01", cntt, k25, advisorCntt, 2025, "Đại học chính quy");
        upsertStudentClass("CNTT-K25-02", "CNTT K25 - Lớp 02", cntt, k25, advisorCntt, 2025, "Đại học chính quy");

        // QTKD
        upsertStudentClass("QTKD-K24-01", "QTKD K24 - Lớp 01", qtkd, k24, advisorQtkd, 2024, "Đại học chính quy");
        upsertStudentClass("QTKD-K25-01", "QTKD K25 - Lớp 01", qtkd, k25, advisorQtkd, 2025, "Đại học chính quy");

        // Kế toán
        upsertStudentClass("KETOAN-K25-01", "Kế toán K25 - Lớp 01", ketoan, k25, advisorKetoan, 2025, "Đại học chính quy");
    }

    private void upsertStudentClass(String code, String name, Department department,
                                    Cohort cohort, Teacher advisorTeacher, Integer intakeYear, String program) {
        
        // Try to find AcademicProgram (Phase 1)
        AcademicProgram academicProgram = null;
        if (department != null && intakeYear != null) {
             String progCode = department.getCode() + "_K" + (intakeYear % 100);
             academicProgram = academicProgramRepository.findByCode(progCode).orElse(null);
        }
        final AcademicProgram finalAcademicProgram = academicProgram;

        studentClassRepository.findByCode(code).ifPresentOrElse(existing -> {
            existing.setName(name);
            existing.setDepartment(department);
            existing.setCohort(cohort);
            existing.setAdvisorTeacher(advisorTeacher);
            existing.setIntakeYear(intakeYear);
            existing.setProgram(program);
            existing.setAcademicProgram(finalAcademicProgram);
            existing.setActive(true);
            studentClassRepository.save(existing);
            log.debug("  [StudentClass] Cập nhật: {}", code);
        }, () -> {
            studentClassRepository.save(StudentClass.builder()
                    .code(code)
                    .name(name)
                    .department(department)
                    .cohort(cohort)
                    .advisorTeacher(advisorTeacher)
                    .intakeYear(intakeYear)
                    .program(program)
                    .academicProgram(finalAcademicProgram)
                    .active(true)
                    .build());
            log.debug("  [StudentClass] Tạo mới: {}", code);
        });
    }

    private StudentClass studentClassByCode(String code) {
        return studentClassRepository.findByCode(code)
                .orElseThrow(() -> new IllegalStateException("Không tìm thấy lớp hành chính: " + code));
    }

    // =========================================================================
    //  6. STUDENT PROFILES
    // =========================================================================

    private void seedStudentProfiles() {
        log.info("[Seeder] Tạo hồ sơ sinh viên...");

        Department cntt   = departmentByCode("CNTT");
        Department qtkd   = departmentByCode("QTKD");
        Department ketoan = departmentByCode("KETOAN");

        StudentClass cnttK25_01 = studentClassByCode("CNTT-K25-01");
        StudentClass cnttK25_02 = studentClassByCode("CNTT-K25-02");
        StudentClass qtkdK25_01 = studentClassByCode("QTKD-K25-01");
        StudentClass ketoanK25  = studentClassByCode("KETOAN-K25-01");
        StudentClass cnttK24_01 = studentClassByCode("CNTT-K24-01");

        // Gán sinh viên vào lớp hành chính
        // Tạo 80 sinh viên cho mỗi lớp
        seedStudentsForClass(cnttK25_01, cntt, "CNTT25", 80);
        seedStudentsForClass(cnttK25_02, cntt, "CNTT25_2", 80);
        seedStudentsForClass(qtkdK25_01, qtkd, "QTKD25", 80);
        seedStudentsForClass(ketoanK25, ketoan, "KETOAN25", 80);
        seedStudentsForClass(cnttK24_01, cntt, "CNTT24", 80);
        
        // Thêm tài khoản mẫu cũ (để test login)
        upsertStudent("student@nws.com.vn", "SV2500001", cntt, cnttK25_01, "0901111111");
        upsertStudent("tranthihinh@nws.com.vn", "SV2500002", cntt, cnttK25_01, "0901111112");
    }

    private void seedStudentsForClass(StudentClass studentClass, Department dept, String prefix, int count) {
        log.info("  [Seeder] Tạo {} sinh viên cho lớp {}", count, studentClass.getCode());
        for (int i = 1; i <= count; i++) {
            String suffix = String.format("%03d", i);
            String username = prefix.toLowerCase() + ".sv" + suffix;
            String email = username + "@nws.com.vn";
            String studentCode = prefix + suffix;
            String phone = "09" + String.format("%08d", i);
            
            upsertStudent(email, studentCode, dept, studentClass, phone);
        }
    }

    private void upsertStudent(String email, String studentCode, Department department,
                               StudentClass studentClass, String phone) {
        // Auto-create user if not exists
        String username = email.split("@")[0];
        upsertUser(username, email, "Student@123", Set.of(RoleType.STUDENT));
        
        User user = userByEmail(email);
        studentRepository.findByUserId(user.getId()).ifPresentOrElse(existing -> {
            existing.setStudentCode(studentCode);
            existing.setDepartment(department);
            existing.setStudentClass(studentClass);
            existing.setPhone(phone);
            existing.setActive(true);
            studentRepository.save(existing);
            log.debug("  [Student] Cập nhật: {} - {}", studentCode, email);
        }, () -> {
            studentRepository.save(Student.builder()
                    .user(user)
                    .studentCode(studentCode)
                    .department(department)
                    .studentClass(studentClass)
                    .phone(phone)
                    .active(true)
                    .build());
            log.debug("  [Student] Tạo mới: {} - {}", studentCode, email);
        });
    }

    private Student studentByEmail(String email) {
        return studentRepository.findByUserId(userByEmail(email).getId())
                .orElseThrow(() -> new IllegalStateException("Không tìm thấy hồ sơ sinh viên: " + email));
    }

    // =========================================================================
    //  7. SEMESTERS
    // =========================================================================

    private void seedSemesters() {
        log.info("[Seeder] Tạo học kỳ...");
        // Học kỳ quá khứ
        upsertSemester("HK1_2324", "Học kỳ 1 2023-2024",
                LocalDate.of(2023, 9,  1), LocalDate.of(2024, 1, 15), false, false);
        upsertSemester("HK2_2324", "Học kỳ 2 2023-2024",
                LocalDate.of(2024, 2,  1), LocalDate.of(2024, 6, 30), false, false);
        upsertSemester("HK1_2425", "Học kỳ 1 2024-2025",
                LocalDate.of(2024, 9,  1), LocalDate.of(2025, 1, 15), false, false);
        // Học kỳ hiện tại (active = true)
        upsertSemester("HK2_2425", "Học kỳ 2 2024-2025",
                LocalDate.of(2025, 2,  1), LocalDate.of(2025, 6, 30), false, false);
        upsertSemester("HK1_2526", "Học kỳ 1 2025-2026",
                LocalDate.of(2025, 9,  1), LocalDate.of(2026, 1, 15), false, false);
        // Học kỳ đang diễn ra (active)
        upsertSemester("HK2_2526", "Học kỳ 2 2025-2026",
                LocalDate.of(2026, 1, 10), LocalDate.of(2026, 6, 15), true, false);
        // Kỳ phụ (secondary_active)
        upsertSemester("HK_PHU_2526", "Kỳ học phụ Hè 2025-2026",
                LocalDate.of(2026, 3,  1), LocalDate.of(2026, 4, 30), false, true);
    }

    private void upsertSemester(String code, String name, LocalDate start, LocalDate end,
                                boolean active, boolean secondaryActive) {
        semesterRepository.findByCode(code).ifPresentOrElse(existing -> {
            existing.setName(name);
            existing.setStartDate(start);
            existing.setEndDate(end);
            existing.setActive(active);
            existing.setSecondaryActive(secondaryActive);
            semesterRepository.save(existing);
            log.debug("  [Semester] Cập nhật: {}", code);
        }, () -> {
            semesterRepository.save(Semester.builder()
                    .code(code)
                    .name(name)
                    .startDate(start)
                    .endDate(end)
                    .active(active)
                    .secondaryActive(secondaryActive)
                    .build());
            log.debug("  [Semester] Tạo mới: {}", code);
        });
    }

    private Semester semesterByCode(String code) {
        return semesterRepository.findByCode(code)
                .orElseThrow(() -> new IllegalStateException("Không tìm thấy học kỳ: " + code));
    }

    // =========================================================================
    //  8. SUBJECTS (môn học)
    // =========================================================================

    private void seedSubjects() {
        log.info("[Seeder] Tạo môn học...");

        Department cntt   = departmentByCode("CNTT");
        Department qtkd   = departmentByCode("QTKD");
        Department ketoan = departmentByCode("KETOAN");

        // Môn CNTT
        upsertSubject("JAVA001",   "Lập trình Java cơ bản",             3, cntt,   "Nền tảng lập trình hướng đối tượng với Java",             (short)40, (short)60);
        upsertSubject("JAVA002",   "Lập trình Java nâng cao",            3, cntt,   "Collections, Generics, Concurrency, Java 17+ features",   (short)40, (short)60);
        upsertSubject("WEB001",    "Lập trình Web với Spring Boot",       4, cntt,   "REST API, Spring Security, JPA, microservices cơ bản",    (short)50, (short)50);
        upsertSubject("DB001",     "Cơ sở dữ liệu",                      3, cntt,   "Mô hình quan hệ, SQL, thiết kế CSDL",                     (short)40, (short)60);
        upsertSubject("DB002",     "Cơ sở dữ liệu nâng cao",             3, cntt,   "Stored procedures, indexing, query optimization",         (short)40, (short)60);
        upsertSubject("NET001",    "Mạng máy tính",                       3, cntt,   "TCP/IP, giao thức mạng, bảo mật cơ bản",                  (short)30, (short)70);
        upsertSubject("AI001",     "Trí tuệ nhân tạo",                    3, cntt,   "Machine learning, neural networks, ứng dụng AI",          (short)50, (short)50);
        upsertSubject("MATH001",   "Toán rời rạc",                        3, cntt,   "Logic, tập hợp, đồ thị, thuật toán cơ bản",               (short)30, (short)70);
        upsertSubject("SE001",     "Kỹ thuật phần mềm",                   3, cntt,   "SDLC, Agile, thiết kế hệ thống, kiểm thử phần mềm",      (short)50, (short)50);

        // Môn QTKD
        upsertSubject("MKT001",    "Marketing căn bản",                   3, qtkd,   "4P, phân tích thị trường, hành vi người tiêu dùng",       (short)50, (short)50);
        upsertSubject("MGMT001",   "Quản trị học",                        3, qtkd,   "Lý thuyết quản trị, lập kế hoạch, tổ chức",               (short)40, (short)60);
        upsertSubject("FIN001",    "Tài chính doanh nghiệp",               3, qtkd,   "Nguồn vốn, quản lý tài chính, đầu tư",                    (short)40, (short)60);

        // Môn Kế toán
        upsertSubject("ACC001",    "Nguyên lý kế toán",                   3, ketoan, "Nguyên tắc kế toán, hệ thống tài khoản, BCTC",           (short)40, (short)60);
        upsertSubject("ACC002",    "Kế toán tài chính",                   3, ketoan, "Kế toán theo chuẩn VAS, lập báo cáo tài chính",           (short)40, (short)60);
    }

    private void upsertSubject(String code, String name, int credits, Department department,
                               String description, short processWeight, short examWeight) {
        subjectRepository.findByCode(code).ifPresentOrElse(existing -> {
            existing.setName(name);
            existing.setCredits(credits);
            existing.setDepartmentId(department != null ? department.getId() : null);
            existing.setDescription(description);
            existing.setActive(true);
            existing.setProcessWeight(processWeight);
            existing.setExamWeight(examWeight);
            subjectRepository.save(existing);
            log.debug("  [Subject] Cập nhật: {}", code);
        }, () -> {
            subjectRepository.save(Subject.builder()
                    .code(code)
                    .name(name)
                    .credits(credits)
                    .departmentId(department != null ? department.getId() : null)
                    .description(description)
                    .active(true)
                    .processWeight(processWeight)
                    .examWeight(examWeight)
                    .build());
            log.debug("  [Subject] Tạo mới: {}", code);
        });
    }

    private Subject subjectByCode(String code) {
        return subjectRepository.findByCode(code)
                .orElseThrow(() -> new IllegalStateException("Không tìm thấy môn học: " + code));
    }

    // =========================================================================
    //  9. SECTIONS (lớp học phần)
    //  *** QUAN TRỌNG: teacher phải là Teacher entity (teachers.id),
    //                  KHÔNG phải User entity. ***
    // =========================================================================

    private void seedSections() {
        log.info("[Seeder] Tạo lớp học phần...");

        Semester hk2_2526 = semesterByCode("HK2_2526");
        Semester hk_phu   = semesterByCode("HK_PHU_2526");
        Semester hk1_2526 = semesterByCode("HK1_2526");
        Semester hk2_2425 = semesterByCode("HK2_2425");

        // ---- Lấy USER của giảng viên (Section.teacher là User; repository sẽ map sang teacher profile) ----
        User gvHoa   = userByEmail("phamthihoa@nws.com.vn");     // Java/Spring
        User gvPhuc  = userByEmail("nguyenhuuphuc@nws.com.vn");  // AI/Python
        User gvDuc   = userByEmail("tranvanduc@nws.com.vn");     // DB
        User gvHung  = userByEmail("doduchung@nws.com.vn");      // QTKD
        User gvLan   = userByEmail("vothilan@nws.com.vn");       // Kế toán
        User gvVu    = userByEmail("giaovu@nws.com.vn");         // Giáo vụ dạy bù

        // ===== HK2 2025-2026 (đang diễn ra — active) =====

        // Java cho K25
        upsertSection("JAVA001_HK2_2526_01", "Lập trình Java CB - Nhóm 01",
                hk2_2526, subjectByCode("JAVA001"), gvHoa, 120, 5, true);
        upsertSection("JAVA001_HK2_2526_02", "Lập trình Java CB - Nhóm 02",
                hk2_2526, subjectByCode("JAVA001"), gvHoa, 120, 5, true);

        // Java nâng cao cho K24
        upsertSection("JAVA002_HK2_2526_01", "Lập trình Java NC - Nhóm 01",
                hk2_2526, subjectByCode("JAVA002"), gvHoa, 120, 5, true);

        // Web Spring Boot
        upsertSection("WEB001_HK2_2526_01",  "Lập trình Web Spring Boot - Nhóm 01",
                hk2_2526, subjectByCode("WEB001"), gvHoa, 120, 5, true);
        upsertSection("WEB001_HK2_2526_02",  "Lập trình Web Spring Boot - Nhóm 02",
                hk2_2526, subjectByCode("WEB001"), gvPhuc, 120, 5, true);

        // Cơ sở dữ liệu
        upsertSection("DB001_HK2_2526_01",   "Cơ sở dữ liệu - Nhóm 01",
                hk2_2526, subjectByCode("DB001"), gvDuc, 120, 5, true);
        upsertSection("DB002_HK2_2526_01",   "CSDL nâng cao - Nhóm 01",
                hk2_2526, subjectByCode("DB002"), gvDuc, 120, 5, true);

        // AI
        upsertSection("AI001_HK2_2526_01",   "Trí tuệ nhân tạo - Nhóm 01",
                hk2_2526, subjectByCode("AI001"), gvPhuc, 120, 5, true);

        // QTKD
        upsertSection("MKT001_HK2_2526_01",  "Marketing CB - Nhóm 01",
                hk2_2526, subjectByCode("MKT001"), gvHung, 120, 5, true);
        upsertSection("FIN001_HK2_2526_01",  "Tài chính doanh nghiệp - Nhóm 01",
                hk2_2526, subjectByCode("FIN001"), gvHung, 120, 5, true);

        // Kế toán
        upsertSection("ACC001_HK2_2526_01",  "Nguyên lý kế toán - Nhóm 01",
                hk2_2526, subjectByCode("ACC001"), gvLan, 120, 5, true);

        // ===== Kỳ học phụ (secondary_active) =====
        upsertSection("JAVA001_PHU_2526_01", "Java CB - Kỳ phụ Hè",
                hk_phu, subjectByCode("JAVA001"), gvVu, 120, 5, true);
        upsertSection("DB001_PHU_2526_01",   "CSDL - Kỳ phụ Hè",
                hk_phu, subjectByCode("DB001"), gvDuc, 120, 5, true);

        // ===== HK1 2025-2026 (đã kết thúc — dữ liệu lịch sử) =====
        upsertSection("JAVA001_HK1_2526_01", "Lập trình Java CB - Nhóm 01",
                hk1_2526, subjectByCode("JAVA001"), gvHoa, 120, 5, false);
        upsertSection("DB001_HK1_2526_01",   "Cơ sở dữ liệu - Nhóm 01",
                hk1_2526, subjectByCode("DB001"), gvDuc, 120, 5, false);
        upsertSection("NET001_HK1_2526_01",  "Mạng máy tính - Nhóm 01",
                hk1_2526, subjectByCode("NET001"), gvPhuc, 120, 5, false);
        upsertSection("MGMT001_HK2_2425_01", "Quản trị học - Nhóm 01",
                hk2_2425, subjectByCode("MGMT001"), gvHung, 120, 5, false);
    }

    private void upsertSection(String code, String name, Semester semester, Subject subject,
                               User teacher, int maxStudents, int minStudents, boolean active) {
        sectionRepository.findByCode(code).ifPresentOrElse(existing -> {
            existing.setName(name);
            existing.setSemester(semester);
            existing.setSubject(subject);
            existing.setTeacher(teacher);
            existing.setMaxStudents(maxStudents);
            existing.setMinStudents(minStudents);
            existing.setActive(active);
            existing.setStatus(SectionLifecycleStatus.OPEN);
            existing.setRegistrationEnabled(active);
            existing.setEnrollmentStartDate(LocalDate.now().minusDays(10));
            existing.setEnrollmentEndDate(LocalDate.now().plusDays(60));
            sectionRepository.save(existing);
            log.debug("  [Section] Cập nhật: {}", code);
        }, () -> {
            sectionRepository.save(Section.builder()
                    .code(code)
                    .name(name)
                    .semester(semester)
                    .subject(subject)
                    .teacher(teacher)
                    .maxStudents(maxStudents)
                    .currentStudents(0)
                    .minStudents(minStudents)
                    .active(active)
                    .status(SectionLifecycleStatus.OPEN)
                    .registrationEnabled(active)
                    .enrollmentStartDate(LocalDate.now().minusDays(10))
                    .enrollmentEndDate(LocalDate.now().plusDays(60))
                    .build());
            log.debug("  [Section] Tạo mới: {}", code);
        });
    }

    private Section sectionByCode(String code) {
        return sectionRepository.findByCode(code)
                .orElseThrow(() -> new IllegalStateException("Không tìm thấy lớp học phần: " + code));
    }

    // =========================================================================
    //  10. SECTION TIME SLOTS (lịch học)
    //  Thứ: 1=CN, 2=T2, 3=T3, 4=T4, 5=T5, 6=T6, 7=T7 (ISO: 1=T2...7=CN)
    //  Dùng chuẩn ISO: 1=Thứ Hai, 2=Thứ Ba, ..., 5=Thứ Sáu, 6=Thứ Bảy, 7=Chủ Nhật
    // =========================================================================

    private void seedSectionTimeSlots() {
        log.info("[Seeder] Tạo lịch học...");

        // JAVA001 - Nhóm 01: T2 và T4 sáng
        replaceSectionSlots("JAVA001_HK2_2526_01", List.of(
                slot(1, "07:30", "10:45"),
                slot(3, "07:30", "10:45")
        ));
        // JAVA001 - Nhóm 02: T3 và T5 chiều
        replaceSectionSlots("JAVA001_HK2_2526_02", List.of(
                slot(2, "13:00", "16:15"),
                slot(4, "13:00", "16:15")
        ));
        // JAVA002 NC: T2 chiều và T5 chiều
        replaceSectionSlots("JAVA002_HK2_2526_01", List.of(
                slot(1, "13:00", "16:15"),
                slot(4, "13:00", "16:15")
        ));
        // WEB001 - Nhóm 01: T3 sáng và T5 sáng
        replaceSectionSlots("WEB001_HK2_2526_01", List.of(
                slot(2, "07:30", "10:45"),
                slot(4, "07:30", "10:45")
        ));
        // WEB001 - Nhóm 02: T3 chiều và T5 chiều
        replaceSectionSlots("WEB001_HK2_2526_02", List.of(
                slot(2, "13:00", "16:15"),
                slot(4, "13:00", "16:15")
        ));
        // DB001 - Nhóm 01: T6 sáng và T7 sáng (đổi để tránh trùng JAVA001 T2/T4)
        replaceSectionSlots("DB001_HK2_2526_01", List.of(
                slot(5, "07:30", "10:45"),
                slot(6, "07:30", "10:45")
        ));
        // DB002 NC: T6 sáng
        replaceSectionSlots("DB002_HK2_2526_01", List.of(
                slot(5, "07:30", "12:00")
        ));
        // AI001: T5 sáng và T7
        replaceSectionSlots("AI001_HK2_2526_01", List.of(
                slot(4, "07:30", "10:45"),
                slot(6, "07:30", "10:45")
        ));
        // MKT001: T2 chiều và T4 chiều
        replaceSectionSlots("MKT001_HK2_2526_01", List.of(
                slot(1, "13:00", "16:15"),
                slot(3, "13:00", "16:15")
        ));
        // FIN001: T3 sáng và T5 sáng
        replaceSectionSlots("FIN001_HK2_2526_01", List.of(
                slot(2, "07:30", "10:45"),
                slot(4, "07:30", "10:45")
        ));
        // ACC001: T4 sáng
        replaceSectionSlots("ACC001_HK2_2526_01", List.of(
                slot(3, "07:30", "10:45"),
                slot(5, "13:00", "16:15")
        ));
        // Kỳ phụ: T7 và CN
        replaceSectionSlots("JAVA001_PHU_2526_01", List.of(
                slot(6, "07:30", "11:30"),
                slot(7, "07:30", "11:30")
        ));
        replaceSectionSlots("DB001_PHU_2526_01", List.of(
                slot(6, "13:00", "17:00")
        ));
    }

    private SectionTimeSlot slot(int isoDayOfWeek, String start, String end) {
        return SectionTimeSlot.builder()
                .dayOfWeek((short) isoDayOfWeek)
                .startTime(LocalTime.parse(start))
                .endTime(LocalTime.parse(end))
                .build();
    }

    private void replaceSectionSlots(String sectionCode, List<SectionTimeSlot> slots) {
        Section section = sectionByCode(sectionCode);
        slots.forEach(s -> s.setSectionId(section.getId()));
        sectionTimeSlotRepository.replaceSectionTimeSlots(section.getId(), slots);
        log.debug("  [TimeSlot] Cập nhật lịch: {} ({} tiết/tuần)", sectionCode, slots.size());
    }

    // =========================================================================
    //  11. ENROLLMENTS (đăng ký học)
    // =========================================================================

    private void seedEnrollments() {
        log.info("[Seeder] Tạo đăng ký học...");

        // Lấy Student profile
        Student sv1  = studentByEmail("student@nws.com.vn");           // Nguyễn Văn An
        Student sv2  = studentByEmail("tranthihinh@nws.com.vn");       // Trần Thị Bình
        Student sv3  = studentByEmail("lequoccuong@nws.com.vn");       // Lê Quốc Cường
        Student sv4  = studentByEmail("phamngocidiem@nws.com.vn");     // Phạm Ngọc Diễm
        Student sv5  = studentByEmail("hoangvanem@nws.com.vn");        // Hoàng Văn Em
        Student sv6  = studentByEmail("nguyenthiphuong@nws.com.vn");   // Nguyễn Thị Phương
        Student sv7  = studentByEmail("buiducgiang@nws.com.vn");       // Bùi Đức Giang
        Student sv8  = studentByEmail("dothihuyen@nws.com.vn");        // Đỗ Thị Huyền
        Student sv9  = studentByEmail("caovanien@nws.com.vn");         // Cao Văn Iên (K24)
        Student sv10 = studentByEmail("maithikhanh@nws.com.vn");       // Mai Thị Khánh (K24)

        // ===== JAVA001 HK2_2526 Nhóm 01 — nhiều SV để thấy đủ dữ liệu =====
        enrollIfNotExists("JAVA001_HK2_2526_01", sv1);
        enrollIfNotExists("JAVA001_HK2_2526_01", sv2);
        enrollIfNotExists("JAVA001_HK2_2526_01", sv3);

        // ===== JAVA001 HK2_2526 Nhóm 02 =====
        enrollIfNotExists("JAVA001_HK2_2526_02", sv4);
        enrollIfNotExists("JAVA001_HK2_2526_02", sv5);

        // ===== JAVA002 nâng cao (K24) =====
        enrollIfNotExists("JAVA002_HK2_2526_01", sv9);
        enrollIfNotExists("JAVA002_HK2_2526_01", sv10);

        // ===== WEB Spring Boot Nhóm 01 =====
        enrollIfNotExists("WEB001_HK2_2526_01", sv1);
        enrollIfNotExists("WEB001_HK2_2526_01", sv3);
        enrollIfNotExists("WEB001_HK2_2526_01", sv9);

        // ===== DB001 =====
        enrollIfNotExists("DB001_HK2_2526_01", sv2);
        enrollIfNotExists("DB001_HK2_2526_01", sv4);
        enrollIfNotExists("DB001_HK2_2526_01", sv10);

        // ===== AI001 =====
        enrollIfNotExists("AI001_HK2_2526_01", sv3);
        enrollIfNotExists("AI001_HK2_2526_01", sv5);

        // ===== Marketing =====
        enrollIfNotExists("MKT001_HK2_2526_01", sv6);
        enrollIfNotExists("MKT001_HK2_2526_01", sv7);

        // ===== Tài chính =====
        enrollIfNotExists("FIN001_HK2_2526_01", sv6);
        enrollIfNotExists("FIN001_HK2_2526_01", sv7);

        // ===== Kế toán =====
        enrollIfNotExists("ACC001_HK2_2526_01", sv8);

        // ===== Kỳ phụ =====
        enrollIfNotExists("JAVA001_PHU_2526_01", sv2);
        enrollIfNotExists("DB001_PHU_2526_01",   sv5);

        // ===== BULK SEEDING FOR K25 CNTT (80 students) =====
        seedBulkK25Enrollments();
    }

    private void seedBulkK25Enrollments() {
        log.info("[Seeder] Tạo đăng ký học và điểm cho toàn bộ K25 CNTT...");
        StudentClass cnttK25_01 = studentClassByCode("CNTT-K25-01");
        
        // Find all students in this class
        List<Student> students = studentRepository.findByStudentClassId(cnttK25_01.getId());
        
        for (Student s : students) {
            // Skip the explicitly named students we already handled (sv1, sv2...)
            if (s.getUser().getEmail().startsWith("student") || 
                s.getUser().getEmail().startsWith("tranthihinh") || 
                s.getUser().getEmail().startsWith("lequoccuong")) {
                continue;
            }

            // 1. Enroll in Semester 1 (Past) - JAVA001, DB001
            // Use forceEnroll for past sections (inactive)
            forceEnroll("JAVA001_HK1_2526_01", s);
            forceEnroll("DB001_HK1_2526_01", s);

            // 2. Give them random grades for Semester 1 (mostly pass)
            double javaScore = 5.0 + (Math.random() * 5.0); // 5.0 - 10.0
            double dbScore = 4.0 + (Math.random() * 6.0);   // 4.0 - 10.0
            
            enterGrade("JAVA001_HK1_2526_01", s.getUser().getEmail(), javaScore, javaScore);
            enterGrade("DB001_HK1_2526_01", s.getUser().getEmail(), dbScore, dbScore);

            // 3. Enroll in Semester 2 (Current) - JAVA002, DB002
            // Distribute into groups
            if (Math.random() > 0.5) {
                enrollIfNotExists("JAVA001_HK2_2526_01", s); // Re-learning or just taking it? Wait, JAVA001 is Sem 1.
                // Actually Program says: JAVA002 is Sem 2.
                // But seedSections created JAVA002_HK2_2526_01 (Advanced Java).
                // Let's enroll them in JAVA001_HK2 (maybe retake) or assume they passed.
                // Program: JAVA002 is Compulsory Sem 2.
                // Section: JAVA002_HK2_2526_01.
                enrollIfNotExists("JAVA002_HK2_2526_01", s);
            } else {
                // Maybe they take Web?
                enrollIfNotExists("WEB001_HK2_2526_01", s);
            }
            
            // Enroll in DB002 (Sem 2)
            enrollIfNotExists("DB002_HK2_2526_01", s);
        }
    }

    private void enrollIfNotExists(String sectionCode, Student student) {
        Section section = sectionByCode(sectionCode);
        if (enrollmentRepository.existsBySectionIdAndStudentId(section.getId(), student.getId())) {
            log.debug("  [Enrollment] Đã tồn tại: {} - SV#{}", sectionCode, student.getStudentCode());
            return;
        }
        EnrollmentCreateRequest req = new EnrollmentCreateRequest();
        req.setSectionId(section.getId());
        req.setStudentId(student.getId());
        try {
            enrollmentService.enrollStudent(req);
            log.debug("  [Enrollment] Đăng ký: {} - SV#{}", sectionCode, student.getStudentCode());
        } catch (Exception e) {
            log.warn("  [Enrollment] Lỗi đăng ký {} - SV#{}: {}", sectionCode, student.getStudentCode(), e.getMessage());
        }
    }

    private void forceEnroll(String sectionCode, Student student) {
        try {
            Section section = sectionByCode(sectionCode);
            if (enrollmentRepository.existsBySectionIdAndStudentId(section.getId(), student.getId())) {
                log.debug("  [ForceEnroll] Đã tồn tại: {} - SV#{}", sectionCode, student.getStudentCode());
                return;
            }

            Enrollment enrollment = Enrollment.builder()
                    .section(section)
                    .student(student)
                    .status(EnrollmentStatus.ENROLLED)
                    .build();

            enrollmentRepository.save(enrollment);

            // Update section count manually since we bypassed service
            section.setCurrentStudents(section.getCurrentStudents() + 1);
            sectionRepository.save(section);

            log.debug("  [ForceEnroll] Đăng ký thành công: {} - SV#{}", sectionCode, student.getStudentCode());
        } catch (Exception e) {
            log.warn("  [ForceEnroll] Lỗi đăng ký {} - SV#{}: {}", sectionCode, student.getStudentCode(), e.getMessage());
        }
    }

    // =========================================================================
    //  12. GRADES (nhập điểm mẫu cho HK đã kết thúc + HK hiện tại)
    // =========================================================================

    private void seedEnrollmentGrades() {
        log.info("[Seeder] Nhập điểm mẫu...");

        // Nhập điểm cho JAVA001 HK2_2526 Nhóm 01
        enterGrade("JAVA001_HK2_2526_01", "student@nws.com.vn",        8.5, 7.0);
        enterGrade("JAVA001_HK2_2526_01", "tranthihinh@nws.com.vn",    7.0, 6.5);
        enterGrade("JAVA001_HK2_2526_01", "lequoccuong@nws.com.vn",    9.0, 8.5);

        // Điểm JAVA002 (K24 - đã có điểm quá trình)
        enterGrade("JAVA002_HK2_2526_01", "caovanien@nws.com.vn",      8.0, 8.5);
        enterGrade("JAVA002_HK2_2526_01", "maithikhanh@nws.com.vn",    7.5, 9.0);

        // Điểm WEB001
        enterGrade("WEB001_HK2_2526_01",  "student@nws.com.vn",        7.0, 7.5);
        enterGrade("WEB001_HK2_2526_01",  "lequoccuong@nws.com.vn",    8.5, 8.0);

        // Điểm DB001
        enterGrade("DB001_HK2_2526_01",   "tranthihinh@nws.com.vn",    6.5, 7.0);
        enterGrade("DB001_HK2_2526_01",   "phamngocidiem@nws.com.vn",  8.0, 7.5);

        // Marketing
        enterGrade("MKT001_HK2_2526_01",  "nguyenthiphuong@nws.com.vn", 8.0, 8.5);
        enterGrade("MKT001_HK2_2526_01",  "buiducgiang@nws.com.vn",     7.5, 7.0);

        // Kế toán
        enterGrade("ACC001_HK2_2526_01",  "dothihuyen@nws.com.vn",      9.0, 8.0);
    }

    private void enterGrade(String sectionCode, String studentEmail, double processScore, double examScore) {
        try {
            Section section = sectionByCode(sectionCode);
            Student student = studentByEmail(studentEmail);

            List<Enrollment> enrollments = enrollmentRepository.findBySectionId(section.getId());
            Enrollment enrollment = enrollments.stream()
                    .filter(e -> e.getStudent().getId().equals(student.getId()))
                    .findFirst()
                    .orElse(null);

            if (enrollment == null) {
                log.debug("  [Grade] Không tìm thấy enrollment: {} - {}", sectionCode, studentEmail);
                return;
            }
            if (enrollment.isScoreLocked()) {
                log.debug("  [Grade] Điểm đã khóa: {} - {}", sectionCode, studentEmail);
                return;
            }

            EnrollmentUpdateRequest req = new EnrollmentUpdateRequest();
            req.setProcessScore(processScore);
            req.setExamScore(examScore);
            enrollmentService.updateEnrollment(enrollment.getId(), "admin@nws.com.vn", true, false, req);
            log.debug("  [Grade] Nhập điểm: {} - {} (QT:{} | Thi:{})", sectionCode, studentEmail, processScore, examScore);
        } catch (Exception e) {
            log.warn("  [Grade] Lỗi nhập điểm {} - {}: {}", sectionCode, studentEmail, e.getMessage());
        }
    }

    // =========================================================================
    //  HELPERS
    // =========================================================================

    private User userByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("Không tìm thấy tài khoản: " + email));
    }
}
