package vn.com.nws.cms.modules.academic.api.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import lombok.Data;

@Data
public class SubjectUpdateRequest {
    private String name;
    private String code;
    
    @Min(value = 0, message = "Credit must be greater than or equal to 0")
    private Integer credit;
    
    private String description;
    private Boolean active;

    @Min(value = 0, message = "Process weight must be between 0 and 100")
    @Max(value = 100, message = "Process weight must be between 0 and 100")
    private Short processWeight;

    @Min(value = 0, message = "Exam weight must be between 0 and 100")
    @Max(value = 100, message = "Exam weight must be between 0 and 100")
    private Short examWeight;
}
