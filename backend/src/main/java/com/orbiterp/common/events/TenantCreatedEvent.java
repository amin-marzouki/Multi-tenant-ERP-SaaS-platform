package com.orbiterp.common.events;

import com.orbiterp.common.DomainEvent;

/** A new tenant exists: each module creates its defaults (roles, chart of accounts, warehouse). */
public record TenantCreatedEvent(Long tenantId, String tenantCode, String tenantName) implements DomainEvent {
}
