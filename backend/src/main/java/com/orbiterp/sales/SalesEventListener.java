package com.orbiterp.sales;

import com.orbiterp.common.events.InvoicePaidEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class SalesEventListener {

    private final InvoiceService invoices;

    @EventListener
    public void onInvoicePaid(InvoicePaidEvent event) {
        invoices.markPaid(event.invoiceId());
    }
}
