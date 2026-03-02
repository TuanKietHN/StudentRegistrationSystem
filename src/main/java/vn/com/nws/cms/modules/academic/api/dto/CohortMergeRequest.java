package vn.com.nws.cms.modules.academic.api.dto;

import lombok.Data;

import java.util.List;

@Data
public class CohortMergeRequest {
    private Long targetCohortId;
    private List<Long> sourceCohortIds;
    private String reason;
}

