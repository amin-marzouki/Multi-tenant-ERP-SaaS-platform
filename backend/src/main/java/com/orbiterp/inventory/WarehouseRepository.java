package com.orbiterp.inventory;

import com.orbiterp.common.TenantScopedRepository;

import java.util.List;
import java.util.Optional;

public interface WarehouseRepository extends TenantScopedRepository<Warehouse, Long> {

    Optional<Warehouse> findFirstByDefaultWarehouseTrue();

    List<Warehouse> findAllByOrderByCodeAsc();

    boolean existsByCode(String code);
}
