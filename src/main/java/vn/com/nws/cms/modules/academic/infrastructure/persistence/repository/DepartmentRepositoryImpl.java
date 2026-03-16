package vn.com.nws.cms.modules.academic.infrastructure.persistence.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import vn.com.nws.cms.modules.academic.domain.model.Department;
import vn.com.nws.cms.modules.academic.domain.repository.DepartmentRepository;
import vn.com.nws.cms.modules.academic.infrastructure.persistence.entity.DepartmentEntity;
import vn.com.nws.cms.modules.academic.infrastructure.persistence.mapper.DepartmentMapper;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class DepartmentRepositoryImpl implements DepartmentRepository {

    private final DepartmentJpaRepository departmentJpaRepository;
    private final DepartmentMapper departmentMapper;

    @Override
    public Department save(Department department) {
        DepartmentEntity entity = departmentMapper.toEntity(department);
        if (department.getId() != null) {
            entity.setId(department.getId());
        }
        // Handle parent and headTeacher manually if needed, or rely on Mapper if fully configured
        // For now, basic save
        DepartmentEntity savedEntity = departmentJpaRepository.save(entity);
        return departmentMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Department> findById(Long id) {
        return departmentJpaRepository.findById(id).map(departmentMapper::toDomain);
    }

    @Override
    public List<Department> findAll() {
        return departmentJpaRepository.findAll().stream()
                .map(departmentMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Page<Department> findAll(String keyword, Boolean active, Pageable pageable) {
        String normalizedKeyword = keyword == null || keyword.isBlank() ? null : keyword;

        if (normalizedKeyword == null && active == null) {
            return departmentJpaRepository.findAll(pageable).map(departmentMapper::toDomain);
        }
        if (normalizedKeyword == null) {
            return departmentJpaRepository.findAllByActive(active, pageable).map(departmentMapper::toDomain);
        }
        return departmentJpaRepository.findAll(normalizedKeyword, active, pageable).map(departmentMapper::toDomain);
    }

    @Override
    public void delete(Long id) {
        departmentJpaRepository.deleteById(id);
    }

    @Override
    public boolean existsByCode(String code) {
        return departmentJpaRepository.existsByCode(code);
    }

    @Override
    public boolean existsByCodeAndIdNot(String code, Long id) {
        return departmentJpaRepository.existsByCodeAndIdNot(code, id);
    }
}
