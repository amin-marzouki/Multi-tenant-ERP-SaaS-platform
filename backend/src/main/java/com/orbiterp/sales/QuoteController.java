package com.orbiterp.sales;

import com.orbiterp.sales.SalesDtos.DocumentSummary;
import com.orbiterp.sales.SalesDtos.OrderResponse;
import com.orbiterp.sales.SalesDtos.QuoteRequest;
import com.orbiterp.sales.SalesDtos.QuoteResponse;
import com.orbiterp.security.Access;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/sales/quotes")
@PreAuthorize(Access.SALES)
@RequiredArgsConstructor
public class QuoteController {

    private final QuoteService quoteService;

    @GetMapping
    public List<DocumentSummary> list() {
        return quoteService.list();
    }

    @GetMapping("/{id}")
    public QuoteResponse get(@PathVariable Long id) {
        return quoteService.get(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public QuoteResponse create(@Valid @RequestBody QuoteRequest request) {
        return quoteService.create(request);
    }

    @PostMapping("/{id}/convert")
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponse convert(@PathVariable Long id) {
        return quoteService.convertToOrder(id);
    }

    @PostMapping("/{id}/reject")
    public QuoteResponse reject(@PathVariable Long id) {
        return quoteService.reject(id);
    }
}
