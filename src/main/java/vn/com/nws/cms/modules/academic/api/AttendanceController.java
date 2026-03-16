package vn.com.nws.cms.modules.academic.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import vn.com.nws.cms.common.dto.ApiResponse;
import vn.com.nws.cms.modules.academic.api.dto.*;
import vn.com.nws.cms.modules.academic.application.AttendanceService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/attendance")
@RequiredArgsConstructor
@Tag(name = "Academic - Attendance", description = "Quản lý điểm danh")
public class AttendanceController {

    private final AttendanceService attendanceService;

    @PostMapping("/sessions/open")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    @Operation(summary = "Mở điểm danh", description = "Teacher/Admin mở một buổi điểm danh. Chỉ được điểm danh 'PRESENT' trong 15 phút.")
    public ResponseEntity<ApiResponse<AttendanceSessionResponse>> openSession(Authentication authentication, @Valid @RequestBody AttendanceSessionOpenRequest request) {
        boolean isAdmin = authentication.getAuthorities().stream().anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
        boolean isTeacher = authentication.getAuthorities().stream().anyMatch(a -> "ROLE_TEACHER".equals(a.getAuthority()));
        AttendanceSessionResponse response = attendanceService.openSession(authentication.getName(), isAdmin, isTeacher, request);
        return ResponseEntity.ok(ApiResponse.success("Mở điểm danh thành công", response));
    }

    @GetMapping("/cohorts/{cohortId}/sessions")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    @Operation(summary = "Danh sách buổi điểm danh", description = "Danh sách các buổi điểm danh theo lớp học phần")
    public ResponseEntity<ApiResponse<List<AttendanceSessionResponse>>> listCohortSessions(Authentication authentication, @PathVariable Long cohortId) {
        boolean isAdmin = authentication.getAuthorities().stream().anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
        boolean isTeacher = authentication.getAuthorities().stream().anyMatch(a -> "ROLE_TEACHER".equals(a.getAuthority()));
        List<AttendanceSessionResponse> response = attendanceService.listCohortSessions(cohortId, authentication.getName(), isAdmin, isTeacher);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách buổi điểm danh thành công", response));
    }

    @GetMapping("/sessions/{sessionId}/roster")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    @Operation(summary = "Roster điểm danh", description = "Danh sách sinh viên và trạng thái điểm danh của một buổi")
    public ResponseEntity<ApiResponse<AttendanceSessionRosterResponse>> getSessionRoster(Authentication authentication, @PathVariable Long sessionId) {
        boolean isAdmin = authentication.getAuthorities().stream().anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
        boolean isTeacher = authentication.getAuthorities().stream().anyMatch(a -> "ROLE_TEACHER".equals(a.getAuthority()));
        AttendanceSessionRosterResponse response = attendanceService.getSessionRoster(sessionId, authentication.getName(), isAdmin, isTeacher);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách điểm danh thành công", response));
    }

    @PutMapping("/sessions/{sessionId}/records/{enrollmentId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    @Operation(summary = "Cập nhật điểm danh", description = "Cập nhật trạng thái điểm danh cho 1 sinh viên trong buổi")
    public ResponseEntity<ApiResponse<AttendanceSessionRosterResponse>> markAttendance(
            Authentication authentication,
            @PathVariable Long sessionId,
            @PathVariable Long enrollmentId,
            @Valid @RequestBody AttendanceMarkRequest request
    ) {
        boolean isAdmin = authentication.getAuthorities().stream().anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
        boolean isTeacher = authentication.getAuthorities().stream().anyMatch(a -> "ROLE_TEACHER".equals(a.getAuthority()));
        AttendanceSessionRosterResponse response = attendanceService.markAttendance(sessionId, enrollmentId, authentication.getName(), isAdmin, isTeacher, request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật điểm danh thành công", response));
    }
}
