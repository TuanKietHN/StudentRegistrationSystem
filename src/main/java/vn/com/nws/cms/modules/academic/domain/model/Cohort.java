package vn.com.nws.cms.modules.academic.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.com.nws.cms.common.audit.AuditEntity;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Cohort extends AuditEntity {
    private Long id;
    private String code;
    private String name;
    private Integer startYear;
    private Integer endYear;
    private boolean active;
}

