package vn.com.nws.cms.modules.academic.domain.repository;

import vn.com.nws.cms.modules.academic.domain.model.ProgramSubject;

import java.util.List;
import java.util.Optional;

public interface ProgramSubjectRepository {
    ProgramSubject save(ProgramSubject programSubject);
    Optional<ProgramSubject> findById(Long id);
    List<ProgramSubject> findByProgramId(Long programId);
    boolean existsByProgramIdAndSubjectId(Long programId, Long subjectId);
    void upsert(Long programId, Long subjectId, Integer semester, String subjectType, Double passScore);
    void deleteById(Long id);
    void deleteByProgramId(Long programId);
}
