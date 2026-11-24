package com.orbiterp.security;

/** Who may call what: {@code @PreAuthorize} expressions shared by the controllers. */
public final class Access {

    public static final String ADMIN = "hasRole('ADMIN')";
    public static final String SALES = "hasAnyRole('SALES_REP','ADMIN')";
    public static final String WAREHOUSE = "hasAnyRole('WAREHOUSE_CLERK','ADMIN')";
    public static final String FINANCE = "hasAnyRole('ACCOUNTANT','ADMIN')";
    public static final String SALES_OR_FINANCE = "hasAnyRole('SALES_REP','ACCOUNTANT','ADMIN')";
    public static final String ANY_ROLE = "hasAnyRole('SALES_REP','WAREHOUSE_CLERK','ACCOUNTANT','ADMIN')";

    private Access() {
    }
}
