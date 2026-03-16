package vn.com.nws.cms.modules.auth.infrastructure.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.com.nws.cms.modules.auth.infrastructure.persistence.entity.RoleEntity;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface JpaRoleRepository extends JpaRepository<RoleEntity, Long> {
    Optional<RoleEntity> findByName(String name);

    @Query("select distinct p.name from RoleEntity r join r.rolePermissions rp join rp.permission p where r.name in :roleNames")
    List<String> findPermissionNamesByRoleNames(@Param("roleNames") Collection<String> roleNames);
}
