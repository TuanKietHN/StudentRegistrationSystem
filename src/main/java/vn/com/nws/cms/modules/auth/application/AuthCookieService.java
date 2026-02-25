package vn.com.nws.cms.modules.auth.application;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Locale;

@Service
public class AuthCookieService {

    @Value("${cms.auth.refresh-cookie-name}")
    private String refreshCookieName;

    @Value("${cms.auth.device-cookie-name}")
    private String deviceCookieName;

    @Value("${cms.auth.cookie.secure}")
    private boolean cookieSecure;

    @Value("${cms.auth.cookie.same-site}")
    private String cookieSameSite;

    public String refreshCookieName() {
        return refreshCookieName;
    }

    public String deviceCookieName() {
        return deviceCookieName;
    }

    public ResponseCookie refreshTokenCookie(String refreshToken, Duration maxAge) {
        return ResponseCookie.from(refreshCookieName, refreshToken)
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite(normalizeSameSite(cookieSameSite))
                .path("/api")
                .maxAge(maxAge)
                .build();
    }

    public ResponseCookie clearRefreshTokenCookie() {
        return ResponseCookie.from(refreshCookieName, "")
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite(normalizeSameSite(cookieSameSite))
                .path("/api")
                .maxAge(Duration.ZERO)
                .build();
    }

    public ResponseCookie deviceIdCookie(String deviceId, Duration maxAge) {
        return ResponseCookie.from(deviceCookieName, deviceId)
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite(normalizeSameSite(cookieSameSite))
                .path("/api")
                .maxAge(maxAge)
                .build();
    }

    private String normalizeSameSite(String sameSite) {
        if (sameSite == null) {
            return "Lax";
        }
        String v = sameSite.trim().toLowerCase(Locale.ROOT);
        return switch (v) {
            case "strict" -> "Strict";
            case "none" -> "None";
            default -> "Lax";
        };
    }
}

