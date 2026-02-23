package vn.com.nws.cms.modules.academic.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.com.nws.cms.modules.academic.domain.model.Department;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DepartmentResponse {
    private Long id;
    private String code;
    private String name;
    private String description;
    private Long parentId;
    private String parentName;
    private Long headTeacherId;
    private String headTeacherName;
    private boolean active;

    public static DepartmentResponse fromDomain(Department department) {
        return DepartmentResponse.builder()
                .id(department.getId())
                .code(department.getCode())
                .name(department.getName())
                .description(department.getDescription())
                .parentId(department.getParent() != null ? department.getParent().getId() : null)
                .parentName(department.getParent() != null ? department.getParent().getName() : null)
                .headTeacherId(department.getHeadTeacher() != null ? department.getHeadTeacher().getId() : null)
                // Assuming Teacher has a user field with full name, or just use Teacher's name if available.
                // For now, let's leave headTeacherName null or handle it in service.
                .active(department.isActive())
                .build();
    }
}
