package com.orbiterp.sales;

import com.orbiterp.common.BusinessException;
import com.orbiterp.common.NotFoundException;
import com.orbiterp.common.events.OrderCancelledEvent;
import com.orbiterp.common.events.OrderConfirmedEvent;
import com.orbiterp.common.events.OrderShippedEvent;
import com.orbiterp.common.sequence.DocumentNumberService;
import com.orbiterp.sales.SalesDtos.DocumentSummary;
import com.orbiterp.sales.SalesDtos.InvoiceRequest;
import com.orbiterp.sales.SalesDtos.InvoiceResponse;
import com.orbiterp.sales.SalesDtos.OrderRequest;
import com.orbiterp.sales.SalesDtos.OrderResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class SalesOrderService {

    private final SalesOrderRepository orders;
    private final CustomerService customers;
    private final InvoiceService invoices;
    private final DocumentNumberService numbers;
    private final ApplicationEventPublisher events;

    @Transactional(readOnly = true)
    public List<DocumentSummary> list() {
        return orders.findAllByOrderByIdDesc().stream()
                .map(o -> new DocumentSummary(o.getId(), o.getNumber(), o.getCustomer().getId(),
                        o.getCustomer().getName(), o.getOrderDate(), o.getStatus().name(), o.getTotal()))
                .toList();
    }

    @Transactional(readOnly = true)
    public OrderResponse get(Long id) {
        return toResponse(require(id));
    }

    @Transactional
    public OrderResponse create(OrderRequest request) {
        SalesOrder order = newOrder(customers.require(request.customerId()), request.orderDate(), request.notes());
        request.lines().forEach(r -> order.addLine(SalesMapper.fill(new SalesOrderLine(), r)));
        order.recalculateTotals();
        return toResponse(orders.save(order));
    }

    @Transactional
    public SalesOrder createFromQuote(Quote quote) {
        SalesOrder order = newOrder(quote.getCustomer(), null, quote.getNotes());
        order.setQuoteId(quote.getId());
        for (QuoteLine quoteLine : quote.getLines()) {
            SalesOrderLine line = new SalesOrderLine();
            line.copyFrom(quoteLine);
            order.addLine(line);
        }
        order.recalculateTotals();
        return orders.save(order);
    }

    /** Confirms the order. Inventory reserves the stock; if it cannot, nothing is saved. */
    @Transactional
    public OrderResponse confirm(Long id) {
        SalesOrder order = require(id);
        expectStatus(order, Set.of(OrderStatus.DRAFT), "confirmed");
        order.setStatus(OrderStatus.CONFIRMED);
        events.publishEvent(new OrderConfirmedEvent(order.getTenantId(), order.getId(), order.getNumber(),
                SalesMapper.quantities(order.getLines())));
        return toResponse(order);
    }

    /** Ships the order: Inventory issues the reserved stock. */
    @Transactional
    public OrderResponse ship(Long id) {
        SalesOrder order = require(id);
        expectStatus(order, Set.of(OrderStatus.CONFIRMED), "shipped");
        order.setStatus(OrderStatus.SHIPPED);
        events.publishEvent(new OrderShippedEvent(order.getTenantId(), order.getId(), order.getNumber(),
                SalesMapper.quantities(order.getLines())));
        return toResponse(order);
    }

    @Transactional
    public OrderResponse cancel(Long id) {
        SalesOrder order = require(id);
        expectStatus(order, Set.of(OrderStatus.DRAFT, OrderStatus.CONFIRMED), "cancelled");
        boolean hadReservation = order.getStatus() == OrderStatus.CONFIRMED;
        order.setStatus(OrderStatus.CANCELLED);
        if (hadReservation) {
            events.publishEvent(new OrderCancelledEvent(order.getTenantId(), order.getId(), order.getNumber(),
                    SalesMapper.quantities(order.getLines())));
        }
        return toResponse(order);
    }

    /** Creates the draft invoice of a shipped order. */
    @Transactional
    public InvoiceResponse invoice(Long id, InvoiceRequest request) {
        SalesOrder order = require(id);
        expectStatus(order, Set.of(OrderStatus.SHIPPED), "invoiced");
        Invoice invoice = invoices.createFromOrder(order,
                request == null ? null : request.issueDate(),
                request == null ? null : request.dueDate());
        order.setStatus(OrderStatus.INVOICED);
        order.setInvoiceId(invoice.getId());
        return invoices.toResponse(invoice);
    }

    SalesOrder require(Long id) {
        return orders.findById(id).orElseThrow(() -> new NotFoundException("Order", id));
    }

    OrderResponse toResponse(SalesOrder o) {
        return new OrderResponse(o.getId(), o.getNumber(), o.getCustomer().getId(), o.getCustomer().getName(),
                o.getOrderDate(), o.getStatus(), o.getQuoteId(), o.getInvoiceId(), o.getNotes(),
                o.getSubtotal(), o.getTaxTotal(), o.getTotal(), SalesMapper.lines(o.getLines()));
    }

    private SalesOrder newOrder(Customer customer, LocalDate orderDate, String notes) {
        SalesOrder order = new SalesOrder();
        order.setNumber(numbers.next("ORDER", "SO"));
        order.setCustomer(customer);
        order.setOrderDate(orderDate != null ? orderDate : LocalDate.now());
        order.setNotes(notes);
        return order;
    }

    private static void expectStatus(SalesOrder order, Set<OrderStatus> allowed, String action) {
        if (!allowed.contains(order.getStatus())) {
            throw BusinessException.conflict("INVALID_STATUS",
                    "Order %s is %s and cannot be %s".formatted(order.getNumber(), order.getStatus(), action));
        }
    }
}
