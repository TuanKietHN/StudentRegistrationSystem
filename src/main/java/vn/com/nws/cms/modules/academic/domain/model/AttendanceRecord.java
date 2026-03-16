package vn.com.nws.cms.modules.academic.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.com.nws.cms.common.audit.AuditEntity;
import vn.com.nws.cms.modules.academic.domain.enums.AttendanceStatus;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceRecord extends AuditEntity {
    private Long id;
    private Long sessionId;
    private Long enrollmentId;
    private Long studentId;
    private AttendanceStatus status;
    private LocalDateTime markedAt;
    private String note;
}

