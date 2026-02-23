package vn.com.nws.cms.modules.academic.application;

import vn.com.nws.cms.common.dto.PageResponse;
import vn.com.nws.cms.modules.academic.api.dto.*;

public interface DepartmentService {
    PageResponse<DepartmentResponse> getDepartments(DepartmentFilterRequest request);
    DepartmentResponse getDepartmentById(Long id);
    DepartmentResponse createDepartment(DepartmentCreateRequest request);
    DepartmentResponse updateDepartment(Long id, DepartmentUpdateRequest request);
    void deleteDepartment(Long id);
}
