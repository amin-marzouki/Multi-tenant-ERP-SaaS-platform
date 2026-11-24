package com.orbiterp.common.events;

import com.orbiterp.common.DomainEvent;

import java.math.BigDecimal;
import java.time.LocalDate;

/** An invoice was posted: Finance writes the journal entry and opens the receivable. */
public record InvoicePostedEvent(
        Long tenantId,
        Long invoiceId,
        String invoiceNumber,
        Long customerId,
        String customerName,
        LocalDate issueDate,
        LocalDate dueDate,
        BigDecimal subtotal,
        BigDecimal taxTotal,
        BigDecimal total) implements DomainEvent {
}
