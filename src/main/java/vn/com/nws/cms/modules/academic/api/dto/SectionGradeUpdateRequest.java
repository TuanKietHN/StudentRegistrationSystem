package vn.com.nws.cms.modules.academic.api.dto;

import lombok.Data;
import vn.com.nws.cms.modules.academic.domain.enums.EnrollmentStatus;

@Data
public class SectionGradeUpdateRequest {
    private EnrollmentStatus status; // To allow teacher to update status from the grade view

    private Double processScore;
    private Double examScore;
    private String overrideReason;
}
