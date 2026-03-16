package vn.com.nws.cms.modules.academic.infrastructure.persistence.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import vn.com.nws.cms.modules.academic.domain.model.Department;
import vn.com.nws.cms.modules.academic.infrastructure.persistence.entity.DepartmentEntity;

@Mapper(componentModel = "spring", uses = {TeacherMapper.class})
public interface DepartmentMapper {
    @Mapping(target = "headTeacher", source = "headTeacher")
    @Mapping(target = "parent", ignore = true) // Avoid cycle for now or handle depth
    Department toDomain(DepartmentEntity entity);

    @Mapping(target = "headTeacher", ignore = true)
    @Mapping(target = "parent", ignore = true)
    DepartmentEntity toEntity(Department domain);
}
