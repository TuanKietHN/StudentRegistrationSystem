package vn.com.nws.cms.modules.academic.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import vn.com.nws.cms.common.dto.ApiResponse;
import vn.com.nws.cms.modules.academic.api.dto.*;
import vn.com.nws.cms.modules.academic.application.AcademicProgramService;

import java.util.List;
import org.springframework.http.ResponseEntity;

@RestController
@RequestMapping("/api/v1/academic-programs")
@RequiredArgsConstructor
@Tag(name = "Academic Programs", description = "API quản lý chương trình đào tạo")
public class AcademicProgramController {
    private final AcademicProgramService academicProgramService;

    @PostMapping
    @PreAuthorize("hasAuthority('ACADEMIC_PROGRAM:CREATE')")
    @Operation(summary = "Tạo mới chương trình đào tạo")
    public ResponseEntity<ApiResponse<AcademicProgramResponse>> create(@Valid @RequestBody AcademicProgramCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Tạo mới chương trình đào tạo thành công", academicProgramService.create(request)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ACADEMIC_PROGRAM:UPDATE')")
    @Operation(summary = "Cập nhật chương trình đào tạo")
    public ResponseEntity<ApiResponse<AcademicProgramResponse>> update(@PathVariable Long id, @Valid @RequestBody AcademicProgramUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Cập nhật chương trình đào tạo thành công", academicProgramService.update(id, request)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ACADEMIC_PROGRAM:READ')")
    @Operation(summary = "Lấy thông tin chi tiết chương trình đào tạo")
    public ResponseEntity<ApiResponse<AcademicProgramResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Lấy thông tin chương trình đào tạo thành công", academicProgramService.getById(id)));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ACADEMIC_PROGRAM:READ')")
    @Operation(summary = "Lấy danh sách tất cả chương trình đào tạo")
    public ResponseEntity<ApiResponse<List<AcademicProgramResponse>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách chương trình đào tạo thành công", academicProgramService.getAll()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ACADEMIC_PROGRAM:DELETE')")
    @Operation(summary = "Xóa chương trình đào tạo")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        academicProgramService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Xóa chương trình đào tạo thành công", null));
    }

    @PostMapping("/{id}/subjects")
    @PreAuthorize("hasAuthority('ACADEMIC_PROGRAM:UPDATE')")
    @Operation(summary = "Thêm môn học vào chương trình")
    public ResponseEntity<ApiResponse<ProgramSubjectResponse>> addSubject(@PathVariable Long id, @Valid @RequestBody ProgramSubjectRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Thêm môn học vào chương trình thành công", academicProgramService.addSubject(id, request)));
    }

    @DeleteMapping("/subjects/{subjectId}")
    @PreAuthorize("hasAuthority('ACADEMIC_PROGRAM:UPDATE')")
    @Operation(summary = "Xóa môn học khỏi chương trình")
    public ResponseEntity<ApiResponse<Void>> removeSubject(@PathVariable Long subjectId) {
        academicProgramService.removeSubject(subjectId);
        return ResponseEntity.ok(ApiResponse.success("Xóa môn học khỏi chương trình thành công", null));
    }

    @GetMapping("/{id}/subjects")
    @PreAuthorize("hasAuthority('ACADEMIC_PROGRAM:READ')")
    @Operation(summary = "Lấy danh sách môn học của chương trình")
    public ResponseEntity<ApiResponse<List<ProgramSubjectResponse>>> getSubjects(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách môn học của chương trình thành công", academicProgramService.getSubjects(id)));
    }
}
