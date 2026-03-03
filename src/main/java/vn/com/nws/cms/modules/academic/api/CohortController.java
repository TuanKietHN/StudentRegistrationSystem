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
import vn.com.nws.cms.modules.academic.application.CohortService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/cohorts")
@RequiredArgsConstructor
@Tag(name = "Academic - Cohort", description = "Quản lý niên khóa (cohort)")
public class CohortController {

    private final CohortService cohortService;

    @GetMapping
    @Operation(summary = "Danh sách niên khóa", description = "Lấy danh sách niên khóa có phân trang và lọc")
    public ResponseEntity<ApiResponse<PageResponse<CohortResponse>>> getCohorts(
            @Parameter(description = "Từ khóa tìm kiếm (tên, mã)") @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer startYear,
            @RequestParam(required = false) Integer endYear,
            @RequestParam(required = false) Boolean active,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        CohortFilterRequest request = new CohortFilterRequest();
        request.setKeyword(keyword);
        request.setStartYear(startYear);
        request.setEndYear(endYear);
        request.setActive(active);
        request.setPage(page);
        request.setSize(size);

        PageResponse<CohortResponse> response = cohortService.getCohorts(request);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách niên khóa thành công", response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Chi tiết niên khóa", description = "Lấy thông tin chi tiết niên khóa")
    public ResponseEntity<ApiResponse<CohortResponse>> getCohortById(@PathVariable Long id) {
        CohortResponse response = cohortService.getCohortById(id);
        return ResponseEntity.ok(ApiResponse.success("Lấy thông tin niên khóa thành công", response));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Tạo niên khóa", description = "Tạo niên khóa (Admin)")
    public ResponseEntity<ApiResponse<CohortResponse>> createCohort(@Valid @RequestBody CohortCreateRequest request) {
        CohortResponse response = cohortService.createCohort(request);
        return ResponseEntity.ok(ApiResponse.success("Tạo niên khóa thành công", response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Cập nhật niên khóa", description = "Cập nhật niên khóa (Admin)")
    public ResponseEntity<ApiResponse<CohortResponse>> updateCohort(@PathVariable Long id, @Valid @RequestBody CohortUpdateRequest request) {
        CohortResponse response = cohortService.updateCohort(id, request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật niên khóa thành công", response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Xóa niên khóa", description = "Xóa niên khóa (Admin)")
    public ResponseEntity<ApiResponse<Void>> deleteCohort(@PathVariable Long id) {
        cohortService.deleteCohort(id);
        return ResponseEntity.ok(ApiResponse.success("Xóa niên khóa thành công", null));
    }
}
