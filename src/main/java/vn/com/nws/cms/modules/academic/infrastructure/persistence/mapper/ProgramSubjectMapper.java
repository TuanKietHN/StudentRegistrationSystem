package vn.com.nws.cms.modules.academic.infrastructure.persistence.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import vn.com.nws.cms.modules.academic.domain.model.ProgramSubject;
import vn.com.nws.cms.modules.academic.infrastructure.persistence.entity.ProgramSubjectEntity;

@Mapper(componentModel = "spring", uses = {SubjectMapper.class})
public interface ProgramSubjectMapper {
    @Mapping(target = "programId", source = "academicProgram.id")
    ProgramSubject toDomain(ProgramSubjectEntity entity);

    @Mapping(target = "academicProgram.id", source = "programId")
    ProgramSubjectEntity toEntity(ProgramSubject domain);
}
