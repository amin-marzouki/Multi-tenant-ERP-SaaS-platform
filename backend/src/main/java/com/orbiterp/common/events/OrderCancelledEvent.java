package com.orbiterp.common.events;

import com.orbiterp.common.DomainEvent;

import java.util.List;

/** A confirmed order was cancelled: Inventory releases its reservation. */
public record OrderCancelledEvent(Long tenantId, Long orderId, String orderNumber, List<OrderLineQuantity> lines)
        implements DomainEvent {
}
