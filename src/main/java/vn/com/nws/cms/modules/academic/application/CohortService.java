package vn.com.nws.cms.modules.academic.application;

import vn.com.nws.cms.common.dto.PageResponse;
import vn.com.nws.cms.modules.academic.api.dto.*;

public interface CohortService {
    PageResponse<CohortResponse> getCohorts(CohortFilterRequest request);
    CohortResponse getCohortById(Long id);
    CohortResponse createCohort(CohortCreateRequest request);
    CohortResponse updateCohort(Long id, CohortUpdateRequest request);
    void deleteCohort(Long id);
}
