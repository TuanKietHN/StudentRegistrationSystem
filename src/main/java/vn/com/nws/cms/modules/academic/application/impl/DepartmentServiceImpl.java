package vn.com.nws.cms.modules.academic.application.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.com.nws.cms.common.dto.PageResponse;
import vn.com.nws.cms.common.exception.BusinessException;
import vn.com.nws.cms.modules.academic.api.dto.*;
import vn.com.nws.cms.modules.academic.application.DepartmentService;
import vn.com.nws.cms.modules.academic.domain.model.Department;
import vn.com.nws.cms.modules.academic.domain.model.Teacher;
import vn.com.nws.cms.modules.academic.domain.repository.DepartmentRepository;
import vn.com.nws.cms.modules.academic.domain.repository.TeacherRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final TeacherRepository teacherRepository;

    @Override
    public PageResponse<DepartmentResponse> getDepartments(DepartmentFilterRequest request) {
        Pageable pageable = PageRequest.of(request.getPage() - 1, request.getSize(), Sort.by("id").descending());
        Page<Department> page = departmentRepository.findAll(request.getKeyword(), request.getActive(), pageable);

        List<DepartmentResponse> responses = page.getContent().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return PageResponse.<DepartmentResponse>builder()
                .page(request.getPage())
                .size(request.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .data(responses)
                .build();
    }

    @Override
    public DepartmentResponse getDepartmentById(Long id) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Không tìm thấy khoa với ID: " + id));
        return toResponse(department);
    }

    @Override
    @Transactional
    public DepartmentResponse createDepartment(DepartmentCreateRequest request) {
        if (departmentRepository.existsByCode(request.getCode())) {
            throw new BusinessException("Mã khoa đã tồn tại: " + request.getCode());
        }

        Department department = Department.builder()
                .code(request.getCode())
                .name(request.getName())
                .description(request.getDescription())
                .active(request.isActive())
                .build();

        if (request.getParentId() != null) {
            Department parent = departmentRepository.findById(request.getParentId())
                    .orElseThrow(() -> new BusinessException("Không tìm thấy khoa cha với ID: " + request.getParentId()));
            department.setParent(parent);
        }

        if (request.getHeadTeacherId() != null) {
            Teacher teacher = teacherRepository.findById(request.getHeadTeacherId())
                    .orElseThrow(() -> new BusinessException("Không tìm thấy giảng viên với ID: " + request.getHeadTeacherId()));
            department.setHeadTeacher(teacher);
        }

        return toResponse(departmentRepository.save(department));
    }

    @Override
    @Transactional
    public DepartmentResponse updateDepartment(Long id, DepartmentUpdateRequest request) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Không tìm thấy khoa với ID: " + id));

        if (!department.getCode().equals(request.getCode()) && departmentRepository.existsByCodeAndIdNot(request.getCode(), id)) {
            throw new BusinessException("Mã khoa đã tồn tại: " + request.getCode());
        }

        department.setCode(request.getCode());
        department.setName(request.getName());
        department.setDescription(request.getDescription());
        department.setActive(request.isActive());

        if (request.getParentId() != null) {
            if (request.getParentId().equals(id)) {
                throw new BusinessException("Khoa cha không hợp lệ (không thể là chính nó)");
            }
            Department parent = departmentRepository.findById(request.getParentId())
                    .orElseThrow(() -> new BusinessException("Không tìm thấy khoa cha với ID: " + request.getParentId()));
            validateNoCycle(id, parent);
            department.setParent(parent);
        } else {
            department.setParent(null);
        }

        if (request.getHeadTeacherId() != null) {
            Teacher teacher = teacherRepository.findById(request.getHeadTeacherId())
                    .orElseThrow(() -> new BusinessException("Không tìm thấy giảng viên với ID: " + request.getHeadTeacherId()));
            department.setHeadTeacher(teacher);
        } else {
            department.setHeadTeacher(null);
        }

        return toResponse(departmentRepository.save(department));
    }

    @Override
    @Transactional
    public void deleteDepartment(Long id) {
        if (departmentRepository.findById(id).isEmpty()) {
            throw new BusinessException("Không tìm thấy khoa với ID: " + id);
        }
        departmentRepository.delete(id);
    }

    private DepartmentResponse toResponse(Department department) {
        DepartmentResponse response = DepartmentResponse.fromDomain(department);
        if (department.getHeadTeacher() != null && department.getHeadTeacher().getUser() != null) {
             // Fallback to username since User domain model doesn't have fullName yet
             response.setHeadTeacherName(department.getHeadTeacher().getUser().getUsername());
        }
        return response;
    }

    private void validateNoCycle(Long departmentId, Department parentCandidate) {
        Department cursor = parentCandidate;
        while (cursor != null) {
            if (departmentId.equals(cursor.getId())) {
                throw new BusinessException("Khoa cha không hợp lệ (tạo vòng lặp cây khoa)");
            }
            if (cursor.getParent() == null || cursor.getParent().getId() == null) {
                break;
            }
            cursor = departmentRepository.findById(cursor.getParent().getId()).orElse(null);
        }
    }
}
