package vn.com.nws.cms.common.seed;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.PlatformTransactionManager;
import vn.com.nws.cms.modules.academic.application.EnrollmentService;
import vn.com.nws.cms.modules.academic.application.SectionGradeService;
import vn.com.nws.cms.modules.academic.domain.repository.*;
import vn.com.nws.cms.modules.auth.domain.repository.UserRepository;
import vn.com.nws.cms.modules.auth.infrastructure.persistence.repository.JpaPermissionRepository;
import vn.com.nws.cms.modules.auth.infrastructure.persistence.repository.JpaRolePermissionRepository;
import vn.com.nws.cms.modules.auth.infrastructure.persistence.repository.JpaRoleRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class DataSeederConditionTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(TestConfig.class);

    @Test
    void shouldNotCreateDataSeederWhenSeedDisabled() {
        contextRunner
                .withPropertyValues(
                        "spring.profiles.active=dev",
                        "cms.seed.enabled=false"
                )
                .run(ctx -> assertThat(ctx).doesNotHaveBean(DataSeeder.class));
    }

    @Test
    void shouldCreateDataSeederWhenDevProfileAndSeedEnabled() {
        contextRunner
                .withPropertyValues(
                        "spring.profiles.active=dev",
                        "cms.seed.enabled=true"
                )
                .run(ctx -> assertThat(ctx).hasSingleBean(DataSeeder.class));
    }

    @Test
    void shouldNotCreateDataSeederWhenNotDevProfile() {
        contextRunner
                .withPropertyValues(
                        "spring.profiles.active=prod",
                        "cms.seed.enabled=true"
                )
                .run(ctx -> assertThat(ctx).doesNotHaveBean(DataSeeder.class));
    }

    @Configuration(proxyBeanMethods = false)
    @ComponentScan(basePackageClasses = DataSeeder.class)
    static class TestConfig {

        @Bean
        PasswordEncoder passwordEncoder() {
            return mock(PasswordEncoder.class);
        }

        @Bean
        UserRepository userRepository() {
            return mock(UserRepository.class);
        }

        @Bean
        JpaRoleRepository jpaRoleRepository() {
            return mock(JpaRoleRepository.class);
        }

        @Bean
        JpaPermissionRepository jpaPermissionRepository() {
            return mock(JpaPermissionRepository.class);
        }

        @Bean
        JpaRolePermissionRepository jpaRolePermissionRepository() {
            return mock(JpaRolePermissionRepository.class);
        }

        @Bean
        DepartmentRepository departmentRepository() {
            return mock(DepartmentRepository.class);
        }

        @Bean
        TeacherRepository teacherRepository() {
            return mock(TeacherRepository.class);
        }

        @Bean
        StudentRepository studentRepository() {
            return mock(StudentRepository.class);
        }

        @Bean
        SemesterRepository semesterRepository() {
            return mock(SemesterRepository.class);
        }

        @Bean
        SubjectRepository subjectRepository() {
            return mock(SubjectRepository.class);
        }

        @Bean
        SectionRepository sectionRepository() {
            return mock(SectionRepository.class);
        }

        @Bean
        SectionTimeSlotRepository sectionTimeSlotRepository() {
            return mock(SectionTimeSlotRepository.class);
        }

        @Bean
        CohortRepository cohortRepository() {
            return mock(CohortRepository.class);
        }

        @Bean
        StudentClassRepository studentClassRepository() {
            return mock(StudentClassRepository.class);
        }

        @Bean
        EnrollmentService enrollmentService() {
            return mock(EnrollmentService.class);
        }

        @Bean
        EnrollmentRepository enrollmentRepository() {
            return mock(EnrollmentRepository.class);
        }

        @Bean
        SectionGradeService sectionGradeService() {
            return mock(SectionGradeService.class);
        }

        @Bean
        PlatformTransactionManager platformTransactionManager() {
            return mock(PlatformTransactionManager.class);
        }
    }
}
