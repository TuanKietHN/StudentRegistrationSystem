package vn.com.nws.cms.modules.academic.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.com.nws.cms.common.domain.model.Audit;
import vn.com.nws.cms.modules.academic.domain.enums.EnrollmentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Enrollment extends Audit {
    private Long id;
    private Section section;
    private Student student;
    private EnrollmentStatus status;

    private BigDecimal processScore;
    private BigDecimal examScore;
    private BigDecimal finalScore;
    private LocalDateTime scoredAt;
    private boolean scoreLocked;
    private boolean scoreOverridden;
    private String scoreOverrideReason;
    private LocalDateTime scoreOverriddenAt;
    private BigDecimal processScoreBeforeOverride;
    private BigDecimal examScoreBeforeOverride;
    private BigDecimal finalScoreBeforeOverride;
}
