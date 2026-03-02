package vn.com.nws.cms.modules.academic.application;

import vn.com.nws.cms.common.dto.PageResponse;
import vn.com.nws.cms.modules.academic.api.dto.*;

import java.util.List;

public interface CohortService {
    PageResponse<CohortResponse> getCohorts(CohortFilterRequest request);
    CohortResponse getCohortById(Long id);
    CohortResponse createCohort(CohortCreateRequest request);
    CohortResponse updateCohort(Long id, CohortUpdateRequest request);
    CohortResponse cancelCohort(Long id, CohortCancelRequest request);
    CohortResponse mergeCohorts(CohortMergeRequest request);
    void deleteCohort(Long id);

    List<CohortTimeSlotResponse> getCohortTimeSlots(Long cohortId);
    void replaceCohortTimeSlots(Long cohortId, List<CohortTimeSlotRequest> slots);
}

