package com.orbiterp.inventory;

import com.orbiterp.inventory.InventoryDtos.StockLevelResponse;
import com.orbiterp.inventory.InventoryDtos.StockMoveRequest;
import com.orbiterp.inventory.InventoryDtos.StockMoveResponse;
import com.orbiterp.security.Access;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/inventory")
@PreAuthorize(Access.WAREHOUSE)
@RequiredArgsConstructor
public class StockController {

    private final StockService stockService;

    @GetMapping("/stock")
    public List<StockLevelResponse> levels() {
        return stockService.levels();
    }

    @GetMapping("/stock-moves")
    public List<StockMoveResponse> moves() {
        return stockService.recentMoves();
    }

    @PostMapping("/stock-moves")
    @ResponseStatus(HttpStatus.CREATED)
    public StockMoveResponse record(@Valid @RequestBody StockMoveRequest request) {
        return stockService.recordManualMove(request);
    }
}
