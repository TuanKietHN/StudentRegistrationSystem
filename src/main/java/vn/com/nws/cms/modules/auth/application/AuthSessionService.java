package vn.com.nws.cms.modules.auth.application;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import vn.com.nws.cms.common.exception.BusinessException;
import vn.com.nws.cms.modules.auth.domain.repository.SessionRepository;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthSessionService {

    private final SessionRepository sessionRepository;

    @Value("${jwt.refresh-expiration}")
    private long refreshExpirationMs;

    @Value("${cms.auth.max-sessions-per-user}")
    private long maxSessionsPerUser;

    public SessionIssueResult issue(String username, String deviceId, String ip, String userAgent) {
        evictIfNeeded(username);
        replaceDeviceSessionIfNeeded(username, deviceId);

        long now = System.currentTimeMillis();
        SessionRepository.SessionIssue issued = sessionRepository.issue(username, deviceId, now, ip, userAgent, refreshExpirationMs);
        sessionRepository.addUserSession(username, issued.sessionId(), now, refreshExpirationMs);
        sessionRepository.setActiveSessionIdForDevice(username, deviceId, issued.sessionId(), refreshExpirationMs);

        return new SessionIssueResult(issued.sessionId(), issued.refreshToken());
    }

    public SessionRotateResult rotate(String refreshToken, String deviceId, String ip, String userAgent) {
        SessionRepository.SessionData preSession = sessionRepository.findByRefreshToken(refreshToken);
        String newRefreshToken = UUID.randomUUID().toString();
        long now = System.currentTimeMillis();
        try {
            SessionRepository.SessionRotate rotated = sessionRepository.rotate(refreshToken, deviceId, newRefreshToken, now, ip, userAgent, refreshExpirationMs);
            return new SessionRotateResult(rotated.sessionId(), rotated.username(), rotated.refreshToken());
        } catch (BusinessException e) {
            if (preSession != null && e.getMessage() != null && e.getMessage().contains("reuse")) {
                sessionRepository.revokeAll(preSession.username());
            }
            throw e;
        }
    }

    public void revokeByRefreshToken(String refreshToken) {
        sessionRepository.revokeByRefreshToken(refreshToken);
    }

    public void revokeBySessionId(String sessionId) {
        sessionRepository.revokeBySessionId(sessionId);
    }

    public void revokeAll(String username) {
        sessionRepository.revokeAll(username);
    }

    public void blacklist(String jti, long ttlMs) {
        sessionRepository.blacklist(jti, ttlMs);
    }

    public boolean isBlacklisted(String jti) {
        return sessionRepository.isBlacklisted(jti);
    }

    public List<SessionData> listSessions(String username) {
        return sessionRepository.listSessions(username).stream()
                .map(s -> new SessionData(
                        s.sessionId(),
                        s.username(),
                        s.deviceId(),
                        s.createdAtMs(),
                        s.lastUsedAtMs(),
                        s.ip(),
                        s.userAgent()
                ))
                .toList();
    }

    public SessionData findBySessionId(String sessionId) {
        SessionRepository.SessionData s = sessionRepository.findBySessionId(sessionId);
        if (s == null) {
            return null;
        }
        return new SessionData(
                s.sessionId(),
                s.username(),
                s.deviceId(),
                s.createdAtMs(),
                s.lastUsedAtMs(),
                s.ip(),
                s.userAgent()
        );
    }

    public SessionData findByRefreshToken(String refreshToken) {
        SessionRepository.SessionData s = sessionRepository.findByRefreshToken(refreshToken);
        if (s == null) {
            return null;
        }
        return new SessionData(
                s.sessionId(),
                s.username(),
                s.deviceId(),
                s.createdAtMs(),
                s.lastUsedAtMs(),
                s.ip(),
                s.userAgent()
        );
    }

    private void replaceDeviceSessionIfNeeded(String username, String deviceId) {
        String existingSessionId = sessionRepository.getActiveSessionIdForDevice(username, deviceId);
        if (existingSessionId != null) {
            revokeBySessionId(existingSessionId);
        }
    }

    private void evictIfNeeded(String username) {
        List<String> sessionIds = sessionRepository.listUserSessionIds(username);
        if (sessionIds.size() < maxSessionsPerUser) {
            return;
        }
        int toRemove = (sessionIds.size() - (int) maxSessionsPerUser) + 1;
        for (int i = 0; i < toRemove && i < sessionIds.size(); i++) {
            revokeBySessionId(sessionIds.get(i));
        }
    }

    public record SessionIssueResult(String sessionId, String refreshToken) {}

    public record SessionRotateResult(String sessionId, String username, String refreshToken) {}

    public record SessionData(
            String sessionId,
            String username,
            String deviceId,
            long createdAtMs,
            long lastUsedAtMs,
            String ip,
            String userAgent
    ) {}
}
