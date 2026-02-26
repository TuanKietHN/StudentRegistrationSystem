package vn.com.nws.cms.modules.academic.api.dto;

import lombok.Data;
import vn.com.nws.cms.modules.academic.domain.enums.EnrollmentStatus;

@Data
public class EnrollmentUpdateRequest {
    private EnrollmentStatus status;
    private Double grade;
}
