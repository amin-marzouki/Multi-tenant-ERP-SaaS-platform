-- Brings the V1 tables in line with BaseEntity, links users to roles
-- and adds the per-tenant document number sequences.

ALTER TABLE tenants
    MODIFY created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6);

ALTER TABLE users
    MODIFY created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    ADD COLUMN full_name  VARCHAR(150) NULL AFTER email,
    ADD COLUMN enabled    BOOLEAN      NOT NULL DEFAULT TRUE AFTER password_hash,
    ADD COLUMN created_by VARCHAR(100) NULL,
    ADD COLUMN updated_at DATETIME(6)  NULL,
    ADD COLUMN updated_by VARCHAR(100) NULL,
    ADD COLUMN version    BIGINT       NOT NULL DEFAULT 0;

ALTER TABLE roles
    ADD COLUMN created_at DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    ADD COLUMN created_by VARCHAR(100) NULL,
    ADD COLUMN updated_at DATETIME(6)  NULL,
    ADD COLUMN updated_by VARCHAR(100) NULL,
    ADD COLUMN version    BIGINT       NOT NULL DEFAULT 0;

-- Both sides are tenant-scoped, so the link inherits their tenant
CREATE TABLE user_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    KEY idx_user_roles_role (role_id),
    CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_user_roles_role FOREIGN KEY (role_id) REFERENCES roles (id) ON DELETE CASCADE
);

CREATE TABLE document_sequences (
    id         BIGINT       NOT NULL AUTO_INCREMENT,
    tenant_id  BIGINT       NOT NULL,
    name       VARCHAR(50)  NOT NULL,
    next_value BIGINT       NOT NULL,
    created_at DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    created_by VARCHAR(100) NULL,
    updated_at DATETIME(6)  NULL,
    updated_by VARCHAR(100) NULL,
    version    BIGINT       NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_document_sequences_tenant_name (tenant_id, name),
    CONSTRAINT fk_document_sequences_tenant FOREIGN KEY (tenant_id) REFERENCES tenants (id)
);
