package vn.com.nws.cms.modules.academic.api.dto;

import lombok.Builder;
import lombok.Data;
import vn.com.nws.cms.modules.academic.domain.model.CourseTimeSlot;

import java.time.LocalTime;

@Data
@Builder
public class CourseTimeSlotResponse {
    private Long id;
    private short dayOfWeek;
    private LocalTime startTime;
    private LocalTime endTime;

    public static CourseTimeSlotResponse fromDomain(CourseTimeSlot slot) {
        return CourseTimeSlotResponse.builder()
                .id(slot.getId())
                .dayOfWeek(slot.getDayOfWeek())
                .startTime(slot.getStartTime())
                .endTime(slot.getEndTime())
                .build();
    }
}

