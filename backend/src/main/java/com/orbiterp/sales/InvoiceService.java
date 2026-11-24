package com.orbiterp.sales;

import com.orbiterp.common.BusinessException;
import com.orbiterp.common.NotFoundException;
import com.orbiterp.common.events.InvoicePostedEvent;
import com.orbiterp.common.sequence.DocumentNumberService;
import com.orbiterp.sales.SalesDtos.DocumentSummary;
import com.orbiterp.sales.SalesDtos.InvoiceResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InvoiceService {

    static final int DEFAULT_PAYMENT_TERM_DAYS = 30;

    private final InvoiceRepository invoices;
    private final DocumentNumberService numbers;
    private final ApplicationEventPublisher events;

    @Transactional(readOnly = true)
    public List<DocumentSummary> list() {
        return invoices.findAllByOrderByIdDesc().stream()
                .map(i -> new DocumentSummary(i.getId(), i.getNumber(), i.getCustomer().getId(),
                        i.getCustomer().getName(), i.getIssueDate(), i.getStatus().name(), i.getTotal()))
                .toList();
    }

    @Transactional(readOnly = true)
    public InvoiceResponse get(Long id) {
        return toResponse(require(id));
    }

    /** A draft invoice with the order's lines. */
    @Transactional
    public Invoice createFromOrder(SalesOrder order, LocalDate issueDate, LocalDate dueDate) {
        LocalDate issued = issueDate != null ? issueDate : LocalDate.now();
        LocalDate due = dueDate != null ? dueDate : issued.plusDays(DEFAULT_PAYMENT_TERM_DAYS);
        if (due.isBefore(issued)) {
            throw BusinessException.invalid("INVALID_DUE_DATE", "The due date cannot be before the issue date");
        }
        Invoice invoice = new Invoice();
        invoice.setNumber(numbers.next("INVOICE", "INV"));
        invoice.setCustomer(order.getCustomer());
        invoice.setOrderId(order.getId());
        invoice.setIssueDate(issued);
        invoice.setDueDate(due);
        invoice.setNotes(order.getNotes());
        for (SalesOrderLine orderLine : order.getLines()) {
            InvoiceLine line = new InvoiceLine();
            line.copyFrom(orderLine);
            invoice.addLine(line);
        }
        invoice.recalculateTotals();
        return invoices.save(invoice);
    }

    /** Posts the invoice; Finance books it and opens the receivable in the same transaction. */
    @Transactional
    public InvoiceResponse post(Long id) {
        Invoice invoice = require(id);
        if (invoice.getStatus() != InvoiceStatus.DRAFT) {
            throw BusinessException.conflict("INVALID_STATUS",
                    "Invoice %s is %s; only draft invoices can be posted".formatted(invoice.getNumber(), invoice.getStatus()));
        }
        if (invoice.getTotal().signum() <= 0) {
            throw BusinessException.invalid("EMPTY_INVOICE", "An invoice needs a total above zero to be posted");
        }
        invoice.setStatus(InvoiceStatus.POSTED);
        events.publishEvent(new InvoicePostedEvent(invoice.getTenantId(), invoice.getId(), invoice.getNumber(),
                invoice.getCustomer().getId(), invoice.getCustomer().getName(), invoice.getIssueDate(),
                invoice.getDueDate(), invoice.getSubtotal(), invoice.getTaxTotal(), invoice.getTotal()));
        return toResponse(invoice);
    }

    @Transactional
    public void markPaid(Long id) {
        Invoice invoice = require(id);
        if (invoice.getStatus() == InvoiceStatus.POSTED) {
            invoice.setStatus(InvoiceStatus.PAID);
        }
    }

    Invoice require(Long id) {
        return invoices.findById(id).orElseThrow(() -> new NotFoundException("Invoice", id));
    }

    InvoiceResponse toResponse(Invoice i) {
        return new InvoiceResponse(i.getId(), i.getNumber(), i.getCustomer().getId(), i.getCustomer().getName(),
                i.getIssueDate(), i.getDueDate(), i.getStatus(), i.getOrderId(), i.getNotes(),
                i.getSubtotal(), i.getTaxTotal(), i.getTotal(), SalesMapper.lines(i.getLines()));
    }
}
