package vn.com.nws.cms.modules.auth.application;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import vn.com.nws.cms.common.exception.BusinessException;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class AuthRateLimitService {

    private final StringRedisTemplate redisTemplate;

    @Value("${cms.auth.ratelimit.login.ip.max}")
    private long loginIpMax;

    @Value("${cms.auth.ratelimit.login.ip.window-seconds}")
    private long loginIpWindowSeconds;

    @Value("${cms.auth.ratelimit.login.user.max}")
    private long loginUserMax;

    @Value("${cms.auth.ratelimit.login.user.window-seconds}")
    private long loginUserWindowSeconds;

    public void recordLoginFailureOrThrow(String ip, String usernameOrEmail) {
        try {
            if (!allow("auth:rl:login:ip:" + ip, loginIpMax, loginIpWindowSeconds)) {
                throw new BusinessException("Bạn đang thao tác quá nhanh. Vui lòng thử lại sau.");
            }
            if (usernameOrEmail != null && !usernameOrEmail.isBlank()) {
                if (!allow("auth:rl:login:user:" + usernameOrEmail.toLowerCase(), loginUserMax, loginUserWindowSeconds)) {
                    throw new BusinessException("Bạn đang thao tác quá nhanh. Vui lòng thử lại sau.");
                }
            }
        } catch (RedisConnectionFailureException e) {
            throw new BusinessException("Hệ thống đang bận (Redis). Vui lòng thử lại sau.");
        }
    }

    public void clearLoginCounters(String ip, String usernameOrEmail) {
        try {
            if (ip != null && !ip.isBlank()) {
                redisTemplate.delete("auth:rl:login:ip:" + ip);
            }
            if (usernameOrEmail != null && !usernameOrEmail.isBlank()) {
                redisTemplate.delete("auth:rl:login:user:" + usernameOrEmail.toLowerCase());
            }
        } catch (RedisConnectionFailureException e) {
            throw new BusinessException("Hệ thống đang bận (Redis). Vui lòng thử lại sau.");
        }
    }

    private boolean allow(String key, long max, long windowSeconds) {
        Long count = redisTemplate.opsForValue().increment(key);
        if (count == null) {
            return true;
        }
        if (count == 1L) {
            redisTemplate.expire(key, Duration.ofSeconds(windowSeconds));
        }
        return count <= max;
    }
}

