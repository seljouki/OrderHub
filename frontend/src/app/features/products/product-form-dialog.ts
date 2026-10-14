import { Component, inject, input, OnInit, output, signal } from '@angular/core';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ProductsApi } from '../../core/api/api.services';
import { errorMessage } from '../../core/errors';
import { Product } from '../../core/models';

@Component({
  selector: 'app-product-form-dialog',
  imports: [ReactiveFormsModule],
  template: `
    <div class="dialog-backdrop" (click)="cancelled.emit()">
      <div
        class="dialog"
        role="dialog"
        aria-modal="true"
        aria-labelledby="product-dialog-title"
        (click)="$event.stopPropagation()"
      >
        <h2 id="product-dialog-title">{{ product() ? 'Edit product' : 'New product' }}</h2>

        @if (error()) {
          <div class="alert" role="alert">{{ error() }}</div>
        }

        <form [formGroup]="form" (ngSubmit)="save()" novalidate>
          <div class="form-grid">
            <div class="field">
              <label for="sku">SKU</label>
              <input id="sku" class="input" formControlName="sku" [readonly]="!!product()" />
              @if (form.controls.sku.touched && form.controls.sku.invalid) {
                <span class="error">Letters, digits and dashes only.</span>
              }
            </div>
            <div class="field">
              <label for="name">Name</label>
              <input id="name" class="input" formControlName="name" />
              @if (form.controls.name.touched && form.controls.name.invalid) {
                <span class="error">Name is required.</span>
              }
            </div>
            <div class="field span-2">
              <label for="description">Description</label>
              <textarea id="description" class="textarea" formControlName="description"></textarea>
            </div>
            <div class="field">
              <label for="price">Price (USD)</label>
              <input
                id="price"
                class="input"
                type="number"
                min="0"
                step="0.01"
                formControlName="price"
              />
            </div>
            <div class="field">
              <label for="stock">Units in stock</label>
              <input
                id="stock"
                class="input"
                type="number"
                min="0"
                step="1"
                formControlName="stockQuantity"
              />
            </div>
            <div class="field">
              <label for="reorder">Reorder level</label>
              <input
                id="reorder"
                class="input"
                type="number"
                min="0"
                step="1"
                formControlName="reorderLevel"
              />
              <span class="hint muted">Flag for restocking at or below this many units.</span>
            </div>
          </div>

          <div class="dialog-actions">
            <button type="button" class="btn" (click)="cancelled.emit()">Cancel</button>
            <button type="submit" class="btn btn-primary" [disabled]="saving()">
              {{ product() ? 'Save changes' : 'Add product' }}
            </button>
          </div>
        </form>
      </div>
    </div>
  `,
})
export class ProductFormDialog implements OnInit {
  private readonly api = inject(ProductsApi);

  readonly product = input<Product | null>(null);
  readonly saved = output<Product>();
  readonly cancelled = output<void>();

  protected readonly saving = signal(false);
  protected readonly error = signal<string | null>(null);

  protected readonly form = inject(NonNullableFormBuilder).group({
    sku: [
      '',
      [Validators.required, Validators.maxLength(40), Validators.pattern(/^[A-Za-z0-9-]+$/)],
    ],
    name: ['', [Validators.required, Validators.maxLength(160)]],
    description: [''],
    price: [0, [Validators.required, Validators.min(0)]],
    stockQuantity: [0, [Validators.required, Validators.min(0)]],
    reorderLevel: [10, [Validators.required, Validators.min(0)]],
  });

  ngOnInit(): void {
    const p = this.product();
    if (p) {
      this.form.setValue({
        sku: p.sku,
        name: p.name,
        description: p.description ?? '',
        price: p.price,
        stockQuantity: p.stockQuantity,
        reorderLevel: p.reorderLevel,
      });
    }
  }

  protected save(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.saving.set(true);
    this.error.set(null);

    const { sku, ...rest } = this.form.getRawValue();
    const input = { ...rest, description: rest.description || null };
    const existing = this.product();
    const request = existing
      ? this.api.update(existing.id, input)
      : this.api.create({ sku, ...input });

    request.subscribe({
      next: (product) => this.saved.emit(product),
      error: (err: unknown) => {
        this.error.set(errorMessage(err));
        this.saving.set(false);
      },
    });
  }
}
