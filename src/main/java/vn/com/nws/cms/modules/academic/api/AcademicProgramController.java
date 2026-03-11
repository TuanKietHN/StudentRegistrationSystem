package vn.com.nws.cms.modules.academic.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import vn.com.nws.cms.modules.academic.api.dto.*;
import vn.com.nws.cms.modules.academic.application.AcademicProgramService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/academic-programs")
@RequiredArgsConstructor
@Tag(name = "Academic Programs", description = "API quản lý chương trình đào tạo")
public class AcademicProgramController {
    private final AcademicProgramService academicProgramService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('ACADEMIC_PROGRAM:CREATE')")
    @Operation(summary = "Tạo mới chương trình đào tạo")
    public AcademicProgramResponse create(@Valid @RequestBody AcademicProgramCreateRequest request) {
        return academicProgramService.create(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ACADEMIC_PROGRAM:UPDATE')")
    @Operation(summary = "Cập nhật chương trình đào tạo")
    public AcademicProgramResponse update(@PathVariable Long id, @Valid @RequestBody AcademicProgramUpdateRequest request) {
        return academicProgramService.update(id, request);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ACADEMIC_PROGRAM:READ')")
    @Operation(summary = "Lấy thông tin chi tiết chương trình đào tạo")
    public AcademicProgramResponse getById(@PathVariable Long id) {
        return academicProgramService.getById(id);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ACADEMIC_PROGRAM:READ')")
    @Operation(summary = "Lấy danh sách tất cả chương trình đào tạo")
    public List<AcademicProgramResponse> getAll() {
        return academicProgramService.getAll();
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('ACADEMIC_PROGRAM:DELETE')")
    @Operation(summary = "Xóa chương trình đào tạo")
    public void delete(@PathVariable Long id) {
        academicProgramService.delete(id);
    }

    @PostMapping("/{id}/subjects")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('ACADEMIC_PROGRAM:UPDATE')")
    @Operation(summary = "Thêm môn học vào chương trình")
    public ProgramSubjectResponse addSubject(@PathVariable Long id, @Valid @RequestBody ProgramSubjectRequest request) {
        return academicProgramService.addSubject(id, request);
    }

    @DeleteMapping("/subjects/{subjectId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('ACADEMIC_PROGRAM:UPDATE')")
    @Operation(summary = "Xóa môn học khỏi chương trình")
    public void removeSubject(@PathVariable Long subjectId) {
        academicProgramService.removeSubject(subjectId);
    }

    @GetMapping("/{id}/subjects")
    @PreAuthorize("hasAuthority('ACADEMIC_PROGRAM:READ')")
    @Operation(summary = "Lấy danh sách môn học của chương trình")
    public List<ProgramSubjectResponse> getSubjects(@PathVariable Long id) {
        return academicProgramService.getSubjects(id);
    }
}
