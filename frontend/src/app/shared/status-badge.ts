import { Component, computed, input } from '@angular/core';
import { OrderStatus } from '../core/models';

const LABELS: Record<OrderStatus, string> = {
  PENDING: 'Pending',
  CONFIRMED: 'Confirmed',
  SHIPPED: 'Shipped',
  DELIVERED: 'Delivered',
  CANCELLED: 'Cancelled',
};

export function statusLabel(status: OrderStatus): string {
  return LABELS[status];
}

@Component({
  selector: 'app-status-badge',
  template: `<span class="badge" [class]="'badge s-' + status().toLowerCase()">{{
    label()
  }}</span>`,
  styles: `
    .s-pending {
      background: var(--line-soft);
      color: var(--ink-soft);
    }
    .s-confirmed {
      background: var(--blue-soft);
      color: var(--blue);
    }
    .s-shipped {
      background: var(--violet-soft);
      color: var(--violet);
    }
    .s-delivered {
      background: var(--green-soft);
      color: var(--green);
    }
    .s-cancelled {
      background: var(--red-soft);
      color: var(--red);
    }
  `,
})
export class StatusBadge {
  readonly status = input.required<OrderStatus>();
  protected readonly label = computed(() => statusLabel(this.status()));
}
