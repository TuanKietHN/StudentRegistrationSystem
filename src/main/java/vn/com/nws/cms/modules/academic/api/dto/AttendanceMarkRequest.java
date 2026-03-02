package vn.com.nws.cms.modules.academic.api.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import vn.com.nws.cms.modules.academic.domain.enums.AttendanceStatus;

@Data
public class AttendanceMarkRequest {
    @NotNull
    private AttendanceStatus status;
    private String note;
}

