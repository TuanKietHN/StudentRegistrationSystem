package vn.com.nws.cms.modules.academic.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import vn.com.nws.cms.common.dto.ApiResponse;
import vn.com.nws.cms.common.dto.PageResponse;
import vn.com.nws.cms.modules.academic.api.dto.*;
import vn.com.nws.cms.modules.academic.application.SectionService;
import vn.com.nws.cms.modules.auth.domain.model.User;
import vn.com.nws.cms.modules.auth.domain.repository.UserRepository;

import java.util.List;

@RestController
@RequestMapping("/api/v1/sections")
@RequiredArgsConstructor
@Tag(name = "Academic - Section", description = "Quản lý lớp học phần (section)")
public class SectionController {

    private final SectionService sectionService;
    private final UserRepository userRepository;

    @GetMapping
    @Operation(summary = "Danh sách lớp học phần", description = "Lấy danh sách lớp học phần có phân trang và lọc")
    public ResponseEntity<ApiResponse<PageResponse<SectionResponse>>> getSections(
            @Parameter(description = "Từ khóa tìm kiếm (tên, mã)") @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long semesterId,
            @RequestParam(required = false) Long subjectId,
            @RequestParam(required = false) Long teacherId,
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication
    ) {
        if (authentication != null && authentication.isAuthenticated()) {
            boolean isAdmin = authentication.getAuthorities().stream().anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
            boolean isTeacher = authentication.getAuthorities().stream().anyMatch(a -> "ROLE_TEACHER".equals(a.getAuthority()));
            if (isTeacher && !isAdmin) {
                User currentUser = userRepository.findByUsername(authentication.getName())
                        .or(() -> userRepository.findByEmail(authentication.getName()))
                        .orElse(null);
                if (currentUser != null) {
                    teacherId = currentUser.getId();
                }
            }
        }

        SectionFilterRequest request = new SectionFilterRequest();
        request.setKeyword(keyword);
        request.setSemesterId(semesterId);
        request.setSubjectId(subjectId);
        request.setTeacherId(teacherId);
        request.setActive(active);
        if (status != null && !status.isBlank()) {
            request.setStatus(Enum.valueOf(vn.com.nws.cms.modules.academic.domain.enums.SectionLifecycleStatus.class, status));
        }
        request.setPage(page);
        request.setSize(size);

        PageResponse<SectionResponse> response = sectionService.getSections(request);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách lớp học phần thành công", response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Chi tiết lớp học phần", description = "Lấy thông tin chi tiết lớp học phần")
    public ResponseEntity<ApiResponse<SectionResponse>> getSectionById(@PathVariable Long id) {
        SectionResponse response = sectionService.getSectionById(id);
        return ResponseEntity.ok(ApiResponse.success("Lấy thông tin lớp học phần thành công", response));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Tạo lớp học phần", description = "Tạo lớp học phần (Admin)")
    public ResponseEntity<ApiResponse<SectionResponse>> createSection(@Valid @RequestBody SectionCreateRequest request) {
        SectionResponse response = sectionService.createSection(request);
        return ResponseEntity.ok(ApiResponse.success("Tạo lớp học phần thành công", response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Cập nhật lớp học phần", description = "Cập nhật lớp học phần (Admin)")
    public ResponseEntity<ApiResponse<SectionResponse>> updateSection(@PathVariable Long id, @Valid @RequestBody SectionUpdateRequest request) {
        SectionResponse response = sectionService.updateSection(id, request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật lớp học phần thành công", response));
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Hủy lớp học phần", description = "Hủy lớp học phần (Admin)")
    public ResponseEntity<ApiResponse<SectionResponse>> cancelSection(@PathVariable Long id, @RequestBody(required = false) SectionCancelRequest request) {
        SectionResponse response = sectionService.cancelSection(id, request);
        return ResponseEntity.ok(ApiResponse.success("Hủy lớp học phần thành công", response));
    }

    @PostMapping("/merge")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Dồn lớp", description = "Dồn nhiều lớp học phần vào 1 lớp (Admin)")
    public ResponseEntity<ApiResponse<SectionResponse>> mergeSections(@Valid @RequestBody SectionMergeRequest request) {
        SectionResponse response = sectionService.mergeSections(request);
        return ResponseEntity.ok(ApiResponse.success("Dồn lớp thành công", response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Xóa lớp học phần", description = "Xóa lớp học phần (Admin)")
    public ResponseEntity<ApiResponse<Void>> deleteSection(@PathVariable Long id) {
        sectionService.deleteSection(id);
        return ResponseEntity.ok(ApiResponse.success("Xóa lớp học phần thành công", null));
    }

    @GetMapping("/{id}/time-slots")
    @Operation(summary = "Lịch học", description = "Lấy lịch học của lớp học phần")
    public ResponseEntity<ApiResponse<List<SectionTimeSlotResponse>>> getTimeSlots(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Lấy lịch học thành công", sectionService.getSectionTimeSlots(id)));
    }

    @PutMapping("/{id}/time-slots")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Cập nhật lịch học", description = "Cập nhật lịch học của lớp học phần (Admin)")
    public ResponseEntity<ApiResponse<Void>> updateTimeSlots(@PathVariable Long id, @Valid @RequestBody List<SectionTimeSlotRequest> request) {
        sectionService.replaceSectionTimeSlots(id, request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật lịch học thành công", null));
    }
}
