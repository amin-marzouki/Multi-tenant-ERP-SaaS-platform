package com.orbiterp.auth;

import com.orbiterp.common.events.TenantCreatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class AuthEventListener {

    private final RoleRepository roles;

    @EventListener
    public void onTenantCreated(TenantCreatedEvent event) {
        roles.saveAll(Role.ALL.stream().map(Role::new).toList());
    }
}
