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
import vn.com.nws.cms.modules.academic.application.ClassService;

@RestController
@RequestMapping("/api/v1/classes")
@RequiredArgsConstructor
@Tag(name = "Academic - Class", description = "Quản lý môn học (catalog)")
public class ClassController {

    private final ClassService classService;

    @GetMapping
    @Operation(summary = "Danh sách môn học", description = "Lấy danh sách môn học có phân trang và tìm kiếm")
    public ResponseEntity<ApiResponse<PageResponse<ClassResponse>>> getClasses(
            @Parameter(description = "Từ khóa tìm kiếm (tên, mã)") @RequestParam(required = false) String keyword,
            @Parameter(description = "Trạng thái hoạt động") @RequestParam(required = false) Boolean active,
            @Parameter(description = "Số trang (bắt đầu từ 1)") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "Số lượng bản ghi mỗi trang") @RequestParam(defaultValue = "10") int size
    ) {
        ClassFilterRequest request = new ClassFilterRequest();
        request.setKeyword(keyword);
        request.setActive(active);
        request.setPage(page);
        request.setSize(size);

        PageResponse<ClassResponse> response = classService.getClasses(request);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách môn học thành công", response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Chi tiết môn học", description = "Lấy thông tin chi tiết của một môn học")
    public ResponseEntity<ApiResponse<ClassResponse>> getClassById(@PathVariable Long id) {
        ClassResponse response = classService.getClassById(id);
        return ResponseEntity.ok(ApiResponse.success("Lấy thông tin môn học thành công", response));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Tạo môn học mới", description = "Tạo mới một môn học (Admin)")
    public ResponseEntity<ApiResponse<ClassResponse>> createClass(@Valid @RequestBody ClassCreateRequest request) {
        ClassResponse response = classService.createClass(request);
        return ResponseEntity.ok(ApiResponse.success("Tạo môn học thành công", response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Cập nhật môn học", description = "Cập nhật thông tin môn học (Admin)")
    public ResponseEntity<ApiResponse<ClassResponse>> updateClass(
            @PathVariable Long id,
            @Valid @RequestBody ClassUpdateRequest request) {
        ClassResponse response = classService.updateClass(id, request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật môn học thành công", response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Xóa môn học", description = "Xóa môn học (Admin)")
    public ResponseEntity<ApiResponse<Void>> deleteClass(@PathVariable Long id) {
        classService.deleteClass(id);
        return ResponseEntity.ok(ApiResponse.success("Xóa môn học thành công", null));
    }
}

