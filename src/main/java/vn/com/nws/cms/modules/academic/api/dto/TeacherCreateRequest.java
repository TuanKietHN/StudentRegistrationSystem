package vn.com.nws.cms.modules.academic.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TeacherCreateRequest {
    @NotNull(message = "User ID là bắt buộc")
    private Long userId;

    @NotBlank(message = "Mã nhân viên là bắt buộc")
    private String employeeCode;

    private Long departmentId;
    private String specialization;
    private String title;
    private String bio;
    private String officeLocation;
    private String officeHours;
    private String phone;
    private boolean active = true;
}
