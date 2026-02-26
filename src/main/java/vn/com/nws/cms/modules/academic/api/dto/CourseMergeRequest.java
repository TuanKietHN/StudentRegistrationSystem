package vn.com.nws.cms.modules.academic.api.dto;

import lombok.Data;

import java.util.List;

@Data
public class CourseMergeRequest {
    private Long targetCourseId;
    private List<Long> sourceCourseIds;
    private String reason;
}

