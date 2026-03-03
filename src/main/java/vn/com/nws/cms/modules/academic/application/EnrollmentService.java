package vn.com.nws.cms.modules.academic.application;

import vn.com.nws.cms.modules.academic.api.dto.*;

import java.util.List;

public interface EnrollmentService {
    EnrollmentResponse enrollStudent(EnrollmentCreateRequest request);
    EnrollmentResponse enrollSelf(String username, EnrollmentSelfRequest request);
    EnrollmentResponse updateEnrollment(Long id, String username, boolean isAdmin, boolean isTeacher, EnrollmentUpdateRequest request);
    void cancelEnrollment(Long id, String username, boolean isAdmin);
    
    List<EnrollmentResponse> getStudentEnrollments(Long studentId);
    List<EnrollmentResponse> getMyEnrollments(String username);
    List<EnrollmentResponse> getSectionEnrollments(Long sectionId, String username, boolean isAdmin, boolean isTeacher);

    GradesImportResultResponse importSectionGrades(Long sectionId, String username, boolean isAdmin, boolean isTeacher, org.springframework.web.multipart.MultipartFile file);
}
