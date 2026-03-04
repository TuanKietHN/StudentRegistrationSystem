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
import vn.com.nws.cms.common.dto.ApiResponse;
import vn.com.nws.cms.common.dto.PageResponse;
import vn.com.nws.cms.modules.academic.api.dto.StudentClassCreateRequest;
import vn.com.nws.cms.modules.academic.api.dto.StudentClassFilterRequest;
import vn.com.nws.cms.modules.academic.api.dto.StudentClassResponse;
import vn.com.nws.cms.modules.academic.api.dto.StudentClassUpdateRequest;
import vn.com.nws.cms.modules.academic.api.dto.StudentResponse;
import vn.com.nws.cms.modules.academic.application.StudentClassService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/student-classes")
@RequiredArgsConstructor
@Tag(name = "Academic - StudentClass", description = "Quản lý lớp hành chính")
public class StudentClassController {

    private final StudentClassService studentClassService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    @Operation(summary = "Danh sách lớp hành chính", description = "Lấy danh sách lớp hành chính có phân trang và lọc")
    public ResponseEntity<ApiResponse<PageResponse<StudentClassResponse>>> getStudentClasses(
            @Parameter(description = "Từ khóa tìm kiếm (tên, mã)") @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) Long cohortId,
            @RequestParam(required = false) Boolean active,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        StudentClassFilterRequest request = new StudentClassFilterRequest();
        request.setKeyword(keyword);
        request.setDepartmentId(departmentId);
        request.setCohortId(cohortId);
        request.setActive(active);
        request.setPage(page);
        request.setSize(size);
        PageResponse<StudentClassResponse> response = studentClassService.getStudentClasses(request);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách lớp hành chính thành công", response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    @Operation(summary = "Chi tiết lớp hành chính", description = "Lấy thông tin chi tiết lớp hành chính")
    public ResponseEntity<ApiResponse<StudentClassResponse>> getStudentClassById(@PathVariable Long id) {
        StudentClassResponse response = studentClassService.getStudentClassById(id);
        return ResponseEntity.ok(ApiResponse.success("Lấy thông tin lớp hành chính thành công", response));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Tạo lớp hành chính", description = "Tạo lớp hành chính (Admin)")
    public ResponseEntity<ApiResponse<StudentClassResponse>> createStudentClass(@Valid @RequestBody StudentClassCreateRequest request) {
        StudentClassResponse response = studentClassService.createStudentClass(request);
        return ResponseEntity.ok(ApiResponse.success("Tạo lớp hành chính thành công", response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Cập nhật lớp hành chính", description = "Cập nhật lớp hành chính (Admin)")
    public ResponseEntity<ApiResponse<StudentClassResponse>> updateStudentClass(@PathVariable Long id, @Valid @RequestBody StudentClassUpdateRequest request) {
        StudentClassResponse response = studentClassService.updateStudentClass(id, request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật lớp hành chính thành công", response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Xóa lớp hành chính", description = "Xóa lớp hành chính (Admin)")
    public ResponseEntity<ApiResponse<Void>> deleteStudentClass(@PathVariable Long id) {
        studentClassService.deleteStudentClass(id);
        return ResponseEntity.ok(ApiResponse.success("Xóa lớp hành chính thành công", null));
    }

    @GetMapping("/{id}/students")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    @Operation(summary = "Danh sách sinh viên theo lớp hành chính", description = "Lấy danh sách sinh viên của lớp hành chính")
    public ResponseEntity<ApiResponse<List<StudentResponse>>> getStudentClassStudents(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách sinh viên thành công", studentClassService.getStudentClassStudents(id)));
    }
}

