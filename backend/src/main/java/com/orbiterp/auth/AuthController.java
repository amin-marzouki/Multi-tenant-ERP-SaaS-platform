package com.orbiterp.auth;

import com.orbiterp.auth.AuthDtos.LoginRequest;
import com.orbiterp.auth.AuthDtos.LoginResponse;
import com.orbiterp.auth.AuthDtos.SignupRequest;
import com.orbiterp.auth.AuthDtos.UserInfo;
import com.orbiterp.security.AuthenticatedUser;
import com.orbiterp.tenant.Tenant;
import com.orbiterp.tenant.TenantContext;
import com.orbiterp.tenant.TenantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final TenantService tenantService;
    private final AuthService authService;
    private final SignupService signupService;

    /** Signs in to one workspace. The tenant is resolved first, then the user is looked up inside it. */
    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        Tenant tenant = tenantService.requireActive(request.tenantCode());
        return TenantContext.callAs(tenant.getId(),
                () -> authService.authenticate(tenant, request.username(), request.password()));
    }

    /** Creates a new workspace with its first admin user, and signs that user in. */
    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    public LoginResponse signup(@Valid @RequestBody SignupRequest request) {
        return signupService.signup(request);
    }

    @GetMapping("/me")
    public UserInfo me(@AuthenticationPrincipal AuthenticatedUser principal) {
        return authService.me(principal);
    }

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "UP");
    }
}
