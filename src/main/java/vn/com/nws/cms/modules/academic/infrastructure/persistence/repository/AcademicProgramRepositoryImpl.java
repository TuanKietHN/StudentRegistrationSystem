package vn.com.nws.cms.modules.academic.infrastructure.persistence.repository;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import vn.com.nws.cms.modules.academic.domain.model.AcademicProgram;
import vn.com.nws.cms.modules.academic.domain.repository.AcademicProgramRepository;
import vn.com.nws.cms.modules.academic.infrastructure.persistence.entity.AcademicProgramEntity;
import vn.com.nws.cms.modules.academic.infrastructure.persistence.entity.DepartmentEntity;
import vn.com.nws.cms.modules.academic.infrastructure.persistence.mapper.AcademicProgramMapper;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class AcademicProgramRepositoryImpl implements AcademicProgramRepository {
    private final JpaAcademicProgramRepository jpaRepository;
    private final AcademicProgramMapper mapper;
    private final EntityManager entityManager;

    @Override
    @Transactional
    public AcademicProgram save(AcademicProgram program) {
        AcademicProgramEntity entity = mapper.toEntity(program);
        
        // Handle Department reference
        if (program.getDepartment() != null && program.getDepartment().getId() != null) {
            entity.setDepartment(entityManager.getReference(DepartmentEntity.class, program.getDepartment().getId()));
        }
        
        return mapper.toDomain(jpaRepository.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<AcademicProgram> findById(Long id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<AcademicProgram> findByCode(String code) {
        return jpaRepository.findByCode(code).map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AcademicProgram> findAll() {
        return jpaRepository.findAll().stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public boolean existsByCode(String code) {
        return jpaRepository.existsByCode(code);
    }
}
