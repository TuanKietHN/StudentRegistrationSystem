package vn.com.nws.cms.modules.academic.infrastructure.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.com.nws.cms.modules.academic.infrastructure.persistence.entity.ProgramSubjectEntity;

import java.util.List;

@Repository
public interface JpaProgramSubjectRepository extends JpaRepository<ProgramSubjectEntity, Long> {
    List<ProgramSubjectEntity> findByAcademicProgramId(Long programId);
    void deleteByAcademicProgramId(Long programId);
}
