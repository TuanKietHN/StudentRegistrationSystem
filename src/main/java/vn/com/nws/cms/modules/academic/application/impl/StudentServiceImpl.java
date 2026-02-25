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
import vn.com.nws.cms.modules.academic.api.dto.StudentCreateRequest;
import vn.com.nws.cms.modules.academic.api.dto.StudentFilterRequest;
import vn.com.nws.cms.modules.academic.api.dto.StudentResponse;
import vn.com.nws.cms.modules.academic.api.dto.StudentUpdateRequest;
import vn.com.nws.cms.modules.academic.application.StudentService;
import vn.com.nws.cms.modules.academic.domain.model.Department;
import vn.com.nws.cms.modules.academic.domain.model.Student;
import vn.com.nws.cms.modules.academic.domain.repository.DepartmentRepository;
import vn.com.nws.cms.modules.academic.domain.repository.StudentRepository;
import vn.com.nws.cms.modules.auth.domain.model.User;
import vn.com.nws.cms.modules.auth.domain.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StudentServiceImpl implements StudentService {

    private final StudentRepository studentRepository;
    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;

    @Override
    public PageResponse<StudentResponse> getStudents(StudentFilterRequest request) {
        Pageable pageable = PageRequest.of(request.getPage() - 1, request.getSize(), Sort.by("id").descending());
        Page<Student> page = studentRepository.findAll(request.getKeyword(), request.getDepartmentId(), request.getActive(), pageable);

        List<StudentResponse> responses = page.getContent().stream()
                .map(StudentResponse::fromDomain)
                .collect(Collectors.toList());

        return PageResponse.<StudentResponse>builder()
                .page(request.getPage())
                .size(request.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .data(responses)
                .build();
    }

    @Override
    public StudentResponse getStudentById(Long id) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Không tìm thấy sinh viên với ID: " + id));
        return StudentResponse.fromDomain(student);
    }

    @Override
    public StudentResponse getStudentByUserId(Long userId) {
        Student student = studentRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException("Không tìm thấy hồ sơ sinh viên cho User ID: " + userId));
        return StudentResponse.fromDomain(student);
    }

    @Override
    @Transactional
    public StudentResponse createStudent(StudentCreateRequest request) {
        if (studentRepository.existsByStudentCode(request.getStudentCode())) {
            throw new BusinessException("Mã sinh viên đã tồn tại: " + request.getStudentCode());
        }

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new BusinessException("User không tồn tại: " + request.getUserId()));

        if (studentRepository.findByUserId(request.getUserId()).isPresent()) {
            throw new BusinessException("User này đã có hồ sơ sinh viên");
        }

        Student student = Student.builder()
                .user(user)
                .studentCode(request.getStudentCode())
                .phone(request.getPhone())
                .active(request.isActive())
                .build();

        if (request.getDepartmentId() != null) {
            Department department = departmentRepository.findById(request.getDepartmentId())
                    .orElseThrow(() -> new BusinessException("Khoa không tồn tại: " + request.getDepartmentId()));
            student.setDepartment(department);
        }

        return StudentResponse.fromDomain(studentRepository.save(student));
    }

    @Override
    @Transactional
    public StudentResponse updateStudent(Long id, StudentUpdateRequest request) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Không tìm thấy sinh viên với ID: " + id));

        if (!student.getStudentCode().equals(request.getStudentCode())
                && studentRepository.existsByStudentCodeAndIdNot(request.getStudentCode(), id)) {
            throw new BusinessException("Mã sinh viên đã tồn tại: " + request.getStudentCode());
        }

        student.setStudentCode(request.getStudentCode());
        student.setPhone(request.getPhone());
        student.setActive(request.isActive());

        if (request.getDepartmentId() != null) {
            Department department = departmentRepository.findById(request.getDepartmentId())
                    .orElseThrow(() -> new BusinessException("Khoa không tồn tại: " + request.getDepartmentId()));
            student.setDepartment(department);
        } else {
            student.setDepartment(null);
        }

        return StudentResponse.fromDomain(studentRepository.save(student));
    }

    @Override
    @Transactional
    public void deleteStudent(Long id) {
        studentRepository.deleteById(id);
    }
}

