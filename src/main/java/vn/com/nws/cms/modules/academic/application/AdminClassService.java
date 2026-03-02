package vn.com.nws.cms.modules.academic.application;

import vn.com.nws.cms.common.dto.PageResponse;
import vn.com.nws.cms.modules.academic.api.dto.AdminClassCreateRequest;
import vn.com.nws.cms.modules.academic.api.dto.AdminClassFilterRequest;
import vn.com.nws.cms.modules.academic.api.dto.AdminClassResponse;
import vn.com.nws.cms.modules.academic.api.dto.AdminClassUpdateRequest;
import vn.com.nws.cms.modules.academic.api.dto.StudentResponse;

import java.util.List;

public interface AdminClassService {
    PageResponse<AdminClassResponse> getAdminClasses(AdminClassFilterRequest request);
    AdminClassResponse getAdminClassById(Long id);
    AdminClassResponse createAdminClass(AdminClassCreateRequest request);
    AdminClassResponse updateAdminClass(Long id, AdminClassUpdateRequest request);
    void deleteAdminClass(Long id);
    List<StudentResponse> getAdminClassStudents(Long id);
}

