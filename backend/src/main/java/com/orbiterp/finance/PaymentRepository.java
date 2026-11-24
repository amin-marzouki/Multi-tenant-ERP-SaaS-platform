package com.orbiterp.finance;

import com.orbiterp.common.TenantScopedRepository;
import org.springframework.data.jpa.repository.EntityGraph;

import java.util.List;

public interface PaymentRepository extends TenantScopedRepository<Payment, Long> {

    @EntityGraph(attributePaths = "receivable")
    List<Payment> findTop200ByOrderByIdDesc();
}
