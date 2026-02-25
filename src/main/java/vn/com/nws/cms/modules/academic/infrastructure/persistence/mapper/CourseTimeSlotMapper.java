package vn.com.nws.cms.modules.academic.infrastructure.persistence.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import vn.com.nws.cms.modules.academic.domain.model.CourseTimeSlot;
import vn.com.nws.cms.modules.academic.infrastructure.persistence.entity.CourseTimeSlotEntity;

@Mapper(componentModel = "spring")
public interface CourseTimeSlotMapper {

    @Mapping(target = "courseId", source = "course.id")
    CourseTimeSlot toDomain(CourseTimeSlotEntity entity);

    @Mapping(target = "course", ignore = true)
    CourseTimeSlotEntity toEntity(CourseTimeSlot domain);
}

