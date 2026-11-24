package com.orbiterp.auth;

import com.orbiterp.auth.AuthDtos.LoginResponse;
import com.orbiterp.auth.AuthDtos.SignupRequest;
import com.orbiterp.tenant.Tenant;
import com.orbiterp.tenant.TenantContext;
import com.orbiterp.tenant.TenantService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Creates a workspace: the tenant, every module's defaults and the first admin user.
 *
 * <p>Deliberately not one transaction: the tenant row is saved first, then the setup runs in
 * a second transaction started inside the new tenant's context, so the tenant filter and the
 * tenant_id of every new row point at it. If the setup fails, the tenant row is removed again.
 */
@Service
@RequiredArgsConstructor
public class SignupService {

    private final TenantService tenantService;
    private final TenantOnboarding onboarding;
    private final AuthService authService;

    public LoginResponse signup(SignupRequest request) {
        Tenant tenant = tenantService.create(request.tenantCode(), request.companyName());
        try {
            return TenantContext.callAs(tenant.getId(), () -> {
                User admin = onboarding.provision(tenant, request);
                return authService.issue(tenant, admin);
            });
        } catch (RuntimeException ex) {
            tenantService.delete(tenant.getId());
            throw ex;
        }
    }
}
