package com.orbiterp.inventory;

import com.orbiterp.common.TenantScopedRepository;

import java.util.List;

public interface ItemRepository extends TenantScopedRepository<Item, Long> {

    boolean existsBySku(String sku);

    List<Item> findAllByOrderBySkuAsc();
}
