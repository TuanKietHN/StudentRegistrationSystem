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
public class Department extends Audit {
    private Long id;
    private String code;
    private String name;
    private String description;
    private Department parent;
    private Teacher headTeacher;
    private boolean active;
}
