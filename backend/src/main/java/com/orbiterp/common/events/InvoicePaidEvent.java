package com.orbiterp.common.events;

import com.orbiterp.common.DomainEvent;

/** Finance received the full amount of an invoice: Sales marks it paid. */
public record InvoicePaidEvent(Long tenantId, Long invoiceId, String invoiceNumber) implements DomainEvent {
}
