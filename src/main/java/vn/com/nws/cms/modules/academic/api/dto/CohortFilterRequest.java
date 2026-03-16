package vn.com.nws.cms.modules.academic.api.dto;

import lombok.Data;
import vn.com.nws.cms.modules.academic.domain.enums.CohortLifecycleStatus;

@Data
public class CohortFilterRequest {
    private String keyword;
    private Long semesterId;
    private Long classId;
    private Long teacherId;
    private Boolean active;
    private CohortLifecycleStatus status;
    private int page = 1;
    private int size = 10;
}

