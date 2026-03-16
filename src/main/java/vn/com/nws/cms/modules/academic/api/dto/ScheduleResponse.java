package vn.com.nws.cms.modules.academic.api.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalTime;

@Data
@Builder
public class ScheduleResponse {
    private Long id;
    private Long sectionId;
    private String sectionName;
    private String subjectName;
    private String subjectCode;
    private String teacherName;
    private Short dayOfWeek;
    private LocalTime startTime;
    private LocalTime endTime;
    private String room;
}
