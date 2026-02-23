package vn.com.nws.cms.modules.academic.domain.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import vn.com.nws.cms.modules.academic.domain.model.Department;

import java.util.List;
import java.util.Optional;

public interface DepartmentRepository {
    Department save(Department department);
    Optional<Department> findById(Long id);
    List<Department> findAll();
    Page<Department> findAll(String keyword, Boolean active, Pageable pageable);
    void delete(Long id);
    boolean existsByCode(String code);
    boolean existsByCodeAndIdNot(String code, Long id);
}
