package com.orderhub.product;

import java.math.BigDecimal;
import java.time.Instant;

import com.orderhub.common.exception.BusinessRuleException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 40)
    private String sku;

    @Column(nullable = false, length = 160)
    private String name;

    @Column(length = 2000)
    private String description;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Column(name = "stock_quantity", nullable = false)
    private int stockQuantity;

    @Column(name = "reorder_level", nullable = false)
    private int reorderLevel;

    @Column(nullable = false)
    private boolean active = true;

    /** Optimistic locking: concurrent stock updates fail fast instead of overselling. */
    @Version
    private long version;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Product() {
        // for JPA
    }

    public Product(String sku, String name, String description, BigDecimal price, int stockQuantity, int reorderLevel) {
        this.sku = sku;
        this.name = name;
        this.description = description;
        this.price = price;
        this.stockQuantity = stockQuantity;
        this.reorderLevel = reorderLevel;
    }

    /** Removes {@code quantity} units from stock, enforcing that stock never goes negative. */
    public void reserveStock(int quantity) {
        if (!active) {
            throw new BusinessRuleException("Product %s is no longer available".formatted(sku));
        }
        if (quantity > stockQuantity) {
            throw new BusinessRuleException("Insufficient stock for %s: requested %d, available %d"
                    .formatted(sku, quantity, stockQuantity));
        }
        stockQuantity -= quantity;
    }

    /** Returns previously reserved units to stock (e.g. when an order is cancelled). */
    public void releaseStock(int quantity) {
        stockQuantity += quantity;
    }

    public boolean isLowStock() {
        return stockQuantity <= reorderLevel;
    }

    public void update(String name, String description, BigDecimal price, int stockQuantity, int reorderLevel) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.stockQuantity = stockQuantity;
        this.reorderLevel = reorderLevel;
    }

    public void deactivate() {
        this.active = false;
    }

    public Long getId() {
        return id;
    }

    public String getSku() {
        return sku;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public int getStockQuantity() {
        return stockQuantity;
    }

    public int getReorderLevel() {
        return reorderLevel;
    }

    public boolean isActive() {
        return active;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
