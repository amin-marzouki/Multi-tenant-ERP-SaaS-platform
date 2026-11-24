package com.orbiterp.sales;

import com.orbiterp.common.events.OrderLineQuantity;
import com.orbiterp.sales.SalesDtos.LineRequest;
import com.orbiterp.sales.SalesDtos.LineResponse;

import java.util.List;

final class SalesMapper {

    private SalesMapper() {
    }

    static <L extends SalesLine> L fill(L line, LineRequest request) {
        line.setItemId(request.itemId());
        line.setDescription(request.description().trim());
        line.setQuantity(request.quantity());
        line.setUnitPrice(request.unitPrice());
        line.setTaxRate(request.taxRate());
        return line;
    }

    static List<LineResponse> lines(List<? extends SalesLine> lines) {
        return lines.stream()
                .map(l -> new LineResponse(l.getId(), l.getLineNo(), l.getItemId(), l.getDescription(),
                        l.getQuantity(), l.getUnitPrice(), l.getTaxRate(), l.netAmount(), l.taxAmount()))
                .toList();
    }

    static List<OrderLineQuantity> quantities(List<? extends SalesLine> lines) {
        return lines.stream().map(l -> new OrderLineQuantity(l.getItemId(), l.getQuantity())).toList();
    }
}
