package vn.com.nws.cms.modules.academic.api.dto;

import lombok.Data;
import vn.com.nws.cms.modules.academic.domain.enums.CohortLifecycleStatus;

import java.time.LocalDate;

@Data
public class CohortUpdateRequest {
    private String name;
    private String code;
    private Integer maxStudents;
    private Integer minStudents;
    private Long classId;
    private Long semesterId;
    private Long teacherId;
    private Boolean active;
    private CohortLifecycleStatus status;
    private String canceledReason;
    private LocalDate enrollmentStartDate;
    private LocalDate enrollmentEndDate;
    private Boolean registrationEnabled;
}

