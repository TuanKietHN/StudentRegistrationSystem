package vn.com.nws.cms.modules.auth.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.com.nws.cms.common.dto.ApiResponse;
import vn.com.nws.cms.common.exception.BusinessException;
import vn.com.nws.cms.modules.auth.api.dto.AuthSessionInfo;
import vn.com.nws.cms.modules.auth.application.AuthSessionService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/auth/sessions")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "Quản lý xác thực và phân quyền")
public class AuthSessionController {

    private final AuthSessionService authSessionService;

    @GetMapping
    @Operation(summary = "Danh sách phiên đăng nhập", description = "Liệt kê các phiên theo thiết bị của tài khoản hiện tại")
    public ResponseEntity<ApiResponse<List<AuthSessionInfo>>> list(Authentication authentication) {
        String username = authentication.getName();
        List<AuthSessionInfo> sessions = authSessionService.listSessions(username).stream()
                .map(s -> AuthSessionInfo.builder()
                        .sessionId(s.sessionId())
                        .deviceId(s.deviceId())
                        .createdAtMs(s.createdAtMs())
                        .lastUsedAtMs(s.lastUsedAtMs())
                        .ip(s.ip())
                        .userAgent(s.userAgent())
                        .build())
                .toList();

        return ResponseEntity.ok(ApiResponse.success("Danh sách phiên đăng nhập", sessions));
    }

    @DeleteMapping("/{sessionId}")
    @Operation(summary = "Thu hồi một phiên", description = "Thu hồi một phiên theo sessionId (tài khoản hiện tại)")
    public ResponseEntity<ApiResponse<Void>> revoke(Authentication authentication, @PathVariable String sessionId) {
        String username = authentication.getName();
        AuthSessionService.SessionData session = authSessionService.findBySessionId(sessionId);
        if (session == null || !username.equals(session.username())) {
            throw new BusinessException("Không tìm thấy phiên đăng nhập");
        }
        authSessionService.revokeBySessionId(sessionId);
        return ResponseEntity.ok(ApiResponse.success("Thu hồi phiên đăng nhập thành công", null));
    }

    @DeleteMapping
    @Operation(summary = "Thu hồi tất cả phiên", description = "Thu hồi tất cả phiên đăng nhập của tài khoản hiện tại")
    public ResponseEntity<ApiResponse<Void>> revokeAll(Authentication authentication) {
        String username = authentication.getName();
        authSessionService.revokeAll(username);
        return ResponseEntity.ok(ApiResponse.success("Thu hồi tất cả phiên đăng nhập thành công", null));
    }
}

