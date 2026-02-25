package vn.com.nws.cms.modules.academic.api.dto;

import lombok.Builder;
import lombok.Data;
import vn.com.nws.cms.modules.iam.api.dto.UserResponse;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class CourseResponse {
    private Long id;
    private String name;
    private String code;
    private Integer maxStudents;
    private Integer currentStudents;
    private boolean active;
    
    private SubjectResponse subject;
    private SemesterResponse semester;
    private UserResponse teacher;

    private LocalDate enrollmentStartDate;
    private LocalDate enrollmentEndDate;

    private List<CourseTimeSlotResponse> timeSlots;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
