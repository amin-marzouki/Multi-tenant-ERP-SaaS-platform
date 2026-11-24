package com.orbiterp.inventory;

import com.orbiterp.common.TenantScopedRepository;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface StockLevelRepository extends TenantScopedRepository<StockLevel, Long> {

    /** Locks the row until the transaction ends, so two orders cannot reserve the same units. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<StockLevel> findByItemIdAndWarehouseId(Long itemId, Long warehouseId);

    @EntityGraph(attributePaths = {"item", "warehouse"})
    List<StockLevel> findAllByOrderByIdAsc();

    @Query("select l.item.id as itemId, sum(l.onHand) as onHand, sum(l.reserved) as reserved "
            + "from StockLevel l group by l.item.id")
    List<ItemStock> totalsByItem();

    interface ItemStock {
        Long getItemId();

        BigDecimal getOnHand();

        BigDecimal getReserved();
    }
}
