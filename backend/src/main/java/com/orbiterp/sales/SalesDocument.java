package com.orbiterp.sales;

import com.orbiterp.common.BaseEntity;
import com.orbiterp.common.Money;
import jakarta.persistence.Column;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

/** What quotes, orders and invoices have in common: a number, a customer, lines and totals. */
@MappedSuperclass
@Getter
@Setter
public abstract class SalesDocument extends BaseEntity {

    @Column(nullable = false, length = 30)
    private String number;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @Column(length = 1000)
    private String notes;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal subtotal = BigDecimal.ZERO;

    @Column(name = "tax_total", nullable = false, precision = 15, scale = 2)
    private BigDecimal taxTotal = BigDecimal.ZERO;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal total = BigDecimal.ZERO;

    public abstract List<? extends SalesLine> getLines();

    public void recalculateTotals() {
        BigDecimal net = BigDecimal.ZERO;
        BigDecimal tax = BigDecimal.ZERO;
        for (SalesLine line : getLines()) {
            net = net.add(line.netAmount());
            tax = tax.add(line.taxAmount());
        }
        subtotal = Money.round(net);
        taxTotal = Money.round(tax);
        total = subtotal.add(taxTotal);
    }
}
