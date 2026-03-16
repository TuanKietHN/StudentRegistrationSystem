package vn.com.nws.cms.modules.academic.application;

import vn.com.nws.cms.common.dto.PageResponse;
import vn.com.nws.cms.modules.academic.api.dto.*;

public interface TeacherService {
    PageResponse<TeacherResponse> getTeachers(TeacherFilterRequest request);
    TeacherResponse getTeacherById(Long id);
    TeacherResponse getTeacherByUserId(Long userId);
    TeacherResponse createTeacher(TeacherCreateRequest request);
    TeacherResponse updateTeacher(Long id, TeacherUpdateRequest request);
    void deleteTeacher(Long id);
}
