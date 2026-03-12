package vn.com.nws.cms.modules.academic.infrastructure.persistence.repository;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import vn.com.nws.cms.modules.academic.domain.model.ProgramSubject;
import vn.com.nws.cms.modules.academic.domain.repository.ProgramSubjectRepository;
import vn.com.nws.cms.modules.academic.infrastructure.persistence.entity.AcademicProgramEntity;
import vn.com.nws.cms.modules.academic.infrastructure.persistence.entity.ProgramSubjectEntity;
import vn.com.nws.cms.modules.academic.infrastructure.persistence.entity.SubjectEntity;
import vn.com.nws.cms.modules.academic.infrastructure.persistence.mapper.ProgramSubjectMapper;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class ProgramSubjectRepositoryImpl implements ProgramSubjectRepository {
    private final JpaProgramSubjectRepository jpaRepository;
    private final ProgramSubjectMapper mapper;
    private final EntityManager entityManager;

    @Override
    @Transactional
    public ProgramSubject save(ProgramSubject programSubject) {
        ProgramSubjectEntity entity = mapper.toEntity(programSubject);
        
        if (programSubject.getProgramId() != null) {
            entity.setAcademicProgram(entityManager.getReference(AcademicProgramEntity.class, programSubject.getProgramId()));
        }
        
        if (programSubject.getSubject() != null && programSubject.getSubject().getId() != null) {
            entity.setSubject(entityManager.getReference(SubjectEntity.class, programSubject.getSubject().getId()));
        }
        
        return mapper.toDomain(jpaRepository.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ProgramSubject> findById(Long id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProgramSubject> findByProgramId(Long programId) {
        return jpaRepository.findByAcademicProgramId(programId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void deleteByProgramId(Long programId) {
        jpaRepository.deleteByAcademicProgramId(programId);
    }
}
