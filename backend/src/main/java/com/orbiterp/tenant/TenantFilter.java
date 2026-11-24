package com.orbiterp.tenant;

import com.orbiterp.common.ApiErrorWriter;
import com.orbiterp.security.AuthenticatedUser;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

/**
 * Sets {@link TenantContext} for the request and clears it afterwards.
 *
 * <p>Runs right after the JWT filter. For a signed-in user the tenant always comes from the
 * token's {@code tenant_id} claim, never from anything else the client sends. When the request
 * arrives on a tenant subdomain, it must be the token's tenant, or the request is refused.
 */
@RequiredArgsConstructor
public class TenantFilter extends OncePerRequestFilter {

    private final TenantSubdomainResolver subdomains;
    private final ApiErrorWriter errorWriter;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        Optional<AuthenticatedUser> user = AuthenticatedUser.current();
        if (user.isPresent()) {
            Optional<String> subdomain = subdomains.resolve(request.getServerName());
            if (subdomain.isPresent() && !subdomain.get().equals(user.get().tenantCode())) {
                errorWriter.write(response, HttpStatus.FORBIDDEN, "TENANT_MISMATCH",
                        "This session belongs to another workspace");
                return;
            }
            TenantContext.set(user.get().tenantId());
        }
        try {
            filterChain.doFilter(request, response);
        } finally {
            TenantContext.clear();
        }
    }
}
