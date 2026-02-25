package vn.com.nws.cms.modules.academic.api.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class EnrollmentSelfRequest {
    @NotNull(message = "Course ID is required")
    private Long courseId;
}

