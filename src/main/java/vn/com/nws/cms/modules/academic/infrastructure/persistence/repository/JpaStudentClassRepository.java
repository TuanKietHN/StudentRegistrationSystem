package vn.com.nws.cms.modules.academic.infrastructure.persistence.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import vn.com.nws.cms.modules.academic.infrastructure.persistence.entity.StudentClassEntity;

import java.util.List;
import java.util.Optional;

@Repository
public interface JpaStudentClassRepository extends JpaRepository<StudentClassEntity, Long> {
    @EntityGraph(attributePaths = {"department", "cohort"})
    Optional<StudentClassEntity> findByCode(String code);

    boolean existsByCode(String code);

    boolean existsByCodeAndIdNot(String code, Long id);

    @EntityGraph(attributePaths = {"department", "cohort"})
    @Query("""
            SELECT c
            FROM StudentClassEntity c
            LEFT JOIN c.department d
            LEFT JOIN c.cohort co
            WHERE (:active IS NULL OR c.active = :active)
              AND (:departmentId IS NULL OR d.id = :departmentId)
              AND (:cohortId IS NULL OR co.id = :cohortId)
              AND (
                :keyword IS NULL
                OR LOWER(c.code) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
              )
            """)
    Page<StudentClassEntity> search(String keyword, Long departmentId, Long cohortId, Boolean active, Pageable pageable);

    @EntityGraph(attributePaths = {"department", "cohort"})
    @Query("""
            SELECT c
            FROM StudentClassEntity c
            LEFT JOIN c.department d
            LEFT JOIN c.cohort co
            WHERE (:active IS NULL OR c.active = :active)
              AND (:departmentId IS NULL OR d.id = :departmentId)
              AND (:cohortId IS NULL OR co.id = :cohortId)
            ORDER BY c.code ASC
            """)
    List<StudentClassEntity> searchNoKeywordList(Long departmentId, Long cohortId, Boolean active);
}

