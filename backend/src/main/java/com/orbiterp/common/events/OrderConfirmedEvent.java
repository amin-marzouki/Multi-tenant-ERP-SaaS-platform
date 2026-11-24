package com.orbiterp.common.events;

import com.orbiterp.common.DomainEvent;

import java.util.List;

/** Sales confirmed an order: Inventory reserves the stock, or rejects the confirmation. */
public record OrderConfirmedEvent(Long tenantId, Long orderId, String orderNumber, List<OrderLineQuantity> lines)
        implements DomainEvent {
}
