package vn.com.nws.cms.modules.auth.domain.repository;

import java.util.List;

public interface SessionRepository {

    SessionIssue issue(String username, String deviceId, long nowMs, String ip, String userAgent, long ttlMs);

    SessionRotate rotate(String refreshToken, String deviceId, String newRefreshToken, long nowMs, String ip, String userAgent, long ttlMs);

    SessionData findBySessionId(String sessionId);

    SessionData findByRefreshToken(String refreshToken);

    void revokeBySessionId(String sessionId);

    void revokeByRefreshToken(String refreshToken);

    void revokeAll(String username);

    List<SessionData> listSessions(String username);

    void addUserSession(String username, String sessionId, long score, long ttlMs);

    List<String> listUserSessionIds(String username);

    String getActiveSessionIdForDevice(String username, String deviceId);

    void setActiveSessionIdForDevice(String username, String deviceId, String sessionId, long ttlMs);

    void removeUserSessionId(String username, String sessionId);

    void expireUserSessions(String username, long ttlMs);

    record SessionIssue(String sessionId, String refreshToken) {}

    record SessionRotate(String sessionId, String username, String refreshToken) {}

    record SessionData(
            String sessionId,
            String username,
            String deviceId,
            long createdAtMs,
            long lastUsedAtMs,
            String ip,
            String userAgent
    ) {}
}
