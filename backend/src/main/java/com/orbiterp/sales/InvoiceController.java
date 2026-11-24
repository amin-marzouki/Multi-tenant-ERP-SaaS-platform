package com.orbiterp.sales;

import com.orbiterp.sales.SalesDtos.DocumentSummary;
import com.orbiterp.sales.SalesDtos.InvoiceResponse;
import com.orbiterp.security.Access;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/sales/invoices")
@PreAuthorize(Access.SALES_OR_FINANCE)
@RequiredArgsConstructor
public class InvoiceController {

    private final InvoiceService invoiceService;

    @GetMapping
    public List<DocumentSummary> list() {
        return invoiceService.list();
    }

    @GetMapping("/{id}")
    public InvoiceResponse get(@PathVariable Long id) {
        return invoiceService.get(id);
    }

    @PostMapping("/{id}/post")
    @PreAuthorize(Access.FINANCE)
    public InvoiceResponse post(@PathVariable Long id) {
        return invoiceService.post(id);
    }
}
