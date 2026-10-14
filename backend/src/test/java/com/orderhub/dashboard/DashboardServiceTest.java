package com.orderhub.dashboard;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import com.orderhub.dashboard.DashboardSummary.DailyRevenue;
import org.junit.jupiter.api.Test;

class DashboardServiceTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 3, 15);

    @Test
    void bucketsRevenuePerDayAndFillsGapsWithZero() {
        List<OrderTotal> totals = List.of(
                new OrderTotal(Instant.parse("2026-03-15T09:00:00Z"), new BigDecimal("100.00")),
                new OrderTotal(Instant.parse("2026-03-15T17:30:00Z"), new BigDecimal("50.50")),
                new OrderTotal(Instant.parse("2026-03-10T12:00:00Z"), new BigDecimal("20.00")));

        List<DailyRevenue> series = DashboardService.dailyRevenue(totals, TODAY);

        assertThat(series).hasSize(DashboardService.CHART_DAYS);
        assertThat(series.getFirst().date()).isEqualTo(TODAY.minusDays(DashboardService.CHART_DAYS - 1));
        assertThat(series.getLast()).isEqualTo(new DailyRevenue(TODAY, new BigDecimal("150.50")));
        assertThat(series).filteredOn(d -> d.date().equals(LocalDate.of(2026, 3, 10)))
                .singleElement()
                .extracting(DailyRevenue::revenue)
                .isEqualTo(new BigDecimal("20.00"));
        assertThat(series).filteredOn(d -> d.date().equals(LocalDate.of(2026, 3, 11)))
                .singleElement()
                .extracting(DailyRevenue::revenue)
                .isEqualTo(BigDecimal.ZERO);
    }

    @Test
    void ignoresOrdersOutsideTheChartWindow() {
        List<OrderTotal> totals = List.of(
                new OrderTotal(Instant.parse("2026-02-01T09:00:00Z"), new BigDecimal("999.00")));

        List<DailyRevenue> series = DashboardService.dailyRevenue(totals, TODAY);

        assertThat(series).allSatisfy(d -> assertThat(d.revenue()).isEqualByComparingTo("0"));
    }
}
