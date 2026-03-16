package vn.com.nws.cms.modules.academic.application;

import vn.com.nws.cms.common.dto.PageResponse;
import vn.com.nws.cms.modules.academic.api.dto.*;

import java.util.List;

public interface SectionService {
    PageResponse<SectionResponse> getSections(SectionFilterRequest request);
    SectionResponse getSectionById(Long id);
    SectionResponse createSection(SectionCreateRequest request);
    SectionResponse updateSection(Long id, SectionUpdateRequest request);
    SectionResponse cancelSection(Long id, SectionCancelRequest request);
    SectionResponse mergeSections(SectionMergeRequest request);
    void deleteSection(Long id);

    List<SectionTimeSlotResponse> getSectionTimeSlots(Long sectionId);
    void replaceSectionTimeSlots(Long sectionId, List<SectionTimeSlotRequest> slots);
}

