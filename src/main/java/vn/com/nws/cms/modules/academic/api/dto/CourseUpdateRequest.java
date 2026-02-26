package vn.com.nws.cms.modules.academic.api.dto;

import jakarta.validation.constraints.Min;
import lombok.Data;
import vn.com.nws.cms.modules.academic.domain.enums.CourseLifecycleStatus;

import java.time.LocalDate;

@Data
public class CourseUpdateRequest {
    private String name;
    private String code;
    
    @Min(value = 1, message = "Max students must be at least 1")
    private Integer maxStudents;

    @Min(value = 0, message = "Min students must be at least 0")
    private Integer minStudents;
    
    private Long subjectId;
    private Long semesterId;
    private Long teacherId;
    private Boolean active;

    private CourseLifecycleStatus status;
    private String canceledReason;

    private LocalDate enrollmentStartDate;
    private LocalDate enrollmentEndDate;
}
