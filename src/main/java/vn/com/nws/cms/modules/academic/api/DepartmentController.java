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
import vn.com.nws.cms.modules.academic.application.DepartmentService;

@RestController
@RequestMapping("/api/v1/departments")
@RequiredArgsConstructor
@Tag(name = "Academic - Department", description = "Quản lý khoa/bộ môn")
public class DepartmentController {

    private final DepartmentService departmentService;

    @GetMapping
    @PreAuthorize("hasAuthority('DEPARTMENT:READ')")
    @Operation(summary = "Danh sách khoa", description = "Lấy danh sách khoa có phân trang và tìm kiếm")
    public ResponseEntity<ApiResponse<PageResponse<DepartmentResponse>>> getDepartments(
            @Parameter(description = "Từ khóa tìm kiếm (tên, mã)") @RequestParam(required = false) String keyword,
            @Parameter(description = "Trạng thái hoạt động") @RequestParam(required = false) Boolean active,
            @Parameter(description = "Số trang (bắt đầu từ 1)") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "Số lượng bản ghi mỗi trang") @RequestParam(defaultValue = "10") int size
    ) {
        DepartmentFilterRequest request = new DepartmentFilterRequest();
        request.setKeyword(keyword);
        request.setActive(active);
        request.setPage(page);
        request.setSize(size);

        PageResponse<DepartmentResponse> response = departmentService.getDepartments(request);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách khoa thành công", response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('DEPARTMENT:READ')")
    @Operation(summary = "Chi tiết khoa", description = "Lấy thông tin chi tiết của một khoa")
    public ResponseEntity<ApiResponse<DepartmentResponse>> getDepartmentById(@PathVariable Long id) {
        DepartmentResponse response = departmentService.getDepartmentById(id);
        return ResponseEntity.ok(ApiResponse.success("Lấy thông tin khoa thành công", response));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('DEPARTMENT:CREATE')")
    @Operation(summary = "Tạo khoa mới", description = "Tạo mới một khoa (Admin)")
    public ResponseEntity<ApiResponse<DepartmentResponse>> createDepartment(@Valid @RequestBody DepartmentCreateRequest request) {
        DepartmentResponse response = departmentService.createDepartment(request);
        return ResponseEntity.ok(ApiResponse.success("Tạo khoa thành công", response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('DEPARTMENT:UPDATE')")
    @Operation(summary = "Cập nhật khoa", description = "Cập nhật thông tin khoa (Admin)")
    public ResponseEntity<ApiResponse<DepartmentResponse>> updateDepartment(
            @PathVariable Long id,
            @Valid @RequestBody DepartmentUpdateRequest request) {
        DepartmentResponse response = departmentService.updateDepartment(id, request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật khoa thành công", response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('DEPARTMENT:DELETE')")
    @Operation(summary = "Xóa khoa", description = "Xóa khoa (Admin)")
    public ResponseEntity<ApiResponse<Void>> deleteDepartment(@PathVariable Long id) {
        departmentService.deleteDepartment(id);
        return ResponseEntity.ok(ApiResponse.success("Xóa khoa thành công", null));
    }
}
