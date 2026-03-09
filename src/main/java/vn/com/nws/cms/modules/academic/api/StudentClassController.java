package vn.com.nws.cms.modules.academic.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import vn.com.nws.cms.common.dto.ApiResponse;
import vn.com.nws.cms.common.dto.PageResponse;
import vn.com.nws.cms.common.exception.BusinessException;
import vn.com.nws.cms.common.exception.ResourceNotFoundException;
import vn.com.nws.cms.modules.academic.api.dto.*;
import vn.com.nws.cms.modules.academic.application.StudentClassService;
import vn.com.nws.cms.modules.academic.infrastructure.persistence.entity.TeacherEntity;
import vn.com.nws.cms.modules.academic.infrastructure.persistence.repository.TeacherJpaRepository;
import vn.com.nws.cms.modules.auth.infrastructure.persistence.entity.UserEntity;
import vn.com.nws.cms.modules.auth.infrastructure.persistence.repository.JpaUserRepository;

import java.util.List;

@RestController
@RequestMapping("/api/v1/student-classes")
@RequiredArgsConstructor
@Tag(name = "Academic - StudentClass", description = "Quản lý lớp hành chính")
public class StudentClassController {

    private final StudentClassService studentClassService;
    private final TeacherJpaRepository teacherJpaRepository;
    private final JpaUserRepository userRepository;

    @GetMapping
    @PreAuthorize("hasAuthority('STUDENT_CLASS:READ')")
    @Operation(summary = "Danh sách lớp hành chính", description = "Lấy danh sách lớp hành chính có phân trang và lọc")
    public ResponseEntity<ApiResponse<PageResponse<StudentClassResponse>>> getStudentClasses(
            @Parameter(description = "Từ khóa tìm kiếm (tên, mã)") @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) Long cohortId,
            @RequestParam(required = false) Long advisorTeacherId,
            @RequestParam(required = false) Boolean active,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isTeacher = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_TEACHER"));
        boolean isAdmin = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        Long finalAdvisorId = advisorTeacherId;
        if (isTeacher && !isAdmin) {
            String username = auth.getName();
            UserEntity user = userRepository.findByUsername(username)
                    .or(() -> userRepository.findByEmail(username))
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));
            
            TeacherEntity teacher = teacherJpaRepository.findByUserId(user.getId())
                    .orElseThrow(() -> new BusinessException("Teacher profile not found"));
            finalAdvisorId = teacher.getId();
        }

        StudentClassFilterRequest request = new StudentClassFilterRequest();
        request.setKeyword(keyword);
        request.setDepartmentId(departmentId);
        request.setCohortId(cohortId);
        request.setAdvisorTeacherId(finalAdvisorId);
        request.setActive(active);
        request.setPage(page);
        request.setSize(size);
        PageResponse<StudentClassResponse> response = studentClassService.getStudentClasses(request);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách lớp hành chính thành công", response));
    }

    @GetMapping("/{id}/grades")
    @PreAuthorize("hasAuthority('STUDENT_CLASS:READ')")
    @Operation(summary = "Bảng điểm sinh viên theo lớp hành chính", description = "Lấy danh sách điểm các môn của sinh viên trong lớp")
    public ResponseEntity<ApiResponse<List<StudentGradeSummaryResponse>>> getStudentClassGrades(@PathVariable Long id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isTeacher = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_TEACHER"));
        boolean isAdmin = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (isTeacher && !isAdmin) {
            String username = auth.getName();
            UserEntity user = userRepository.findByUsername(username)
                    .or(() -> userRepository.findByEmail(username))
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));
            
            TeacherEntity teacher = teacherJpaRepository.findByUserId(user.getId())
                    .orElseThrow(() -> new BusinessException("Teacher profile not found"));
            
            StudentClassResponse sc = studentClassService.getStudentClassById(id);
            if (sc.getAdvisorTeacherId() == null || !sc.getAdvisorTeacherId().equals(teacher.getId())) {
                throw new BusinessException("Bạn không có quyền xem bảng điểm lớp này");
            }
        }

        return ResponseEntity.ok(ApiResponse.success("Lấy bảng điểm thành công", studentClassService.getStudentClassGrades(id)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('STUDENT_CLASS:READ')")
    @Operation(summary = "Chi tiết lớp hành chính", description = "Lấy thông tin chi tiết lớp hành chính")
    public ResponseEntity<ApiResponse<StudentClassResponse>> getStudentClassById(@PathVariable Long id) {
        StudentClassResponse response = studentClassService.getStudentClassById(id);
        return ResponseEntity.ok(ApiResponse.success("Lấy thông tin lớp hành chính thành công", response));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('STUDENT_CLASS:CREATE')")
    @Operation(summary = "Tạo lớp hành chính", description = "Tạo lớp hành chính (Admin)")
    public ResponseEntity<ApiResponse<StudentClassResponse>> createStudentClass(@Valid @RequestBody StudentClassCreateRequest request) {
        StudentClassResponse response = studentClassService.createStudentClass(request);
        return ResponseEntity.ok(ApiResponse.success("Tạo lớp hành chính thành công", response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('STUDENT_CLASS:UPDATE')")
    @Operation(summary = "Cập nhật lớp hành chính", description = "Cập nhật lớp hành chính (Admin)")
    public ResponseEntity<ApiResponse<StudentClassResponse>> updateStudentClass(@PathVariable Long id, @Valid @RequestBody StudentClassUpdateRequest request) {
        StudentClassResponse response = studentClassService.updateStudentClass(id, request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật lớp hành chính thành công", response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('STUDENT_CLASS:DELETE')")
    @Operation(summary = "Xóa lớp hành chính", description = "Xóa lớp hành chính (Admin)")
    public ResponseEntity<ApiResponse<Void>> deleteStudentClass(@PathVariable Long id) {
        studentClassService.deleteStudentClass(id);
        return ResponseEntity.ok(ApiResponse.success("Xóa lớp hành chính thành công", null));
    }

    @GetMapping("/{id}/students")
    @PreAuthorize("hasAuthority('STUDENT_CLASS:READ')")
    @Operation(summary = "Danh sách sinh viên theo lớp hành chính", description = "Lấy danh sách sinh viên của lớp hành chính")
    public ResponseEntity<ApiResponse<List<StudentResponse>>> getStudentClassStudents(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách sinh viên thành công", studentClassService.getStudentClassStudents(id)));
    }
}
