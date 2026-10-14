import { CurrencyPipe, DatePipe, DecimalPipe } from '@angular/common';
import { Component, computed, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { DashboardApi } from '../../core/api/api.services';
import { errorMessage } from '../../core/errors';
import { DashboardSummary, ORDER_STATUSES, OrderStatus } from '../../core/models';
import { statusLabel } from '../../shared/status-badge';

interface Bar {
  date: string;
  revenue: number;
  x: number;
  height: number;
}

const CHART_HEIGHT = 140;

@Component({
  selector: 'app-dashboard-page',
  imports: [CurrencyPipe, DatePipe, DecimalPipe, RouterLink],
  templateUrl: './dashboard-page.html',
  styleUrl: './dashboard-page.scss',
})
export class DashboardPage {
  private readonly api = inject(DashboardApi);

  protected readonly summary = signal<DashboardSummary | null>(null);
  protected readonly error = signal<string | null>(null);
  protected readonly chartHeight = CHART_HEIGHT;

  protected readonly bars = computed<Bar[]>(() => {
    const days = this.summary()?.dailyRevenue ?? [];
    const max = Math.max(1, ...days.map((d) => d.revenue));
    return days.map((d, i) => ({
      date: d.date,
      revenue: d.revenue,
      x: i * 24,
      height: Math.max(2, (d.revenue / max) * CHART_HEIGHT),
    }));
  });

  protected readonly statusMix = computed(() => {
    const counts = this.summary()?.ordersByStatus;
    if (!counts) {
      return [];
    }
    const total = ORDER_STATUSES.reduce((sum, s) => sum + (counts[s] ?? 0), 0) || 1;
    return ORDER_STATUSES.map((status: OrderStatus) => ({
      status,
      label: statusLabel(status),
      count: counts[status] ?? 0,
      share: ((counts[status] ?? 0) / total) * 100,
    }));
  });

  constructor() {
    this.api.summary().subscribe({
      next: (s) => this.summary.set(s),
      error: (err: unknown) => this.error.set(errorMessage(err)),
    });
  }
}
