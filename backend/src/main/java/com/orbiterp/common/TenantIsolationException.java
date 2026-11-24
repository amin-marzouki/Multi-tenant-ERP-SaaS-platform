package com.orbiterp.common;

/** A row of another tenant was loaded. Should never happen; answered as "not found" to reveal nothing. */
public class TenantIsolationException extends RuntimeException {

    public TenantIsolationException(String entity, Object id) {
        super("%s %s does not belong to the current tenant".formatted(entity, id));
    }
}
