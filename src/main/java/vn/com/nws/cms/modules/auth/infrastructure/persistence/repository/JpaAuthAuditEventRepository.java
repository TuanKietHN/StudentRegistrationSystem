package vn.com.nws.cms.modules.auth.infrastructure.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.com.nws.cms.modules.auth.infrastructure.persistence.entity.AuthAuditEventEntity;

public interface JpaAuthAuditEventRepository extends JpaRepository<AuthAuditEventEntity, Long> {
}

