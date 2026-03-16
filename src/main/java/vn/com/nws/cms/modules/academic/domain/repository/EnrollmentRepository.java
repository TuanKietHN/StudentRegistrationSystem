package vn.com.nws.cms.modules.academic.domain.repository;

import vn.com.nws.cms.modules.academic.domain.model.Enrollment;

import java.util.List;
import java.util.Optional;

public interface EnrollmentRepository {
    Enrollment save(Enrollment enrollment);
    Optional<Enrollment> findById(Long id);
    void deleteById(Long id);
    
    boolean existsBySectionIdAndStudentId(Long sectionId, Long studentId);
    
    List<Enrollment> findBySectionId(Long sectionId);
    List<Enrollment> findByStudentId(Long studentId);
    
    long countBySectionId(Long sectionId);
}
