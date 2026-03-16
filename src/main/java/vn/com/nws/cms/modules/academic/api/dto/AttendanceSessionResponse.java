package vn.com.nws.cms.modules.academic.api.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class AttendanceSessionResponse {
    private Long id;
    private Long cohortId;
    private LocalDate sessionDate;
    private short periods;
    private LocalDateTime openedAt;
    private LocalDateTime closesAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
