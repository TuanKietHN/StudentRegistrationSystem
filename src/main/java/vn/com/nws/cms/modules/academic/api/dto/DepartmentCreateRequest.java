package vn.com.nws.cms.modules.academic.api.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DepartmentCreateRequest {
    @NotBlank(message = "Mã khoa không được để trống")
    private String code;

    @NotBlank(message = "Tên khoa không được để trống")
    private String name;

    private String description;
    private Long parentId;
    private Long headTeacherId;
    private boolean active = true;
}
