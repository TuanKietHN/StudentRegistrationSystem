package vn.com.nws.cms.modules.academic.api.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import vn.com.nws.cms.modules.academic.domain.enums.CohortLifecycleStatus;

import java.time.LocalDate;

@Data
public class CohortCreateRequest {
    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Code is required")
    private String code;

    @NotNull(message = "Max students is required")
    @Min(value = 1, message = "Max students must be at least 1")
    private Integer maxStudents;

    @Min(value = 0, message = "Min students must be at least 0")
    private Integer minStudents;

    @NotNull(message = "Class ID is required")
    private Long classId;

    @NotNull(message = "Semester ID is required")
    private Long semesterId;

    private Long teacherId;

    private boolean active;

    private CohortLifecycleStatus status;

    private LocalDate enrollmentStartDate;
    private LocalDate enrollmentEndDate;

    private boolean registrationEnabled = true;
}

