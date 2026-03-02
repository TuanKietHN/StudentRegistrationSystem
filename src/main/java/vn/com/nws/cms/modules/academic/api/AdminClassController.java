package vn.com.nws.cms.modules.academic.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import vn.com.nws.cms.common.dto.ApiResponse;
import vn.com.nws.cms.common.dto.PageResponse;
import vn.com.nws.cms.modules.academic.api.dto.*;
import vn.com.nws.cms.modules.academic.application.AdminClassService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin-classes")
@RequiredArgsConstructor
@Tag(name = "Academic - Admin Class", description = "Quản lý lớp hành chính")
public class AdminClassController {

    private final AdminClassService adminClassService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    @Operation(summary = "Danh sách lớp hành chính", description = "Lấy danh sách lớp hành chính có phân trang và tìm kiếm")
    public ResponseEntity<ApiResponse<PageResponse<AdminClassResponse>>> getAdminClasses(
            @Parameter(description = "Từ khóa tìm kiếm (mã, tên)") @RequestParam(required = false) String keyword,
            @Parameter(description = "ID khoa") @RequestParam(required = false) Long departmentId,
            @Parameter(description = "Khóa (năm nhập học)") @RequestParam(required = false) Integer intakeYear,
            @Parameter(description = "Trạng thái hoạt động") @RequestParam(required = false) Boolean active,
            @Parameter(description = "Số trang (bắt đầu từ 1)") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "Số lượng bản ghi mỗi trang") @RequestParam(defaultValue = "10") int size
    ) {
        AdminClassFilterRequest request = new AdminClassFilterRequest();
        request.setKeyword(keyword);
        request.setDepartmentId(departmentId);
        request.setIntakeYear(intakeYear);
        request.setActive(active);
        request.setPage(page);
        request.setSize(size);
        PageResponse<AdminClassResponse> response = adminClassService.getAdminClasses(request);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách lớp hành chính thành công", response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    @Operation(summary = "Chi tiết lớp hành chính", description = "Lấy chi tiết lớp hành chính")
    public ResponseEntity<ApiResponse<AdminClassResponse>> getAdminClassById(@PathVariable Long id) {
        AdminClassResponse response = adminClassService.getAdminClassById(id);
        return ResponseEntity.ok(ApiResponse.success("Lấy thông tin lớp hành chính thành công", response));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Tạo lớp hành chính", description = "Tạo lớp hành chính (Admin)")
    public ResponseEntity<ApiResponse<AdminClassResponse>> createAdminClass(@Valid @RequestBody AdminClassCreateRequest request) {
        AdminClassResponse response = adminClassService.createAdminClass(request);
        return ResponseEntity.ok(ApiResponse.success("Tạo lớp hành chính thành công", response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Cập nhật lớp hành chính", description = "Cập nhật lớp hành chính (Admin)")
    public ResponseEntity<ApiResponse<AdminClassResponse>> updateAdminClass(@PathVariable Long id, @Valid @RequestBody AdminClassUpdateRequest request) {
        AdminClassResponse response = adminClassService.updateAdminClass(id, request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật lớp hành chính thành công", response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Xóa lớp hành chính", description = "Xóa lớp hành chính (Admin)")
    public ResponseEntity<ApiResponse<Void>> deleteAdminClass(@PathVariable Long id) {
        adminClassService.deleteAdminClass(id);
        return ResponseEntity.ok(ApiResponse.success("Xóa lớp hành chính thành công", null));
    }

    @GetMapping("/{id}/students")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    @Operation(summary = "Danh sách sinh viên trong lớp hành chính", description = "Lấy danh sách sinh viên theo lớp hành chính")
    public ResponseEntity<ApiResponse<List<StudentResponse>>> getAdminClassStudents(@PathVariable Long id) {
        List<StudentResponse> response = adminClassService.getAdminClassStudents(id);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách sinh viên thành công", response));
    }
}

