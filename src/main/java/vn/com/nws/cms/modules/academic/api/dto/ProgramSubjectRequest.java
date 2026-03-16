package vn.com.nws.cms.modules.academic.api.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.com.nws.cms.modules.academic.domain.model.ProgramSubject;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProgramSubjectRequest {
    @NotNull(message = "Subject ID is required")
    private Long subjectId;

    @NotNull(message = "Semester is required")
    private Integer semester;

    private String subjectType; // COMPULSORY, ELECTIVE

    private Double passScore;
}
