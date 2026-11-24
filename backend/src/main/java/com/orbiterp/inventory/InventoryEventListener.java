package com.orbiterp.inventory;

import com.orbiterp.common.events.OrderCancelledEvent;
import com.orbiterp.common.events.OrderConfirmedEvent;
import com.orbiterp.common.events.OrderShippedEvent;
import com.orbiterp.common.events.TenantCreatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class InventoryEventListener {

    private final WarehouseService warehouses;
    private final StockService stock;

    @EventListener
    public void onTenantCreated(TenantCreatedEvent event) {
        warehouses.createDefault();
    }

    @EventListener
    public void onOrderConfirmed(OrderConfirmedEvent event) {
        stock.reserve(event.orderNumber(), event.lines());
    }

    @EventListener
    public void onOrderShipped(OrderShippedEvent event) {
        stock.issue(event.orderNumber(), event.lines());
    }

    @EventListener
    public void onOrderCancelled(OrderCancelledEvent event) {
        stock.release(event.lines());
    }
}
