package vn.com.nws.cms.modules.academic.api.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalTime;

@Data
public class CourseTimeSlotRequest {

    @Min(value = 1, message = "Day of week phải từ 1 đến 7")
    @Max(value = 7, message = "Day of week phải từ 1 đến 7")
    private short dayOfWeek;

    @NotNull(message = "Start time là bắt buộc")
    private LocalTime startTime;

    @NotNull(message = "End time là bắt buộc")
    private LocalTime endTime;
}

