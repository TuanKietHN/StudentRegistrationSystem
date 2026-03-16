package vn.com.nws.cms.modules.academic.api.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class AttendanceSessionOpenRequest {
    @NotNull
    private Long cohortId;

    private LocalDate sessionDate;

    @Min(1)
    private Integer periods;
}
