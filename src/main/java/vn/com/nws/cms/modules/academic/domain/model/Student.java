package vn.com.nws.cms.modules.academic.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.com.nws.cms.common.audit.AuditEntity;
import vn.com.nws.cms.modules.auth.domain.model.User;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Student extends AuditEntity {
    private Long id;
    private User user;
    private String studentCode;
    private Department department;
    private String phone;
    private boolean active;
}

