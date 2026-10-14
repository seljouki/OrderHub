import { Component, computed, input, output } from '@angular/core';

@Component({
  selector: 'app-pager',
  template: `
    @if (totalPages() > 1) {
      <div class="pager">
        <span class="muted num"> {{ from() }}–{{ to() }} of {{ totalElements() }} </span>
        <button
          type="button"
          class="btn"
          [disabled]="page() === 0"
          (click)="pageChange.emit(page() - 1)"
        >
          Previous
        </button>
        <button
          type="button"
          class="btn"
          [disabled]="page() >= totalPages() - 1"
          (click)="pageChange.emit(page() + 1)"
        >
          Next
        </button>
      </div>
    }
  `,
  styles: `
    .pager {
      display: flex;
      align-items: center;
      justify-content: flex-end;
      gap: 8px;
      padding: 12px 16px;
      border-top: 1px solid var(--line-soft);
      font-size: 13px;
    }
    .muted {
      margin-right: auto;
    }
  `,
})
export class Pager {
  readonly page = input.required<number>();
  readonly size = input.required<number>();
  readonly totalElements = input.required<number>();
  readonly totalPages = input.required<number>();
  readonly pageChange = output<number>();

  protected readonly from = computed(() => this.page() * this.size() + 1);
  protected readonly to = computed(() =>
    Math.min((this.page() + 1) * this.size(), this.totalElements()),
  );
}
