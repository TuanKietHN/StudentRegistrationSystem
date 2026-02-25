package vn.com.nws.cms.modules.auth.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthSessionInfo {
    private String sessionId;
    private String deviceId;
    private Long createdAtMs;
    private Long lastUsedAtMs;
    private String ip;
    private String userAgent;
}

