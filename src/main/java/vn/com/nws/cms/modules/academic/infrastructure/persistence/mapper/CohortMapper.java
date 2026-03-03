package vn.com.nws.cms.modules.academic.infrastructure.persistence.mapper;

import org.mapstruct.Mapper;
import vn.com.nws.cms.modules.academic.domain.model.Cohort;
import vn.com.nws.cms.modules.academic.infrastructure.persistence.entity.CohortEntity;

@Mapper(componentModel = "spring")
public interface CohortMapper {
    Cohort toDomain(CohortEntity entity);
    CohortEntity toEntity(Cohort domain);
}
