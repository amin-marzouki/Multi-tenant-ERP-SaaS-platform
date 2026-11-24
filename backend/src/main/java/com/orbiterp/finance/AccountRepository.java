package com.orbiterp.finance;

import com.orbiterp.common.TenantScopedRepository;

import java.util.List;
import java.util.Optional;

public interface AccountRepository extends TenantScopedRepository<Account, Long> {

    Optional<Account> findByCode(String code);

    boolean existsByCode(String code);

    List<Account> findAllByOrderByCodeAsc();
}
