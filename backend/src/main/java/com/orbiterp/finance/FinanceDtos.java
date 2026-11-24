package com.orbiterp.finance;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public final class FinanceDtos {

    private FinanceDtos() {
    }

    public record AccountRequest(
            @NotBlank @Size(max = 20) String code,
            @NotBlank @Size(max = 255) String name,
            @NotNull AccountType type) {
    }

    public record AccountResponse(Long id, String code, String name, AccountType type, boolean active) {
    }

    public record JournalLineRequest(
            @NotNull Long accountId,
            @Size(max = 255) String description,
            @NotNull @PositiveOrZero BigDecimal debit,
            @NotNull @PositiveOrZero BigDecimal credit) {
    }

    public record JournalEntryRequest(
            LocalDate entryDate,
            @NotBlank @Size(max = 500) String description,
            @NotNull @Size(min = 2, message = "an entry needs at least two lines") @Valid List<JournalLineRequest> lines) {
    }

    public record JournalLineResponse(
            Long id,
            int lineNo,
            Long accountId,
            String accountCode,
            String accountName,
            String description,
            BigDecimal debit,
            BigDecimal credit) {
    }

    public record JournalEntrySummary(
            Long id,
            String number,
            LocalDate entryDate,
            String description,
            JournalSource sourceType,
            String sourceReference,
            BigDecimal amount) {
    }

    public record JournalEntryResponse(
            Long id,
            String number,
            LocalDate entryDate,
            String description,
            JournalSource sourceType,
            String sourceReference,
            BigDecimal amount,
            List<JournalLineResponse> lines) {
    }

    public record ReceivableResponse(
            Long id,
            Long invoiceId,
            String invoiceNumber,
            Long customerId,
            String customerName,
            LocalDate issueDate,
            LocalDate dueDate,
            BigDecimal amount,
            BigDecimal openAmount,
            ReceivableStatus status,
            long daysOverdue) {
    }

    public record AgingBuckets(
            BigDecimal current,
            BigDecimal days1to30,
            BigDecimal days31to60,
            BigDecimal days61to90,
            BigDecimal over90,
            BigDecimal total) {
    }

    public record AgingRow(Long customerId, String customerName, AgingBuckets buckets) {
    }

    /** Open receivables by customer and by how many days past due they are. */
    public record AgingReport(LocalDate asOf, List<AgingRow> rows, AgingBuckets totals) {
    }

    public record PaymentRequest(
            @NotNull Long receivableId,
            LocalDate paymentDate,
            @NotNull @Positive BigDecimal amount,
            @NotNull PaymentMethod method,
            @Size(max = 100) String reference) {
    }

    public record PaymentResponse(
            Long id,
            Long receivableId,
            String invoiceNumber,
            String customerName,
            LocalDate paymentDate,
            BigDecimal amount,
            PaymentMethod method,
            String reference,
            Long journalEntryId) {
    }

    /** balance = debit - credit; positive is a debit balance. */
    public record TrialBalanceRow(
            Long accountId,
            String code,
            String name,
            AccountType type,
            BigDecimal debit,
            BigDecimal credit,
            BigDecimal balance) {
    }

    public record TrialBalance(List<TrialBalanceRow> rows, BigDecimal totalDebit, BigDecimal totalCredit) {
    }
}
