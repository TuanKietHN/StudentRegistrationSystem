package vn.com.nws.cms.modules.academic.infrastructure.persistence.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import vn.com.nws.cms.modules.academic.infrastructure.persistence.entity.CourseClassEntity;

import java.util.Optional;

@Repository
public interface JpaClassRepository extends JpaRepository<CourseClassEntity, Long> {
    Optional<CourseClassEntity> findByCode(String code);
    boolean existsByCode(String code);
    Page<CourseClassEntity> findAllByActive(boolean active, Pageable pageable);
    long countByActive(boolean active);

    @Query("SELECT c FROM CourseClassEntity c WHERE (:keyword IS NULL OR LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(c.code) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND (:active IS NULL OR c.active = :active)")
    Page<CourseClassEntity> search(String keyword, Boolean active, Pageable pageable);

    @Query("SELECT COUNT(c) FROM CourseClassEntity c WHERE (:keyword IS NULL OR LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(c.code) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND (:active IS NULL OR c.active = :active)")
    long count(String keyword, Boolean active);
}
