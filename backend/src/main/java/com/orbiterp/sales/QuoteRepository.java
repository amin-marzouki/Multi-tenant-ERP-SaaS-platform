package com.orbiterp.sales;

import com.orbiterp.common.TenantScopedRepository;
import org.springframework.data.jpa.repository.EntityGraph;

import java.util.List;

public interface QuoteRepository extends TenantScopedRepository<Quote, Long> {

    @EntityGraph(attributePaths = "customer")
    List<Quote> findAllByOrderByIdDesc();
}
