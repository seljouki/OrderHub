import { Component, inject } from '@angular/core';
import { ToastService } from './toast.service';

@Component({
  selector: 'app-toasts',
  template: `
    <div class="toasts" aria-live="polite">
      @for (toast of toasts.toasts(); track toast.id) {
        <div class="toast" [class.error]="toast.kind === 'error'" role="status">
          <span>{{ toast.message }}</span>
          <button type="button" aria-label="Dismiss" (click)="toasts.dismiss(toast.id)">×</button>
        </div>
      }
    </div>
  `,
  styles: `
    .toasts {
      position: fixed;
      right: 16px;
      bottom: 16px;
      display: flex;
      flex-direction: column;
      gap: 8px;
      z-index: 50;
    }
    .toast {
      display: flex;
      align-items: center;
      gap: 12px;
      max-width: 380px;
      padding: 12px 14px;
      border-radius: var(--radius-sm);
      background: var(--ink);
      color: #fff;
      font-size: 14px;
      border-left: 4px solid var(--green);
    }
    .toast.error {
      border-left-color: var(--red);
    }
    button {
      margin-left: auto;
      background: none;
      border: 0;
      color: inherit;
      font-size: 18px;
      cursor: pointer;
    }
  `,
})
export class Toasts {
  protected readonly toasts = inject(ToastService);
}
