package vn.com.nws.cms.modules.academic.application.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.com.nws.cms.common.dto.PageResponse;
import vn.com.nws.cms.common.exception.BusinessException;
import vn.com.nws.cms.modules.academic.api.dto.StudentClassCreateRequest;
import vn.com.nws.cms.modules.academic.api.dto.StudentClassFilterRequest;
import vn.com.nws.cms.modules.academic.api.dto.StudentClassResponse;
import vn.com.nws.cms.modules.academic.api.dto.StudentClassUpdateRequest;
import vn.com.nws.cms.modules.academic.api.dto.StudentResponse;
import vn.com.nws.cms.modules.academic.api.dto.StudentGradeSummaryResponse;
import vn.com.nws.cms.modules.academic.api.dto.SubjectGradeResponse;
import vn.com.nws.cms.modules.academic.application.StudentClassService;
import vn.com.nws.cms.modules.academic.domain.model.*;
import vn.com.nws.cms.modules.academic.domain.repository.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StudentClassServiceImpl implements StudentClassService {

    private final StudentClassRepository studentClassRepository;
    private final DepartmentRepository departmentRepository;
    private final CohortRepository cohortRepository;
    private final TeacherRepository teacherRepository;
    private final StudentRepository studentRepository;
    private final EnrollmentRepository enrollmentRepository;

    @Override
    public PageResponse<StudentClassResponse> getStudentClasses(StudentClassFilterRequest request) {
        Page<StudentClass> page = studentClassRepository.search(
                request.getKeyword(),
                request.getDepartmentId(),
                request.getCohortId(),
                request.getAdvisorTeacherId(),
                request.getActive(),
                PageRequest.of(request.getPage() - 1, request.getSize())
        );

        List<StudentClassResponse> data = page.getContent().stream().map(this::toResponse).toList();
        return PageResponse.<StudentClassResponse>builder()
                .page(request.getPage())
                .size(request.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .data(data)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<StudentGradeSummaryResponse> getStudentClassGrades(Long id) {
        StudentClass studentClass = studentClassRepository.findById(id).orElseThrow(() -> new BusinessException("Student class not found"));
        
        List<Student> students = studentRepository.findByStudentClassId(id);
        List<StudentGradeSummaryResponse> summary = new ArrayList<>();

        for (Student student : students) {
            // Fix: Use Student JPA Entity to fetch user info properly via repository if needed, 
            // but here 'student' is Domain Model mapped from Entity.
            // Check mapper to ensure User info is populated.
            
            List<Enrollment> enrollments = enrollmentRepository.findByStudentId(student.getId());
            List<SubjectGradeResponse> grades = enrollments.stream()
                    .map(e -> SubjectGradeResponse.builder()
                            .subjectCode(e.getSection().getSubject() != null ? e.getSection().getSubject().getCode() : "N/A")
                            .subjectName(e.getSection().getSubject() != null ? e.getSection().getSubject().getName() : "N/A")
                            .processScore(e.getProcessScore())
                            .examScore(e.getExamScore())
                            .finalScore(e.getFinalScore())
                            .semesterCode(e.getSection().getSemester() != null ? e.getSection().getSemester().getCode() : "N/A")
                            .build())
                    .toList();

            BigDecimal totalScore = BigDecimal.ZERO;
            int count = 0;
            for (Enrollment e : enrollments) {
                if (e.getFinalScore() != null) {
                    totalScore = totalScore.add(e.getFinalScore());
                    count++;
                }
            }
            BigDecimal gpa = count > 0 ? totalScore.divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
            
            String studentName = "N/A";
            if (student.getUser() != null) {
                studentName = student.getUser().getFullName();
            }

            summary.add(StudentGradeSummaryResponse.builder()
                    .studentId(student.getId())
                    .studentCode(student.getStudentCode())
                    .studentName(studentName)
                    .grades(grades)
                    .gpa(gpa)
                    .build());
        }
        return summary;
    }

    @Override
    public StudentClassResponse getStudentClassById(Long id) {
        StudentClass studentClass = studentClassRepository.findById(id).orElseThrow(() -> new BusinessException("Student class not found"));
        return toResponse(studentClass);
    }

    @Override
    @Transactional
    public StudentClassResponse createStudentClass(StudentClassCreateRequest request) {
        if (studentClassRepository.existsByCode(request.getCode())) {
            throw new BusinessException("Student class code already exists");
        }

        Department department = null;
        if (request.getDepartmentId() != null) {
            department = departmentRepository.findById(request.getDepartmentId())
                    .orElseThrow(() -> new BusinessException("Department not found"));
        }

        Cohort cohort = null;
        if (request.getCohortId() != null) {
            cohort = cohortRepository.findById(request.getCohortId())
                    .orElseThrow(() -> new BusinessException("Cohort not found"));
        }

        Teacher advisorTeacher = null;
        if (request.getAdvisorTeacherId() != null) {
            advisorTeacher = teacherRepository.findById(request.getAdvisorTeacherId())
                    .orElseThrow(() -> new BusinessException("Advisor teacher not found"));
        }

        StudentClass studentClass = StudentClass.builder()
                .code(request.getCode())
                .name(request.getName())
                .department(department)
                .cohort(cohort)
                .advisorTeacher(advisorTeacher)
                .intakeYear(request.getIntakeYear())
                .program(request.getProgram())
                .active(request.isActive())
                .build();

        studentClass = studentClassRepository.save(studentClass);
        return toResponse(studentClass);
    }

    @Override
    @Transactional
    public StudentClassResponse updateStudentClass(Long id, StudentClassUpdateRequest request) {
        StudentClass studentClass = studentClassRepository.findById(id).orElseThrow(() -> new BusinessException("Student class not found"));

        if (request.getCode() != null && !request.getCode().equals(studentClass.getCode())) {
            if (studentClassRepository.existsByCodeAndIdNot(request.getCode(), id)) {
                throw new BusinessException("Student class code already exists");
            }
            studentClass.setCode(request.getCode());
        }

        if (request.getName() != null) studentClass.setName(request.getName());
        if (request.getIntakeYear() != null) studentClass.setIntakeYear(request.getIntakeYear());
        if (request.getProgram() != null) studentClass.setProgram(request.getProgram());
        if (request.getActive() != null) studentClass.setActive(request.getActive());

        if (request.getDepartmentId() != null) {
            Department department = departmentRepository.findById(request.getDepartmentId())
                    .orElseThrow(() -> new BusinessException("Department not found"));
            studentClass.setDepartment(department);
        }

        if (request.getCohortId() != null) {
            Cohort cohort = cohortRepository.findById(request.getCohortId())
                    .orElseThrow(() -> new BusinessException("Cohort not found"));
            studentClass.setCohort(cohort);
        }

        if (request.getAdvisorTeacherId() != null) {
            Teacher advisorTeacher = teacherRepository.findById(request.getAdvisorTeacherId())
                    .orElseThrow(() -> new BusinessException("Advisor teacher not found"));
            studentClass.setAdvisorTeacher(advisorTeacher);
        }

        studentClass = studentClassRepository.save(studentClass);
        return toResponse(studentClass);
    }

    @Override
    @Transactional
    public void deleteStudentClass(Long id) {
        if (studentClassRepository.findById(id).isEmpty()) {
            throw new BusinessException("Student class not found");
        }
        studentClassRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<StudentResponse> getStudentClassStudents(Long id) {
        if (studentClassRepository.findById(id).isEmpty()) {
            throw new BusinessException("Student class not found");
        }
        // Force initialize collection or fetch with join
        // Here we rely on mapper or manual conversion to trigger lazy loading within transaction
        List<Student> students = studentRepository.findByStudentClassId(id);
        
        return students.stream().map(StudentResponse::fromDomain).toList();
    }

    private StudentClassResponse toResponse(StudentClass studentClass) {
        return StudentClassResponse.builder()
                .id(studentClass.getId())
                .code(studentClass.getCode())
                .name(studentClass.getName())
                .departmentId(studentClass.getDepartment() != null ? studentClass.getDepartment().getId() : null)
                .departmentName(studentClass.getDepartment() != null ? studentClass.getDepartment().getName() : null)
                .cohortId(studentClass.getCohort() != null ? studentClass.getCohort().getId() : null)
                .cohortCode(studentClass.getCohort() != null ? studentClass.getCohort().getCode() : null)
                .cohortName(studentClass.getCohort() != null ? studentClass.getCohort().getName() : null)
                .advisorTeacherId(studentClass.getAdvisorTeacher() != null ? studentClass.getAdvisorTeacher().getId() : null)
                .intakeYear(studentClass.getIntakeYear())
                .program(studentClass.getProgram())
                .active(studentClass.isActive())
                .createdAt(studentClass.getCreatedAt())
                .updatedAt(studentClass.getUpdatedAt())
                .build();
    }
}
