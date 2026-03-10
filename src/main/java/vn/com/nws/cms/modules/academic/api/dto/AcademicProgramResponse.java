package vn.com.nws.cms.modules.academic.api.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AcademicProgramResponse {
    private Long id;
    private String code;
    private String name;
    private DepartmentResponse department;
    private Integer totalCredits;
    private String description;
    private boolean active;
}
