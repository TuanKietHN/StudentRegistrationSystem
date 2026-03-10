package vn.com.nws.cms.modules.academic.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.com.nws.cms.common.domain.model.Audit;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AcademicProgram extends Audit {
    private Long id;
    private String code;
    private String name;
    private Department department;
    private Integer totalCredits;
    private String description;
    private boolean active;
    private List<ProgramSubject> subjects;
}
