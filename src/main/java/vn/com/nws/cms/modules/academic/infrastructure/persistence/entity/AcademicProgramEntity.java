package vn.com.nws.cms.modules.academic.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.com.nws.cms.common.audit.AuditEntity;

import java.util.List;

@Entity
@Table(name = "academic_programs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AcademicProgramEntity extends AuditEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String code;

    @Column(nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private DepartmentEntity department;

    @Column(name = "total_credits", nullable = false)
    private Integer totalCredits;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "is_active")
    private boolean active;

    @OneToMany(mappedBy = "academicProgram", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProgramSubjectEntity> subjects;
}
