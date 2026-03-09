package vn.com.nws.cms.modules.academic.api.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class StudentGradeSummaryResponse {
    private Long studentId;
    private String studentCode;
    private String studentName;
    private List<SubjectGradeResponse> grades;
    private BigDecimal gpa;
}

