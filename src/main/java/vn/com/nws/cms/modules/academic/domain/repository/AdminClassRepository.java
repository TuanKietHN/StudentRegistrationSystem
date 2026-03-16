package vn.com.nws.cms.modules.academic.domain.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import vn.com.nws.cms.modules.academic.domain.model.AdminClass;

import java.util.Optional;

public interface AdminClassRepository {
    AdminClass save(AdminClass adminClass);
    Optional<AdminClass> findById(Long id);
    Optional<AdminClass> findByCode(String code);
    boolean existsByCode(String code);
    boolean existsByCodeAndIdNot(String code, Long id);
    Page<AdminClass> search(String keyword, Long departmentId, Integer intakeYear, Boolean active, Pageable pageable);
    void deleteById(Long id);
}

