package vn.com.nws.cms.modules.academic.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.com.nws.cms.modules.academic.domain.model.Subject;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProgramSubjectResponse {
    private Long id;
    private Long programId;
    private SubjectResponse subject;
    private Integer semester;
    private String subjectType;
    private Double passScore;
}
