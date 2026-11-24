package com.orbiterp.sales;

import com.orbiterp.common.BaseEntity;
import com.orbiterp.common.Money;
import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** The columns shared by quote, order and invoice lines. */
@MappedSuperclass
@Getter
@Setter
public abstract class SalesLine extends BaseEntity {

    @Column(name = "line_no", nullable = false)
    private int lineNo;

    /** The inventory item; kept as a plain id, as modules do not share entities. */
    @Column(name = "item_id", nullable = false)
    private Long itemId;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false, precision = 15, scale = 3)
    private BigDecimal quantity;

    @Column(name = "unit_price", nullable = false, precision = 15, scale = 2)
    private BigDecimal unitPrice;

    /** Percent, e.g. 19.00 for 19 %. */
    @Column(name = "tax_rate", nullable = false, precision = 5, scale = 2)
    private BigDecimal taxRate;

    public BigDecimal netAmount() {
        return Money.round(quantity.multiply(unitPrice));
    }

    public BigDecimal taxAmount() {
        return Money.round(netAmount().multiply(taxRate).divide(Money.HUNDRED, 4, RoundingMode.HALF_UP));
    }

    public void copyFrom(SalesLine other) {
        setItemId(other.getItemId());
        setDescription(other.getDescription());
        setQuantity(other.getQuantity());
        setUnitPrice(other.getUnitPrice());
        setTaxRate(other.getTaxRate());
    }
}
