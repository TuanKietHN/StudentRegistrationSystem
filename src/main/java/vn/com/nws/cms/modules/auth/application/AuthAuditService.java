package vn.com.nws.cms.modules.auth.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.com.nws.cms.modules.auth.infrastructure.persistence.entity.AuthAuditEventEntity;
import vn.com.nws.cms.modules.auth.infrastructure.persistence.repository.JpaAuthAuditEventRepository;

@Service
@RequiredArgsConstructor
public class AuthAuditService {

    private final JpaAuthAuditEventRepository repository;

    @Transactional
    public void record(String username, String eventType, boolean success, String ip, String userAgent, String sessionId, String details) {
        repository.save(AuthAuditEventEntity.builder()
                .username(username)
                .eventType(eventType)
                .success(success)
                .ip(ip)
                .userAgent(userAgent)
                .sessionId(sessionId)
                .details(details)
                .build());
    }
}

