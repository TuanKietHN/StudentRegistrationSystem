package vn.com.nws.cms.modules.academic.infrastructure.persistence.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import vn.com.nws.cms.modules.academic.domain.model.StudentClass;
import vn.com.nws.cms.modules.academic.infrastructure.persistence.entity.StudentClassEntity;

@Mapper(componentModel = "spring", uses = {DepartmentMapper.class, CohortMapper.class})
public interface StudentClassMapper {
    StudentClass toDomain(StudentClassEntity entity);

    @Mapping(target = "department", ignore = true)
    @Mapping(target = "cohort", ignore = true)
    StudentClassEntity toEntity(StudentClass domain);
}

