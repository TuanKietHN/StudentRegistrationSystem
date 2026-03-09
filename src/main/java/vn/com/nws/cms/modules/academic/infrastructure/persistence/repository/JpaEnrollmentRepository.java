package vn.com.nws.cms.modules.academic.infrastructure.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.stereotype.Repository;
import vn.com.nws.cms.modules.academic.infrastructure.persistence.entity.EnrollmentEntity;

import java.util.List;
import java.util.Optional;

@Repository
public interface JpaEnrollmentRepository extends JpaRepository<EnrollmentEntity, Long> {
    boolean existsBySection_IdAndStudent_Id(Long sectionId, Long studentId);

    @Override
    @EntityGraph(attributePaths = {"section", "section.semester", "section.subject", "section.teacher", "section.teacher.user", "student", "student.user"})
    Optional<EnrollmentEntity> findById(Long id);

    @EntityGraph(attributePaths = {"section", "section.semester", "section.subject", "section.teacher", "section.teacher.user", "student", "student.user"})
    List<EnrollmentEntity> findBySection_Id(Long sectionId);

    @EntityGraph(attributePaths = {"section", "section.semester", "section.subject", "section.teacher", "section.teacher.user", "student", "student.user"})
    List<EnrollmentEntity> findByStudentId(Long studentId);

    @EntityGraph(attributePaths = {"section", "section.semester", "section.subject", "section.teacher", "section.teacher.user"})
    List<EnrollmentEntity> findByStudentIdAndSection_Semester_Id(Long studentId, Long semesterId);

    long countBySection_Id(Long sectionId);
}
