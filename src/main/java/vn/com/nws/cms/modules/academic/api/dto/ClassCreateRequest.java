package vn.com.nws.cms.modules.academic.api.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ClassCreateRequest {
    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Code is required")
    private String code;

    @NotNull(message = "Credit is required")
    @Min(value = 0, message = "Credit must be greater than or equal to 0")
    private Integer credit;

    private String description;
    private boolean active;

    @Min(value = 0, message = "Process weight must be between 0 and 100")
    @Max(value = 100, message = "Process weight must be between 0 and 100")
    private Short processWeight;

    @Min(value = 0, message = "Exam weight must be between 0 and 100")
    @Max(value = 100, message = "Exam weight must be between 0 and 100")
    private Short examWeight;
}

