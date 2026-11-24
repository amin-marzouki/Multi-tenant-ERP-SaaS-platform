package com.orbiterp.common;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

/**
 * Base repository of every tenant-owned entity.
 *
 * <p>Hibernate's tenant filter applies to queries but not to a plain load by primary key, which is
 * what {@code findById} normally does. Here {@code findById} is a JPQL query instead, so the filter
 * applies and another tenant's id simply finds nothing. Use this instead of {@link JpaRepository}.
 */
@NoRepositoryBean
public interface TenantScopedRepository<T extends BaseEntity, ID> extends JpaRepository<T, ID> {

    @Override
    @Query("select e from #{#entityName} e where e.id = :id")
    Optional<T> findById(@Param("id") ID id);

    @Override
    @Query("select count(e) > 0 from #{#entityName} e where e.id = :id")
    boolean existsById(@Param("id") ID id);
}
