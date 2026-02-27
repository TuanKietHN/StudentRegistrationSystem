package vn.com.nws.cms.modules.academic.api.dto;

import lombok.Builder;
import lombok.Data;
import vn.com.nws.cms.modules.academic.domain.enums.EnrollmentStatus;
import vn.com.nws.cms.modules.iam.api.dto.UserResponse;

import java.time.LocalDateTime;

@Data
@Builder
public class EnrollmentResponse {
    private Long id;
    private CourseResponse course;
    private UserResponse student;
    private EnrollmentStatus status;
    private Double grade;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
