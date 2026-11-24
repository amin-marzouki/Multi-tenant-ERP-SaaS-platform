package com.orbiterp.inventory;

import com.orbiterp.common.BusinessException;
import com.orbiterp.common.NotFoundException;
import com.orbiterp.inventory.InventoryDtos.ItemRequest;
import com.orbiterp.inventory.InventoryDtos.ItemResponse;
import com.orbiterp.inventory.StockLevelRepository.ItemStock;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemService {

    private final ItemRepository items;
    private final StockLevelRepository levels;

    @Transactional(readOnly = true)
    public List<ItemResponse> list() {
        Map<Long, ItemStock> stock = levels.totalsByItem().stream()
                .collect(Collectors.toMap(ItemStock::getItemId, Function.identity()));
        return items.findAllByOrderBySkuAsc().stream()
                .map(item -> toResponse(item, stock.get(item.getId())))
                .toList();
    }

    @Transactional(readOnly = true)
    public ItemResponse get(Long id) {
        return toResponse(require(id), null);
    }

    @Transactional
    public ItemResponse create(ItemRequest request) {
        String sku = request.sku().trim();
        if (items.existsBySku(sku)) {
            throw BusinessException.conflict("SKU_TAKEN", "An item with SKU " + sku + " already exists");
        }
        Item item = new Item();
        apply(item, request);
        return toResponse(items.save(item), null);
    }

    @Transactional
    public ItemResponse update(Long id, ItemRequest request) {
        Item item = require(id);
        String sku = request.sku().trim();
        if (!sku.equals(item.getSku()) && items.existsBySku(sku)) {
            throw BusinessException.conflict("SKU_TAKEN", "An item with SKU " + sku + " already exists");
        }
        apply(item, request);
        return toResponse(item, null);
    }

    Item require(Long id) {
        return items.findById(id).orElseThrow(() -> new NotFoundException("Item", id));
    }

    Optional<Item> find(Long id) {
        return items.findById(id);
    }

    private static void apply(Item item, ItemRequest request) {
        item.setSku(request.sku().trim());
        item.setName(request.name().trim());
        item.setDescription(request.description() == null || request.description().isBlank() ? null : request.description().trim());
        item.setUnit(request.unit().trim());
        item.setSalePrice(request.salePrice());
        item.setActive(request.active() == null || request.active());
    }

    private static ItemResponse toResponse(Item item, ItemStock stock) {
        BigDecimal onHand = stock == null ? BigDecimal.ZERO : stock.getOnHand();
        BigDecimal reserved = stock == null ? BigDecimal.ZERO : stock.getReserved();
        return new ItemResponse(item.getId(), item.getSku(), item.getName(), item.getDescription(), item.getUnit(),
                item.getSalePrice(), item.isActive(), onHand, onHand.subtract(reserved));
    }
}
