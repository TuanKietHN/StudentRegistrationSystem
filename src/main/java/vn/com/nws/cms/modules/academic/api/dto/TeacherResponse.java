package vn.com.nws.cms.modules.academic.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.com.nws.cms.modules.academic.domain.model.Teacher;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeacherResponse {
    private Long id;
    private Long userId;
    private String username;
    private String fullName; // Fallback to username if not available
    private String email;
    private String employeeCode;
    private Long departmentId;
    private String departmentName;
    private String specialization;
    private String title;
    private String bio;
    private String officeLocation;
    private String officeHours;
    private String phone;
    private boolean active;

    public static TeacherResponse fromDomain(Teacher teacher) {
        return TeacherResponse.builder()
                .id(teacher.getId())
                .userId(teacher.getUser() != null ? teacher.getUser().getId() : null)
                .username(teacher.getUser() != null ? teacher.getUser().getUsername() : null)
                .email(teacher.getUser() != null ? teacher.getUser().getEmail() : null)
                .employeeCode(teacher.getEmployeeCode())
                .departmentId(teacher.getDepartment() != null ? teacher.getDepartment().getId() : null)
                .departmentName(teacher.getDepartment() != null ? teacher.getDepartment().getName() : null)
                .specialization(teacher.getSpecialization())
                .title(teacher.getTitle())
                .bio(teacher.getBio())
                .officeLocation(teacher.getOfficeLocation())
                .officeHours(teacher.getOfficeHours())
                .phone(teacher.getPhone())
                .active(teacher.isActive())
                .build();
    }
}
