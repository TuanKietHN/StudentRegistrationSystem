package vn.com.nws.cms.modules.academic.infrastructure.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.com.nws.cms.modules.academic.infrastructure.persistence.entity.AcademicProgramEntity;

import java.util.Optional;

@Repository
public interface JpaAcademicProgramRepository extends JpaRepository<AcademicProgramEntity, Long> {
    Optional<AcademicProgramEntity> findByCode(String code);
    boolean existsByCode(String code);
}
