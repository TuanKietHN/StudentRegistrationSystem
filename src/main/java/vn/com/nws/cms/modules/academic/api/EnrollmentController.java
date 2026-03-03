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
import vn.com.nws.cms.modules.academic.api.dto.*;
import vn.com.nws.cms.modules.academic.application.EnrollmentService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/enrollments")
@RequiredArgsConstructor
@Tag(name = "Academic - Enrollment", description = "Quản lý đăng ký học phần")
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Đăng ký học phần (Admin)", description = "Admin đăng ký lớp học phần cho sinh viên")
    public ResponseEntity<ApiResponse<EnrollmentResponse>> enrollStudent(@Valid @RequestBody EnrollmentCreateRequest request) {
        EnrollmentResponse response = enrollmentService.enrollStudent(request);
        return ResponseEntity.ok(ApiResponse.success("Đăng ký học phần thành công", response));
    }

    @PostMapping("/self")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "Đăng ký học phần (Sinh viên)", description = "Sinh viên tự đăng ký vào lớp học phần trong thời gian mở đăng ký")
    public ResponseEntity<ApiResponse<EnrollmentResponse>> enrollSelf(Authentication authentication, @Valid @RequestBody EnrollmentSelfRequest request) {
        EnrollmentResponse response = enrollmentService.enrollSelf(authentication.getName(), request);
        return ResponseEntity.ok(ApiResponse.success("Đăng ký học phần thành công", response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    @Operation(summary = "Cập nhật đăng ký", description = "Cập nhật trạng thái hoặc điểm số (Admin/Teacher)")
    public ResponseEntity<ApiResponse<EnrollmentResponse>> updateEnrollment(
            @PathVariable Long id,
            Authentication authentication,
            @Valid @RequestBody EnrollmentUpdateRequest request) {
        boolean isAdmin = authentication.getAuthorities().stream().anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
        boolean isTeacher = authentication.getAuthorities().stream().anyMatch(a -> "ROLE_TEACHER".equals(a.getAuthority()));
        EnrollmentResponse response = enrollmentService.updateEnrollment(id, authentication.getName(), isAdmin, isTeacher, request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật đăng ký thành công", response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STUDENT')")
    @Operation(summary = "Hủy đăng ký", description = "Sinh viên hủy đăng ký trong thời gian mở đăng ký; Admin có thể hủy")
    public ResponseEntity<ApiResponse<Void>> cancelEnrollment(Authentication authentication, @PathVariable Long id) {
        boolean isAdmin = authentication.getAuthorities().stream().anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
        enrollmentService.cancelEnrollment(id, authentication.getName(), isAdmin);
        return ResponseEntity.ok(ApiResponse.success("Hủy đăng ký thành công", null));
    }

    @GetMapping("/student/{studentId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Danh sách học phần của sinh viên", description = "Lấy danh sách các lớp học phần sinh viên đã đăng ký")
    public ResponseEntity<ApiResponse<List<EnrollmentResponse>>> getStudentEnrollments(@PathVariable Long studentId) {
        List<EnrollmentResponse> response = enrollmentService.getStudentEnrollments(studentId);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách học phần thành công", response));
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "Danh sách học phần của tôi", description = "Lấy danh sách các lớp học phần tôi đã đăng ký")
    public ResponseEntity<ApiResponse<List<EnrollmentResponse>>> getMyEnrollments(Authentication authentication) {
        List<EnrollmentResponse> response = enrollmentService.getMyEnrollments(authentication.getName());
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách học phần thành công", response));
    }

    @GetMapping("/sections/{sectionId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    @Operation(summary = "Danh sách sinh viên trong lớp", description = "Lấy danh sách sinh viên đăng ký lớp học phần")
    public ResponseEntity<ApiResponse<List<EnrollmentResponse>>> getSectionEnrollments(Authentication authentication, @PathVariable Long sectionId) {
        boolean isAdmin = authentication.getAuthorities().stream().anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
        boolean isTeacher = authentication.getAuthorities().stream().anyMatch(a -> "ROLE_TEACHER".equals(a.getAuthority()));
        List<EnrollmentResponse> response = enrollmentService.getSectionEnrollments(sectionId, authentication.getName(), isAdmin, isTeacher);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách sinh viên thành công", response));
    }

    @PostMapping(value = "/sections/{sectionId}/grades/import", consumes = {"multipart/form-data"})
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    @Operation(summary = "Import điểm từ Excel", description = "Import điểm (quá trình/thi) vào lớp học phần. Teacher chỉ được import 1 lần (điểm đã khóa sẽ bị bỏ qua).")
    public ResponseEntity<ApiResponse<GradesImportResultResponse>> importGrades(
            Authentication authentication,
            @PathVariable Long sectionId,
            @RequestPart("file") MultipartFile file
    ) {
        boolean isAdmin = authentication.getAuthorities().stream().anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
        boolean isTeacher = authentication.getAuthorities().stream().anyMatch(a -> "ROLE_TEACHER".equals(a.getAuthority()));
        GradesImportResultResponse response = enrollmentService.importSectionGrades(sectionId, authentication.getName(), isAdmin, isTeacher, file);
        return ResponseEntity.ok(ApiResponse.success("Import điểm thành công", response));
    }
}
