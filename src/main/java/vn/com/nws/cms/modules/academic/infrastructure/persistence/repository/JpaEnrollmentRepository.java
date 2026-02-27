package vn.com.nws.cms.modules.academic.infrastructure.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.stereotype.Repository;
import vn.com.nws.cms.modules.academic.infrastructure.persistence.entity.EnrollmentEntity;

import java.util.List;
import java.util.Optional;

@Repository
public interface JpaEnrollmentRepository extends JpaRepository<EnrollmentEntity, Long> {
    boolean existsByCourseIdAndStudentId(Long courseId, Long studentId);

    @Override
    @EntityGraph(attributePaths = {"course", "course.semester", "course.subject", "course.teacher", "course.teacher.user", "student", "student.user"})
    Optional<EnrollmentEntity> findById(Long id);

    @EntityGraph(attributePaths = {"course", "course.semester", "course.subject", "course.teacher", "course.teacher.user", "student", "student.user"})
    List<EnrollmentEntity> findByCourseId(Long courseId);

    @EntityGraph(attributePaths = {"course", "course.semester", "course.subject", "course.teacher", "course.teacher.user", "student", "student.user"})
    List<EnrollmentEntity> findByStudentId(Long studentId);
    long countByCourseId(Long courseId);
}
