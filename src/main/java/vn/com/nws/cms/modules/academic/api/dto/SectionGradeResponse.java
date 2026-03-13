package vn.com.nws.cms.modules.academic.api.dto;

import lombok.Builder;
import lombok.Data;
import vn.com.nws.cms.modules.academic.domain.enums.EnrollmentStatus;
import vn.com.nws.cms.modules.iam.api.dto.UserResponse;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class SectionGradeResponse {
    private Long enrollmentId; // or rename from id
    private Long id; // Keep it as id to match frontend e.id
    private SectionResponse section;
    private UserResponse student;
    private String studentCode;
    private String studentPhone;
    private Boolean studentActive;
    private String studentDepartmentCode;
    private String studentDepartmentName;
    private String studentClassCode;
    private String studentClassName;
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
