import { CurrencyPipe, DatePipe } from '@angular/common';
import { Component, computed, effect, inject, input, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { OrdersApi } from '../../core/api/api.services';
import { errorMessage } from '../../core/errors';
import { Order, OrderStatus } from '../../core/models';
import { StatusBadge, statusLabel } from '../../shared/status-badge';
import { ToastService } from '../../shared/toast.service';

/** The happy path an order travels; cancellation leaves the track. */
const TRACK: OrderStatus[] = ['PENDING', 'CONFIRMED', 'SHIPPED', 'DELIVERED'];

const ACTION_LABELS: Record<OrderStatus, string> = {
  PENDING: 'Reopen',
  CONFIRMED: 'Confirm order',
  SHIPPED: 'Mark as shipped',
  DELIVERED: 'Mark as delivered',
  CANCELLED: 'Cancel order',
};

@Component({
  selector: 'app-order-detail-page',
  imports: [CurrencyPipe, DatePipe, RouterLink, StatusBadge],
  templateUrl: './order-detail-page.html',
  styleUrl: './order-detail-page.scss',
})
export class OrderDetailPage {
  private readonly api = inject(OrdersApi);
  private readonly toast = inject(ToastService);

  /** Bound from the `:id` route parameter. */
  readonly id = input.required<string>();

  protected readonly order = signal<Order | null>(null);
  protected readonly error = signal<string | null>(null);
  protected readonly updating = signal(false);

  protected readonly steps = computed(() => {
    const order = this.order();
    if (!order) {
      return [];
    }
    const reached = TRACK.indexOf(order.status);
    return TRACK.map((status, i) => ({
      status,
      label: statusLabel(status),
      done: order.status !== 'CANCELLED' && i <= reached,
      current: i === reached,
    }));
  });

  protected readonly actions = computed(() =>
    (this.order()?.allowedTransitions ?? [])
      .map((status) => ({ status, label: ACTION_LABELS[status], danger: status === 'CANCELLED' }))
      // primary action first, cancel last
      .sort((a, b) => Number(a.danger) - Number(b.danger)),
  );

  constructor() {
    effect(() => {
      const id = Number(this.id());
      this.api.get(id).subscribe({
        next: (order) => this.order.set(order),
        error: (err: unknown) => this.error.set(errorMessage(err)),
      });
    });
  }

  protected changeStatus(status: OrderStatus): void {
    const order = this.order();
    if (!order) {
      return;
    }
    if (
      status === 'CANCELLED' &&
      !confirm(`Cancel ${order.orderNumber}? Reserved stock goes back on the shelf.`)
    ) {
      return;
    }
    this.updating.set(true);
    this.api.changeStatus(order.id, status).subscribe({
      next: (updated) => {
        this.order.set(updated);
        this.updating.set(false);
        this.toast.success(
          `${updated.orderNumber} is now ${statusLabel(updated.status).toLowerCase()}`,
        );
      },
      error: (err: unknown) => {
        this.updating.set(false);
        this.toast.error(errorMessage(err));
      },
    });
  }
}
