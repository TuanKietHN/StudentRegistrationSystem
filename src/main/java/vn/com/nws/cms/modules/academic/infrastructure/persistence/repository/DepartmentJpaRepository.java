package vn.com.nws.cms.modules.academic.infrastructure.persistence.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.com.nws.cms.modules.academic.infrastructure.persistence.entity.DepartmentEntity;

@Repository
public interface DepartmentJpaRepository extends JpaRepository<DepartmentEntity, Long> {
    
    @Query("SELECT d FROM DepartmentEntity d WHERE " +
           "(:keyword IS NULL OR LOWER(d.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(d.code) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "AND (:active IS NULL OR d.active = :active)")
    Page<DepartmentEntity> findAll(@Param("keyword") String keyword, @Param("active") Boolean active, Pageable pageable);

    @Query("SELECT d FROM DepartmentEntity d WHERE (:active IS NULL OR d.active = :active)")
    Page<DepartmentEntity> findAllByActive(@Param("active") Boolean active, Pageable pageable);

    boolean existsByCode(String code);
    
    boolean existsByCodeAndIdNot(String code, Long id);
}
