import { CurrencyPipe } from '@angular/common';
import { Component, DestroyRef, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { debounceTime, distinctUntilChanged } from 'rxjs';
import { ProductsApi } from '../../core/api/api.services';
import { AuthService } from '../../core/auth/auth.service';
import { errorMessage } from '../../core/errors';
import { Page, Product } from '../../core/models';
import { Pager } from '../../shared/pager';
import { ToastService } from '../../shared/toast.service';
import { ProductFormDialog } from './product-form-dialog';

const PAGE_SIZE = 15;

@Component({
  selector: 'app-products-page',
  imports: [CurrencyPipe, ReactiveFormsModule, Pager, ProductFormDialog],
  templateUrl: './products-page.html',
})
export class ProductsPage {
  private readonly api = inject(ProductsApi);
  private readonly toast = inject(ToastService);
  protected readonly auth = inject(AuthService);

  protected readonly search = new FormControl('', { nonNullable: true });
  protected readonly page = signal<Page<Product> | null>(null);
  protected readonly loading = signal(true);
  protected readonly error = signal<string | null>(null);

  /** undefined = dialog closed, null = creating, Product = editing */
  protected readonly editing = signal<Product | null | undefined>(undefined);

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

  protected onSaved(product: Product): void {
    const isNew = this.editing() === null;
    this.editing.set(undefined);
    this.toast.success(isNew ? `Added ${product.name}` : `Saved ${product.name}`);
    this.load();
  }

  protected archive(product: Product): void {
    if (!confirm(`Archive ${product.name}? It will no longer be available for new orders.`)) {
      return;
    }
    this.api.archive(product.id).subscribe({
      next: () => {
        this.toast.success(`Archived ${product.name}`);
        this.load();
      },
      error: (err: unknown) => this.toast.error(errorMessage(err)),
    });
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
