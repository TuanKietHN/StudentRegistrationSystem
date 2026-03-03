package vn.com.nws.cms.modules.academic.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CohortCreateRequest {
    @NotBlank(message = "Code is required")
    private String code;

    @NotBlank(message = "Name is required")
    private String name;

    @NotNull(message = "Start year is required")
    private Integer startYear;

    @NotNull(message = "End year is required")
    private Integer endYear;

    private boolean active = true;
}

