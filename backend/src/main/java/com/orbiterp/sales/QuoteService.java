package com.orbiterp.sales;

import com.orbiterp.common.BusinessException;
import com.orbiterp.common.NotFoundException;
import com.orbiterp.common.sequence.DocumentNumberService;
import com.orbiterp.sales.SalesDtos.DocumentSummary;
import com.orbiterp.sales.SalesDtos.OrderResponse;
import com.orbiterp.sales.SalesDtos.QuoteRequest;
import com.orbiterp.sales.SalesDtos.QuoteResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class QuoteService {

    private final QuoteRepository quotes;
    private final CustomerService customers;
    private final SalesOrderService orders;
    private final DocumentNumberService numbers;

    @Transactional(readOnly = true)
    public List<DocumentSummary> list() {
        return quotes.findAllByOrderByIdDesc().stream()
                .map(q -> new DocumentSummary(q.getId(), q.getNumber(), q.getCustomer().getId(),
                        q.getCustomer().getName(), q.getQuoteDate(), q.getStatus().name(), q.getTotal()))
                .toList();
    }

    @Transactional(readOnly = true)
    public QuoteResponse get(Long id) {
        return toResponse(require(id));
    }

    @Transactional
    public QuoteResponse create(QuoteRequest request) {
        LocalDate quoteDate = request.quoteDate() != null ? request.quoteDate() : LocalDate.now();
        if (request.validUntil() != null && request.validUntil().isBefore(quoteDate)) {
            throw BusinessException.invalid("INVALID_VALIDITY", "A quote cannot expire before its date");
        }
        Quote quote = new Quote();
        quote.setNumber(numbers.next("QUOTE", "Q"));
        quote.setCustomer(customers.require(request.customerId()));
        quote.setQuoteDate(quoteDate);
        quote.setValidUntil(request.validUntil());
        quote.setNotes(request.notes());
        request.lines().forEach(r -> quote.addLine(SalesMapper.fill(new QuoteLine(), r)));
        quote.recalculateTotals();
        return toResponse(quotes.save(quote));
    }

    /** The customer accepted: the quote becomes a draft order with the same lines. */
    @Transactional
    public OrderResponse convertToOrder(Long id) {
        Quote quote = requireDraft(id, "converted");
        SalesOrder order = orders.createFromQuote(quote);
        quote.setStatus(QuoteStatus.CONVERTED);
        quote.setOrderId(order.getId());
        return orders.toResponse(order);
    }

    @Transactional
    public QuoteResponse reject(Long id) {
        Quote quote = requireDraft(id, "rejected");
        quote.setStatus(QuoteStatus.REJECTED);
        return toResponse(quote);
    }

    private Quote require(Long id) {
        return quotes.findById(id).orElseThrow(() -> new NotFoundException("Quote", id));
    }

    private Quote requireDraft(Long id, String action) {
        Quote quote = require(id);
        if (quote.getStatus() != QuoteStatus.DRAFT) {
            throw BusinessException.conflict("INVALID_STATUS",
                    "Quote %s is %s and cannot be %s".formatted(quote.getNumber(), quote.getStatus(), action));
        }
        return quote;
    }

    private static QuoteResponse toResponse(Quote q) {
        return new QuoteResponse(q.getId(), q.getNumber(), q.getCustomer().getId(), q.getCustomer().getName(),
                q.getQuoteDate(), q.getValidUntil(), q.getStatus(), q.getOrderId(), q.getNotes(),
                q.getSubtotal(), q.getTaxTotal(), q.getTotal(), SalesMapper.lines(q.getLines()));
    }
}
