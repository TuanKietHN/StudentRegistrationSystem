package vn.com.nws.cms.modules.academic.api.dto;

import lombok.Data;

@Data
public class StudentClassFilterRequest {
    private String keyword;
    private Long departmentId;
    private Long cohortId;
    private Boolean active;
    private int page = 1;
    private int size = 10;
}

