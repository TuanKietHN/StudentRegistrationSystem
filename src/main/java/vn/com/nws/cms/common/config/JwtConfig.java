package vn.com.nws.cms.common.config;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Configuration
public class JwtConfig {

    private final String jwtSecret;
    private final String jwtSecretB64;

    public JwtConfig(
            @Value("${jwt.secret:}") String jwtSecret,
            @Value("${jwt.secret-b64:}") String jwtSecretB64
    ) {
        this.jwtSecret = jwtSecret;
        this.jwtSecretB64 = jwtSecretB64;
    }

    @Bean
    public JwtEncoder jwtEncoder() {
        byte[] secret = resolveSecretBytes();
        return new NimbusJwtEncoder(
                new ImmutableSecret<>(secret)
        );
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        byte[] secret = resolveSecretBytes();
        SecretKeySpec secretKey = new SecretKeySpec(secret, "HmacSHA256");

        return NimbusJwtDecoder.withSecretKey(secretKey)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
    }

    private byte[] resolveSecretBytes() {
        byte[] bytes;
        if (jwtSecretB64 != null && !jwtSecretB64.isBlank()) {
            try {
                bytes = Base64.getDecoder().decode(jwtSecretB64.trim());
            } catch (IllegalArgumentException e) {
                throw new IllegalStateException("JWT secret-b64 không hợp lệ (không decode được Base64)");
            }
        } else if (jwtSecret != null && !jwtSecret.isBlank()) {
            bytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        } else {
            throw new IllegalStateException("Thiếu cấu hình JWT secret (jwt.secret-b64 hoặc jwt.secret)");
        }

        if (bytes.length < 32) {
            throw new IllegalStateException("JWT secret phải tối thiểu 256-bit (32 bytes) cho HS256");
        }

        return bytes;
    }
}
