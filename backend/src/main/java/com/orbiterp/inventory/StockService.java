package com.orbiterp.inventory;

import com.orbiterp.common.BusinessException;
import com.orbiterp.common.Money;
import com.orbiterp.common.events.OrderLineQuantity;
import com.orbiterp.inventory.InventoryDtos.StockLevelResponse;
import com.orbiterp.inventory.InventoryDtos.StockMoveRequest;
import com.orbiterp.inventory.InventoryDtos.StockMoveResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Stock levels and movements. Sales orders reserve stock when confirmed, issue it when shipped
 * and release it when cancelled, always from the default warehouse.
 */
@Service
@RequiredArgsConstructor
public class StockService {

    private final StockLevelRepository levels;
    private final StockMoveRepository moves;
    private final ItemService items;
    private final WarehouseService warehouses;

    @Transactional(readOnly = true)
    public List<StockLevelResponse> levels() {
        return levels.findAllByOrderByIdAsc().stream()
                .map(l -> new StockLevelResponse(l.getId(), l.getItem().getId(), l.getItem().getSku(),
                        l.getItem().getName(), l.getWarehouse().getId(), l.getWarehouse().getCode(),
                        l.getOnHand(), l.getReserved(), l.available()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<StockMoveResponse> recentMoves() {
        return moves.findTop200ByOrderByIdDesc().stream().map(StockService::toResponse).toList();
    }

    /** Goods received, or a count correction. Issues only happen by shipping an order. */
    @Transactional
    public StockMoveResponse recordManualMove(StockMoveRequest request) {
        Item item = items.require(request.itemId());
        Warehouse warehouse = request.warehouseId() == null
                ? warehouses.requireDefault()
                : warehouses.require(request.warehouseId());
        StockLevel level = lock(item, warehouse);
        BigDecimal quantity = request.quantity();

        switch (request.type()) {
            case RECEIPT -> {
                if (quantity.signum() <= 0) {
                    throw BusinessException.invalid("INVALID_QUANTITY", "A receipt needs a quantity above zero");
                }
                level.setOnHand(level.getOnHand().add(quantity));
            }
            case ADJUSTMENT -> {
                if (quantity.signum() == 0) {
                    throw BusinessException.invalid("INVALID_QUANTITY", "An adjustment cannot be zero");
                }
                BigDecimal newOnHand = level.getOnHand().add(quantity);
                if (newOnHand.compareTo(level.getReserved()) < 0) {
                    throw BusinessException.conflict("BELOW_RESERVED", "%s would drop to %s, below the %s reserved for orders"
                            .formatted(item.getSku(), Money.plain(newOnHand), Money.plain(level.getReserved())));
                }
                level.setOnHand(newOnHand);
            }
            case ISSUE -> throw BusinessException.invalid("ISSUE_NOT_ALLOWED",
                    "Stock is issued by shipping a sales order");
        }
        return toResponse(moves.save(newMove(item, warehouse, request.type(), quantity,
                request.reference(), request.note())));
    }

    @Transactional
    public void reserve(String orderNumber, List<OrderLineQuantity> lines) {
        Warehouse warehouse = warehouses.requireDefault();
        totalsPerItem(lines).forEach((itemId, quantity) -> {
            Item item = items.find(itemId).orElseThrow(() -> BusinessException.invalid("UNKNOWN_ITEM",
                    "Order %s refers to item %d, which does not exist".formatted(orderNumber, itemId)));
            StockLevel level = lock(item, warehouse);
            if (level.available().compareTo(quantity) < 0) {
                throw BusinessException.conflict("INSUFFICIENT_STOCK", "Not enough %s in stock: %s available, %s needed"
                        .formatted(item.getSku(), Money.plain(level.available()), Money.plain(quantity)));
            }
            level.setReserved(level.getReserved().add(quantity));
        });
    }

    @Transactional
    public void release(List<OrderLineQuantity> lines) {
        Warehouse warehouse = warehouses.requireDefault();
        totalsPerItem(lines).forEach((itemId, quantity) -> items.find(itemId).ifPresent(item -> {
            StockLevel level = lock(item, warehouse);
            level.setReserved(level.getReserved().subtract(quantity).max(BigDecimal.ZERO));
        }));
    }

    /** The reserved units leave the warehouse. */
    @Transactional
    public void issue(String orderNumber, List<OrderLineQuantity> lines) {
        Warehouse warehouse = warehouses.requireDefault();
        totalsPerItem(lines).forEach((itemId, quantity) -> {
            Item item = items.require(itemId);
            StockLevel level = lock(item, warehouse);
            if (level.getOnHand().compareTo(quantity) < 0) {
                throw BusinessException.conflict("INSUFFICIENT_STOCK", "Only %s of %s on hand, %s to ship"
                        .formatted(Money.plain(level.getOnHand()), item.getSku(), Money.plain(quantity)));
            }
            level.setReserved(level.getReserved().subtract(quantity).max(BigDecimal.ZERO));
            level.setOnHand(level.getOnHand().subtract(quantity));
            moves.save(newMove(item, warehouse, StockMoveType.ISSUE, quantity.negate(), orderNumber, "Shipped"));
        });
    }

    private StockLevel lock(Item item, Warehouse warehouse) {
        return levels.findByItemIdAndWarehouseId(item.getId(), warehouse.getId())
                .orElseGet(() -> levels.save(new StockLevel(item, warehouse)));
    }

    private static Map<Long, BigDecimal> totalsPerItem(List<OrderLineQuantity> lines) {
        Map<Long, BigDecimal> totals = new LinkedHashMap<>();
        for (OrderLineQuantity line : lines) {
            totals.merge(line.itemId(), line.quantity(), BigDecimal::add);
        }
        return totals;
    }

    private static StockMove newMove(Item item, Warehouse warehouse, StockMoveType type, BigDecimal quantity,
                                     String reference, String note) {
        StockMove move = new StockMove();
        move.setItem(item);
        move.setWarehouse(warehouse);
        move.setType(type);
        move.setQuantity(quantity);
        move.setReference(reference == null || reference.isBlank() ? null : reference.trim());
        move.setNote(note == null || note.isBlank() ? null : note.trim());
        move.setMovedAt(Instant.now());
        return move;
    }

    private static StockMoveResponse toResponse(StockMove m) {
        return new StockMoveResponse(m.getId(), m.getMovedAt(), m.getType(), m.getItem().getId(), m.getItem().getSku(),
                m.getItem().getName(), m.getWarehouse().getCode(), m.getQuantity(), m.getReference(), m.getNote(),
                m.getCreatedBy());
    }
}
