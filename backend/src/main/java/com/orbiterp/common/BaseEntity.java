package com.orbiterp.common;

import com.orbiterp.tenant.CurrentTenantSupplier;
import com.orbiterp.tenant.TenantContext;
import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PostLoad;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

/**
 * Base class of every tenant-owned entity. Tenant isolation has three layers:
 *
 * <ol>
 *   <li>The tenant filter is switched on for every Hibernate session, so every JPQL query only
 *       sees rows of the tenant in {@link TenantContext}. Native SQL bypasses it: do not use it.</li>
 *   <li>The filter does not apply to loads by primary key, so {@link TenantScopedRepository}
 *       turns {@code findById} into a filtered query.</li>
 *   <li>Any row that still arrives from another tenant, by whatever path, is refused on load.</li>
 * </ol>
 *
 * New rows get their {@code tenant_id} from {@link TenantContext} when they are first saved.
 */
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@FilterDef(
        name = BaseEntity.TENANT_FILTER,
        defaultCondition = "tenant_id = :tenantId",
        parameters = @ParamDef(name = "tenantId", type = Long.class, resolver = CurrentTenantSupplier.class),
        autoEnabled = true)
@Filter(name = BaseEntity.TENANT_FILTER)
@Getter
@Setter
public abstract class BaseEntity {

    public static final String TENANT_FILTER = "tenantFilter";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false, updatable = false)
    private Long tenantId;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @CreatedBy
    @Column(name = "created_by", updatable = false)
    private String createdBy;

    @LastModifiedDate
    @Column(name = "updated_at")
    private Instant updatedAt;

    @LastModifiedBy
    @Column(name = "updated_by")
    private String updatedBy;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    @PrePersist
    protected void assignTenant() {
        if (tenantId == null) {
            tenantId = TenantContext.require();
        }
    }

    @PostLoad
    protected void checkTenant() {
        if (!tenantId.equals(TenantContext.get())) {
            throw new TenantIsolationException(getClass().getSimpleName(), id);
        }
    }
}
