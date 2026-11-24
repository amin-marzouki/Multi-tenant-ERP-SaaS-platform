package com.orbiterp.sales;

import com.orbiterp.sales.SalesDtos.DocumentSummary;
import com.orbiterp.sales.SalesDtos.InvoiceRequest;
import com.orbiterp.sales.SalesDtos.InvoiceResponse;
import com.orbiterp.sales.SalesDtos.OrderRequest;
import com.orbiterp.sales.SalesDtos.OrderResponse;
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
@RequestMapping("/api/sales/orders")
@PreAuthorize(Access.SALES)
@RequiredArgsConstructor
public class SalesOrderController {

    private final SalesOrderService orderService;

    @GetMapping
    @PreAuthorize(Access.ANY_ROLE)
    public List<DocumentSummary> list() {
        return orderService.list();
    }

    @GetMapping("/{id}")
    @PreAuthorize(Access.ANY_ROLE)
    public OrderResponse get(@PathVariable Long id) {
        return orderService.get(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponse create(@Valid @RequestBody OrderRequest request) {
        return orderService.create(request);
    }

    @PostMapping("/{id}/confirm")
    public OrderResponse confirm(@PathVariable Long id) {
        return orderService.confirm(id);
    }

    @PostMapping("/{id}/ship")
    @PreAuthorize(Access.WAREHOUSE)
    public OrderResponse ship(@PathVariable Long id) {
        return orderService.ship(id);
    }

    @PostMapping("/{id}/cancel")
    public OrderResponse cancel(@PathVariable Long id) {
        return orderService.cancel(id);
    }

    @PostMapping("/{id}/invoice")
    @PreAuthorize(Access.SALES_OR_FINANCE)
    @ResponseStatus(HttpStatus.CREATED)
    public InvoiceResponse invoice(@PathVariable Long id, @Valid @RequestBody(required = false) InvoiceRequest request) {
        return orderService.invoice(id, request);
    }
}
