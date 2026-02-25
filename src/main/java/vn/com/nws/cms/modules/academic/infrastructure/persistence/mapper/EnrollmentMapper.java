package vn.com.nws.cms.modules.academic.infrastructure.persistence.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import vn.com.nws.cms.modules.academic.domain.model.Enrollment;
import vn.com.nws.cms.modules.academic.infrastructure.persistence.entity.EnrollmentEntity;
import vn.com.nws.cms.modules.auth.infrastructure.persistence.mapper.UserMapper;

@Mapper(componentModel = "spring", uses = {CourseMapper.class, UserMapper.class})
public interface EnrollmentMapper {
    @Mapping(target = "student", source = "student.user")
    Enrollment toDomain(EnrollmentEntity entity);

    @Mapping(target = "student", ignore = true)
    EnrollmentEntity toEntity(Enrollment domain);
}
