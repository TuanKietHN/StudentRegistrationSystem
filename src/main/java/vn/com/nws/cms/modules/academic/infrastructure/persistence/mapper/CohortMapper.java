package vn.com.nws.cms.modules.academic.infrastructure.persistence.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import vn.com.nws.cms.modules.academic.domain.model.Cohort;
import vn.com.nws.cms.modules.academic.infrastructure.persistence.entity.CohortEntity;
import vn.com.nws.cms.modules.auth.infrastructure.persistence.mapper.UserMapper;

@Mapper(componentModel = "spring", uses = {ClassMapper.class, SemesterMapper.class, UserMapper.class})
public interface CohortMapper {
    @Mapping(target = "teacher", source = "teacher.user")
    @Mapping(target = "clazz", source = "clazz")
    Cohort toDomain(CohortEntity entity);

    @Mapping(target = "teacher", ignore = true)
    @Mapping(target = "teacherId", ignore = true)
    @Mapping(target = "clazz", source = "clazz")
    CohortEntity toEntity(Cohort domain);
}

