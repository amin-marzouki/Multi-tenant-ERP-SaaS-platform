package com.orbiterp.finance;

import com.orbiterp.common.events.InvoicePostedEvent;
import com.orbiterp.common.events.TenantCreatedEvent;
import com.orbiterp.finance.JournalService.DraftLine;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
class FinanceEventListener {

    private final AccountService accounts;
    private final JournalService journal;
    private final ReceivableService receivables;

    @EventListener
    public void onTenantCreated(TenantCreatedEvent event) {
        accounts.createDefaultChart();
    }

    /** Debit receivables with the total; credit revenue with the net amount and tax payable with the tax. */
    @EventListener
    public void onInvoicePosted(InvoicePostedEvent event) {
        List<DraftLine> lines = new ArrayList<>();
        lines.add(new DraftLine(accounts.requireByCode(PostingAccounts.RECEIVABLES), event.customerName(),
                event.total(), BigDecimal.ZERO));
        lines.add(new DraftLine(accounts.requireByCode(PostingAccounts.SALES_REVENUE), "Sales",
                BigDecimal.ZERO, event.subtotal()));
        if (event.taxTotal().signum() > 0) {
            lines.add(new DraftLine(accounts.requireByCode(PostingAccounts.SALES_TAX), "Sales tax",
                    BigDecimal.ZERO, event.taxTotal()));
        }
        journal.post(event.issueDate(), "Invoice %s to %s".formatted(event.invoiceNumber(), event.customerName()),
                JournalSource.INVOICE, event.invoiceId(), event.invoiceNumber(), lines);
        receivables.createFromInvoice(event);
    }
}
