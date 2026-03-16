package vn.com.nws.cms.modules.academic.infrastructure.persistence.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import vn.com.nws.cms.modules.academic.domain.model.Enrollment;
import vn.com.nws.cms.modules.academic.infrastructure.persistence.entity.EnrollmentEntity;

@Mapper(componentModel = "spring", uses = {SectionMapper.class, StudentMapper.class})
public interface EnrollmentMapper {
    Enrollment toDomain(EnrollmentEntity entity);

    @Mapping(target = "student", ignore = true)
    @Mapping(target = "section", ignore = true)
    EnrollmentEntity toEntity(Enrollment domain);
}
