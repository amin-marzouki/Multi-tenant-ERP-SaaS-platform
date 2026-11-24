package com.orbiterp.auth;

import com.orbiterp.auth.AuthDtos.CreateUserRequest;
import com.orbiterp.auth.AuthDtos.UserResponse;
import com.orbiterp.common.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

/** Users of the current tenant. */
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository users;
    private final RoleRepository roles;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public List<UserResponse> list() {
        return users.findAllByOrderByUsernameAsc().stream().map(UserService::toResponse).toList();
    }

    @Transactional
    public UserResponse create(CreateUserRequest request) {
        return toResponse(createUser(request.username(), request.email(), request.fullName(),
                request.password(), request.roles()));
    }

    @Transactional
    public User createUser(String username, String email, String fullName, String rawPassword, Set<String> roleNames) {
        for (String roleName : roleNames) {
            if (!Role.ALL.contains(roleName)) {
                throw BusinessException.invalid("UNKNOWN_ROLE", "There is no role called " + roleName);
            }
        }
        String cleanUsername = username.trim();
        String cleanEmail = email.trim().toLowerCase();
        if (users.existsByUsername(cleanUsername)) {
            throw BusinessException.conflict("USERNAME_TAKEN", "The username " + cleanUsername + " is already used");
        }
        if (users.existsByEmail(cleanEmail)) {
            throw BusinessException.conflict("EMAIL_TAKEN", "The email " + cleanEmail + " is already used");
        }
        List<Role> found = roles.findByNameIn(roleNames);
        if (found.size() != roleNames.size()) {
            throw BusinessException.conflict("ROLES_MISSING", "This workspace is missing some of its roles");
        }

        User user = new User();
        user.setUsername(cleanUsername);
        user.setEmail(cleanEmail);
        user.setFullName(fullName == null || fullName.isBlank() ? null : fullName.trim());
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        user.getRoles().addAll(found);
        return users.save(user);
    }

    static UserResponse toResponse(User user) {
        return new UserResponse(user.getId(), user.getUsername(), user.getEmail(), user.getFullName(),
                user.isEnabled(), user.roleNames(), user.getCreatedAt());
    }
}
