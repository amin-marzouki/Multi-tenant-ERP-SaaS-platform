package com.orbiterp.finance;

import com.orbiterp.common.BusinessException;
import com.orbiterp.common.Money;
import com.orbiterp.common.NotFoundException;
import com.orbiterp.common.events.InvoicePaidEvent;
import com.orbiterp.finance.FinanceDtos.PaymentRequest;
import com.orbiterp.finance.FinanceDtos.PaymentResponse;
import com.orbiterp.finance.JournalService.DraftLine;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository payments;
    private final ReceivableRepository receivables;
    private final AccountService accounts;
    private final JournalService journal;
    private final ApplicationEventPublisher events;

    /** A customer paid: cash in, receivable down. Once fully paid, Sales marks the invoice paid. */
    @Transactional
    public PaymentResponse record(PaymentRequest request) {
        Receivable receivable = receivables.findWithLockById(request.receivableId())
                .orElseThrow(() -> new NotFoundException("Receivable", request.receivableId()));
        if (receivable.getStatus() != ReceivableStatus.OPEN) {
            throw BusinessException.conflict("RECEIVABLE_CLOSED",
                    "Invoice " + receivable.getInvoiceNumber() + " is already paid");
        }
        BigDecimal amount = Money.round(request.amount());
        if (amount.compareTo(receivable.getOpenAmount()) > 0) {
            throw BusinessException.invalid("OVERPAYMENT", "Only %s is still open on invoice %s"
                    .formatted(receivable.getOpenAmount().toPlainString(), receivable.getInvoiceNumber()));
        }
        LocalDate date = request.paymentDate() != null ? request.paymentDate() : LocalDate.now();

        JournalEntry entry = journal.post(date, "Payment for invoice " + receivable.getInvoiceNumber(),
                JournalSource.PAYMENT, receivable.getId(), receivable.getInvoiceNumber(), List.of(
                        new DraftLine(accounts.requireByCode(PostingAccounts.CASH), request.method().name(), amount, BigDecimal.ZERO),
                        new DraftLine(accounts.requireByCode(PostingAccounts.RECEIVABLES), receivable.getCustomerName(), BigDecimal.ZERO, amount)));

        Payment payment = new Payment();
        payment.setReceivable(receivable);
        payment.setPaymentDate(date);
        payment.setAmount(amount);
        payment.setMethod(request.method());
        payment.setReference(request.reference() == null || request.reference().isBlank() ? null : request.reference().trim());
        payment.setJournalEntryId(entry.getId());
        payments.save(payment);

        receivable.setOpenAmount(receivable.getOpenAmount().subtract(amount));
        if (receivable.getOpenAmount().signum() == 0) {
            receivable.setStatus(ReceivableStatus.PAID);
            events.publishEvent(new InvoicePaidEvent(receivable.getTenantId(), receivable.getInvoiceId(),
                    receivable.getInvoiceNumber()));
        }
        return toResponse(payment);
    }

    @Transactional(readOnly = true)
    public List<PaymentResponse> list() {
        return payments.findTop200ByOrderByIdDesc().stream().map(PaymentService::toResponse).toList();
    }

    private static PaymentResponse toResponse(Payment p) {
        return new PaymentResponse(p.getId(), p.getReceivable().getId(), p.getReceivable().getInvoiceNumber(),
                p.getReceivable().getCustomerName(), p.getPaymentDate(), p.getAmount(), p.getMethod(),
                p.getReference(), p.getJournalEntryId());
    }
}
