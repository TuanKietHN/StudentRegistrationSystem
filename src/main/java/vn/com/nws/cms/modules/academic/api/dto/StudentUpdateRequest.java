package vn.com.nws.cms.modules.academic.api.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class StudentUpdateRequest {

    @NotBlank(message = "Mã sinh viên là bắt buộc")
    private String studentCode;

    private Long departmentId;

    private String phone;

    private boolean active = true;
}

