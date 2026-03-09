package vn.com.nws.cms.modules.academic.domain.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import vn.com.nws.cms.modules.academic.domain.model.StudentClass;

import java.util.Optional;

public interface StudentClassRepository {
    StudentClass save(StudentClass studentClass);
    Optional<StudentClass> findById(Long id);
    Optional<StudentClass> findByCode(String code);
    boolean existsByCode(String code);
    boolean existsByCodeAndIdNot(String code, Long id);
    Page<StudentClass> search(String keyword, Long departmentId, Long cohortId, Long advisorTeacherId, Boolean active, Pageable pageable);
    void deleteById(Long id);
}

