package vn.com.nws.cms.modules.auth.infrastructure.persistence.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import vn.com.nws.cms.modules.auth.domain.repository.PermissionRepository;

import java.util.Collection;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class PermissionRepositoryImpl implements PermissionRepository {

    private final JpaRoleRepository jpaRoleRepository;

    @Override
    @Transactional(readOnly = true)
    public List<String> findPermissionNamesByRoleNames(Collection<String> roleNames) {
        if (roleNames == null || roleNames.isEmpty()) {
            return List.of();
        }
        return jpaRoleRepository.findPermissionNamesByRoleNames(roleNames);
    }
}

