package vn.com.nws.cms.modules.academic.application;

import vn.com.nws.cms.common.dto.PageResponse;
import vn.com.nws.cms.modules.academic.api.dto.StudentClassCreateRequest;
import vn.com.nws.cms.modules.academic.api.dto.StudentClassFilterRequest;
import vn.com.nws.cms.modules.academic.api.dto.StudentClassResponse;
import vn.com.nws.cms.modules.academic.api.dto.StudentClassUpdateRequest;
import vn.com.nws.cms.modules.academic.api.dto.StudentResponse;

import java.util.List;

public interface StudentClassService {
    PageResponse<StudentClassResponse> getStudentClasses(StudentClassFilterRequest request);
    StudentClassResponse getStudentClassById(Long id);
    StudentClassResponse createStudentClass(StudentClassCreateRequest request);
    StudentClassResponse updateStudentClass(Long id, StudentClassUpdateRequest request);
    void deleteStudentClass(Long id);
    List<StudentResponse> getStudentClassStudents(Long id);
}

