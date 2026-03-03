package vn.com.nws.cms.modules.academic.api.dto;

import lombok.Data;
import vn.com.nws.cms.modules.academic.domain.enums.SectionLifecycleStatus;

import java.time.LocalDate;

@Data
public class SectionUpdateRequest {
    private String name;
    private String code;
    private Integer maxStudents;
    private Integer minStudents;
    private Long subjectId;
    private Long semesterId;
    private Long teacherId;
    private Boolean active;
    private SectionLifecycleStatus status;
    private String canceledReason;
    private LocalDate enrollmentStartDate;
    private LocalDate enrollmentEndDate;
    private Boolean registrationEnabled;
}

