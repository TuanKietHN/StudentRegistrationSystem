package vn.com.nws.cms.modules.academic.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.com.nws.cms.common.domain.model.Audit;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProgramSubject extends Audit {
    private Long id;
    private Long programId;
    private Subject subject;
    private Integer semester;
    private String subjectType; // COMPULSORY, ELECTIVE
    private Double passScore;
    
    // Constants for SubjectType
    public static final String TYPE_COMPULSORY = "COMPULSORY";
    public static final String TYPE_ELECTIVE = "ELECTIVE";
}
