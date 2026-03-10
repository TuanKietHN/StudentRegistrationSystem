package vn.com.nws.cms.modules.academic.infrastructure.persistence.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import vn.com.nws.cms.modules.academic.domain.model.ProgramSubject;
import vn.com.nws.cms.modules.academic.domain.repository.ProgramSubjectRepository;
import vn.com.nws.cms.modules.academic.infrastructure.persistence.entity.ProgramSubjectEntity;
import vn.com.nws.cms.modules.academic.infrastructure.persistence.mapper.ProgramSubjectMapper;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class ProgramSubjectRepositoryImpl implements ProgramSubjectRepository {
    private final JpaProgramSubjectRepository jpaRepository;
    private final ProgramSubjectMapper mapper;

    @Override
    public ProgramSubject save(ProgramSubject programSubject) {
        ProgramSubjectEntity entity = mapper.toEntity(programSubject);
        return mapper.toDomain(jpaRepository.save(entity));
    }

    @Override
    public Optional<ProgramSubject> findById(Long id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<ProgramSubject> findByProgramId(Long programId) {
        return jpaRepository.findByAcademicProgramId(programId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public void deleteByProgramId(Long programId) {
        jpaRepository.deleteByAcademicProgramId(programId);
    }
}
