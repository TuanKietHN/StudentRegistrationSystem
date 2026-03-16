package vn.com.nws.cms.modules.academic.infrastructure.persistence.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.com.nws.cms.modules.academic.infrastructure.persistence.entity.TeacherEntity;

import java.util.Optional;

@Repository
public interface TeacherJpaRepository extends JpaRepository<TeacherEntity, Long> {

    @Query("SELECT t FROM TeacherEntity t WHERE " +
           "(:keyword IS NULL OR LOWER(t.user.username) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(t.employeeCode) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "AND (:departmentId IS NULL OR t.department.id = :departmentId) " +
           "AND (:active IS NULL OR t.active = :active)")
    Page<TeacherEntity> findAll(@Param("keyword") String keyword, 
                                @Param("departmentId") Long departmentId, 
                                @Param("active") Boolean active, 
                                Pageable pageable);

    Optional<TeacherEntity> findByUserId(Long userId);
    
    Optional<TeacherEntity> findByEmployeeCode(String employeeCode);

    boolean existsByEmployeeCode(String employeeCode);

    boolean existsByEmployeeCodeAndIdNot(String employeeCode, Long id);
}
