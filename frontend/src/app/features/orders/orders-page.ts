import { CurrencyPipe, DatePipe } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { OrdersApi } from '../../core/api/api.services';
import { errorMessage } from '../../core/errors';
import { ORDER_STATUSES, OrderStatus, OrderSummary, Page } from '../../core/models';
import { Pager } from '../../shared/pager';
import { StatusBadge, statusLabel } from '../../shared/status-badge';

const PAGE_SIZE = 20;

@Component({
  selector: 'app-orders-page',
  imports: [CurrencyPipe, DatePipe, RouterLink, Pager, StatusBadge],
  templateUrl: './orders-page.html',
  styleUrl: './orders-page.scss',
})
export class OrdersPage {
  private readonly api = inject(OrdersApi);
  private readonly router = inject(Router);

  protected readonly filters: { value: OrderStatus | undefined; label: string }[] = [
    { value: undefined, label: 'All' },
    ...ORDER_STATUSES.map((s) => ({ value: s, label: statusLabel(s) })),
  ];

  protected readonly status = signal<OrderStatus | undefined>(undefined);
  protected readonly page = signal<Page<OrderSummary> | null>(null);
  protected readonly loading = signal(true);
  protected readonly error = signal<string | null>(null);

  private pageIndex = 0;

  constructor() {
    this.load();
  }

  protected filter(status: OrderStatus | undefined): void {
    this.status.set(status);
    this.pageIndex = 0;
    this.load();
  }

  protected goTo(page: number): void {
    this.pageIndex = page;
    this.load();
  }

  protected open(order: OrderSummary): void {
    void this.router.navigate(['/orders', order.id]);
  }

  private load(): void {
    this.loading.set(true);
    this.api.list({ page: this.pageIndex, size: PAGE_SIZE, status: this.status() }).subscribe({
      next: (page) => {
        this.page.set(page);
        this.error.set(null);
        this.loading.set(false);
      },
      error: (err: unknown) => {
        this.error.set(errorMessage(err));
        this.loading.set(false);
      },
    });
  }
}
