package vn.com.nws.cms.modules.academic.api.dto;

import lombok.Builder;
import lombok.Data;
import vn.com.nws.cms.modules.academic.domain.enums.AttendanceStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class AttendanceRosterRowResponse {
    private Long enrollmentId;
    private Long studentId;
    private String studentCode;
    private String username;
    private String email;
    private String phone;
    private String departmentCode;
    private String departmentName;
    private String adminClassCode;
    private String adminClassName;

    private AttendanceStatus attendanceStatus;
    private LocalDateTime markedAt;
    private String note;

    private BigDecimal absentEquivalentPeriods;
    private BigDecimal absentLimitPeriods;
    private boolean bannedExam;
}

