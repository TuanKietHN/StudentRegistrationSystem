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
import vn.com.nws.cms.modules.academic.application.TeacherService;
import vn.com.nws.cms.modules.academic.domain.model.Department;
import vn.com.nws.cms.modules.academic.domain.model.Teacher;
import vn.com.nws.cms.modules.academic.domain.repository.DepartmentRepository;
import vn.com.nws.cms.modules.academic.domain.repository.TeacherRepository;
import vn.com.nws.cms.modules.auth.domain.model.User;
import vn.com.nws.cms.modules.auth.domain.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TeacherServiceImpl implements TeacherService {

    private final TeacherRepository teacherRepository;
    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;

    @Override
    public PageResponse<TeacherResponse> getTeachers(TeacherFilterRequest request) {
        Pageable pageable = PageRequest.of(request.getPage() - 1, request.getSize(), Sort.by("id").descending());
        Page<Teacher> page = teacherRepository.findAll(request.getKeyword(), request.getDepartmentId(), request.getActive(), pageable);

        List<TeacherResponse> responses = page.getContent().stream()
                .map(TeacherResponse::fromDomain)
                .collect(Collectors.toList());

        return PageResponse.<TeacherResponse>builder()
                .page(request.getPage())
                .size(request.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .data(responses)
                .build();
    }

    @Override
    public TeacherResponse getTeacherById(Long id) {
        Teacher teacher = teacherRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Không tìm thấy giảng viên với ID: " + id));
        return TeacherResponse.fromDomain(teacher);
    }

    @Override
    public TeacherResponse getTeacherByUserId(Long userId) {
        Teacher teacher = teacherRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException("Không tìm thấy hồ sơ giảng viên cho User ID: " + userId));
        return TeacherResponse.fromDomain(teacher);
    }

    @Override
    @Transactional
    public TeacherResponse createTeacher(TeacherCreateRequest request) {
        if (teacherRepository.existsByEmployeeCode(request.getEmployeeCode())) {
            throw new BusinessException("Mã nhân viên đã tồn tại: " + request.getEmployeeCode());
        }

        // Check if user exists
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new BusinessException("User không tồn tại: " + request.getUserId()));

        // Check if user already has teacher profile? Optional constraint.
        if (teacherRepository.findByUserId(request.getUserId()).isPresent()) {
             throw new BusinessException("User này đã có hồ sơ giảng viên");
        }

        Teacher teacher = Teacher.builder()
                .user(user)
                .employeeCode(request.getEmployeeCode())
                .specialization(request.getSpecialization())
                .title(request.getTitle())
                .bio(request.getBio())
                .officeLocation(request.getOfficeLocation())
                .officeHours(request.getOfficeHours())
                .phone(request.getPhone())
                .active(request.isActive())
                .build();

        if (request.getDepartmentId() != null) {
            Department department = departmentRepository.findById(request.getDepartmentId())
                    .orElseThrow(() -> new BusinessException("Khoa không tồn tại: " + request.getDepartmentId()));
            teacher.setDepartment(department);
        }

        return TeacherResponse.fromDomain(teacherRepository.save(teacher));
    }

    @Override
    @Transactional
    public TeacherResponse updateTeacher(Long id, TeacherUpdateRequest request) {
        Teacher teacher = teacherRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Không tìm thấy giảng viên với ID: " + id));

        if (!teacher.getEmployeeCode().equals(request.getEmployeeCode()) && 
            teacherRepository.existsByEmployeeCodeAndIdNot(request.getEmployeeCode(), id)) {
            throw new BusinessException("Mã nhân viên đã tồn tại: " + request.getEmployeeCode());
        }

        teacher.setEmployeeCode(request.getEmployeeCode());
        teacher.setSpecialization(request.getSpecialization());
        teacher.setTitle(request.getTitle());
        teacher.setBio(request.getBio());
        teacher.setOfficeLocation(request.getOfficeLocation());
        teacher.setOfficeHours(request.getOfficeHours());
        teacher.setPhone(request.getPhone());
        teacher.setActive(request.isActive());

        if (request.getDepartmentId() != null) {
            Department department = departmentRepository.findById(request.getDepartmentId())
                    .orElseThrow(() -> new BusinessException("Khoa không tồn tại: " + request.getDepartmentId()));
            teacher.setDepartment(department);
        } else {
            teacher.setDepartment(null);
        }

        return TeacherResponse.fromDomain(teacherRepository.save(teacher));
    }

    @Override
    @Transactional
    public void deleteTeacher(Long id) {
        if (teacherRepository.findById(id).isEmpty()) {
            throw new BusinessException("Không tìm thấy giảng viên với ID: " + id);
        }
        teacherRepository.deleteById(id);
    }
}
