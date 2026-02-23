package vn.com.nws.cms.modules.academic.infrastructure.persistence.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import vn.com.nws.cms.modules.academic.domain.model.Teacher;
import vn.com.nws.cms.modules.academic.infrastructure.persistence.entity.TeacherEntity;
import vn.com.nws.cms.modules.auth.infrastructure.persistence.mapper.UserMapper;

@Mapper(componentModel = "spring", uses = {UserMapper.class})
public interface TeacherMapper {
    @Mapping(target = "user", source = "user")
    @Mapping(target = "department", ignore = true) // Avoid cycle
    Teacher toDomain(TeacherEntity entity);

    @Mapping(target = "user", ignore = true)
    @Mapping(target = "department", ignore = true)
    TeacherEntity toEntity(Teacher domain);
}
