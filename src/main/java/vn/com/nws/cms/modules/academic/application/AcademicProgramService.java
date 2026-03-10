package vn.com.nws.cms.modules.academic.application;

import vn.com.nws.cms.modules.academic.api.dto.*;

import java.util.List;

public interface AcademicProgramService {
    AcademicProgramResponse create(AcademicProgramCreateRequest request);
    AcademicProgramResponse update(Long id, AcademicProgramUpdateRequest request);
    AcademicProgramResponse getById(Long id);
    List<AcademicProgramResponse> getAll();
    void delete(Long id);

    ProgramSubjectResponse addSubject(Long programId, ProgramSubjectRequest request);
    void removeSubject(Long programSubjectId);
    List<ProgramSubjectResponse> getSubjects(Long programId);
}
