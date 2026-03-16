package vn.com.nws.cms.modules.academic.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AcademicProgramResponse {
    private Long id;
    private String code;
    private String name;
    private DepartmentResponse department;
    private Integer totalCredits;
    private String description;
    private boolean active;
}
