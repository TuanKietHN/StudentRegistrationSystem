package vn.com.nws.cms.modules.academic.api.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SubjectProgressDTO {
    private String subjectCode;
    private String subjectName;
    private Integer credits;
    private Double finalScore;
    private String letterGrade;
    private Double grade4;
    private String status; // PASSED, NOT_PASSED, NOT_STARTED
    private String type; // COMPULSORY, ELECTIVE
}
