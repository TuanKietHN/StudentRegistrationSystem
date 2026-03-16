package vn.com.nws.cms.modules.academic.application;

import vn.com.nws.cms.common.dto.PageResponse;
import vn.com.nws.cms.modules.academic.api.dto.StudentCreateRequest;
import vn.com.nws.cms.modules.academic.api.dto.StudentFilterRequest;
import vn.com.nws.cms.modules.academic.api.dto.StudentResponse;
import vn.com.nws.cms.modules.academic.api.dto.StudentUpdateRequest;

public interface StudentService {
    PageResponse<StudentResponse> getStudents(StudentFilterRequest request);
    StudentResponse getStudentById(Long id);
    StudentResponse getStudentByUserId(Long userId);
    StudentResponse createStudent(StudentCreateRequest request);
    StudentResponse updateStudent(Long id, StudentUpdateRequest request);
    void deleteStudent(Long id);
}

