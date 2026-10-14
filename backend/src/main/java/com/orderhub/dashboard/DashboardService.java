package com.orderhub.dashboard;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import com.orderhub.customer.CustomerRepository;
import com.orderhub.dashboard.DashboardSummary.DailyRevenue;
import com.orderhub.order.OrderRepository;
import com.orderhub.order.OrderStatus;
import com.orderhub.product.ProductDtos.ProductResponse;
import com.orderhub.product.ProductRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class DashboardService {

    static final int REVENUE_WINDOW_DAYS = 30;
    static final int CHART_DAYS = 14;

    private final OrderRepository orders;
    private final ProductRepository products;
    private final CustomerRepository customers;
    private final Clock clock;

    public DashboardService(OrderRepository orders, ProductRepository products, CustomerRepository customers,
                            Clock clock) {
        this.orders = orders;
        this.products = products;
        this.customers = customers;
        this.clock = clock;
    }

    public DashboardSummary summary() {
        Instant now = clock.instant();
        Instant windowStart = now.minus(REVENUE_WINDOW_DAYS, ChronoUnit.DAYS);

        List<OrderTotal> recent = orders.findOrderTotalsSince(windowStart, OrderStatus.CANCELLED);

        BigDecimal revenue = recent.stream()
                .map(OrderTotal::totalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<OrderStatus, Long> byStatus = new EnumMap<>(OrderStatus.class);
        for (OrderStatus status : OrderStatus.values()) {
            byStatus.put(status, 0L);
        }
        orders.countGroupedByStatus().forEach(sc -> byStatus.put(sc.status(), sc.count()));

        List<ProductResponse> lowStock = products.findLowStock().stream()
                .limit(10)
                .map(ProductResponse::from)
                .toList();

        return new DashboardSummary(
                revenue,
                recent.size(),
                products.countByActiveTrue(),
                customers.count(),
                byStatus,
                dailyRevenue(recent, LocalDate.ofInstant(now, ZoneOffset.UTC)),
                orders.findTopProducts(OrderStatus.CANCELLED, PageRequest.of(0, 5)),
                lowStock);
    }

    /** Buckets order totals per UTC day, filling days without orders with zero. */
    static List<DailyRevenue> dailyRevenue(List<OrderTotal> totals, LocalDate today) {
        Map<LocalDate, BigDecimal> byDay = new TreeMap<>();
        for (int i = CHART_DAYS - 1; i >= 0; i--) {
            byDay.put(today.minusDays(i), BigDecimal.ZERO);
        }
        Map<LocalDate, BigDecimal> actual = totals.stream().collect(Collectors.groupingBy(
                t -> LocalDate.ofInstant(t.createdAt(), ZoneOffset.UTC),
                Collectors.reducing(BigDecimal.ZERO, OrderTotal::totalAmount, BigDecimal::add)));
        actual.forEach((day, amount) -> byDay.computeIfPresent(day, (d, zero) -> amount));

        return byDay.entrySet().stream()
                .map(e -> new DailyRevenue(e.getKey(), e.getValue()))
                .toList();
    }
}
