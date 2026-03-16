package vn.com.nws.cms.modules.academic.infrastructure.persistence.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import vn.com.nws.cms.modules.academic.domain.enums.CohortLifecycleStatus;
import vn.com.nws.cms.modules.academic.infrastructure.persistence.entity.CohortEntity;

import java.util.Optional;

@Repository
public interface JpaCohortRepository extends JpaRepository<CohortEntity, Long> {
    @EntityGraph(attributePaths = {"clazz", "semester", "teacher", "teacher.user"})
    Optional<CohortEntity> findByCode(String code);
    boolean existsByCode(String code);

    @EntityGraph(attributePaths = {"clazz", "semester", "teacher", "teacher.user"})
    @Query("SELECT c FROM CohortEntity c WHERE " +
           "(:keyword IS NULL OR LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(c.code) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
           "(:semesterId IS NULL OR c.semester.id = :semesterId) AND " +
           "(:classId IS NULL OR c.clazz.id = :classId) AND " +
           "(:teacherId IS NULL OR c.teacherId = :teacherId) AND " +
           "(:active IS NULL OR c.active = :active) AND " +
           "(:status IS NULL OR c.status = :status)")
    Page<CohortEntity> search(String keyword, Long semesterId, Long classId, Long teacherId, Boolean active, CohortLifecycleStatus status, Pageable pageable);

    @Query("SELECT COUNT(c) FROM CohortEntity c WHERE " +
           "(:keyword IS NULL OR LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(c.code) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
           "(:semesterId IS NULL OR c.semester.id = :semesterId) AND " +
           "(:classId IS NULL OR c.clazz.id = :classId) AND " +
           "(:teacherId IS NULL OR c.teacherId = :teacherId) AND " +
           "(:active IS NULL OR c.active = :active) AND " +
           "(:status IS NULL OR c.status = :status)")
    long count(String keyword, Long semesterId, Long classId, Long teacherId, Boolean active, CohortLifecycleStatus status);
}

