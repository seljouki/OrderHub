package com.orderhub.dashboard;

import com.orderhub.order.OrderStatus;

public record StatusCount(OrderStatus status, Long count) {
}
