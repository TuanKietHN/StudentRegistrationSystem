package vn.com.nws.cms.modules.academic.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.com.nws.cms.common.domain.model.Audit;
import vn.com.nws.cms.modules.auth.domain.model.User;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Teacher extends Audit {
    private Long id;
    private User user;
    private String employeeCode;
    private Department department;
    private String specialization;
    private String title;
    private String bio;
    private String officeLocation;
    private String officeHours;
    private String phone;
    private boolean active;
}
