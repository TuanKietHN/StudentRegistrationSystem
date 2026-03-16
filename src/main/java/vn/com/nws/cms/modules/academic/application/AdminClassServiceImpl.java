package vn.com.nws.cms.modules.academic.application;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.com.nws.cms.common.dto.PageResponse;
import vn.com.nws.cms.common.exception.BusinessException;
import vn.com.nws.cms.modules.academic.api.dto.*;
import vn.com.nws.cms.modules.academic.domain.model.AdminClass;
import vn.com.nws.cms.modules.academic.domain.model.Department;
import vn.com.nws.cms.modules.academic.domain.model.Student;
import vn.com.nws.cms.modules.academic.domain.repository.AdminClassRepository;
import vn.com.nws.cms.modules.academic.domain.repository.DepartmentRepository;
import vn.com.nws.cms.modules.academic.domain.repository.StudentRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminClassServiceImpl implements AdminClassService {

    private final AdminClassRepository adminClassRepository;
    private final DepartmentRepository departmentRepository;
    private final StudentRepository studentRepository;

    @Override
    public PageResponse<AdminClassResponse> getAdminClasses(AdminClassFilterRequest request) {
        Page<AdminClass> page = adminClassRepository.search(
                request.getKeyword(),
                request.getDepartmentId(),
                request.getIntakeYear(),
                request.getActive(),
                PageRequest.of(request.getPage() - 1, request.getSize())
        );

        List<AdminClassResponse> data = page.getContent().stream().map(this::toResponse).toList();
        return PageResponse.<AdminClassResponse>builder()
                .page(request.getPage())
                .size(request.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .data(data)
                .build();
    }

    @Override
    public AdminClassResponse getAdminClassById(Long id) {
        AdminClass adminClass = adminClassRepository.findById(id).orElseThrow(() -> new BusinessException("Admin class not found"));
        return toResponse(adminClass);
    }

    @Override
    @Transactional
    public AdminClassResponse createAdminClass(AdminClassCreateRequest request) {
        if (adminClassRepository.existsByCode(request.getCode())) {
            throw new BusinessException("Admin class code already exists");
        }

        Department department = null;
        if (request.getDepartmentId() != null) {
            department = departmentRepository.findById(request.getDepartmentId())
                    .orElseThrow(() -> new BusinessException("Department not found"));
        }

        AdminClass adminClass = AdminClass.builder()
                .code(request.getCode())
                .name(request.getName())
                .department(department)
                .intakeYear(request.getIntakeYear())
                .program(request.getProgram())
                .active(request.isActive())
                .build();

        adminClass = adminClassRepository.save(adminClass);
        return toResponse(adminClass);
    }

    @Override
    @Transactional
    public AdminClassResponse updateAdminClass(Long id, AdminClassUpdateRequest request) {
        AdminClass adminClass = adminClassRepository.findById(id).orElseThrow(() -> new BusinessException("Admin class not found"));

        if (request.getCode() != null && !request.getCode().equals(adminClass.getCode())) {
            if (adminClassRepository.existsByCodeAndIdNot(request.getCode(), id)) {
                throw new BusinessException("Admin class code already exists");
            }
            adminClass.setCode(request.getCode());
        }

        if (request.getName() != null) adminClass.setName(request.getName());
        if (request.getIntakeYear() != null) adminClass.setIntakeYear(request.getIntakeYear());
        if (request.getProgram() != null) adminClass.setProgram(request.getProgram());
        if (request.getActive() != null) adminClass.setActive(request.getActive());

        if (request.getDepartmentId() != null) {
            Department department = departmentRepository.findById(request.getDepartmentId())
                    .orElseThrow(() -> new BusinessException("Department not found"));
            adminClass.setDepartment(department);
        }

        adminClass = adminClassRepository.save(adminClass);
        return toResponse(adminClass);
    }

    @Override
    @Transactional
    public void deleteAdminClass(Long id) {
        if (adminClassRepository.findById(id).isEmpty()) {
            throw new BusinessException("Admin class not found");
        }
        adminClassRepository.deleteById(id);
    }

    @Override
    public List<StudentResponse> getAdminClassStudents(Long id) {
        if (adminClassRepository.findById(id).isEmpty()) {
            throw new BusinessException("Admin class not found");
        }
        List<Student> students = studentRepository.findByAdminClassId(id);
        return students.stream().map(StudentResponse::fromDomain).toList();
    }

    private AdminClassResponse toResponse(AdminClass adminClass) {
        return AdminClassResponse.builder()
                .id(adminClass.getId())
                .code(adminClass.getCode())
                .name(adminClass.getName())
                .departmentId(adminClass.getDepartment() != null ? adminClass.getDepartment().getId() : null)
                .departmentName(adminClass.getDepartment() != null ? adminClass.getDepartment().getName() : null)
                .intakeYear(adminClass.getIntakeYear())
                .program(adminClass.getProgram())
                .active(adminClass.isActive())
                .createdAt(adminClass.getCreatedAt())
                .updatedAt(adminClass.getUpdatedAt())
                .build();
    }
}

