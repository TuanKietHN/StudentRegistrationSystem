package vn.com.nws.cms.modules.academic.infrastructure.persistence.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import vn.com.nws.cms.modules.academic.domain.model.StudentClass;
import vn.com.nws.cms.modules.academic.domain.repository.StudentClassRepository;
import vn.com.nws.cms.modules.academic.infrastructure.persistence.entity.CohortEntity;
import vn.com.nws.cms.modules.academic.infrastructure.persistence.entity.DepartmentEntity;
import vn.com.nws.cms.modules.academic.infrastructure.persistence.entity.StudentClassEntity;
import vn.com.nws.cms.modules.academic.infrastructure.persistence.mapper.StudentClassMapper;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class StudentClassRepositoryImpl implements StudentClassRepository {

    private final JpaStudentClassRepository jpaRepository;
    private final DepartmentJpaRepository departmentJpaRepository;
    private final JpaCohortRepository cohortJpaRepository;
    private final StudentClassMapper mapper;

    @Override
    public StudentClass save(StudentClass studentClass) {
        StudentClassEntity entity;
        if (studentClass.getId() != null) {
            StudentClassEntity existing = jpaRepository.findById(studentClass.getId()).orElse(null);
            StudentClassEntity mapped = mapper.toEntity(studentClass);
            if (existing != null) {
                existing.setCode(mapped.getCode());
                existing.setName(mapped.getName());
                existing.setIntakeYear(mapped.getIntakeYear());
                existing.setProgram(mapped.getProgram());
                existing.setActive(mapped.isActive());
                entity = existing;
            } else {
                entity = mapped;
                entity.setId(studentClass.getId());
            }
        } else {
            entity = mapper.toEntity(studentClass);
        }

        if (studentClass.getDepartment() != null && studentClass.getDepartment().getId() != null) {
            DepartmentEntity dept = departmentJpaRepository.findById(studentClass.getDepartment().getId()).orElse(null);
            entity.setDepartment(dept);
        } else {
            entity.setDepartment(null);
        }

        if (studentClass.getCohort() != null && studentClass.getCohort().getId() != null) {
            CohortEntity cohort = cohortJpaRepository.findById(studentClass.getCohort().getId()).orElse(null);
            entity.setCohort(cohort);
        } else {
            entity.setCohort(null);
        }

        return mapper.toDomain(jpaRepository.save(entity));
    }

    @Override
    public Optional<StudentClass> findById(Long id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<StudentClass> findByCode(String code) {
        return jpaRepository.findByCode(code).map(mapper::toDomain);
    }

    @Override
    public boolean existsByCode(String code) {
        return jpaRepository.existsByCode(code);
    }

    @Override
    public boolean existsByCodeAndIdNot(String code, Long id) {
        return jpaRepository.existsByCodeAndIdNot(code, id);
    }

    @Override
    public Page<StudentClass> search(String keyword, Long departmentId, Long cohortId, Boolean active, Pageable pageable) {
        List<StudentClassEntity> base = jpaRepository.searchNoKeywordList(departmentId, cohortId, active);
        String normalizedKeyword = keyword == null || keyword.isBlank() ? null : keyword.trim().toLowerCase(Locale.ROOT);
        List<StudentClassEntity> filtered = normalizedKeyword == null
                ? base
                : base.stream()
                .filter(c -> (c.getCode() != null && c.getCode().toLowerCase(Locale.ROOT).contains(normalizedKeyword))
                        || (c.getName() != null && c.getName().toLowerCase(Locale.ROOT).contains(normalizedKeyword)))
                .toList();

        int start = (int) pageable.getOffset();
        if (start >= filtered.size()) {
            return new PageImpl<StudentClassEntity>(List.of(), pageable, filtered.size()).map(mapper::toDomain);
        }
        int end = Math.min(start + pageable.getPageSize(), filtered.size());
        return new PageImpl<>(filtered.subList(start, end), pageable, filtered.size()).map(mapper::toDomain);
    }

    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }
}

