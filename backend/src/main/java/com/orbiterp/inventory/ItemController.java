package com.orbiterp.inventory;

import com.orbiterp.inventory.InventoryDtos.ItemRequest;
import com.orbiterp.inventory.InventoryDtos.ItemResponse;
import com.orbiterp.security.Access;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/inventory/items")
@PreAuthorize(Access.WAREHOUSE)
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;

    /** Everyone can read items: sales reps pick them on quotes and orders. */
    @GetMapping
    @PreAuthorize(Access.ANY_ROLE)
    public List<ItemResponse> list() {
        return itemService.list();
    }

    @GetMapping("/{id}")
    @PreAuthorize(Access.ANY_ROLE)
    public ItemResponse get(@PathVariable Long id) {
        return itemService.get(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ItemResponse create(@Valid @RequestBody ItemRequest request) {
        return itemService.create(request);
    }

    @PutMapping("/{id}")
    public ItemResponse update(@PathVariable Long id, @Valid @RequestBody ItemRequest request) {
        return itemService.update(id, request);
    }
}
