package vn.com.nws.cms.modules.academic.api.dto;

import lombok.Data;
import vn.com.nws.cms.modules.academic.domain.enums.SectionLifecycleStatus;

@Data
public class SectionFilterRequest {
    private String keyword;
    private Long semesterId;
    private Long subjectId;
    private Long teacherId;
    private Boolean active;
    private SectionLifecycleStatus status;
    private int page = 1;
    private int size = 10;
}

