package vn.com.nws.cms.modules.auth.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import vn.com.nws.cms.common.exception.BusinessException;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthSessionService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    @Value("${jwt.refresh-expiration}")
    private long refreshExpirationMs;

    @Value("${cms.auth.max-sessions-per-user}")
    private long maxSessionsPerUser;

    public SessionIssueResult issue(String username, String deviceId, String ip, String userAgent) {
        String sessionId = UUID.randomUUID().toString();
        String refreshToken = UUID.randomUUID().toString();

        evictIfNeeded(username);
        replaceDeviceSessionIfNeeded(username, deviceId);

        long now = System.currentTimeMillis();
        SessionData data = new SessionData(sessionId, username, deviceId, now, now, ip, userAgent);
        storeSession(sessionId, refreshToken, data);
        addUserSession(username, sessionId, now);
        setDeviceSession(username, deviceId, sessionId);

        return new SessionIssueResult(sessionId, refreshToken);
    }

    public SessionRotateResult rotate(String refreshToken, String deviceId, String ip, String userAgent) {
        String sessionId = getString("auth:rt:" + refreshToken);
        if (sessionId == null) {
            throw new BusinessException("Refresh token is invalid or expired!");
        }

        String current = getString("auth:sess:rt:" + sessionId);
        if (current == null || !current.equals(refreshToken)) {
            SessionData session = getSession(sessionId);
            if (session != null) {
                revokeAll(session.username());
            } else {
                revokeBySessionId(sessionId);
            }
            throw new BusinessException("Refresh token reuse detected! Please login again.");
        }

        SessionData session = getSession(sessionId);
        if (session == null) {
            revokeBySessionId(sessionId);
            throw new BusinessException("Refresh token is invalid or expired!");
        }

        if (deviceId == null || !deviceId.equals(session.deviceId())) {
            revokeAll(session.username());
            throw new BusinessException("Refresh token reuse detected! Please login again.");
        }

        String activeSessionIdForDevice = getString(deviceKey(session.username(), session.deviceId()));
        if (activeSessionIdForDevice == null || !activeSessionIdForDevice.equals(sessionId)) {
            revokeAll(session.username());
            throw new BusinessException("Refresh token reuse detected! Please login again.");
        }

        String newRefreshToken = UUID.randomUUID().toString();
        long now = System.currentTimeMillis();
        SessionData updated = new SessionData(
                session.sessionId(),
                session.username(),
                session.deviceId(),
                session.createdAtMs(),
                now,
                ip,
                userAgent
        );

        storeSession(sessionId, newRefreshToken, updated);
        redisTemplate.delete("auth:rt:" + refreshToken);

        return new SessionRotateResult(session.sessionId(), session.username(), newRefreshToken);
    }

    public void revokeByRefreshToken(String refreshToken) {
        String sessionId = getString("auth:rt:" + refreshToken);
        if (sessionId == null) {
            return;
        }
        revokeBySessionId(sessionId);
    }

    public void revokeBySessionId(String sessionId) {
        SessionData session = getSession(sessionId);
        String refreshToken = getString("auth:sess:rt:" + sessionId);

        redisTemplate.delete("auth:sess:" + sessionId);
        redisTemplate.delete("auth:sess:rt:" + sessionId);
        if (refreshToken != null) {
            redisTemplate.delete("auth:rt:" + refreshToken);
        }

        if (session != null) {
            redisTemplate.opsForZSet().remove(userSessionsKey(session.username()), sessionId);
            String deviceKey = deviceKey(session.username(), session.deviceId());
            String mappedSessionId = getString(deviceKey);
            if (mappedSessionId != null && mappedSessionId.equals(sessionId)) {
                redisTemplate.delete(deviceKey);
            }
        }
    }

    public void revokeAll(String username) {
        Set<Object> sessionIds = redisTemplate.opsForZSet().range(userSessionsKey(username), 0, -1);
        if (sessionIds == null || sessionIds.isEmpty()) {
            return;
        }
        for (Object raw : sessionIds) {
            if (raw instanceof String sessionId) {
                revokeBySessionId(sessionId);
            }
        }
        redisTemplate.delete(userSessionsKey(username));
    }

    public List<SessionData> listSessions(String username) {
        Set<Object> ids = redisTemplate.opsForZSet().range(userSessionsKey(username), 0, -1);
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        List<SessionData> out = new ArrayList<>();
        for (Object raw : ids) {
            if (!(raw instanceof String sessionId)) {
                continue;
            }
            SessionData session = getSession(sessionId);
            if (session == null) {
                redisTemplate.opsForZSet().remove(userSessionsKey(username), sessionId);
                continue;
            }
            out.add(session);
        }
        return out;
    }

    public SessionData findBySessionId(String sessionId) {
        if (sessionId == null || sessionId.isBlank()) {
            return null;
        }
        return getSession(sessionId);
    }

    public SessionData findByRefreshToken(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            return null;
        }
        String sessionId = getString("auth:rt:" + refreshToken);
        if (sessionId == null) {
            return null;
        }
        return getSession(sessionId);
    }

    private void storeSession(String sessionId, String refreshToken, SessionData data) {
        Duration ttl = Duration.ofMillis(refreshExpirationMs);
        redisTemplate.opsForValue().set("auth:sess:" + sessionId, toJson(data), ttl);
        redisTemplate.opsForValue().set("auth:sess:rt:" + sessionId, refreshToken, ttl);
        redisTemplate.opsForValue().set("auth:rt:" + refreshToken, sessionId, ttl);
        redisTemplate.expire(userSessionsKey(data.username()), ttl);
        redisTemplate.expire(deviceKey(data.username(), data.deviceId()), ttl);
    }

    private void addUserSession(String username, String sessionId, long score) {
        redisTemplate.opsForZSet().add(userSessionsKey(username), sessionId, score);
        redisTemplate.expire(userSessionsKey(username), Duration.ofMillis(refreshExpirationMs));
    }

    private void setDeviceSession(String username, String deviceId, String sessionId) {
        redisTemplate.opsForValue().set(deviceKey(username, deviceId), sessionId, Duration.ofMillis(refreshExpirationMs));
    }

    private void replaceDeviceSessionIfNeeded(String username, String deviceId) {
        String existingSessionId = getString(deviceKey(username, deviceId));
        if (existingSessionId != null) {
            revokeBySessionId(existingSessionId);
        }
    }

    private void evictIfNeeded(String username) {
        Long size = redisTemplate.opsForZSet().zCard(userSessionsKey(username));
        if (size == null || size < maxSessionsPerUser) {
            return;
        }
        long toRemove = (size - maxSessionsPerUser) + 1;
        Set<Object> evict = redisTemplate.opsForZSet().range(userSessionsKey(username), 0, toRemove - 1);
        if (evict == null || evict.isEmpty()) {
            return;
        }
        for (Object raw : evict) {
            if (raw instanceof String sessionId) {
                revokeBySessionId(sessionId);
            }
        }
    }

    private SessionData getSession(String sessionId) {
        Object raw = redisTemplate.opsForValue().get("auth:sess:" + sessionId);
        if (!(raw instanceof String json) || json.isBlank()) {
            return null;
        }
        try {
            return objectMapper.readValue(json, SessionData.class);
        } catch (Exception e) {
            return null;
        }
    }

    private String toJson(SessionData data) {
        try {
            return objectMapper.writeValueAsString(data);
        } catch (JsonProcessingException e) {
            throw new BusinessException("Internal error");
        }
    }

    private String getString(String key) {
        Object raw = redisTemplate.opsForValue().get(key);
        return raw instanceof String s ? s : null;
    }

    private String userSessionsKey(String username) {
        return "auth:u:sess:" + username;
    }

    private String deviceKey(String username, String deviceId) {
        return "auth:u:dev:" + username + ":" + deviceId;
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
