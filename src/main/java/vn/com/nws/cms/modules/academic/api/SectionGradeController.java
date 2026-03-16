package vn.com.nws.cms.modules.academic.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import vn.com.nws.cms.common.dto.ApiResponse;
import vn.com.nws.cms.modules.academic.api.dto.GradesImportResultResponse;
import vn.com.nws.cms.modules.academic.api.dto.SectionGradeResponse;
import vn.com.nws.cms.modules.academic.api.dto.SectionGradeUpdateRequest;
import vn.com.nws.cms.modules.academic.application.SectionGradeService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/sections")
@RequiredArgsConstructor
@Tag(name = "Academic - Section Grade", description = "Quản lý điểm số lớp học phần")
public class SectionGradeController {

    private final SectionGradeService sectionGradeService;

    @GetMapping("/{sectionId}/grades")
    @PreAuthorize("hasAuthority('ENROLLMENT:READ') and (hasRole('ADMIN') or hasRole('TEACHER'))")
    @Operation(summary = "Danh sách sinh viên và điểm trong lớp", description = "Lấy danh sách điểm số sinh viên lớp học phần")
    public ResponseEntity<ApiResponse<List<SectionGradeResponse>>> getSectionGrades(Authentication authentication, @PathVariable Long sectionId) {
        boolean isAdmin = authentication.getAuthorities().stream().anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
        boolean isTeacher = authentication.getAuthorities().stream().anyMatch(a -> "ROLE_TEACHER".equals(a.getAuthority()));
        List<SectionGradeResponse> response = sectionGradeService.getSectionGrades(sectionId, authentication.getName(), isAdmin, isTeacher);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách điểm số thành công", response));
    }

    @PutMapping("/grades/{enrollmentId}")
    @PreAuthorize("hasAuthority('ENROLLMENT:UPDATE') and (hasRole('ADMIN') or hasRole('TEACHER'))")
    @Operation(summary = "Cập nhật điểm đăng ký", description = "Cập nhật điểm số (Admin/Teacher)")
    public ResponseEntity<ApiResponse<SectionGradeResponse>> updateGrade(
            @PathVariable Long enrollmentId,
            Authentication authentication,
            @Valid @RequestBody SectionGradeUpdateRequest request) {
        boolean isAdmin = authentication.getAuthorities().stream().anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
        boolean isTeacher = authentication.getAuthorities().stream().anyMatch(a -> "ROLE_TEACHER".equals(a.getAuthority()));
        SectionGradeResponse response = sectionGradeService.updateGrade(enrollmentId, authentication.getName(), isAdmin, isTeacher, request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật điểm thành công", response));
    }

    @PostMapping(value = "/{sectionId}/grades/import", consumes = {"multipart/form-data"})
    @PreAuthorize("hasAuthority('ENROLLMENT:UPDATE') and (hasRole('ADMIN') or hasRole('TEACHER'))")
    @Operation(summary = "Import điểm từ Excel", description = "Import điểm vào lớp học phần. Teacher chỉ được import 1 lần.")
    public ResponseEntity<ApiResponse<GradesImportResultResponse>> importGrades(
            Authentication authentication,
            @PathVariable Long sectionId,
            @RequestPart("file") MultipartFile file
    ) {
        boolean isAdmin = authentication.getAuthorities().stream().anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
        boolean isTeacher = authentication.getAuthorities().stream().anyMatch(a -> "ROLE_TEACHER".equals(a.getAuthority()));
        GradesImportResultResponse response = sectionGradeService.importSectionGrades(sectionId, authentication.getName(), isAdmin, isTeacher, file);
        return ResponseEntity.ok(ApiResponse.success("Import điểm thành công", response));
    }
}
