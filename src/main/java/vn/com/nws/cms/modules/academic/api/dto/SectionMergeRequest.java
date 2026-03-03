package vn.com.nws.cms.modules.academic.api.dto;

import lombok.Data;

import java.util.List;

@Data
public class SectionMergeRequest {
    private Long targetSectionId;
    private List<Long> sourceSectionIds;
    private String reason;
}

