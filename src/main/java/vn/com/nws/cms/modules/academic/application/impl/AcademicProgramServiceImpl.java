package vn.com.nws.cms.modules.academic.application.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.com.nws.cms.common.exception.ResourceNotFoundException;
import vn.com.nws.cms.modules.academic.api.dto.*;
import vn.com.nws.cms.modules.academic.application.AcademicProgramService;
import vn.com.nws.cms.modules.academic.application.mapper.AcademicProgramDTOMapper;
import vn.com.nws.cms.modules.academic.domain.model.AcademicProgram;
import vn.com.nws.cms.modules.academic.domain.model.Department;
import vn.com.nws.cms.modules.academic.domain.model.ProgramSubject;
import vn.com.nws.cms.modules.academic.domain.model.Subject;
import vn.com.nws.cms.modules.academic.domain.repository.AcademicProgramRepository;
import vn.com.nws.cms.modules.academic.domain.repository.DepartmentRepository;
import vn.com.nws.cms.modules.academic.domain.repository.ProgramSubjectRepository;
import vn.com.nws.cms.modules.academic.domain.repository.StudentClassRepository;
import vn.com.nws.cms.modules.academic.domain.repository.SubjectRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AcademicProgramServiceImpl implements AcademicProgramService {
    private final AcademicProgramRepository academicProgramRepository;
    private final ProgramSubjectRepository programSubjectRepository;
    private final DepartmentRepository departmentRepository;
    private final SubjectRepository subjectRepository;
    private final StudentClassRepository studentClassRepository;
    private final AcademicProgramDTOMapper mapper;

    @Override
    @Transactional
    public AcademicProgramResponse create(AcademicProgramCreateRequest request) {
        if (academicProgramRepository.existsByCode(request.getCode())) {
            throw new IllegalArgumentException("Program code already exists: " + request.getCode());
        }

        Department department = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + request.getDepartmentId()));

        AcademicProgram program = AcademicProgram.builder()
                .code(request.getCode())
                .name(request.getName())
                .department(department)
                .totalCredits(request.getTotalCredits())
                .description(request.getDescription())
                .active(true)
                .build();

        program = academicProgramRepository.save(program);
        return mapper.toResponse(program);
    }

    @Override
    @Transactional
    public AcademicProgramResponse update(Long id, AcademicProgramUpdateRequest request) {
        AcademicProgram program = academicProgramRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Program not found with id: " + id));

        if (request.getName() != null) {
            program.setName(request.getName());
        }
        if (request.getDescription() != null) {
            program.setDescription(request.getDescription());
        }
        if (request.getTotalCredits() != null) {
            program.setTotalCredits(request.getTotalCredits());
        }
        if (request.getActive() != null) {
            program.setActive(request.getActive());
        }
        if (request.getDepartmentId() != null) {
            Department department = departmentRepository.findById(request.getDepartmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + request.getDepartmentId()));
            program.setDepartment(department);
        }

        program = academicProgramRepository.save(program);
        return mapper.toResponse(program);
    }

    @Override
    @Transactional(readOnly = true)
    public AcademicProgramResponse getById(Long id) {
        AcademicProgram program = academicProgramRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Program not found with id: " + id));
        return mapper.toResponse(program);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AcademicProgramResponse> getAll() {
        return academicProgramRepository.findAll().stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!academicProgramRepository.findById(id).isPresent()) {
            throw new ResourceNotFoundException("Program not found with id: " + id);
        }
        
        if (studentClassRepository.existsByProgramId(id)) {
            throw new IllegalArgumentException("Cannot delete program because it is being used by one or more classes.");
        }
        
        academicProgramRepository.deleteById(id);
    }

    @Override
    @Transactional
    public ProgramSubjectResponse addSubject(Long programId, ProgramSubjectRequest request) {
        // Verify program exists
        if (!academicProgramRepository.findById(programId).isPresent()) {
            throw new ResourceNotFoundException("Program not found with id: " + programId);
        }

        Subject subject = subjectRepository.findById(request.getSubjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Subject not found with id: " + request.getSubjectId()));

        // Check if subject already in program? 
        // Ideally should check unique constraint or repository check.
        // For now, let database constraint handle or check manually if needed.

        ProgramSubject ps = ProgramSubject.builder()
                .programId(programId)
                .subject(subject)
                .semester(request.getSemester())
                .subjectType(request.getSubjectType() != null ? request.getSubjectType() : ProgramSubject.TYPE_COMPULSORY)
                .passScore(request.getPassScore() != null ? request.getPassScore() : 4.0)
                .build();

        ps = programSubjectRepository.save(ps);
        return mapper.toSubjectResponse(ps);
    }

    @Override
    @Transactional
    public void removeSubject(Long programSubjectId) {
        if (!programSubjectRepository.findById(programSubjectId).isPresent()) {
            throw new ResourceNotFoundException("Program subject entry not found with id: " + programSubjectId);
        }
        programSubjectRepository.deleteById(programSubjectId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProgramSubjectResponse> getSubjects(Long programId) {
        if (!academicProgramRepository.findById(programId).isPresent()) {
            throw new ResourceNotFoundException("Program not found with id: " + programId);
        }
        return programSubjectRepository.findByProgramId(programId).stream()
                .map(mapper::toSubjectResponse)
                .collect(Collectors.toList());
    }
}
