package vn.com.nws.cms.modules.academic.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.com.nws.cms.modules.academic.domain.model.Student;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentResponse {
    private Long id;
    private Long userId;
    private String username;
    private String email;
    private String studentCode;
    private Long departmentId;
    private String departmentName;
    private String phone;
    private boolean active;

    public static StudentResponse fromDomain(Student student) {
        return StudentResponse.builder()
                .id(student.getId())
                .userId(student.getUser() != null ? student.getUser().getId() : null)
                .username(student.getUser() != null ? student.getUser().getUsername() : null)
                .email(student.getUser() != null ? student.getUser().getEmail() : null)
                .studentCode(student.getStudentCode())
                .departmentId(student.getDepartment() != null ? student.getDepartment().getId() : null)
                .departmentName(student.getDepartment() != null ? student.getDepartment().getName() : null)
                .phone(student.getPhone())
                .active(student.isActive())
                .build();
    }
}

