package com.orbiterp.inventory;

import com.orbiterp.common.BusinessException;
import com.orbiterp.common.NotFoundException;
import com.orbiterp.inventory.InventoryDtos.WarehouseRequest;
import com.orbiterp.inventory.InventoryDtos.WarehouseResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class WarehouseService {

    private final WarehouseRepository warehouses;

    @Transactional(readOnly = true)
    public List<WarehouseResponse> list() {
        return warehouses.findAllByOrderByCodeAsc().stream().map(WarehouseService::toResponse).toList();
    }

    @Transactional
    public WarehouseResponse create(WarehouseRequest request) {
        String code = request.code().trim().toUpperCase(Locale.ROOT);
        if (warehouses.existsByCode(code)) {
            throw BusinessException.conflict("WAREHOUSE_CODE_TAKEN", "A warehouse with code " + code + " already exists");
        }
        boolean makeDefault = request.defaultWarehouse() || warehouses.findFirstByDefaultWarehouseTrue().isEmpty();
        if (makeDefault) {
            warehouses.findAll().forEach(w -> w.setDefaultWarehouse(false));
        }
        Warehouse warehouse = new Warehouse();
        warehouse.setCode(code);
        warehouse.setName(request.name().trim());
        warehouse.setDefaultWarehouse(makeDefault);
        return toResponse(warehouses.save(warehouse));
    }

    /** Every new tenant starts with one default warehouse. */
    @Transactional
    public void createDefault() {
        create(new WarehouseRequest("MAIN", "Main warehouse", true));
    }

    Warehouse require(Long id) {
        return warehouses.findById(id).orElseThrow(() -> new NotFoundException("Warehouse", id));
    }

    Warehouse requireDefault() {
        return warehouses.findFirstByDefaultWarehouseTrue()
                .orElseThrow(() -> BusinessException.conflict("NO_DEFAULT_WAREHOUSE", "Set a default warehouse first"));
    }

    static WarehouseResponse toResponse(Warehouse w) {
        return new WarehouseResponse(w.getId(), w.getCode(), w.getName(), w.isDefaultWarehouse());
    }
}
