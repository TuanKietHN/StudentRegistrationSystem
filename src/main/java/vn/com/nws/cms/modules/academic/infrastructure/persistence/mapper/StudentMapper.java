package vn.com.nws.cms.modules.academic.infrastructure.persistence.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import vn.com.nws.cms.modules.academic.domain.model.Student;
import vn.com.nws.cms.modules.academic.infrastructure.persistence.entity.StudentEntity;
import vn.com.nws.cms.modules.auth.infrastructure.persistence.mapper.UserMapper;

@Mapper(componentModel = "spring", uses = {UserMapper.class})
public interface StudentMapper {

    Student toDomain(StudentEntity entity);

    @Mapping(target = "user", ignore = true)
    @Mapping(target = "department", ignore = true)
    StudentEntity toEntity(Student domain);
}

