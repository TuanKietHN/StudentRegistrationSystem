package vn.com.nws.cms.modules.academic.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.com.nws.cms.common.audit.AuditEntity;

@Entity
@Table(name = "cohorts")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CohortEntity extends AuditEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String code;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Integer startYear;

    @Column(nullable = false)
    private Integer endYear;

    @Column(nullable = false)
    private boolean active;
}
