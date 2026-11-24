package com.orbiterp.sales;

import com.orbiterp.common.TenantScopedRepository;
import org.springframework.data.jpa.repository.EntityGraph;

import java.util.List;

public interface SalesOrderRepository extends TenantScopedRepository<SalesOrder, Long> {

    @EntityGraph(attributePaths = "customer")
    List<SalesOrder> findAllByOrderByIdDesc();
}
