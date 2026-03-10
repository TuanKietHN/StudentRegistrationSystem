package vn.com.nws.cms.modules.academic.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.com.nws.cms.common.audit.AuditEntity;

@Entity
@Table(name = "program_subjects", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"program_id", "subject_id"})
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProgramSubjectEntity extends AuditEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "program_id", nullable = false)
    private AcademicProgramEntity academicProgram;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id", nullable = false)
    private SubjectEntity subject;

    @Column(nullable = false)
    private Integer semester;

    @Column(name = "subject_type", nullable = false)
    private String subjectType;

    @Column(name = "pass_score")
    private Double passScore;
}
