package vn.com.nws.cms.modules.academic.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.com.nws.cms.common.audit.AuditEntity;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceSession extends AuditEntity {
    private Long id;
    private Long cohortId;
    private LocalDate sessionDate;
    private short periods;
    private LocalDateTime openedAt;
    private LocalDateTime closesAt;
    private Long createdByUserId;
}
