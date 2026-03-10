package vn.com.nws.cms.modules.academic.api.dto;

import lombok.Builder;
import lombok.Data;
import vn.com.nws.cms.modules.academic.domain.model.Subject;

@Data
@Builder
public class ProgramSubjectResponse {
    private Long id;
    private Long programId;
    private SubjectResponse subject;
    private Integer semester;
    private String subjectType;
    private Double passScore;
}
