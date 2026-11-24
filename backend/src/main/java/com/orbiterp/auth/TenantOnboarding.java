package com.orbiterp.auth;

import com.orbiterp.auth.AuthDtos.SignupRequest;
import com.orbiterp.tenant.Tenant;
import com.orbiterp.tenant.TenantService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
@RequiredArgsConstructor
class TenantOnboarding {

    private final TenantService tenantService;
    private final UserService userService;

    /** Runs as the new tenant: every module saves its defaults, then the admin user is created. */
    @Transactional
    public User provision(Tenant tenant, SignupRequest request) {
        tenantService.publishCreated(tenant);
        return userService.createUser(request.username(), request.email(), request.fullName(),
                request.password(), Set.of(Role.ADMIN));
    }
}
