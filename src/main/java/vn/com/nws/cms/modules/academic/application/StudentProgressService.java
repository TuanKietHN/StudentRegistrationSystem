package vn.com.nws.cms.modules.academic.application;

import vn.com.nws.cms.modules.academic.api.dto.StudentProgressResponse;

public interface StudentProgressService {
    StudentProgressResponse getStudentProgress(Long studentId);
}
