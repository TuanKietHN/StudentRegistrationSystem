package vn.com.nws.cms.modules.auth.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.com.nws.cms.common.exception.BusinessException;
import vn.com.nws.cms.common.security.JwtProvider;
import vn.com.nws.cms.modules.academic.domain.model.Student;
import vn.com.nws.cms.modules.academic.domain.model.Teacher;
import vn.com.nws.cms.modules.academic.domain.repository.StudentRepository;
import vn.com.nws.cms.modules.academic.domain.repository.TeacherRepository;
import vn.com.nws.cms.modules.auth.api.dto.LoginRequest;
import vn.com.nws.cms.modules.auth.api.dto.TokenResponse;
import vn.com.nws.cms.modules.auth.domain.model.User;
import vn.com.nws.cms.modules.auth.domain.repository.PermissionRepository;
import vn.com.nws.cms.modules.auth.domain.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import java.time.Instant;
import java.time.Duration;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PermissionRepository permissionRepository;
    private final JwtProvider jwtProvider;
    private final AuthSessionService authSessionService;
    private final AuthRateLimitService authRateLimitService;
    private final AuthAuditService authAuditService;
    private final StudentRepository studentRepository;
    private final TeacherRepository teacherRepository;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    @Value("${cms.auth.login.max-failures}")
    private int maxLoginFailures;

    @Value("${cms.auth.login.lock-minutes}")
    private int loginLockMinutes;

    @Transactional
    public LoginResult login(LoginRequest loginRequest, String deviceId, String ip, String userAgent) {
        User preUser = userRepository.findByUsername(loginRequest.getUsername())
                .or(() -> userRepository.findByEmail(loginRequest.getUsername()))
                .orElse(null);
        if (preUser != null && isLocked(preUser)) {
            authAuditService.record(preUser.getUsername(), "LOGIN_BLOCKED_LOCKED", false, ip, userAgent, null, null);
            throw new BusinessException("Tài khoản đang bị khóa. Vui lòng thử lại sau.");
        }

        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));
        } catch (AuthenticationException e) {
            authRateLimitService.recordLoginFailureOrThrow(ip, loginRequest.getUsername());
            if (preUser != null) {
                handleLoginFailure(preUser, ip, userAgent);
            }
            authAuditService.record(preUser != null ? preUser.getUsername() : null, "LOGIN_FAILED", false, ip, userAgent, null, null);
            throw new BusinessException("Sai thông tin đăng nhập");
        }

        String principalUsername = authentication.getName();
        User user = userRepository.findByUsername(principalUsername)
                .orElseThrow(() -> new BusinessException("User not found"));

        Authentication authWithAuthorities = buildAuthentication(user);
        SecurityContextHolder.getContext().setAuthentication(authWithAuthorities);
        String jwt = jwtProvider.generateToken(authWithAuthorities);

        clearLoginFailure(user, ip, userAgent);
        authRateLimitService.clearLoginCounters(ip, principalUsername);
        authRateLimitService.clearLoginCounters(ip, loginRequest.getUsername());

        AuthSessionService.SessionIssueResult session = authSessionService.issue(user.getUsername(), deviceId, ip, userAgent);
        TokenResponse tokenResponse = buildTokenResponse(jwt, null, user);
        authAuditService.record(user.getUsername(), "LOGIN_SUCCESS", true, ip, userAgent, session.sessionId(), null);

        return new LoginResult(tokenResponse, session.sessionId(), session.refreshToken());
    }

    public RefreshResult refresh(String refreshToken, String deviceId, String ip, String userAgent) {
        AuthSessionService.SessionData preSession = authSessionService.findByRefreshToken(refreshToken);
        AuthSessionService.SessionRotateResult rotated;
        try {
            rotated = authSessionService.rotate(refreshToken, deviceId, ip, userAgent);
        } catch (BusinessException e) {
            if (preSession != null && e.getMessage() != null && e.getMessage().contains("reuse")) {
                userRepository.findByUsername(preSession.username()).ifPresent(user -> {
                    lockUser(user);
                    authAuditService.record(user.getUsername(), "REFRESH_REUSE_DETECTED", false, ip, userAgent, preSession.sessionId(), null);
                });
            }
            throw e;
        }
        User user = userRepository.findByUsername(rotated.username())
                .orElseThrow(() -> new BusinessException("User not found"));
        if (isLocked(user)) {
            authSessionService.revokeAll(user.getUsername());
            authAuditService.record(user.getUsername(), "REFRESH_BLOCKED_LOCKED", false, ip, userAgent, rotated.sessionId(), null);
            throw new BusinessException("Tài khoản đang bị khóa. Vui lòng thử lại sau.");
        }

        Authentication auth = new UsernamePasswordAuthenticationToken(
                user.getUsername(),
                null,
                buildAuthorities(user)
        );
        String newAccessToken = jwtProvider.generateToken(auth);
        TokenResponse tokenResponse = buildTokenResponse(newAccessToken, null, user);
        authAuditService.record(user.getUsername(), "REFRESH_SUCCESS", true, ip, userAgent, rotated.sessionId(), null);

        return new RefreshResult(tokenResponse, rotated.sessionId(), rotated.refreshToken());
    }

    public void logout(String refreshToken, Authentication authentication, String ip, String userAgent) {
        if (refreshToken != null && !refreshToken.isBlank()) {
            authSessionService.revokeByRefreshToken(refreshToken);
        }

        if (authentication instanceof JwtAuthenticationToken jwtAuth) {
            Jwt jwt = jwtAuth.getToken();
            String jti = jwt.getId();
            Instant expiresAt = jwt.getExpiresAt();
            if (jti != null && expiresAt != null) {
                long ttlMs = Duration.between(Instant.now(), expiresAt).toMillis();
                if (ttlMs > 0) {
                    authSessionService.blacklist(jti, ttlMs);
                }
            }
        }

        authAuditService.record(null, "LOGOUT", true, ip, userAgent, null, null);
    }

    public void revokeAllSessions(String username) {
        authSessionService.revokeAll(username);
    }

    private TokenResponse buildTokenResponse(String accessToken, String refreshToken, User user) {
        Long studentId = studentRepository.findByUserId(user.getId())
                .map(Student::getId)
                .orElse(null);
        Long teacherId = teacherRepository.findByUserId(user.getId())
                .map(Teacher::getId)
                .orElse(null);

        return TokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtExpiration / 1000)
                .username(user.getUsername())
                .role(user.getRoles().stream().map(Enum::name).collect(Collectors.joining(",")))
                .studentId(studentId)
                .teacherId(teacherId)
                .build();
    }

    private void handleLoginFailure(User user, String ip, String userAgent) {
        int failures = user.getFailedLoginAttempts() + 1;
        if (failures >= maxLoginFailures) {
            user.setFailedLoginAttempts(0);
            user.setLockUntil(LocalDateTime.now().plusMinutes(loginLockMinutes));
            userRepository.save(user);
            authAuditService.record(user.getUsername(), "LOGIN_LOCKED", false, ip, userAgent, null, null);
            return;
        }
        user.setFailedLoginAttempts(failures);
        userRepository.save(user);
    }

    private void clearLoginFailure(User user, String ip, String userAgent) {
        user.setFailedLoginAttempts(0);
        user.setLockUntil(null);
        user.setLastLoginAt(LocalDateTime.now());
        user.setLastLoginIp(ip);
        user.setLastLoginUserAgent(userAgent);
        userRepository.save(user);
    }

    private void lockUser(User user) {
        user.setFailedLoginAttempts(0);
        user.setLockUntil(LocalDateTime.now().plusMinutes(loginLockMinutes));
        userRepository.save(user);
    }

    private boolean isLocked(User user) {
        return user.getLockUntil() != null && user.getLockUntil().isAfter(LocalDateTime.now());
    }

    private Authentication buildAuthentication(User user) {
        return new UsernamePasswordAuthenticationToken(user.getUsername(), null, buildAuthorities(user));
    }

    private List<SimpleGrantedAuthority> buildAuthorities(User user) {
        Set<String> roleNames = user.getRoles().stream().map(r -> r.authority()).collect(Collectors.toSet());
        List<String> permissionNames = permissionRepository.findPermissionNamesByRoleNames(roleNames);
        Set<String> all = new LinkedHashSet<>();
        all.addAll(roleNames);
        all.addAll(permissionNames);
        return all.stream().map(SimpleGrantedAuthority::new).toList();
    }

    public record LoginResult(TokenResponse tokenResponse, String sessionId, String refreshToken) {}

    public record RefreshResult(TokenResponse tokenResponse, String sessionId, String refreshToken) {}
}
