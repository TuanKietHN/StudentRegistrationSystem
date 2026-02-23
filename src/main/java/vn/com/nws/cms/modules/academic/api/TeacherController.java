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
import vn.com.nws.cms.modules.academic.application.TeacherService;

@RestController
@RequestMapping("/api/v1/teachers")
@RequiredArgsConstructor
@Tag(name = "Academic - Teacher", description = "Quản lý hồ sơ giảng viên")
public class TeacherController {

    private final TeacherService teacherService;

    @GetMapping
    @Operation(summary = "Danh sách giảng viên", description = "Lấy danh sách giảng viên có phân trang và tìm kiếm")
    public ResponseEntity<ApiResponse<PageResponse<TeacherResponse>>> getTeachers(
            @Parameter(description = "Từ khóa tìm kiếm (tên, mã)") @RequestParam(required = false) String keyword,
            @Parameter(description = "ID khoa") @RequestParam(required = false) Long departmentId,
            @Parameter(description = "Trạng thái hoạt động") @RequestParam(required = false) Boolean active,
            @Parameter(description = "Số trang (bắt đầu từ 1)") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "Số lượng bản ghi mỗi trang") @RequestParam(defaultValue = "10") int size
    ) {
        TeacherFilterRequest request = new TeacherFilterRequest();
        request.setKeyword(keyword);
        request.setDepartmentId(departmentId);
        request.setActive(active);
        request.setPage(page);
        request.setSize(size);

        PageResponse<TeacherResponse> response = teacherService.getTeachers(request);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách giảng viên thành công", response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Chi tiết giảng viên", description = "Lấy thông tin chi tiết hồ sơ giảng viên")
    public ResponseEntity<ApiResponse<TeacherResponse>> getTeacherById(@PathVariable Long id) {
        TeacherResponse response = teacherService.getTeacherById(id);
        return ResponseEntity.ok(ApiResponse.success("Lấy thông tin giảng viên thành công", response));
    }
    
    @GetMapping("/user/{userId}")
    @Operation(summary = "Chi tiết giảng viên theo User ID", description = "Lấy hồ sơ giảng viên dựa trên User ID")
    public ResponseEntity<ApiResponse<TeacherResponse>> getTeacherByUserId(@PathVariable Long userId) {
        TeacherResponse response = teacherService.getTeacherByUserId(userId);
        return ResponseEntity.ok(ApiResponse.success("Lấy thông tin giảng viên thành công", response));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Tạo hồ sơ giảng viên", description = "Tạo mới hồ sơ giảng viên (Admin)")
    public ResponseEntity<ApiResponse<TeacherResponse>> createTeacher(@Valid @RequestBody TeacherCreateRequest request) {
        TeacherResponse response = teacherService.createTeacher(request);
        return ResponseEntity.ok(ApiResponse.success("Tạo hồ sơ giảng viên thành công", response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Cập nhật hồ sơ giảng viên", description = "Cập nhật thông tin giảng viên (Admin)")
    public ResponseEntity<ApiResponse<TeacherResponse>> updateTeacher(
            @PathVariable Long id,
            @Valid @RequestBody TeacherUpdateRequest request) {
        TeacherResponse response = teacherService.updateTeacher(id, request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật hồ sơ giảng viên thành công", response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Xóa hồ sơ giảng viên", description = "Xóa hồ sơ giảng viên (Admin)")
    public ResponseEntity<ApiResponse<Void>> deleteTeacher(@PathVariable Long id) {
        teacherService.deleteTeacher(id);
        return ResponseEntity.ok(ApiResponse.success("Xóa hồ sơ giảng viên thành công", null));
    }
}
