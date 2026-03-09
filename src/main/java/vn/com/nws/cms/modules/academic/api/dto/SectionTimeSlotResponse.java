package vn.com.nws.cms.modules.academic.api.dto;

import lombok.Builder;
import lombok.Data;
import vn.com.nws.cms.modules.academic.domain.model.SectionTimeSlot;

import java.time.LocalTime;

@Data
@Builder
public class SectionTimeSlotResponse {
    private Long id;
    private short dayOfWeek;
    private LocalTime startTime;
    private LocalTime endTime;
    private String room;

    public static SectionTimeSlotResponse fromDomain(SectionTimeSlot slot) {
        return SectionTimeSlotResponse.builder()
                .id(slot.getId())
                .dayOfWeek(slot.getDayOfWeek())
                .startTime(slot.getStartTime())
                .endTime(slot.getEndTime())
                .room(slot.getRoom())
                .build();
    }
}

