package com.orbiterp.tenant;

import java.util.function.Supplier;

/** Gives Hibernate's tenant filter the tenant id of the current thread. Hibernate creates it. */
public class CurrentTenantSupplier implements Supplier<Long> {

    @Override
    public Long get() {
        return TenantContext.get();
    }
}
