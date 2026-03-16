package vn.com.nws.cms.modules.academic.api.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class StudentProgressResponse {
    private Long studentId;
    private String studentName;
    private String programName;
    private Double progressPercentage;
    private Integer totalCredits;
    private Integer earnedCredits;
    private Double gpa10;
    private Double gpa4;
    private List<SubjectProgressDTO> subjects;
}
