package vn.com.nws.cms.modules.academic.infrastructure.persistence.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import vn.com.nws.cms.modules.academic.domain.model.SectionTimeSlot;
import vn.com.nws.cms.modules.academic.infrastructure.persistence.entity.SectionTimeSlotEntity;

@Mapper(componentModel = "spring")
public interface SectionTimeSlotMapper {

    @Mapping(target = "sectionId", source = "section.id")
    SectionTimeSlot toDomain(SectionTimeSlotEntity entity);

    @Mapping(target = "section", ignore = true)
    SectionTimeSlotEntity toEntity(SectionTimeSlot domain);
}

