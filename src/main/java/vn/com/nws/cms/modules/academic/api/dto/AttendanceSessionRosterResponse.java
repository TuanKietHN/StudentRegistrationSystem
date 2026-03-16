package vn.com.nws.cms.modules.academic.api.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class AttendanceSessionRosterResponse {
    private AttendanceSessionResponse session;
    private List<AttendanceRosterRowResponse> students;
}

