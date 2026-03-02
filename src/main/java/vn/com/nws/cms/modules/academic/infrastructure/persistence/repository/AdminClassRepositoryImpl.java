package vn.com.nws.cms.modules.academic.infrastructure.persistence.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import vn.com.nws.cms.modules.academic.domain.model.AdminClass;
import vn.com.nws.cms.modules.academic.domain.repository.AdminClassRepository;
import vn.com.nws.cms.modules.academic.infrastructure.persistence.entity.AdminClassEntity;
import vn.com.nws.cms.modules.academic.infrastructure.persistence.entity.DepartmentEntity;
import vn.com.nws.cms.modules.academic.infrastructure.persistence.mapper.AdminClassMapper;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class AdminClassRepositoryImpl implements AdminClassRepository {

    private final JpaAdminClassRepository jpaRepository;
    private final DepartmentJpaRepository departmentJpaRepository;
    private final AdminClassMapper adminClassMapper;

    @Override
    public AdminClass save(AdminClass adminClass) {
        AdminClassEntity entity;
        if (adminClass.getId() != null) {
            AdminClassEntity existing = jpaRepository.findById(adminClass.getId()).orElse(null);
            AdminClassEntity mapped = adminClassMapper.toEntity(adminClass);
            if (existing != null) {
                existing.setCode(mapped.getCode());
                existing.setName(mapped.getName());
                existing.setIntakeYear(mapped.getIntakeYear());
                existing.setProgram(mapped.getProgram());
                existing.setActive(mapped.isActive());
                entity = existing;
            } else {
                entity = mapped;
                entity.setId(adminClass.getId());
            }
        } else {
            entity = adminClassMapper.toEntity(adminClass);
        }

        if (adminClass.getDepartment() != null && adminClass.getDepartment().getId() != null) {
            DepartmentEntity dept = departmentJpaRepository.findById(adminClass.getDepartment().getId()).orElse(null);
            entity.setDepartment(dept);
        } else {
            entity.setDepartment(null);
        }

        AdminClassEntity saved = jpaRepository.save(entity);
        return adminClassMapper.toDomain(saved);
    }

    @Override
    public Optional<AdminClass> findById(Long id) {
        return jpaRepository.findById(id).map(adminClassMapper::toDomain);
    }

    @Override
    public Optional<AdminClass> findByCode(String code) {
        return jpaRepository.findByCode(code).map(adminClassMapper::toDomain);
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
    public Page<AdminClass> search(String keyword, Long departmentId, Integer intakeYear, Boolean active, Pageable pageable) {
        String normalizedKeyword = keyword == null || keyword.isBlank() ? null : keyword;
        return jpaRepository.search(normalizedKeyword, departmentId, intakeYear, active, pageable).map(adminClassMapper::toDomain);
    }

    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }
}

