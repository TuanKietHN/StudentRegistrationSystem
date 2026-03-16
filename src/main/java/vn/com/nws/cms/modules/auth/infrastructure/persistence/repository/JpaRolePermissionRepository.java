package vn.com.nws.cms.modules.auth.infrastructure.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.com.nws.cms.modules.auth.infrastructure.persistence.entity.RolePermissionEntity;
import vn.com.nws.cms.modules.auth.infrastructure.persistence.entity.RolePermissionId;

@Repository
public interface JpaRolePermissionRepository extends JpaRepository<RolePermissionEntity, RolePermissionId> {
}

