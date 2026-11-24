package com.orbiterp.common.events;

import com.orbiterp.common.DomainEvent;

import java.util.List;

/** A confirmed order left the warehouse: Inventory turns its reservation into a stock issue. */
public record OrderShippedEvent(Long tenantId, Long orderId, String orderNumber, List<OrderLineQuantity> lines)
        implements DomainEvent {
}
