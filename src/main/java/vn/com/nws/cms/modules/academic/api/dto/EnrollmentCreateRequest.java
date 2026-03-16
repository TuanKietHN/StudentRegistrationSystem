package vn.com.nws.cms.modules.academic.api.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class EnrollmentCreateRequest {
    @NotNull(message = "Section ID is required")
    private Long sectionId;
    
    @NotNull(message = "Student ID is required")
    private Long studentId;
}
