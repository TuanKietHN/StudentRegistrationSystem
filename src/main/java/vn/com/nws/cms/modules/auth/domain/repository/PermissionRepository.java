package vn.com.nws.cms.modules.auth.domain.repository;

import java.util.Collection;
import java.util.List;

public interface PermissionRepository {
    List<String> findPermissionNamesByRoleNames(Collection<String> roleNames);
}

