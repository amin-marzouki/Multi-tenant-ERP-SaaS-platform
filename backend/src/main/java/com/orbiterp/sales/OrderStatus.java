package com.orbiterp.sales;

/** DRAFT -> CONFIRMED (stock reserved) -> SHIPPED (stock issued) -> INVOICED; DRAFT or CONFIRMED -> CANCELLED. */
public enum OrderStatus {
    DRAFT,
    CONFIRMED,
    SHIPPED,
    INVOICED,
    CANCELLED
}
