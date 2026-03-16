package vn.com.nws.cms.modules.academic.infrastructure.persistence.mapper;

import org.mapstruct.Mapper;
import vn.com.nws.cms.modules.academic.domain.model.CourseClass;
import vn.com.nws.cms.modules.academic.infrastructure.persistence.entity.CourseClassEntity;

@Mapper(componentModel = "spring")
public interface ClassMapper {
    CourseClass toDomain(CourseClassEntity entity);
    CourseClassEntity toEntity(CourseClass domain);
}
