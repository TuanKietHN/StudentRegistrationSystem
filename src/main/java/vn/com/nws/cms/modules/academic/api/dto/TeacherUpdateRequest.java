package vn.com.nws.cms.modules.academic.api.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TeacherUpdateRequest {
    @NotBlank(message = "Mã nhân viên là bắt buộc")
    private String employeeCode;

    private Long departmentId;
    private String specialization;
    private String title;
    private String bio;
    private String officeLocation;
    private String officeHours;
    private String phone;
    private boolean active;
}
