package vn.com.nws.cms.modules.academic.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.com.nws.cms.common.domain.model.Audit;

import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SectionTimeSlot extends Audit {
    private Long id;
    private Long sectionId;
    private short dayOfWeek;
    private LocalTime startTime;
    private LocalTime endTime;
    private String room;
}

