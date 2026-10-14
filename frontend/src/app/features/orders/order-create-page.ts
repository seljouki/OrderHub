import { CurrencyPipe } from '@angular/common';
import { Component, computed, inject, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import {
  FormArray,
  FormControl,
  FormGroup,
  NonNullableFormBuilder,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { forkJoin, map, startWith } from 'rxjs';
import { CustomersApi, OrdersApi, ProductsApi } from '../../core/api/api.services';
import { errorMessage } from '../../core/errors';
import { Customer, Product } from '../../core/models';
import { ToastService } from '../../shared/toast.service';
import { DraftLine, draftTotal, overStockedProducts } from './order-math';

type LineForm = FormGroup<{
  productId: FormControl<number | null>;
  quantity: FormControl<number>;
}>;

@Component({
  selector: 'app-order-create-page',
  imports: [CurrencyPipe, ReactiveFormsModule, RouterLink],
  templateUrl: './order-create-page.html',
  styleUrl: './order-create-page.scss',
})
export class OrderCreatePage {
  private readonly fb = inject(NonNullableFormBuilder);
  private readonly ordersApi = inject(OrdersApi);
  private readonly router = inject(Router);
  private readonly toast = inject(ToastService);

  protected readonly customers = signal<Customer[]>([]);
  protected readonly products = signal<Product[]>([]);
  protected readonly productsById = computed(
    () => new Map(this.products().map((p) => [p.id, p] as const)),
  );

  protected readonly submitting = signal(false);
  protected readonly error = signal<string | null>(null);

  protected readonly form = this.fb.group({
    customerId: this.fb.control<number | null>(null, Validators.required),
    lines: this.fb.array<LineForm>([this.newLine()]),
    notes: this.fb.control('', Validators.maxLength(1000)),
  });

  private readonly lineValues = toSignal(
    this.form.controls.lines.valueChanges.pipe(
      startWith(this.form.controls.lines.value),
      map(() => this.form.controls.lines.getRawValue() as DraftLine[]),
    ),
    { initialValue: [] as DraftLine[] },
  );

  protected readonly total = computed(() => draftTotal(this.lineValues(), this.productsById()));
  protected readonly overStocked = computed(() =>
    overStockedProducts(this.lineValues(), this.productsById()),
  );

  constructor() {
    forkJoin({
      customers: inject(CustomersApi).list({ size: 200, sort: 'name,asc' }),
      products: inject(ProductsApi).list({ size: 200, sort: 'name,asc' }),
    }).subscribe({
      next: ({ customers, products }) => {
        this.customers.set(customers.content);
        this.products.set(products.content);
      },
      error: (err: unknown) => this.error.set(errorMessage(err)),
    });
  }

  protected get lines(): FormArray<LineForm> {
    return this.form.controls.lines;
  }

  protected addLine(): void {
    this.lines.push(this.newLine());
  }

  protected removeLine(index: number): void {
    if (this.lines.length > 1) {
      this.lines.removeAt(index);
    }
  }

  protected lineTotal(index: number): number {
    const line = this.lineValues()[index];
    return line ? draftTotal([line], this.productsById()) : 0;
  }

  protected submit(): void {
    if (this.form.invalid || this.overStocked().length > 0) {
      this.form.markAllAsTouched();
      return;
    }
    this.submitting.set(true);
    this.error.set(null);

    const value = this.form.getRawValue();
    this.ordersApi
      .create({
        customerId: value.customerId as number,
        items: value.lines.map((l) => ({ productId: l.productId as number, quantity: l.quantity })),
        notes: value.notes.trim() || null,
      })
      .subscribe({
        next: (order) => {
          this.toast.success(`Placed order ${order.orderNumber}`);
          void this.router.navigate(['/orders', order.id]);
        },
        error: (err: unknown) => {
          this.error.set(errorMessage(err));
          this.submitting.set(false);
        },
      });
  }

  private newLine(): LineForm {
    return this.fb.group({
      productId: this.fb.control<number | null>(null, Validators.required),
      quantity: this.fb.control(1, [Validators.required, Validators.min(1)]),
    });
  }
}
