-- Inventory: items, warehouses, stock levels per item and warehouse, and stock movements.

CREATE TABLE items (
    id          BIGINT        NOT NULL AUTO_INCREMENT,
    tenant_id   BIGINT        NOT NULL,
    sku         VARCHAR(50)   NOT NULL,
    name        VARCHAR(255)  NOT NULL,
    description VARCHAR(1000) NULL,
    unit        VARCHAR(20)   NOT NULL,
    sale_price  DECIMAL(15,2) NOT NULL DEFAULT 0,
    active      BOOLEAN       NOT NULL DEFAULT TRUE,
    created_at  DATETIME(6)   NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    created_by  VARCHAR(100)  NULL,
    updated_at  DATETIME(6)   NULL,
    updated_by  VARCHAR(100)  NULL,
    version     BIGINT        NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_items_tenant_sku (tenant_id, sku),
    CONSTRAINT fk_items_tenant FOREIGN KEY (tenant_id) REFERENCES tenants (id)
);

CREATE TABLE warehouses (
    id         BIGINT       NOT NULL AUTO_INCREMENT,
    tenant_id  BIGINT       NOT NULL,
    code       VARCHAR(30)  NOT NULL,
    name       VARCHAR(255) NOT NULL,
    is_default BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    created_by VARCHAR(100) NULL,
    updated_at DATETIME(6)  NULL,
    updated_by VARCHAR(100) NULL,
    version    BIGINT       NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_warehouses_tenant_code (tenant_id, code),
    CONSTRAINT fk_warehouses_tenant FOREIGN KEY (tenant_id) REFERENCES tenants (id)
);

CREATE TABLE stock_levels (
    id           BIGINT        NOT NULL AUTO_INCREMENT,
    tenant_id    BIGINT        NOT NULL,
    item_id      BIGINT        NOT NULL,
    warehouse_id BIGINT        NOT NULL,
    on_hand      DECIMAL(15,3) NOT NULL DEFAULT 0,
    reserved     DECIMAL(15,3) NOT NULL DEFAULT 0,
    created_at   DATETIME(6)   NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    created_by   VARCHAR(100)  NULL,
    updated_at   DATETIME(6)   NULL,
    updated_by   VARCHAR(100)  NULL,
    version      BIGINT        NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_stock_levels_tenant_item_warehouse (tenant_id, item_id, warehouse_id),
    CONSTRAINT fk_stock_levels_tenant FOREIGN KEY (tenant_id) REFERENCES tenants (id),
    CONSTRAINT fk_stock_levels_item FOREIGN KEY (item_id) REFERENCES items (id),
    CONSTRAINT fk_stock_levels_warehouse FOREIGN KEY (warehouse_id) REFERENCES warehouses (id)
);

CREATE TABLE stock_moves (
    id           BIGINT        NOT NULL AUTO_INCREMENT,
    tenant_id    BIGINT        NOT NULL,
    item_id      BIGINT        NOT NULL,
    warehouse_id BIGINT        NOT NULL,
    move_type    VARCHAR(20)   NOT NULL,
    quantity     DECIMAL(15,3) NOT NULL,
    reference    VARCHAR(50)   NULL,
    note         VARCHAR(500)  NULL,
    moved_at     DATETIME(6)   NOT NULL,
    created_at   DATETIME(6)   NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    created_by   VARCHAR(100)  NULL,
    updated_at   DATETIME(6)   NULL,
    updated_by   VARCHAR(100)  NULL,
    version      BIGINT        NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    KEY idx_stock_moves_tenant_item (tenant_id, item_id),
    KEY idx_stock_moves_tenant_moved_at (tenant_id, moved_at),
    CONSTRAINT fk_stock_moves_tenant FOREIGN KEY (tenant_id) REFERENCES tenants (id),
    CONSTRAINT fk_stock_moves_item FOREIGN KEY (item_id) REFERENCES items (id),
    CONSTRAINT fk_stock_moves_warehouse FOREIGN KEY (warehouse_id) REFERENCES warehouses (id)
);
