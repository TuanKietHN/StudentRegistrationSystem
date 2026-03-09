package vn.com.nws.cms.modules.academic.api.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Builder
public class SubjectGradeResponse {
    private String subjectCode;
    private String subjectName;
    private BigDecimal processScore;
    private BigDecimal examScore;
    private BigDecimal finalScore;
    private String semesterCode;
}
