package vn.com.nws.cms.modules.academic.api.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class CohortResponse {
    private Long id;
    private String name;
    private String code;
    private boolean active;
    private Integer startYear;
    private Integer endYear;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
