package com.orderhub.order;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.orderhub.common.exception.ResourceNotFoundException;
import com.orderhub.common.web.PageResponse;
import com.orderhub.customer.Customer;
import com.orderhub.customer.CustomerRepository;
import com.orderhub.order.OrderDtos.CreateOrderRequest;
import com.orderhub.order.OrderDtos.OrderLineRequest;
import com.orderhub.order.OrderDtos.OrderResponse;
import com.orderhub.order.OrderDtos.OrderSummary;
import com.orderhub.product.Product;
import com.orderhub.product.ProductRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class OrderService {

    private final OrderRepository orders;
    private final CustomerRepository customers;
    private final ProductRepository products;

    public OrderService(OrderRepository orders, CustomerRepository customers, ProductRepository products) {
        this.orders = orders;
        this.customers = customers;
        this.products = products;
    }

    public PageResponse<OrderSummary> list(OrderStatus status, Pageable pageable) {
        var page = status == null ? orders.findAllBy(pageable) : orders.findByStatus(status, pageable);
        return PageResponse.from(page, OrderSummary::from);
    }

    public OrderResponse get(Long id) {
        return OrderResponse.from(findWithDetails(id));
    }

    /**
     * Places an order and reserves stock for every line in a single transaction:
     * either all lines are reserved or none are.
     */
    @Transactional
    public OrderResponse create(CreateOrderRequest request, String createdBy) {
        Customer customer = customers.findById(request.customerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer", request.customerId()));

        // Merge duplicate product lines so "2 x A" + "3 x A" becomes "5 x A".
        Map<Long, Integer> quantities = request.items().stream()
                .collect(Collectors.toMap(OrderLineRequest::productId, OrderLineRequest::quantity,
                        Integer::sum, LinkedHashMap::new));

        Map<Long, Product> productsById = products.findAllById(quantities.keySet()).stream()
                .collect(Collectors.toMap(Product::getId, Function.identity()));

        SalesOrder order = new SalesOrder(customer, request.notes(), createdBy);
        quantities.forEach((productId, quantity) -> {
            Product product = productsById.get(productId);
            if (product == null) {
                throw new ResourceNotFoundException("Product", productId);
            }
            order.addItem(product, quantity);
        });

        SalesOrder saved = orders.saveAndFlush(order);
        saved.assignOrderNumber();
        return OrderResponse.from(saved);
    }

    @Transactional
    public OrderResponse changeStatus(Long id, OrderStatus target) {
        SalesOrder order = findWithDetails(id);
        order.changeStatus(target);
        return OrderResponse.from(orders.saveAndFlush(order));
    }

    private SalesOrder findWithDetails(Long id) {
        return orders.findWithDetailsById(id).orElseThrow(() -> new ResourceNotFoundException("Order", id));
    }
}
