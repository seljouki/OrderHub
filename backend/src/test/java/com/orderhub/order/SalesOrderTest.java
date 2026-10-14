package com.orderhub.order;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;

import com.orderhub.common.exception.BusinessRuleException;
import com.orderhub.customer.Customer;
import com.orderhub.product.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SalesOrderTest {

    private Product keyboard;
    private Product mouse;
    private SalesOrder order;

    @BeforeEach
    void setUp() {
        keyboard = new Product("KB-1", "Keyboard", null, new BigDecimal("129.00"), 10, 2);
        mouse = new Product("MS-1", "Mouse", null, new BigDecimal("59.90"), 5, 2);
        order = new SalesOrder(new Customer("Acme", "ops@acme.example", null, null), null, "staff@orderhub.dev");
    }

    @Test
    void addingItemsReservesStockAndComputesTotal() {
        order.addItem(keyboard, 2);
        order.addItem(mouse, 3);

        assertThat(keyboard.getStockQuantity()).isEqualTo(8);
        assertThat(mouse.getStockQuantity()).isEqualTo(2);
        assertThat(order.getTotalAmount()).isEqualByComparingTo("437.70"); // 2*129.00 + 3*59.90
        assertThat(order.getItems()).hasSize(2);
    }

    @Test
    void rejectsOrderingMoreThanAvailableStock() {
        assertThatThrownBy(() -> order.addItem(mouse, 6))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Insufficient stock for MS-1");

        assertThat(mouse.getStockQuantity()).isEqualTo(5);
    }

    @Test
    void rejectsArchivedProducts() {
        keyboard.deactivate();

        assertThatThrownBy(() -> order.addItem(keyboard, 1))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("no longer available");
    }

    @Test
    void cancellingReturnsReservedStock() {
        order.addItem(keyboard, 4);
        order.changeStatus(OrderStatus.CONFIRMED);

        order.changeStatus(OrderStatus.CANCELLED);

        assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
        assertThat(keyboard.getStockQuantity()).isEqualTo(10);
    }

    @Test
    void shippingDoesNotTouchStock() {
        order.addItem(keyboard, 4);
        order.changeStatus(OrderStatus.CONFIRMED);
        order.changeStatus(OrderStatus.SHIPPED);

        assertThat(keyboard.getStockQuantity()).isEqualTo(6);
    }

    @Test
    void rejectsIllegalTransitions() {
        order.addItem(keyboard, 1);

        assertThatThrownBy(() -> order.changeStatus(OrderStatus.DELIVERED))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("from PENDING to DELIVERED");
    }

    @Test
    void itemsCannotBeAddedAfterConfirmation() {
        order.addItem(keyboard, 1);
        order.changeStatus(OrderStatus.CONFIRMED);

        assertThatThrownBy(() -> order.addItem(mouse, 1)).isInstanceOf(BusinessRuleException.class);
    }
}
