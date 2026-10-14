import { DatePipe } from '@angular/common';
import { Component, DestroyRef, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { debounceTime, distinctUntilChanged } from 'rxjs';
import { CustomersApi } from '../../core/api/api.services';
import { errorMessage } from '../../core/errors';
import { Customer, Page } from '../../core/models';
import { Pager } from '../../shared/pager';
import { ToastService } from '../../shared/toast.service';
import { CustomerFormDialog } from './customer-form-dialog';

const PAGE_SIZE = 15;

@Component({
  selector: 'app-customers-page',
  imports: [DatePipe, ReactiveFormsModule, Pager, CustomerFormDialog],
  template: `
    <div class="page">
      <header class="page-head">
        <div>
          <h1>Customers</h1>
          <p>Accounts you sell to.</p>
        </div>
        <button type="button" class="btn btn-primary" (click)="editing.set(null)">
          Add customer
        </button>
      </header>

      <div class="toolbar">
        <input
          class="input search"
          type="search"
          placeholder="Search by name or email"
          aria-label="Search customers"
          [formControl]="search"
        />
      </div>

      @if (error()) {
        <div class="alert" role="alert">{{ error() }}</div>
      }

      <section class="panel">
        <div class="table-wrap">
          <table class="table">
            <thead>
              <tr>
                <th>Name</th>
                <th>Email</th>
                <th>Phone</th>
                <th>City</th>
                <th>Customer since</th>
                <th><span class="visually-hidden">Actions</span></th>
              </tr>
            </thead>
            <tbody>
              @for (c of page()?.content ?? []; track c.id) {
                <tr>
                  <td>
                    <strong>{{ c.name }}</strong>
                  </td>
                  <td>
                    <a [href]="'mailto:' + c.email">{{ c.email }}</a>
                  </td>
                  <td>{{ c.phone ?? '—' }}</td>
                  <td>{{ c.city ?? '—' }}</td>
                  <td class="num">{{ c.createdAt | date: 'mediumDate' }}</td>
                  <td class="right actions">
                    <button type="button" class="btn" (click)="editing.set(c)">Edit</button>
                  </td>
                </tr>
              } @empty {
                @if (!loading()) {
                  <tr>
                    <td colspan="6" class="empty">
                      {{
                        search.value
                          ? 'No customers match your search.'
                          : 'No customers yet. Add one to place their first order.'
                      }}
                    </td>
                  </tr>
                }
              }
            </tbody>
          </table>
        </div>
        @if (page(); as pg) {
          <app-pager
            [page]="pg.page"
            [size]="pg.size"
            [totalElements]="pg.totalElements"
            [totalPages]="pg.totalPages"
            (pageChange)="goTo($event)"
          />
        }
      </section>
    </div>

    @if (editing() !== undefined) {
      <app-customer-form-dialog
        [customer]="editing() ?? null"
        (saved)="onSaved($event)"
        (cancelled)="editing.set(undefined)"
      />
    }
  `,
})
export class CustomersPage {
  private readonly api = inject(CustomersApi);
  private readonly toast = inject(ToastService);

  protected readonly search = new FormControl('', { nonNullable: true });
  protected readonly page = signal<Page<Customer> | null>(null);
  protected readonly loading = signal(true);
  protected readonly error = signal<string | null>(null);
  protected readonly editing = signal<Customer | null | undefined>(undefined);

  private pageIndex = 0;

  constructor() {
    this.search.valueChanges
      .pipe(debounceTime(250), distinctUntilChanged(), takeUntilDestroyed(inject(DestroyRef)))
      .subscribe(() => {
        this.pageIndex = 0;
        this.load();
      });
    this.load();
  }

  protected goTo(page: number): void {
    this.pageIndex = page;
    this.load();
  }

  protected onSaved(customer: Customer): void {
    const isNew = this.editing() === null;
    this.editing.set(undefined);
    this.toast.success(isNew ? `Added ${customer.name}` : `Saved ${customer.name}`);
    this.load();
  }

  private load(): void {
    this.loading.set(true);
    this.api
      .list({ page: this.pageIndex, size: PAGE_SIZE, search: this.search.value.trim() })
      .subscribe({
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
