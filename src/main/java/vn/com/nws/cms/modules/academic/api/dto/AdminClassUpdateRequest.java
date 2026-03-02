package vn.com.nws.cms.modules.academic.api.dto;

import lombok.Data;

@Data
public class AdminClassUpdateRequest {
    private String code;
    private String name;
    private Long departmentId;
    private Integer intakeYear;
    private String program;
    private Boolean active;
}

