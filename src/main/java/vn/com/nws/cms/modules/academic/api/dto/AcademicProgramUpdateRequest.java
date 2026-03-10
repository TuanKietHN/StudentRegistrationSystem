package vn.com.nws.cms.modules.academic.api.dto;

import jakarta.validation.constraints.Min;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AcademicProgramUpdateRequest {
    private String name;
    private Long departmentId;

    @Min(value = 1, message = "Total credits must be greater than 0")
    private Integer totalCredits;
    
    private String description;
    private Boolean active;
}
