package vn.com.nws.cms.modules.academic.infrastructure.persistence.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import vn.com.nws.cms.modules.academic.domain.model.AcademicProgram;
import vn.com.nws.cms.modules.academic.infrastructure.persistence.entity.AcademicProgramEntity;

@Mapper(componentModel = "spring", uses = {DepartmentMapper.class, ProgramSubjectMapper.class})
public interface AcademicProgramMapper {
    AcademicProgram toDomain(AcademicProgramEntity entity);
    
    @Mapping(target = "subjects", ignore = true) // Handled separately or cascaded
    AcademicProgramEntity toEntity(AcademicProgram domain);
}
