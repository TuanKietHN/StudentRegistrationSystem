package vn.com.nws.cms.modules.academic.api.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AdminClassCreateRequest {
    @NotBlank(message = "Code is required")
    private String code;

    @NotBlank(message = "Name is required")
    private String name;

    private Long departmentId;
    private Integer intakeYear;
    private String program;
    private boolean active;
}

