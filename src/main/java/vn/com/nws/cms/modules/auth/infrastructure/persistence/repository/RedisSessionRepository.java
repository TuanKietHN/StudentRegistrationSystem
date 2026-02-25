package vn.com.nws.cms.modules.auth.infrastructure.persistence.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;
import vn.com.nws.cms.common.exception.BusinessException;
import vn.com.nws.cms.modules.auth.domain.repository.SessionRepository;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class RedisSessionRepository implements SessionRepository {

    private final StringRedisTemplate redis;
    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Override
    public SessionIssue issue(String username, String deviceId, long nowMs, String ip, String userAgent, long ttlMs) {
        String sessionId = UUID.randomUUID().toString();
        String refreshToken = UUID.randomUUID().toString();

        SessionData data = new SessionData(sessionId, username, deviceId, nowMs, nowMs, ip, userAgent);
        storeSession(sessionId, refreshToken, data, ttlMs);
        return new SessionIssue(sessionId, refreshToken);
    }

    @Override
    public SessionRotate rotate(String refreshToken, String deviceId, String newRefreshToken, long nowMs, String ip, String userAgent, long ttlMs) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new BusinessException("Refresh token is invalid or expired!");
        }
        if (deviceId == null || deviceId.isBlank()) {
            throw new BusinessException("Refresh token reuse detected! Please login again.");
        }

        int attempts = 0;
        while (attempts++ < 2) {
            try {
                return doRotate(refreshToken, deviceId, newRefreshToken, nowMs, ip, userAgent, ttlMs);
            } catch (OptimisticLockingFailureException e) {
                continue;
            }
        }
        throw new BusinessException("Refresh token is invalid or expired!");
    }

    private SessionRotate doRotate(String refreshToken, String deviceId, String newRefreshToken, long nowMs, String ip, String userAgent, long ttlMs) {
        String rtKey = rtKey(refreshToken);

        Object result = redis.execute(new SessionCallback<>() {
            @Override
            public Object execute(RedisOperations operations) {
                operations.watch(rtKey);
                String sessionId = (String) operations.opsForValue().get(rtKey);
                if (sessionId == null || sessionId.isBlank()) {
                    operations.unwatch();
                    return null;
                }

                String sessKey = sessKey(sessionId);
                String sessRtKey = sessRtKey(sessionId);

                operations.watch(sessRtKey);
                String currentRt = (String) operations.opsForValue().get(sessRtKey);
                String sessionJson = (String) operations.opsForValue().get(sessKey);
                if (currentRt == null || !currentRt.equals(refreshToken) || sessionJson == null || sessionJson.isBlank()) {
                    operations.unwatch();
                    return new ReuseDetected();
                }

                SessionData session = fromJson(sessionJson);
                if (session == null) {
                    operations.unwatch();
                    return null;
                }
                if (!deviceId.equals(session.deviceId())) {
                    operations.unwatch();
                    return new ReuseDetected();
                }

                String devKey = deviceKey(session.username(), session.deviceId());
                operations.watch(devKey);
                String activeSessionIdForDevice = (String) operations.opsForValue().get(devKey);
                if (activeSessionIdForDevice == null || !activeSessionIdForDevice.equals(sessionId)) {
                    operations.unwatch();
                    return new ReuseDetected();
                }

                String newSessionJson = toJson(new SessionData(
                        session.sessionId(),
                        session.username(),
                        session.deviceId(),
                        session.createdAtMs(),
                        nowMs,
                        ip,
                        userAgent
                ));

                operations.multi();
                operations.opsForValue().set(rtKey(newRefreshToken), sessionId, Duration.ofMillis(ttlMs));
                operations.opsForValue().set(sessRtKey, newRefreshToken, Duration.ofMillis(ttlMs));
                operations.opsForValue().set(sessKey, newSessionJson, Duration.ofMillis(ttlMs));
                operations.delete(rtKey);
                operations.expire(userSessionsKey(session.username()), Duration.ofMillis(ttlMs));
                operations.expire(devKey, Duration.ofMillis(ttlMs));
                List<Object> exec = operations.exec();
                if (exec == null) {
                    throw new OptimisticLockingFailureException("redis_watch_conflict");
                }
                return new RotateOk(session.sessionId(), session.username());
            }
        });

        if (result == null) {
            throw new BusinessException("Refresh token is invalid or expired!");
        }
        if (result instanceof ReuseDetected) {
            throw new BusinessException("Refresh token reuse detected! Please login again.");
        }
        if (result instanceof RotateOk ok) {
            return new SessionRotate(ok.sessionId, ok.username, newRefreshToken);
        }
        throw new BusinessException("Refresh token is invalid or expired!");
    }

    @Override
    public SessionData findBySessionId(String sessionId) {
        if (sessionId == null || sessionId.isBlank()) {
            return null;
        }
        String json = redis.opsForValue().get(sessKey(sessionId));
        return fromJson(json);
    }

    @Override
    public SessionData findByRefreshToken(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            return null;
        }
        String sessionId = redis.opsForValue().get(rtKey(refreshToken));
        if (sessionId == null || sessionId.isBlank()) {
            return null;
        }
        return findBySessionId(sessionId);
    }

    @Override
    public void revokeBySessionId(String sessionId) {
        if (sessionId == null || sessionId.isBlank()) {
            return;
        }
        SessionData session = findBySessionId(sessionId);
        String refreshToken = redis.opsForValue().get(sessRtKey(sessionId));

        redis.delete(sessKey(sessionId));
        redis.delete(sessRtKey(sessionId));
        if (refreshToken != null && !refreshToken.isBlank()) {
            redis.delete(rtKey(refreshToken));
        }

        if (session != null) {
            removeUserSessionId(session.username(), sessionId);
            String devKey = deviceKey(session.username(), session.deviceId());
            String mappedSessionId = redis.opsForValue().get(devKey);
            if (mappedSessionId != null && mappedSessionId.equals(sessionId)) {
                redis.delete(devKey);
            }
        }
    }

    @Override
    public void revokeByRefreshToken(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            return;
        }
        String sessionId = redis.opsForValue().get(rtKey(refreshToken));
        if (sessionId == null || sessionId.isBlank()) {
            return;
        }
        revokeBySessionId(sessionId);
    }

    @Override
    public void revokeAll(String username) {
        if (username == null || username.isBlank()) {
            return;
        }
        List<String> sessionIds = listUserSessionIds(username);
        for (String sessionId : sessionIds) {
            revokeBySessionId(sessionId);
        }
        redis.delete(userSessionsKey(username));
    }

    @Override
    public List<SessionData> listSessions(String username) {
        List<String> ids = listUserSessionIds(username);
        if (ids.isEmpty()) {
            return List.of();
        }
        List<SessionData> out = new ArrayList<>();
        for (String sessionId : ids) {
            SessionData session = findBySessionId(sessionId);
            if (session == null) {
                removeUserSessionId(username, sessionId);
                continue;
            }
            out.add(session);
        }
        return out;
    }

    @Override
    public void addUserSession(String username, String sessionId, long score, long ttlMs) {
        redis.opsForZSet().add(userSessionsKey(username), sessionId, score);
        expireUserSessions(username, ttlMs);
    }

    @Override
    public List<String> listUserSessionIds(String username) {
        Set<String> ids = redis.opsForZSet().range(userSessionsKey(username), 0, -1);
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return new ArrayList<>(ids);
    }

    @Override
    public String getActiveSessionIdForDevice(String username, String deviceId) {
        return redis.opsForValue().get(deviceKey(username, deviceId));
    }

    @Override
    public void setActiveSessionIdForDevice(String username, String deviceId, String sessionId, long ttlMs) {
        redis.opsForValue().set(deviceKey(username, deviceId), sessionId, Duration.ofMillis(ttlMs));
    }

    @Override
    public void removeUserSessionId(String username, String sessionId) {
        redis.opsForZSet().remove(userSessionsKey(username), sessionId);
    }

    @Override
    public void expireUserSessions(String username, long ttlMs) {
        redis.expire(userSessionsKey(username), Duration.ofMillis(ttlMs));
    }

    private void storeSession(String sessionId, String refreshToken, SessionData data, long ttlMs) {
        Duration ttl = Duration.ofMillis(ttlMs);
        redis.opsForValue().set(sessKey(sessionId), toJson(data), ttl);
        redis.opsForValue().set(sessRtKey(sessionId), refreshToken, ttl);
        redis.opsForValue().set(rtKey(refreshToken), sessionId, ttl);
    }

    private String toJson(SessionData data) {
        try {
            return objectMapper.writeValueAsString(data);
        } catch (Exception e) {
            throw new BusinessException("Internal error");
        }
    }

    private SessionData fromJson(String json) {
        if (json == null || json.isBlank()) {
            return null;
        }
        try {
            return objectMapper.readValue(json, SessionData.class);
        } catch (Exception e) {
            return null;
        }
    }

    private String rtKey(String refreshToken) {
        return "auth:rt:" + refreshToken;
    }

    private String sessKey(String sessionId) {
        return "auth:sess:" + sessionId;
    }

    private String sessRtKey(String sessionId) {
        return "auth:sess:rt:" + sessionId;
    }

    private String userSessionsKey(String username) {
        return "auth:u:sess:" + username;
    }

    private String deviceKey(String username, String deviceId) {
        return "auth:u:dev:" + username + ":" + deviceId;
    }

    private static final class ReuseDetected {}

    private static final class RotateOk {
        private final String sessionId;
        private final String username;

        private RotateOk(String sessionId, String username) {
            this.sessionId = sessionId;
            this.username = username;
        }
    }
}
