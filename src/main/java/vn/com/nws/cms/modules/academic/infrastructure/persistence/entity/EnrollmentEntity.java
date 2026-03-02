package vn.com.nws.cms.modules.academic.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.com.nws.cms.common.audit.AuditEntity;
import vn.com.nws.cms.modules.academic.domain.enums.EnrollmentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "enrollments", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"course_id", "student_id"})
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnrollmentEntity extends AuditEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private CourseEntity course;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private StudentEntity student;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EnrollmentStatus status;

    private Double grade;

    @Column(name = "process_score", precision = 4, scale = 2)
    private BigDecimal processScore;

    @Column(name = "exam_score", precision = 4, scale = 2)
    private BigDecimal examScore;

    @Column(name = "final_score", precision = 4, scale = 2)
    private BigDecimal finalScore;

    @Column(name = "scored_at")
    private LocalDateTime scoredAt;

    @Column(name = "score_locked", nullable = false)
    private boolean scoreLocked;

    @Column(name = "score_overridden", nullable = false)
    private boolean scoreOverridden;

    @Column(name = "score_override_reason", columnDefinition = "TEXT")
    private String scoreOverrideReason;

    @Column(name = "score_overridden_at")
    private LocalDateTime scoreOverriddenAt;

    @Column(name = "process_score_before_override", precision = 4, scale = 2)
    private BigDecimal processScoreBeforeOverride;

    @Column(name = "exam_score_before_override", precision = 4, scale = 2)
    private BigDecimal examScoreBeforeOverride;

    @Column(name = "final_score_before_override", precision = 4, scale = 2)
    private BigDecimal finalScoreBeforeOverride;
}
