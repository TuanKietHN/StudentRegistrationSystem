package vn.com.nws.cms.modules.academic.infrastructure.persistence.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import vn.com.nws.cms.modules.academic.infrastructure.persistence.entity.AdminClassEntity;

import java.util.Optional;

@Repository
public interface JpaAdminClassRepository extends JpaRepository<AdminClassEntity, Long> {
    @EntityGraph(attributePaths = {"department"})
    Optional<AdminClassEntity> findByCode(String code);

    boolean existsByCode(String code);

    boolean existsByCodeAndIdNot(String code, Long id);

    @EntityGraph(attributePaths = {"department"})
    @Query("""
            SELECT c
            FROM AdminClassEntity c
            LEFT JOIN c.department d
            WHERE (:active IS NULL OR c.active = :active)
              AND (:departmentId IS NULL OR d.id = :departmentId)
              AND (:intakeYear IS NULL OR c.intakeYear = :intakeYear)
              AND (
                :keyword IS NULL
                OR LOWER(c.code) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
              )
            """)
    Page<AdminClassEntity> search(String keyword, Long departmentId, Integer intakeYear, Boolean active, Pageable pageable);
}

