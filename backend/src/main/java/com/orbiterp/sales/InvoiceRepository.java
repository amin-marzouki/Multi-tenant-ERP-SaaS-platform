package com.orbiterp.sales;

import com.orbiterp.common.TenantScopedRepository;
import org.springframework.data.jpa.repository.EntityGraph;

import java.util.List;

public interface InvoiceRepository extends TenantScopedRepository<Invoice, Long> {

    @EntityGraph(attributePaths = "customer")
    List<Invoice> findAllByOrderByIdDesc();
}
