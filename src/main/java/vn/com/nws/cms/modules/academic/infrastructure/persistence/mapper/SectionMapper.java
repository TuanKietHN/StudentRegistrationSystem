package vn.com.nws.cms.modules.academic.infrastructure.persistence.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import vn.com.nws.cms.modules.academic.domain.model.Section;
import vn.com.nws.cms.modules.academic.infrastructure.persistence.entity.SectionEntity;
import vn.com.nws.cms.modules.auth.infrastructure.persistence.mapper.UserMapper;

@Mapper(componentModel = "spring", uses = {SubjectMapper.class, SemesterMapper.class, UserMapper.class})
public interface SectionMapper {
    @Mapping(target = "teacher", source = "teacher.user")
    Section toDomain(SectionEntity entity);

    @Mapping(target = "teacher", ignore = true)
    SectionEntity toEntity(Section domain);
}
