package vn.com.nws.cms.modules.academic.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.com.nws.cms.common.domain.model.Audit;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentClass extends Audit {
    private Long id;
    private String code;
    private String name;
    private Department department;
    private Cohort cohort;
    private Teacher advisorTeacher;
    private Integer intakeYear;
    private String program;
    private boolean active;
}
