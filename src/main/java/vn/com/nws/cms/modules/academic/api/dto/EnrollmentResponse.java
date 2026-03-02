package vn.com.nws.cms.modules.academic.api.dto;

import lombok.Builder;
import lombok.Data;
import vn.com.nws.cms.modules.academic.domain.enums.EnrollmentStatus;
import vn.com.nws.cms.modules.iam.api.dto.UserResponse;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class EnrollmentResponse {
    private Long id;
    private CourseResponse course;
    private UserResponse student;
    private String studentCode;
    private EnrollmentStatus status;
    private Double grade;
    private BigDecimal processScore;
    private BigDecimal examScore;
    private BigDecimal finalScore;
    private boolean scoreLocked;
    private boolean scoreOverridden;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
