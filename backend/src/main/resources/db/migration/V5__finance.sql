-- Finance: chart of accounts, journal entries, accounts receivable and payments.
-- Receivables keep the sales invoice id without a foreign key: modules do not reach into each other's tables.

CREATE TABLE accounts (
    id           BIGINT       NOT NULL AUTO_INCREMENT,
    tenant_id    BIGINT       NOT NULL,
    code         VARCHAR(20)  NOT NULL,
    name         VARCHAR(255) NOT NULL,
    account_type VARCHAR(20)  NOT NULL,
    active       BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at   DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    created_by   VARCHAR(100) NULL,
    updated_at   DATETIME(6)  NULL,
    updated_by   VARCHAR(100) NULL,
    version      BIGINT       NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_accounts_tenant_code (tenant_id, code),
    CONSTRAINT fk_accounts_tenant FOREIGN KEY (tenant_id) REFERENCES tenants (id)
);

CREATE TABLE journal_entries (
    id               BIGINT        NOT NULL AUTO_INCREMENT,
    tenant_id        BIGINT        NOT NULL,
    number           VARCHAR(30)   NOT NULL,
    entry_date       DATE          NOT NULL,
    description      VARCHAR(500)  NOT NULL,
    source_type      VARCHAR(20)   NOT NULL,
    source_id        BIGINT        NULL,
    source_reference VARCHAR(50)   NULL,
    amount           DECIMAL(15,2) NOT NULL,
    created_at       DATETIME(6)   NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    created_by       VARCHAR(100)  NULL,
    updated_at       DATETIME(6)   NULL,
    updated_by       VARCHAR(100)  NULL,
    version          BIGINT        NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_journal_entries_tenant_number (tenant_id, number),
    KEY idx_journal_entries_tenant_date (tenant_id, entry_date),
    CONSTRAINT fk_journal_entries_tenant FOREIGN KEY (tenant_id) REFERENCES tenants (id)
);

CREATE TABLE journal_lines (
    id          BIGINT        NOT NULL AUTO_INCREMENT,
    tenant_id   BIGINT        NOT NULL,
    entry_id    BIGINT        NOT NULL,
    line_no     INT           NOT NULL,
    account_id  BIGINT        NOT NULL,
    description VARCHAR(255)  NULL,
    debit       DECIMAL(15,2) NOT NULL DEFAULT 0,
    credit      DECIMAL(15,2) NOT NULL DEFAULT 0,
    created_at  DATETIME(6)   NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    created_by  VARCHAR(100)  NULL,
    updated_at  DATETIME(6)   NULL,
    updated_by  VARCHAR(100)  NULL,
    version     BIGINT        NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    KEY idx_journal_lines_tenant_account (tenant_id, account_id),
    CONSTRAINT fk_journal_lines_tenant FOREIGN KEY (tenant_id) REFERENCES tenants (id),
    CONSTRAINT fk_journal_lines_entry FOREIGN KEY (entry_id) REFERENCES journal_entries (id) ON DELETE CASCADE,
    CONSTRAINT fk_journal_lines_account FOREIGN KEY (account_id) REFERENCES accounts (id)
);

CREATE TABLE receivables (
    id             BIGINT        NOT NULL AUTO_INCREMENT,
    tenant_id      BIGINT        NOT NULL,
    invoice_id     BIGINT        NOT NULL,
    invoice_number VARCHAR(30)   NOT NULL,
    customer_id    BIGINT        NOT NULL,
    customer_name  VARCHAR(255)  NOT NULL,
    issue_date     DATE          NOT NULL,
    due_date       DATE          NOT NULL,
    amount         DECIMAL(15,2) NOT NULL,
    open_amount    DECIMAL(15,2) NOT NULL,
    status         VARCHAR(20)   NOT NULL,
    created_at     DATETIME(6)   NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    created_by     VARCHAR(100)  NULL,
    updated_at     DATETIME(6)   NULL,
    updated_by     VARCHAR(100)  NULL,
    version        BIGINT        NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_receivables_tenant_invoice (tenant_id, invoice_id),
    KEY idx_receivables_tenant_status_due (tenant_id, status, due_date),
    CONSTRAINT fk_receivables_tenant FOREIGN KEY (tenant_id) REFERENCES tenants (id)
);

CREATE TABLE payments (
    id               BIGINT        NOT NULL AUTO_INCREMENT,
    tenant_id        BIGINT        NOT NULL,
    receivable_id    BIGINT        NOT NULL,
    payment_date     DATE          NOT NULL,
    amount           DECIMAL(15,2) NOT NULL,
    method           VARCHAR(20)   NOT NULL,
    reference        VARCHAR(100)  NULL,
    journal_entry_id BIGINT        NULL,
    created_at       DATETIME(6)   NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    created_by       VARCHAR(100)  NULL,
    updated_at       DATETIME(6)   NULL,
    updated_by       VARCHAR(100)  NULL,
    version          BIGINT        NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    KEY idx_payments_tenant_date (tenant_id, payment_date),
    CONSTRAINT fk_payments_tenant FOREIGN KEY (tenant_id) REFERENCES tenants (id),
    CONSTRAINT fk_payments_receivable FOREIGN KEY (receivable_id) REFERENCES receivables (id),
    CONSTRAINT fk_payments_journal_entry FOREIGN KEY (journal_entry_id) REFERENCES journal_entries (id)
);
