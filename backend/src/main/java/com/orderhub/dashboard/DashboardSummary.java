package com.orderhub.dashboard;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import com.orderhub.order.OrderStatus;
import com.orderhub.product.ProductDtos.ProductResponse;

public record DashboardSummary(
        BigDecimal revenueLast30Days,
        long ordersLast30Days,
        long activeProducts,
        long customers,
        Map<OrderStatus, Long> ordersByStatus,
        List<DailyRevenue> dailyRevenue,
        List<TopProduct> topProducts,
        List<ProductResponse> lowStock
) {

    public record DailyRevenue(LocalDate date, BigDecimal revenue) {
    }
}
