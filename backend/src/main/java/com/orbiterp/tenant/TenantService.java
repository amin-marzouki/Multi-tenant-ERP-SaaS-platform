package com.orbiterp.tenant;

import com.orbiterp.common.BusinessException;
import com.orbiterp.common.NotFoundException;
import com.orbiterp.common.events.TenantCreatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TenantService {

    private final TenantRepository tenants;
    private final ApplicationEventPublisher events;

    public static String normalizeCode(String code) {
        return code == null ? null : code.trim().toLowerCase(Locale.ROOT);
    }

    /** The active tenant with this code. Unknown and suspended tenants look like a failed login. */
    @Transactional(readOnly = true)
    public Tenant requireActive(String code) {
        return tenants.findByCode(normalizeCode(code))
                .filter(tenant -> tenant.getStatus() == TenantStatus.ACTIVE)
                .orElseThrow(() -> new BusinessException(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS",
                        "The workspace, username or password is wrong"));
    }

    @Transactional(readOnly = true)
    public Tenant get(Long id) {
        return tenants.findById(id).orElseThrow(() -> new NotFoundException("Tenant", id));
    }

    @Transactional(readOnly = true)
    public Optional<Tenant> findByCode(String code) {
        return tenants.findByCode(normalizeCode(code));
    }

    @Transactional
    public Tenant create(String code, String name) {
        String normalized = normalizeCode(code);
        if (tenants.existsByCode(normalized)) {
            throw BusinessException.conflict("TENANT_CODE_TAKEN", "The workspace name '%s' is already taken".formatted(normalized));
        }
        Tenant tenant = new Tenant();
        tenant.setCode(normalized);
        tenant.setName(name.trim());
        return tenants.save(tenant);
    }

    /** Removes a tenant whose setup failed, so its code can be used again. */
    @Transactional
    public void delete(Long id) {
        tenants.deleteById(id);
    }

    /**
     * Tells every module that the tenant exists. Must run inside a transaction that started
     * after {@link TenantContext} was set to this tenant; the modules' defaults are saved in it.
     */
    @Transactional(propagation = Propagation.MANDATORY)
    public void publishCreated(Tenant tenant) {
        events.publishEvent(new TenantCreatedEvent(tenant.getId(), tenant.getCode(), tenant.getName()));
    }
}
