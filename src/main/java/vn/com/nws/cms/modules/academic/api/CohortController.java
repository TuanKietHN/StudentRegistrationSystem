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
@Tag(name = "Academic - Cohort", description = "Quản lý lớp học phần (cohort)")
public class CohortController {

    private final CohortService cohortService;

    @GetMapping
    @Operation(summary = "Danh sách lớp học phần", description = "Lấy danh sách lớp học phần có phân trang và lọc")
    public ResponseEntity<ApiResponse<PageResponse<CohortResponse>>> getCohorts(
            @Parameter(description = "Từ khóa tìm kiếm (tên, mã)") @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long semesterId,
            @RequestParam(required = false) Long classId,
            @RequestParam(required = false) Long teacherId,
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        CohortFilterRequest request = new CohortFilterRequest();
        request.setKeyword(keyword);
        request.setSemesterId(semesterId);
        request.setClassId(classId);
        request.setTeacherId(teacherId);
        request.setActive(active);
        if (status != null && !status.isBlank()) {
            request.setStatus(Enum.valueOf(vn.com.nws.cms.modules.academic.domain.enums.CohortLifecycleStatus.class, status));
        }
        request.setPage(page);
        request.setSize(size);

        PageResponse<CohortResponse> response = cohortService.getCohorts(request);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách lớp học phần thành công", response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Chi tiết lớp học phần", description = "Lấy thông tin chi tiết lớp học phần")
    public ResponseEntity<ApiResponse<CohortResponse>> getCohortById(@PathVariable Long id) {
        CohortResponse response = cohortService.getCohortById(id);
        return ResponseEntity.ok(ApiResponse.success("Lấy thông tin lớp học phần thành công", response));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Tạo lớp học phần", description = "Tạo lớp học phần (Admin)")
    public ResponseEntity<ApiResponse<CohortResponse>> createCohort(@Valid @RequestBody CohortCreateRequest request) {
        CohortResponse response = cohortService.createCohort(request);
        return ResponseEntity.ok(ApiResponse.success("Tạo lớp học phần thành công", response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Cập nhật lớp học phần", description = "Cập nhật lớp học phần (Admin)")
    public ResponseEntity<ApiResponse<CohortResponse>> updateCohort(@PathVariable Long id, @Valid @RequestBody CohortUpdateRequest request) {
        CohortResponse response = cohortService.updateCohort(id, request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật lớp học phần thành công", response));
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Hủy lớp học phần", description = "Hủy lớp học phần (Admin)")
    public ResponseEntity<ApiResponse<CohortResponse>> cancelCohort(@PathVariable Long id, @RequestBody(required = false) CohortCancelRequest request) {
        CohortResponse response = cohortService.cancelCohort(id, request);
        return ResponseEntity.ok(ApiResponse.success("Hủy lớp học phần thành công", response));
    }

    @PostMapping("/merge")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Dồn lớp", description = "Dồn nhiều lớp học phần vào 1 lớp (Admin)")
    public ResponseEntity<ApiResponse<CohortResponse>> mergeCohorts(@Valid @RequestBody CohortMergeRequest request) {
        CohortResponse response = cohortService.mergeCohorts(request);
        return ResponseEntity.ok(ApiResponse.success("Dồn lớp thành công", response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Xóa lớp học phần", description = "Xóa lớp học phần (Admin)")
    public ResponseEntity<ApiResponse<Void>> deleteCohort(@PathVariable Long id) {
        cohortService.deleteCohort(id);
        return ResponseEntity.ok(ApiResponse.success("Xóa lớp học phần thành công", null));
    }

    @GetMapping("/{id}/time-slots")
    @Operation(summary = "Lịch học", description = "Lấy lịch học của lớp học phần")
    public ResponseEntity<ApiResponse<List<CohortTimeSlotResponse>>> getTimeSlots(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Lấy lịch học thành công", cohortService.getCohortTimeSlots(id)));
    }

    @PutMapping("/{id}/time-slots")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Cập nhật lịch học", description = "Cập nhật lịch học của lớp học phần (Admin)")
    public ResponseEntity<ApiResponse<Void>> updateTimeSlots(@PathVariable Long id, @Valid @RequestBody List<CohortTimeSlotRequest> request) {
        cohortService.replaceCohortTimeSlots(id, request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật lịch học thành công", null));
    }
}

