package vn.com.nws.cms.modules.academic.domain.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import vn.com.nws.cms.modules.academic.domain.model.Student;

import java.util.Optional;

public interface StudentRepository {
    Student save(Student student);
    Optional<Student> findById(Long id);
    Optional<Student> findByUserId(Long userId);

    boolean existsByStudentCode(String studentCode);
    boolean existsByStudentCodeAndIdNot(String studentCode, Long id);

    Page<Student> findAll(String keyword, Long departmentId, Boolean active, Pageable pageable);
    void deleteById(Long id);
}

