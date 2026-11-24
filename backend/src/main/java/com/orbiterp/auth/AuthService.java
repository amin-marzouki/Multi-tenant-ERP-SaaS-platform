package com.orbiterp.auth;

import com.orbiterp.auth.AuthDtos.LoginResponse;
import com.orbiterp.auth.AuthDtos.UserInfo;
import com.orbiterp.common.BusinessException;
import com.orbiterp.common.NotFoundException;
import com.orbiterp.security.AuthenticatedUser;
import com.orbiterp.security.JwtService;
import com.orbiterp.tenant.Tenant;
import com.orbiterp.tenant.TenantService;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class AuthService {

    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final TenantService tenantService;
    /** Checked when the username does not exist, so both failures take as long. */
    private final String dummyHash;

    public AuthService(UserRepository users, PasswordEncoder passwordEncoder, JwtService jwtService,
                       TenantService tenantService) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.tenantService = tenantService;
        this.dummyHash = passwordEncoder.encode("not-a-real-password");
    }

    /** Checks the password of a user of {@code tenant}, which must already be in TenantContext. */
    @Transactional(readOnly = true)
    public LoginResponse authenticate(Tenant tenant, String username, String password) {
        Optional<User> user = users.findByUsername(username.trim());
        boolean matches = passwordEncoder.matches(password, user.map(User::getPasswordHash).orElse(dummyHash));
        if (user.isEmpty() || !matches || !user.get().isEnabled()) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS",
                    "The workspace, username or password is wrong");
        }
        return issue(tenant, user.get());
    }

    @Transactional(readOnly = true)
    public UserInfo me(AuthenticatedUser principal) {
        User user = users.findById(principal.userId())
                .orElseThrow(() -> new NotFoundException("User", principal.userId()));
        return toInfo(user, tenantService.get(principal.tenantId()));
    }

    LoginResponse issue(Tenant tenant, User user) {
        AuthenticatedUser principal = new AuthenticatedUser(user.getId(), tenant.getId(), tenant.getCode(),
                user.getUsername(), user.roleNames());
        JwtService.IssuedToken token = jwtService.issue(principal);
        return new LoginResponse(token.token(), token.expiresAt(), toInfo(user, tenant));
    }

    private static UserInfo toInfo(User user, Tenant tenant) {
        return new UserInfo(user.getId(), user.getUsername(), user.getFullName(), user.getEmail(),
                tenant.getCode(), tenant.getName(), user.roleNames());
    }
}
