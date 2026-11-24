package com.orbiterp.finance;

import com.orbiterp.common.events.InvoicePostedEvent;
import com.orbiterp.finance.FinanceDtos.AgingBuckets;
import com.orbiterp.finance.FinanceDtos.AgingReport;
import com.orbiterp.finance.FinanceDtos.AgingRow;
import com.orbiterp.finance.FinanceDtos.ReceivableResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ReceivableService {

    private final ReceivableRepository receivables;

    @Transactional
    public void createFromInvoice(InvoicePostedEvent event) {
        if (receivables.existsByInvoiceId(event.invoiceId())) {
            return;
        }
        Receivable receivable = new Receivable();
        receivable.setInvoiceId(event.invoiceId());
        receivable.setInvoiceNumber(event.invoiceNumber());
        receivable.setCustomerId(event.customerId());
        receivable.setCustomerName(event.customerName());
        receivable.setIssueDate(event.issueDate());
        receivable.setDueDate(event.dueDate());
        receivable.setAmount(event.total());
        receivable.setOpenAmount(event.total());
        receivables.save(receivable);
    }

    @Transactional(readOnly = true)
    public List<ReceivableResponse> list(ReceivableStatus status) {
        List<Receivable> found = status == null
                ? receivables.findAllByOrderByDueDateAsc()
                : receivables.findByStatusOrderByDueDateAsc(status);
        LocalDate today = LocalDate.now();
        return found.stream().map(r -> toResponse(r, today)).toList();
    }

    /** Open amounts by customer: not yet due, 1-30, 31-60, 61-90 and over 90 days past due. */
    @Transactional(readOnly = true)
    public AgingReport aging(LocalDate asOf) {
        LocalDate date = asOf != null ? asOf : LocalDate.now();
        Map<Long, String> names = new LinkedHashMap<>();
        Map<Long, Buckets> byCustomer = new LinkedHashMap<>();
        Buckets totals = new Buckets();
        for (Receivable r : receivables.findByStatusOrderByDueDateAsc(ReceivableStatus.OPEN)) {
            long daysOverdue = ChronoUnit.DAYS.between(r.getDueDate(), date);
            names.putIfAbsent(r.getCustomerId(), r.getCustomerName());
            byCustomer.computeIfAbsent(r.getCustomerId(), id -> new Buckets()).add(daysOverdue, r.getOpenAmount());
            totals.add(daysOverdue, r.getOpenAmount());
        }
        List<AgingRow> rows = byCustomer.entrySet().stream()
                .map(e -> new AgingRow(e.getKey(), names.get(e.getKey()), e.getValue().toDto()))
                .toList();
        return new AgingReport(date, rows, totals.toDto());
    }

    static ReceivableResponse toResponse(Receivable r, LocalDate today) {
        long daysOverdue = r.getStatus() == ReceivableStatus.OPEN
                ? Math.max(0, ChronoUnit.DAYS.between(r.getDueDate(), today))
                : 0;
        return new ReceivableResponse(r.getId(), r.getInvoiceId(), r.getInvoiceNumber(), r.getCustomerId(),
                r.getCustomerName(), r.getIssueDate(), r.getDueDate(), r.getAmount(), r.getOpenAmount(),
                r.getStatus(), daysOverdue);
    }

    private static final class Buckets {
        private BigDecimal current = BigDecimal.ZERO;
        private BigDecimal days1to30 = BigDecimal.ZERO;
        private BigDecimal days31to60 = BigDecimal.ZERO;
        private BigDecimal days61to90 = BigDecimal.ZERO;
        private BigDecimal over90 = BigDecimal.ZERO;

        void add(long daysOverdue, BigDecimal amount) {
            if (daysOverdue <= 0) {
                current = current.add(amount);
            } else if (daysOverdue <= 30) {
                days1to30 = days1to30.add(amount);
            } else if (daysOverdue <= 60) {
                days31to60 = days31to60.add(amount);
            } else if (daysOverdue <= 90) {
                days61to90 = days61to90.add(amount);
            } else {
                over90 = over90.add(amount);
            }
        }

        AgingBuckets toDto() {
            BigDecimal total = current.add(days1to30).add(days31to60).add(days61to90).add(over90);
            return new AgingBuckets(current, days1to30, days31to60, days61to90, over90, total);
        }
    }
}
