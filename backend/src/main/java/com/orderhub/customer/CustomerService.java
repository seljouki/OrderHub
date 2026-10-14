package com.orderhub.customer;

import com.orderhub.common.exception.ConflictException;
import com.orderhub.common.exception.ResourceNotFoundException;
import com.orderhub.common.web.PageResponse;
import com.orderhub.customer.CustomerDtos.CustomerRequest;
import com.orderhub.customer.CustomerDtos.CustomerResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@Transactional(readOnly = true)
public class CustomerService {

    private final CustomerRepository customers;

    public CustomerService(CustomerRepository customers) {
        this.customers = customers;
    }

    public PageResponse<CustomerResponse> search(String search, Pageable pageable) {
        var page = StringUtils.hasText(search)
                ? customers.findByNameContainingIgnoreCaseOrEmailContainingIgnoreCase(search.trim(), search.trim(), pageable)
                : customers.findAll(pageable);
        return PageResponse.from(page, CustomerResponse::from);
    }

    public CustomerResponse get(Long id) {
        return CustomerResponse.from(find(id));
    }

    @Transactional
    public CustomerResponse create(CustomerRequest request) {
        String email = normalizeEmail(request.email());
        if (customers.existsByEmailIgnoreCase(email)) {
            throw new ConflictException("A customer with email %s already exists".formatted(email));
        }
        Customer customer = new Customer(request.name().trim(), email, request.phone(), request.city());
        return CustomerResponse.from(customers.save(customer));
    }

    @Transactional
    public CustomerResponse update(Long id, CustomerRequest request) {
        Customer customer = find(id);
        String email = normalizeEmail(request.email());
        if (customers.existsByEmailIgnoreCaseAndIdNot(email, id)) {
            throw new ConflictException("A customer with email %s already exists".formatted(email));
        }
        customer.update(request.name().trim(), email, request.phone(), request.city());
        return CustomerResponse.from(customer);
    }

    Customer find(Long id) {
        return customers.findById(id).orElseThrow(() -> new ResourceNotFoundException("Customer", id));
    }

    private static String normalizeEmail(String email) {
        return email.trim().toLowerCase();
    }
}
