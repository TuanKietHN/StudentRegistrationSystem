package vn.com.nws.cms.modules.academic.api.dto;

import lombok.Builder;
import lombok.Data;
import vn.com.nws.cms.modules.academic.domain.model.CohortTimeSlot;

import java.time.LocalTime;

@Data
@Builder
public class CohortTimeSlotResponse {
    private Long id;
    private short dayOfWeek;
    private LocalTime startTime;
    private LocalTime endTime;

    public static CohortTimeSlotResponse fromDomain(CohortTimeSlot slot) {
        return CohortTimeSlotResponse.builder()
                .id(slot.getId())
                .dayOfWeek(slot.getDayOfWeek())
                .startTime(slot.getStartTime())
                .endTime(slot.getEndTime())
                .build();
    }
}
