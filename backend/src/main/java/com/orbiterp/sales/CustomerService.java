package com.orbiterp.sales;

import com.orbiterp.common.NotFoundException;
import com.orbiterp.common.sequence.DocumentNumberService;
import com.orbiterp.sales.SalesDtos.CustomerRequest;
import com.orbiterp.sales.SalesDtos.CustomerResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customers;
    private final DocumentNumberService numbers;

    @Transactional(readOnly = true)
    public List<CustomerResponse> list() {
        return customers.findAllByOrderByNameAsc().stream().map(CustomerService::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public CustomerResponse get(Long id) {
        return toResponse(require(id));
    }

    @Transactional
    public CustomerResponse create(CustomerRequest request) {
        Customer customer = new Customer();
        customer.setCode(numbers.next("CUSTOMER", "C"));
        apply(customer, request);
        return toResponse(customers.save(customer));
    }

    @Transactional
    public CustomerResponse update(Long id, CustomerRequest request) {
        Customer customer = require(id);
        apply(customer, request);
        return toResponse(customer);
    }

    Customer require(Long id) {
        return customers.findById(id).orElseThrow(() -> new NotFoundException("Customer", id));
    }

    private static void apply(Customer customer, CustomerRequest request) {
        customer.setName(request.name().trim());
        customer.setEmail(blankToNull(request.email()));
        customer.setPhone(blankToNull(request.phone()));
        customer.setAddress(blankToNull(request.address()));
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    static CustomerResponse toResponse(Customer c) {
        return new CustomerResponse(c.getId(), c.getCode(), c.getName(), c.getEmail(), c.getPhone(), c.getAddress());
    }
}
