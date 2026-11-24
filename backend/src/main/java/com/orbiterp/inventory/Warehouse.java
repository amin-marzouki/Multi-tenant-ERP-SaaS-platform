package com.orbiterp.inventory;

import com.orbiterp.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "warehouses")
@Getter
@Setter
public class Warehouse extends BaseEntity {

    @Column(nullable = false, length = 30)
    private String code;

    @Column(nullable = false)
    private String name;

    /** Sales orders reserve and ship stock from the default warehouse. */
    @Column(name = "is_default", nullable = false)
    private boolean defaultWarehouse;
}
