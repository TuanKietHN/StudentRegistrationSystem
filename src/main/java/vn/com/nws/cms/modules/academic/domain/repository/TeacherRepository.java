package vn.com.nws.cms.modules.academic.domain.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import vn.com.nws.cms.modules.academic.domain.model.Teacher;

import java.util.Optional;

public interface TeacherRepository {
    Teacher save(Teacher teacher);
    Optional<Teacher> findById(Long id);
    Optional<Teacher> findByUserId(Long userId);
    Optional<Teacher> findByEmployeeCode(String employeeCode);
    void deleteById(Long id);
    boolean existsByEmployeeCode(String employeeCode);
    boolean existsByEmployeeCodeAndIdNot(String employeeCode, Long id);
    Page<Teacher> findAll(String keyword, Long departmentId, Boolean active, Pageable pageable);
}
