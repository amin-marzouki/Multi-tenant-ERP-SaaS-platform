package com.orbiterp.inventory;

import com.orbiterp.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/** How much of one item a warehouse holds, and how much of that is promised to confirmed orders. */
@Entity
@Table(name = "stock_levels")
@Getter
@Setter
@NoArgsConstructor
public class StockLevel extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "item_id")
    private Item item;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "warehouse_id")
    private Warehouse warehouse;

    @Column(name = "on_hand", nullable = false, precision = 15, scale = 3)
    private BigDecimal onHand = BigDecimal.ZERO;

    @Column(nullable = false, precision = 15, scale = 3)
    private BigDecimal reserved = BigDecimal.ZERO;

    public StockLevel(Item item, Warehouse warehouse) {
        this.item = item;
        this.warehouse = warehouse;
    }

    public BigDecimal available() {
        return onHand.subtract(reserved);
    }
}
