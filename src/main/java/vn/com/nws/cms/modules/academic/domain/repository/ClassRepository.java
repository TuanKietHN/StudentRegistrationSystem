package vn.com.nws.cms.modules.academic.domain.repository;

import vn.com.nws.cms.modules.academic.domain.model.CourseClass;

import java.util.List;
import java.util.Optional;

public interface ClassRepository {
    CourseClass save(CourseClass clazz);
    Optional<CourseClass> findById(Long id);
    Optional<CourseClass> findByCode(String code);
    void deleteById(Long id);
    boolean existsByCode(String code);

    List<CourseClass> search(String keyword, Boolean active, int page, int size);
    long count(String keyword, Boolean active);
}
