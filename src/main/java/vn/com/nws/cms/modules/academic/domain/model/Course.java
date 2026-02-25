package vn.com.nws.cms.modules.academic.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.com.nws.cms.common.audit.AuditEntity;
import vn.com.nws.cms.modules.auth.domain.model.User;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Course extends AuditEntity {
    private Long id;
    private String name;
    private String code;
    private Integer maxStudents;
    private Integer currentStudents;
    private boolean active;

    private LocalDate enrollmentStartDate;
    private LocalDate enrollmentEndDate;
    
    // Relationships
    private Subject subject;
    private Semester semester;
    private User teacher;
}
