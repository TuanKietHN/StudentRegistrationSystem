package vn.com.nws.cms.modules.academic.api.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class StudentClassResponse {
    private Long id;
    private String code;
    private String name;
    private Long departmentId;
    private String departmentName;
    private Long cohortId;
    private String cohortCode;
    private String cohortName;
    private Integer intakeYear;
    private String program;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

