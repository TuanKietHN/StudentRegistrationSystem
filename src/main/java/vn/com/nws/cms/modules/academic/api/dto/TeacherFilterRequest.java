package vn.com.nws.cms.modules.academic.api.dto;

import lombok.Data;

@Data
public class TeacherFilterRequest {
    private String keyword;
    private Long departmentId;
    private Boolean active;
    private int page = 1;
    private int size = 10;
}
