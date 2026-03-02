package vn.com.nws.cms.modules.academic.infrastructure.persistence.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import vn.com.nws.cms.modules.academic.domain.model.AdminClass;
import vn.com.nws.cms.modules.academic.infrastructure.persistence.entity.AdminClassEntity;

@Mapper(componentModel = "spring", uses = {DepartmentMapper.class})
public interface AdminClassMapper {
    AdminClass toDomain(AdminClassEntity entity);

    @Mapping(target = "department", ignore = true)
    AdminClassEntity toEntity(AdminClass domain);
}

