package vn.com.nws.cms.modules.academic.infrastructure.persistence.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import vn.com.nws.cms.modules.academic.infrastructure.persistence.entity.CohortEntity;

import java.util.Optional;

@Repository
public interface JpaCohortRepository extends JpaRepository<CohortEntity, Long> {
    Optional<CohortEntity> findByCode(String code);
    boolean existsByCode(String code);

    @Query("SELECT c FROM CohortEntity c WHERE " +
           "(:keyword IS NULL OR LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(c.code) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
           "(:startYear IS NULL OR c.startYear = :startYear) AND " +
           "(:endYear IS NULL OR c.endYear = :endYear) AND " +
           "(:active IS NULL OR c.active = :active)")
    Page<CohortEntity> search(String keyword, Integer startYear, Integer endYear, Boolean active, Pageable pageable);

    @Query("SELECT COUNT(c) FROM CohortEntity c WHERE " +
           "(:keyword IS NULL OR LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(c.code) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
           "(:startYear IS NULL OR c.startYear = :startYear) AND " +
           "(:endYear IS NULL OR c.endYear = :endYear) AND " +
           "(:active IS NULL OR c.active = :active)")
    long count(String keyword, Integer startYear, Integer endYear, Boolean active);
}
