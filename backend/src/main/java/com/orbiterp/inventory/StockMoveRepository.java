package com.orbiterp.inventory;

import com.orbiterp.common.TenantScopedRepository;
import org.springframework.data.jpa.repository.EntityGraph;

import java.util.List;

public interface StockMoveRepository extends TenantScopedRepository<StockMove, Long> {

    @EntityGraph(attributePaths = {"item", "warehouse"})
    List<StockMove> findTop200ByOrderByIdDesc();
}
