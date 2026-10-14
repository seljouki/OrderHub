import { Component, inject, input, OnInit, output, signal } from '@angular/core';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { CustomersApi } from '../../core/api/api.services';
import { errorMessage } from '../../core/errors';
import { Customer } from '../../core/models';

@Component({
  selector: 'app-customer-form-dialog',
  imports: [ReactiveFormsModule],
  template: `
    <div class="dialog-backdrop" (click)="cancelled.emit()">
      <div
        class="dialog"
        role="dialog"
        aria-modal="true"
        aria-labelledby="customer-dialog-title"
        (click)="$event.stopPropagation()"
      >
        <h2 id="customer-dialog-title">{{ customer() ? 'Edit customer' : 'New customer' }}</h2>

        @if (error()) {
          <div class="alert" role="alert">{{ error() }}</div>
        }

        <form [formGroup]="form" (ngSubmit)="save()" novalidate>
          <div class="form-grid">
            <div class="field span-2">
              <label for="c-name">Company or contact name</label>
              <input id="c-name" class="input" formControlName="name" />
              @if (form.controls.name.touched && form.controls.name.invalid) {
                <span class="error">Name is required.</span>
              }
            </div>
            <div class="field span-2">
              <label for="c-email">Email</label>
              <input id="c-email" class="input" type="email" formControlName="email" />
              @if (form.controls.email.touched && form.controls.email.invalid) {
                <span class="error">Enter a valid email address.</span>
              }
            </div>
            <div class="field">
              <label for="c-phone">Phone</label>
              <input id="c-phone" class="input" type="tel" formControlName="phone" />
            </div>
            <div class="field">
              <label for="c-city">City</label>
              <input id="c-city" class="input" formControlName="city" />
            </div>
          </div>

          <div class="dialog-actions">
            <button type="button" class="btn" (click)="cancelled.emit()">Cancel</button>
            <button type="submit" class="btn btn-primary" [disabled]="saving()">
              {{ customer() ? 'Save changes' : 'Add customer' }}
            </button>
          </div>
        </form>
      </div>
    </div>
  `,
})
export class CustomerFormDialog implements OnInit {
  private readonly api = inject(CustomersApi);

  readonly customer = input<Customer | null>(null);
  readonly saved = output<Customer>();
  readonly cancelled = output<void>();

  protected readonly saving = signal(false);
  protected readonly error = signal<string | null>(null);

  protected readonly form = inject(NonNullableFormBuilder).group({
    name: ['', [Validators.required, Validators.maxLength(160)]],
    email: ['', [Validators.required, Validators.email]],
    phone: [''],
    city: [''],
  });

  ngOnInit(): void {
    const c = this.customer();
    if (c) {
      this.form.setValue({
        name: c.name,
        email: c.email,
        phone: c.phone ?? '',
        city: c.city ?? '',
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

    const v = this.form.getRawValue();
    const input = { name: v.name, email: v.email, phone: v.phone || null, city: v.city || null };
    const existing = this.customer();
    const request = existing ? this.api.update(existing.id, input) : this.api.create(input);

    request.subscribe({
      next: (customer) => this.saved.emit(customer),
      error: (err: unknown) => {
        this.error.set(errorMessage(err));
        this.saving.set(false);
      },
    });
  }
}
