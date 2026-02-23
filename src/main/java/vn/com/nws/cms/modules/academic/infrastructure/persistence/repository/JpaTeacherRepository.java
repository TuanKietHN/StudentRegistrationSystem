package vn.com.nws.cms.modules.academic.infrastructure.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.com.nws.cms.modules.academic.infrastructure.persistence.entity.TeacherEntity;

import java.util.Optional;

@Repository
public interface JpaTeacherRepository extends JpaRepository<TeacherEntity, Long> {
    Optional<TeacherEntity> findByUserId(Long userId);
    boolean existsByUserId(Long userId);
}

