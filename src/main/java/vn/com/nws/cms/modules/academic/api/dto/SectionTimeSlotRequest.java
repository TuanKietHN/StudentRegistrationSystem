package vn.com.nws.cms.modules.academic.api.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalTime;

@Data
public class SectionTimeSlotRequest {
    @NotNull
    private Short dayOfWeek;
    @NotNull
    private LocalTime startTime;
    @NotNull
    private LocalTime endTime;
}

