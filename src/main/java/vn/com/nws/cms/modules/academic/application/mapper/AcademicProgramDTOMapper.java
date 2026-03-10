package vn.com.nws.cms.modules.academic.application.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import vn.com.nws.cms.modules.academic.api.dto.AcademicProgramResponse;
import vn.com.nws.cms.modules.academic.api.dto.DepartmentResponse;
import vn.com.nws.cms.modules.academic.api.dto.ProgramSubjectResponse;
import vn.com.nws.cms.modules.academic.api.dto.SubjectResponse;
import vn.com.nws.cms.modules.academic.domain.model.AcademicProgram;
import vn.com.nws.cms.modules.academic.domain.model.Department;
import vn.com.nws.cms.modules.academic.domain.model.ProgramSubject;
import vn.com.nws.cms.modules.academic.domain.model.Subject;

@Mapper(componentModel = "spring")
public interface AcademicProgramDTOMapper {
    
    @Mapping(target = "department", source = "department")
    AcademicProgramResponse toResponse(AcademicProgram program);

    @Mapping(target = "subject", source = "subject")
    ProgramSubjectResponse toSubjectResponse(ProgramSubject programSubject);

    @Mapping(target = "parentId", source = "parent.id")
    @Mapping(target = "parentName", source = "parent.name")
    @Mapping(target = "headTeacherId", source = "headTeacher.id")
    @Mapping(target = "headTeacherName", source = "headTeacher.user.fullName")
    DepartmentResponse toDepartmentResponse(Department department);

    @Mapping(target = "credit", source = "credits")
    SubjectResponse toSubjectResponse(Subject subject);
}
