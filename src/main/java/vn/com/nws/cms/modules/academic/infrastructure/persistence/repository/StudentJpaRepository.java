package vn.com.nws.cms.modules.academic.infrastructure.persistence.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.com.nws.cms.modules.academic.infrastructure.persistence.entity.StudentEntity;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentJpaRepository extends JpaRepository<StudentEntity, Long> {

    Optional<StudentEntity> findByUserId(Long userId);

    boolean existsByStudentCode(String studentCode);

    boolean existsByStudentCodeAndIdNot(String studentCode, Long id);

    @Query("""
            SELECT s
            FROM StudentEntity s
            LEFT JOIN s.user u
            LEFT JOIN s.department d
            WHERE (:active IS NULL OR s.active = :active)
              AND (:departmentId IS NULL OR d.id = :departmentId)
              AND (
                    :keyword IS NULL
                    OR LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%'))
                    OR LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%'))
                    OR LOWER(s.studentCode) LIKE LOWER(CONCAT('%', :keyword, '%'))
                  )
            """)
    Page<StudentEntity> findAll(
            @Param("keyword") String keyword,
            @Param("departmentId") Long departmentId,
            @Param("active") Boolean active,
            Pageable pageable
    );

    @Query("""
            SELECT s
            FROM StudentEntity s
            LEFT JOIN FETCH s.user u
            WHERE s.studentClass.id = :studentClassId
            ORDER BY s.studentCode ASC, u.username ASC
            """)
    List<StudentEntity> findByStudentClassId(@Param("studentClassId") Long studentClassId);
}
