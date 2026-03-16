package vn.com.nws.cms.modules.academic.application;

import vn.com.nws.cms.common.dto.PageResponse;
import vn.com.nws.cms.modules.academic.api.dto.*;

import java.util.List;

public interface StudentClassService {
    PageResponse<StudentClassResponse> getStudentClasses(StudentClassFilterRequest request);
    StudentClassResponse getStudentClassById(Long id);
    StudentClassResponse createStudentClass(StudentClassCreateRequest request);
    StudentClassResponse updateStudentClass(Long id, StudentClassUpdateRequest request);
    void deleteStudentClass(Long id);
    List<StudentResponse> getStudentClassStudents(Long id);
    List<StudentGradeSummaryResponse> getStudentClassGrades(Long id);
}

