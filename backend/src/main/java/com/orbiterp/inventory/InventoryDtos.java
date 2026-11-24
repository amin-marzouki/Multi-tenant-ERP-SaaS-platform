package com.orbiterp.inventory;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.Instant;

public final class InventoryDtos {

    private InventoryDtos() {
    }

    public record ItemRequest(
            @NotBlank @Size(max = 50) String sku,
            @NotBlank @Size(max = 255) String name,
            @Size(max = 1000) String description,
            @NotBlank @Size(max = 20) String unit,
            @NotNull @PositiveOrZero BigDecimal salePrice,
            Boolean active) {
    }

    /** onHand and available are summed over every warehouse. */
    public record ItemResponse(
            Long id,
            String sku,
            String name,
            String description,
            String unit,
            BigDecimal salePrice,
            boolean active,
            BigDecimal onHand,
            BigDecimal available) {
    }

    public record WarehouseRequest(
            @NotBlank @Size(max = 30) String code,
            @NotBlank @Size(max = 255) String name,
            boolean defaultWarehouse) {
    }

    public record WarehouseResponse(Long id, String code, String name, boolean defaultWarehouse) {
    }

    public record StockLevelResponse(
            Long id,
            Long itemId,
            String sku,
            String itemName,
            Long warehouseId,
            String warehouseCode,
            BigDecimal onHand,
            BigDecimal reserved,
            BigDecimal available) {
    }

    /** A receipt (positive quantity) or an adjustment (signed). No warehouse means the default one. */
    public record StockMoveRequest(
            @NotNull StockMoveType type,
            @NotNull Long itemId,
            Long warehouseId,
            @NotNull BigDecimal quantity,
            @Size(max = 50) String reference,
            @Size(max = 500) String note) {
    }

    public record StockMoveResponse(
            Long id,
            Instant movedAt,
            StockMoveType type,
            Long itemId,
            String sku,
            String itemName,
            String warehouseCode,
            BigDecimal quantity,
            String reference,
            String note,
            String createdBy) {
    }
}
