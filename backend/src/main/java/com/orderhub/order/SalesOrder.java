package com.orderhub.order;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.orderhub.common.exception.BusinessRuleException;
import com.orderhub.customer.Customer;
import com.orderhub.product.Product;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

/**
 * Aggregate root for an order. All stock movements go through this class so
 * the inventory rules live in one place.
 */
@Entity
@Table(name = "orders")
public class SalesOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_number", unique = true, length = 30)
    private String orderNumber;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrderStatus status = OrderStatus.PENDING;

    @Column(name = "total_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Column(length = 1000)
    private String notes;

    @Column(name = "created_by", nullable = false)
    private String createdBy;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @BatchSize(size = 50)
    private List<OrderItem> items = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected SalesOrder() {
        // for JPA
    }

    public SalesOrder(Customer customer, String notes, String createdBy) {
        this.customer = customer;
        this.notes = notes;
        this.createdBy = createdBy;
    }

    /** Adds a line item, reserving stock at the product's current price. */
    public void addItem(Product product, int quantity) {
        if (status != OrderStatus.PENDING) {
            throw new BusinessRuleException("Items can only be added to pending orders");
        }
        product.reserveStock(quantity);
        items.add(new OrderItem(this, product, quantity, product.getPrice()));
        recalculateTotal();
    }

    /** Moves the order through its lifecycle; cancelling returns all reserved stock. */
    public void changeStatus(OrderStatus target) {
        if (!status.canTransitionTo(target)) {
            throw new BusinessRuleException("Cannot change order %s from %s to %s"
                    .formatted(orderNumber, status, target));
        }
        if (target == OrderStatus.CANCELLED) {
            items.forEach(item -> item.getProduct().releaseStock(item.getQuantity()));
        }
        this.status = target;
    }

    /** Called once the database id is known. */
    void assignOrderNumber() {
        if (orderNumber == null && id != null) {
            orderNumber = "ORD-%06d".formatted(id);
        }
    }

    private void recalculateTotal() {
        totalAmount = items.stream()
                .map(OrderItem::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public Long getId() {
        return id;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public Customer getCustomer() {
        return customer;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public String getNotes() {
        return notes;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public List<OrderItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
