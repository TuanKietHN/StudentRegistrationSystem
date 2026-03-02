package vn.com.nws.cms.modules.academic.infrastructure.persistence.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import vn.com.nws.cms.modules.academic.domain.model.CohortTimeSlot;
import vn.com.nws.cms.modules.academic.infrastructure.persistence.entity.CohortTimeSlotEntity;

@Mapper(componentModel = "spring")
public interface CohortTimeSlotMapper {

    @Mapping(target = "cohortId", source = "cohort.id")
    CohortTimeSlot toDomain(CohortTimeSlotEntity entity);

    @Mapping(target = "cohort", ignore = true)
    CohortTimeSlotEntity toEntity(CohortTimeSlot domain);
}

