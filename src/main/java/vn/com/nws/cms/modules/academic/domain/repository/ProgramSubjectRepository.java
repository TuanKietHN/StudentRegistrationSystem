package vn.com.nws.cms.modules.academic.domain.repository;

import vn.com.nws.cms.modules.academic.domain.model.ProgramSubject;

import java.util.List;
import java.util.Optional;

public interface ProgramSubjectRepository {
    ProgramSubject save(ProgramSubject programSubject);
    Optional<ProgramSubject> findById(Long id);
    List<ProgramSubject> findByProgramId(Long programId);
    void deleteById(Long id);
    void deleteByProgramId(Long programId);
}
