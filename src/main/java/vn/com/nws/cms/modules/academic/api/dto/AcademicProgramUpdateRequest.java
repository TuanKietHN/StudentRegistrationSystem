package vn.com.nws.cms.modules.academic.api.dto;

import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AcademicProgramUpdateRequest {
    private String name;
    private Long departmentId;

    @Min(value = 1, message = "Total credits must be greater than 0")
    private Integer totalCredits;
    
    private String description;
    private Boolean active;
}
