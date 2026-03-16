package vn.com.nws.cms.modules.academic.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.com.nws.cms.common.domain.model.Audit;
import vn.com.nws.cms.modules.academic.domain.enums.SectionLifecycleStatus;
import vn.com.nws.cms.modules.auth.domain.model.User;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Section extends Audit {
    private Long id;
    private String name;
    private String code;
    private Integer maxStudents;
    private Integer currentStudents;
    private boolean active;

    private SectionLifecycleStatus status;
    private Integer minStudents;
    private LocalDateTime canceledAt;
    private String canceledReason;
    private Long mergedIntoSectionId;

    private LocalDate enrollmentStartDate;
    private LocalDate enrollmentEndDate;

    private boolean registrationEnabled;

    private Subject subject;
    private Semester semester;
    private User teacher;
}

