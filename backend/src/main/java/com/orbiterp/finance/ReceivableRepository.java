package com.orbiterp.finance;

import com.orbiterp.common.TenantScopedRepository;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;

import java.util.List;
import java.util.Optional;

public interface ReceivableRepository extends TenantScopedRepository<Receivable, Long> {

    List<Receivable> findAllByOrderByDueDateAsc();

    List<Receivable> findByStatusOrderByDueDateAsc(ReceivableStatus status);

    boolean existsByInvoiceId(Long invoiceId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Receivable> findWithLockById(Long id);
}
