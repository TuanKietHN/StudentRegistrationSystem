package vn.com.nws.cms.modules.academic.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class StudentCreateRequest {

    @NotNull(message = "User ID là bắt buộc")
    private Long userId;

    @NotBlank(message = "Mã sinh viên là bắt buộc")
    private String studentCode;

    private Long departmentId;

    private String phone;

    private boolean active = true;
}

