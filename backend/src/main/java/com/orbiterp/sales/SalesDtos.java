package com.orbiterp.sales;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public final class SalesDtos {

    private SalesDtos() {
    }

    public record CustomerRequest(
            @NotBlank @Size(max = 255) String name,
            @Email @Size(max = 150) String email,
            @Size(max = 50) String phone,
            @Size(max = 500) String address) {
    }

    public record CustomerResponse(Long id, String code, String name, String email, String phone, String address) {
    }

    public record LineRequest(
            @NotNull Long itemId,
            @NotBlank @Size(max = 255) String description,
            @NotNull @Positive BigDecimal quantity,
            @NotNull @PositiveOrZero BigDecimal unitPrice,
            @NotNull @PositiveOrZero @DecimalMax("100") BigDecimal taxRate) {
    }

    public record LineResponse(
            Long id,
            int lineNo,
            Long itemId,
            String description,
            BigDecimal quantity,
            BigDecimal unitPrice,
            BigDecimal taxRate,
            BigDecimal netAmount,
            BigDecimal taxAmount) {
    }

    /** One row of a quote, order or invoice list. */
    public record DocumentSummary(
            Long id,
            String number,
            Long customerId,
            String customerName,
            LocalDate date,
            String status,
            BigDecimal total) {
    }

    public record QuoteRequest(
            @NotNull Long customerId,
            LocalDate quoteDate,
            LocalDate validUntil,
            @Size(max = 1000) String notes,
            @NotEmpty @Valid List<LineRequest> lines) {
    }

    public record QuoteResponse(
            Long id,
            String number,
            Long customerId,
            String customerName,
            LocalDate quoteDate,
            LocalDate validUntil,
            QuoteStatus status,
            Long orderId,
            String notes,
            BigDecimal subtotal,
            BigDecimal taxTotal,
            BigDecimal total,
            List<LineResponse> lines) {
    }

    public record OrderRequest(
            @NotNull Long customerId,
            LocalDate orderDate,
            @Size(max = 1000) String notes,
            @NotEmpty @Valid List<LineRequest> lines) {
    }

    public record OrderResponse(
            Long id,
            String number,
            Long customerId,
            String customerName,
            LocalDate orderDate,
            OrderStatus status,
            Long quoteId,
            Long invoiceId,
            String notes,
            BigDecimal subtotal,
            BigDecimal taxTotal,
            BigDecimal total,
            List<LineResponse> lines) {
    }

    /** Both dates are optional: today, and 30 days after the issue date. */
    public record InvoiceRequest(LocalDate issueDate, LocalDate dueDate) {
    }

    public record InvoiceResponse(
            Long id,
            String number,
            Long customerId,
            String customerName,
            LocalDate issueDate,
            LocalDate dueDate,
            InvoiceStatus status,
            Long orderId,
            String notes,
            BigDecimal subtotal,
            BigDecimal taxTotal,
            BigDecimal total,
            List<LineResponse> lines) {
    }
}
