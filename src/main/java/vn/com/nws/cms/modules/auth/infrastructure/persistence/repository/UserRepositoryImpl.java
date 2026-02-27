package vn.com.nws.cms.modules.auth.infrastructure.persistence.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;
import vn.com.nws.cms.domain.enums.RoleType;
import vn.com.nws.cms.modules.auth.domain.model.User;
import vn.com.nws.cms.modules.auth.domain.repository.UserRepository;
import vn.com.nws.cms.modules.auth.infrastructure.persistence.entity.RoleEntity;
import vn.com.nws.cms.modules.auth.infrastructure.persistence.entity.UserEntity;
import vn.com.nws.cms.modules.auth.infrastructure.persistence.entity.UserRoleEntity;
import vn.com.nws.cms.modules.auth.infrastructure.persistence.entity.UserRoleId;
import vn.com.nws.cms.modules.auth.infrastructure.persistence.mapper.UserMapper;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {

    private final JpaUserRepository jpaUserRepository;
    private final JpaRoleRepository jpaRoleRepository;
    private final UserMapper userMapper;

    @Override
    public User save(User user) {
        UserEntity entity;

        // Ensure ID is preserved if updating
        if (user.getId() != null) {
            // Fetch existing entity to avoid detaching or issues with updates
            Optional<UserEntity> existingOpt = jpaUserRepository.findById(user.getId());
            if (existingOpt.isPresent()) {
                UserEntity existing = existingOpt.get();
                UserEntity mappedEntity = userMapper.toEntity(user);
                
                // Copy non-relational fields
                existing.setUsername(mappedEntity.getUsername());
                existing.setPassword(mappedEntity.getPassword());
                existing.setEmail(mappedEntity.getEmail());
                existing.setAvatar(mappedEntity.getAvatar());
                existing.setFailedLoginAttempts(mappedEntity.getFailedLoginAttempts());
                existing.setLockUntil(mappedEntity.getLockUntil());
                existing.setLastLoginAt(mappedEntity.getLastLoginAt());
                existing.setLastLoginIp(mappedEntity.getLastLoginIp());
                existing.setLastLoginUserAgent(mappedEntity.getLastLoginUserAgent());
                entity = existing;
            } else {
                entity = userMapper.toEntity(user);
                entity.setId(user.getId());
            }
        } else {
            entity = userMapper.toEntity(user);
        }

        // Map roles manually using UserRoleEntity
        if (user.getRoles() != null) {
            if (entity.getUserRoles() == null) {
                entity.setUserRoles(new ArrayList<>());
            }

            Set<String> desiredRoleNames = user.getRoles().stream()
                    .map(RoleType::authority)
                    .collect(Collectors.toCollection(HashSet::new));

            Map<String, UserRoleEntity> existingByRoleName = new HashMap<>();
            for (UserRoleEntity ur : entity.getUserRoles()) {
                if (ur.getRole() != null && ur.getRole().getName() != null) {
                    existingByRoleName.put(ur.getRole().getName(), ur);
                }
            }

            entity.getUserRoles().removeIf(ur -> ur.getRole() == null || ur.getRole().getName() == null || !desiredRoleNames.contains(ur.getRole().getName()));

            for (String roleName : desiredRoleNames) {
                if (existingByRoleName.containsKey(roleName)) {
                    continue;
                }
                RoleEntity roleEntity = jpaRoleRepository.findByName(roleName)
                        .orElseGet(() -> jpaRoleRepository.save(RoleEntity.builder()
                                .name(roleName)
                                .description(roleName)
                                .build()));

                UserRoleEntity userRole = new UserRoleEntity();
                userRole.setId(new UserRoleId());
                userRole.setUser(entity);
                userRole.setRole(roleEntity);
                userRole.setAssignedAt(LocalDateTime.now());
                entity.getUserRoles().add(userRole);
            }
        }

        UserEntity savedEntity = jpaUserRepository.save(entity);
        return userMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<User> findById(Long id) {
        return jpaUserRepository.findById(id).map(userMapper::toDomain);
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return jpaUserRepository.findByUsername(username).map(userMapper::toDomain);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return jpaUserRepository.findByEmail(email).map(userMapper::toDomain);
    }

    @Override
    public boolean existsByUsername(String username) {
        return jpaUserRepository.existsByUsername(username);
    }

    @Override
    public boolean existsByEmail(String email) {
        return jpaUserRepository.existsByEmail(email);
    }

    @Override
    public void deleteById(Long id) {
        jpaUserRepository.deleteById(id);
    }

    @Override
    public List<User> search(String keyword, String role, int page, int size) {
        String normalizedKeyword = keyword == null || keyword.isBlank() ? null : keyword;
        String normalizedRole = role == null || role.isBlank() ? null : role;

        String roleName = null;
        if (normalizedRole != null) {
            if (!normalizedRole.startsWith("ROLE_")) {
                roleName = "ROLE_" + normalizedRole;
            } else {
                roleName = normalizedRole;
            }
        }

        Page<UserEntity> entities;
        if (normalizedKeyword == null && roleName == null) {
            entities = jpaUserRepository.findAll(PageRequest.of(page - 1, size));
        } else {
            entities = jpaUserRepository.search(normalizedKeyword, roleName, PageRequest.of(page - 1, size));
        }
        return entities.getContent().stream()
                .map(userMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public long count(String keyword, String role) {
        String normalizedKeyword = keyword == null || keyword.isBlank() ? null : keyword;
        String normalizedRole = role == null || role.isBlank() ? null : role;

        String roleName = null;
        if (normalizedRole != null) {
            if (!normalizedRole.startsWith("ROLE_")) {
                roleName = "ROLE_" + normalizedRole;
            } else {
                roleName = normalizedRole;
            }
        }
        if (normalizedKeyword == null && roleName == null) {
            return jpaUserRepository.count();
        }
        return jpaUserRepository.count(normalizedKeyword, roleName);
    }
}
