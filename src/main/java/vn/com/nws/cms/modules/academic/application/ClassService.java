package vn.com.nws.cms.modules.academic.application;

import vn.com.nws.cms.common.dto.PageResponse;
import vn.com.nws.cms.modules.academic.api.dto.*;

public interface ClassService {
    PageResponse<ClassResponse> getClasses(ClassFilterRequest request);
    ClassResponse getClassById(Long id);
    ClassResponse createClass(ClassCreateRequest request);
    ClassResponse updateClass(Long id, ClassUpdateRequest request);
    void deleteClass(Long id);
}

