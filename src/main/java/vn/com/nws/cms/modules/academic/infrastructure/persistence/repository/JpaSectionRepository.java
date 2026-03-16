package vn.com.nws.cms.modules.academic.infrastructure.persistence.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import vn.com.nws.cms.modules.academic.domain.enums.SectionLifecycleStatus;
import vn.com.nws.cms.modules.academic.infrastructure.persistence.entity.SectionEntity;

import java.util.Optional;

@Repository
public interface JpaSectionRepository extends JpaRepository<SectionEntity, Long> {
    @EntityGraph(attributePaths = {"subject", "semester", "teacher", "teacher.user"})
    Optional<SectionEntity> findByCode(String code);
    boolean existsByCode(String code);

    @EntityGraph(attributePaths = {"subject", "semester", "teacher", "teacher.user"})
    @Query("SELECT s FROM SectionEntity s WHERE " +
           "(:keyword IS NULL OR LOWER(s.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(s.code) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
           "(:semesterId IS NULL OR s.semester.id = :semesterId) AND " +
           "(:subjectId IS NULL OR s.subject.id = :subjectId) AND " +
           "(:teacherId IS NULL OR s.teacher.id = :teacherId) AND " +
           "(:active IS NULL OR s.active = :active) AND " +
           "(:status IS NULL OR s.status = :status)")
    Page<SectionEntity> search(String keyword, Long semesterId, Long subjectId, Long teacherId, Boolean active, SectionLifecycleStatus status, Pageable pageable);

    @Query("SELECT COUNT(s) FROM SectionEntity s WHERE " +
           "(:keyword IS NULL OR LOWER(s.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(s.code) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
           "(:semesterId IS NULL OR s.semester.id = :semesterId) AND " +
           "(:subjectId IS NULL OR s.subject.id = :subjectId) AND " +
           "(:teacherId IS NULL OR s.teacher.id = :teacherId) AND " +
           "(:active IS NULL OR s.active = :active) AND " +
           "(:status IS NULL OR s.status = :status)")
    long count(String keyword, Long semesterId, Long subjectId, Long teacherId, Boolean active, SectionLifecycleStatus status);
}
