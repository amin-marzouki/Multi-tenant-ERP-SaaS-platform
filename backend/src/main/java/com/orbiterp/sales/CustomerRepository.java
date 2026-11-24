package com.orbiterp.sales;

import com.orbiterp.common.TenantScopedRepository;

import java.util.List;

public interface CustomerRepository extends TenantScopedRepository<Customer, Long> {

    List<Customer> findAllByOrderByNameAsc();
}
