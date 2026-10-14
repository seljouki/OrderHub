package com.orderhub.order;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Set;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public final class OrderDtos {

    private OrderDtos() {
    }

    public record CreateOrderRequest(
            @NotNull Long customerId,
            @NotEmpty @Size(max = 50) List<@Valid @NotNull OrderLineRequest> items,
            @Size(max = 1000) String notes
    ) {
    }

    public record OrderLineRequest(
            @NotNull Long productId,
            @Min(1) @Max(10_000) int quantity
    ) {
    }

    public record ChangeStatusRequest(@NotNull OrderStatus status) {
    }

    public record OrderSummary(
            Long id,
            String orderNumber,
            Long customerId,
            String customerName,
            OrderStatus status,
            int itemCount,
            BigDecimal totalAmount,
            Instant createdAt
    ) {

        public static OrderSummary from(SalesOrder o) {
            return new OrderSummary(o.getId(), o.getOrderNumber(), o.getCustomer().getId(),
                    o.getCustomer().getName(), o.getStatus(), o.getItems().size(), o.getTotalAmount(),
                    o.getCreatedAt());
        }
    }

    public record OrderResponse(
            Long id,
            String orderNumber,
            OrderStatus status,
            Set<OrderStatus> allowedTransitions,
            CustomerRef customer,
            List<OrderLineResponse> items,
            BigDecimal totalAmount,
            String notes,
            String createdBy,
            Instant createdAt,
            Instant updatedAt
    ) {

        public static OrderResponse from(SalesOrder o) {
            return new OrderResponse(
                    o.getId(),
                    o.getOrderNumber(),
                    o.getStatus(),
                    o.getStatus().allowedTransitions(),
                    new CustomerRef(o.getCustomer().getId(), o.getCustomer().getName(), o.getCustomer().getEmail()),
                    o.getItems().stream().map(OrderLineResponse::from).toList(),
                    o.getTotalAmount(),
                    o.getNotes(),
                    o.getCreatedBy(),
                    o.getCreatedAt(),
                    o.getUpdatedAt());
        }
    }

    public record CustomerRef(Long id, String name, String email) {
    }

    public record OrderLineResponse(
            Long productId,
            String sku,
            String productName,
            int quantity,
            BigDecimal unitPrice,
            BigDecimal lineTotal
    ) {

        static OrderLineResponse from(OrderItem item) {
            return new OrderLineResponse(item.getProduct().getId(), item.getProduct().getSku(),
                    item.getProduct().getName(), item.getQuantity(), item.getUnitPrice(), item.getLineTotal());
        }
    }
}
