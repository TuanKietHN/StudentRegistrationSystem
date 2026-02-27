package vn.com.nws.cms.modules.auth.application;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.com.nws.cms.common.exception.BusinessException;
import vn.com.nws.cms.domain.enums.RoleType;
import vn.com.nws.cms.modules.academic.domain.model.Student;
import vn.com.nws.cms.modules.academic.domain.repository.StudentRepository;
import vn.com.nws.cms.modules.auth.api.dto.RegisterRequest;
import vn.com.nws.cms.modules.auth.domain.model.User;
import vn.com.nws.cms.modules.auth.domain.repository.UserRepository;

import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class RegistrationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final StudentRepository studentRepository;

    @Transactional
    public void register(RegisterRequest registerRequest) {
        if (userRepository.existsByUsername(registerRequest.getUsername())) {
            throw new BusinessException("Username is already taken!");
        }

        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new BusinessException("Email is already in use!");
        }

        Set<RoleType> roles = new HashSet<>();
        if (registerRequest.getRole() != null && !registerRequest.getRole().trim().isEmpty()) {
            try {
                // Remove ROLE_ prefix if present and convert to uppercase to match Enum
                String roleName = registerRequest.getRole().replace("ROLE_", "").toUpperCase();
                if (!"STUDENT".equals(roleName)) {
                    throw new BusinessException("Chỉ cho phép đăng ký tài khoản sinh viên");
                }
                roles.add(RoleType.STUDENT);
            } catch (IllegalArgumentException e) {
                throw new BusinessException("Invalid role provided: " + registerRequest.getRole());
            }
        } else {
            roles.add(RoleType.STUDENT);
        }

        User user = User.builder()
                .username(registerRequest.getUsername())
                .email(registerRequest.getEmail())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .roles(roles)
                .build();

        User savedUser = userRepository.save(user);
        if (roles.contains(RoleType.STUDENT)) {
            studentRepository.findByUserId(savedUser.getId()).orElseGet(() -> studentRepository.save(Student.builder()
                    .user(savedUser)
                    .studentCode("SV" + savedUser.getId())
                    .active(true)
                    .build()));
        }
    }
}
