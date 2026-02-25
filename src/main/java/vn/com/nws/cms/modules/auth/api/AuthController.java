package vn.com.nws.cms.modules.auth.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import vn.com.nws.cms.common.dto.ApiResponse;
import vn.com.nws.cms.modules.auth.api.dto.*;
import vn.com.nws.cms.modules.auth.application.AuthCookieService;
import vn.com.nws.cms.modules.auth.application.AuthenticationService;
import vn.com.nws.cms.modules.auth.application.PasswordService;
import vn.com.nws.cms.modules.auth.application.RegistrationService;

import java.time.Duration;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "Quản lý xác thực và phân quyền")
public class AuthController {

    private final AuthenticationService authenticationService;
    private final RegistrationService registrationService;
    private final PasswordService passwordService;
    private final AuthCookieService authCookieService;

    @Value("${jwt.refresh-expiration}")
    private long refreshExpirationMs;

    @PostMapping("/login")
    @Operation(summary = "Đăng nhập", description = "Đăng nhập bằng username và password để lấy Access Token")
    public ResponseEntity<ApiResponse<TokenResponse>> login(HttpServletRequest httpRequest, @Valid @RequestBody LoginRequest loginRequest) {
        String deviceId = resolveOrCreateDeviceId(httpRequest);
        String ip = resolveIp(httpRequest);
        String userAgent = resolveUserAgent(httpRequest);

        AuthenticationService.LoginResult result = authenticationService.login(loginRequest, deviceId, ip, userAgent);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, authCookieService.deviceIdCookie(deviceId, Duration.ofDays(365)).toString())
                .header(HttpHeaders.SET_COOKIE, authCookieService.refreshTokenCookie(result.refreshToken(), Duration.ofMillis(refreshExpirationMs)).toString())
                .body(ApiResponse.success("Đăng nhập thành công", result.tokenResponse()));
    }

    @PostMapping("/register")
    @Operation(summary = "Đăng ký", description = "Đăng ký tài khoản mới")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> register(@Valid @RequestBody RegisterRequest registerRequest) {
        registrationService.register(registerRequest);
        return ResponseEntity.ok(ApiResponse.success("Đăng ký thành công", null));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Làm mới Token", description = "Làm mới Access Token bằng Refresh Token (cookie HttpOnly)")
    public ResponseEntity<ApiResponse<TokenResponse>> refreshToken(HttpServletRequest httpRequest, @RequestBody(required = false) RefreshTokenRequest request) {
        String refreshToken = request != null ? request.getRefreshToken() : null;
        if (refreshToken == null || refreshToken.isBlank()) {
            refreshToken = getCookieValue(httpRequest, authCookieService.refreshCookieName());
        }
        String deviceId = getCookieValue(httpRequest, authCookieService.deviceCookieName());
        String ip = resolveIp(httpRequest);
        String userAgent = resolveUserAgent(httpRequest);

        AuthenticationService.RefreshResult result = authenticationService.refresh(refreshToken, deviceId, ip, userAgent);
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, authCookieService.refreshTokenCookie(result.refreshToken(), Duration.ofMillis(refreshExpirationMs)).toString())
                .body(ApiResponse.success("Làm mới token thành công", result.tokenResponse()));
    }

    @PostMapping("/logout")
    @Operation(summary = "Đăng xuất", description = "Vô hiệu hóa Refresh Token hiện tại")
    public ResponseEntity<ApiResponse<Void>> logout(HttpServletRequest httpRequest, @RequestBody(required = false) RefreshTokenRequest request) {
        String refreshToken = request != null ? request.getRefreshToken() : null;
        if (refreshToken == null || refreshToken.isBlank()) {
            refreshToken = getCookieValue(httpRequest, authCookieService.refreshCookieName());
        }
        String ip = resolveIp(httpRequest);
        String userAgent = resolveUserAgent(httpRequest);

        authenticationService.logout(refreshToken, ip, userAgent);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, authCookieService.clearRefreshTokenCookie().toString())
                .body(ApiResponse.success("Đăng xuất thành công", null));
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Quên mật khẩu", description = "Yêu cầu đặt lại mật khẩu qua email")
    public ResponseEntity<ApiResponse<ForgotPasswordResponse>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        ForgotPasswordResponse response = passwordService.forgotPassword(request);
        return ResponseEntity.ok(ApiResponse.success("Hướng dẫn đặt lại mật khẩu đã được gửi đến email", response));
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Đặt lại mật khẩu", description = "Đặt lại mật khẩu mới bằng token")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        passwordService.resetPassword(request);
        return ResponseEntity.ok(ApiResponse.success("Đặt lại mật khẩu thành công", null));
    }

    private String resolveOrCreateDeviceId(HttpServletRequest request) {
        String existing = getCookieValue(request, authCookieService.deviceCookieName());
        if (existing != null && !existing.isBlank()) {
            return existing;
        }
        return UUID.randomUUID().toString();
    }

    private static String getCookieValue(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null || name == null) {
            return null;
        }
        for (Cookie cookie : cookies) {
            if (name.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }

    private static String resolveIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            String[] parts = forwardedFor.split(",");
            if (parts.length > 0) {
                return parts[0].trim();
            }
        }
        return request.getRemoteAddr();
    }

    private static String resolveUserAgent(HttpServletRequest request) {
        return request.getHeader("User-Agent");
    }
}
