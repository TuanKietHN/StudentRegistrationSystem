package vn.com.nws.cms.modules.academic.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.com.nws.cms.common.audit.AuditEntity;
import vn.com.nws.cms.modules.academic.domain.enums.CourseLifecycleStatus;
import vn.com.nws.cms.modules.auth.domain.model.User;

import java.time.LocalDate;
import java.time.LocalDateTime;

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

    private CourseLifecycleStatus status;
    private Integer minStudents;
    private LocalDateTime canceledAt;
    private String canceledReason;
    private Long mergedIntoCourseId;

    private LocalDate enrollmentStartDate;
    private LocalDate enrollmentEndDate;
    
    // Relationships
    private Subject subject;
    private Semester semester;
    private User teacher;
}
