package vn.com.nws.cms.modules.academic.application;

import org.springframework.web.multipart.MultipartFile;
import vn.com.nws.cms.modules.academic.api.dto.GradesImportResultResponse;
import vn.com.nws.cms.modules.academic.api.dto.SectionGradeResponse;
import vn.com.nws.cms.modules.academic.api.dto.SectionGradeUpdateRequest;

import java.util.List;

public interface SectionGradeService {
    List<SectionGradeResponse> getSectionGrades(Long sectionId, String username, boolean isAdmin, boolean isTeacher);
    SectionGradeResponse updateGrade(Long enrollmentId, String username, boolean isAdmin, boolean isTeacher, SectionGradeUpdateRequest request);
    GradesImportResultResponse importSectionGrades(Long sectionId, String username, boolean isAdmin, boolean isTeacher, MultipartFile file);
}
