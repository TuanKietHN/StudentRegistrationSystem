package vn.com.nws.cms.modules.academic.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import vn.com.nws.cms.common.dto.ApiResponse;
import vn.com.nws.cms.common.dto.PageResponse;
import vn.com.nws.cms.modules.academic.api.dto.StudentCreateRequest;
import vn.com.nws.cms.modules.academic.api.dto.StudentFilterRequest;
import vn.com.nws.cms.modules.academic.api.dto.StudentResponse;
import vn.com.nws.cms.modules.academic.api.dto.StudentUpdateRequest;
import vn.com.nws.cms.modules.academic.application.StudentService;

@RestController
@RequestMapping("/api/v1/students")
@RequiredArgsConstructor
@Tag(name = "Academic - Student", description = "Quản lý hồ sơ sinh viên")
public class StudentController {

    private final StudentService studentService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Danh sách sinh viên", description = "Lấy danh sách hồ sơ sinh viên (Admin)")
    public ResponseEntity<ApiResponse<PageResponse<StudentResponse>>> getStudents(StudentFilterRequest request) {
        PageResponse<StudentResponse> response = studentService.getStudents(request);
        return ResponseEntity.ok(ApiResponse.success("Danh sách sinh viên", response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Chi tiết sinh viên", description = "Lấy thông tin hồ sơ sinh viên theo ID (Admin)")
    public ResponseEntity<ApiResponse<StudentResponse>> getStudentById(@PathVariable Long id) {
        StudentResponse response = studentService.getStudentById(id);
        return ResponseEntity.ok(ApiResponse.success("Thông tin sinh viên", response));
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Sinh viên theo user", description = "Lấy hồ sơ sinh viên theo User ID (Admin)")
    public ResponseEntity<ApiResponse<StudentResponse>> getStudentByUserId(@PathVariable Long userId) {
        StudentResponse response = studentService.getStudentByUserId(userId);
        return ResponseEntity.ok(ApiResponse.success("Thông tin sinh viên", response));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Tạo hồ sơ sinh viên", description = "Tạo hồ sơ sinh viên cho một user (Admin)")
    public ResponseEntity<ApiResponse<StudentResponse>> createStudent(@Valid @RequestBody StudentCreateRequest request) {
        StudentResponse response = studentService.createStudent(request);
        return ResponseEntity.ok(ApiResponse.success("Tạo hồ sơ sinh viên thành công", response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Cập nhật hồ sơ sinh viên", description = "Cập nhật hồ sơ sinh viên (Admin)")
    public ResponseEntity<ApiResponse<StudentResponse>> updateStudent(@PathVariable Long id, @Valid @RequestBody StudentUpdateRequest request) {
        StudentResponse response = studentService.updateStudent(id, request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật hồ sơ sinh viên thành công", response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Xóa hồ sơ sinh viên", description = "Xóa hồ sơ sinh viên (Admin)")
    public ResponseEntity<ApiResponse<Void>> deleteStudent(@PathVariable Long id) {
        studentService.deleteStudent(id);
        return ResponseEntity.ok(ApiResponse.success("Xóa hồ sơ sinh viên thành công", null));
    }
}

