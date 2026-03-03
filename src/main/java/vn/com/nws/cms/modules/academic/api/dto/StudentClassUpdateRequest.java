package vn.com.nws.cms.modules.academic.api.dto;

import lombok.Data;

@Data
public class StudentClassUpdateRequest {
    private String code;
    private String name;
    private Long departmentId;
    private Long cohortId;
    private Integer intakeYear;
    private String program;
    private Boolean active;
}

