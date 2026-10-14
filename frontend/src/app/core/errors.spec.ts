import { HttpErrorResponse } from '@angular/common/http';
import { errorMessage } from './errors';

describe('errorMessage', () => {
  it('lists field validation errors', () => {
    const error = new HttpErrorResponse({
      status: 400,
      error: {
        title: 'Validation failed',
        errors: { sku: 'must not be blank', price: 'must be >= 0' },
      },
    });

    expect(errorMessage(error)).toBe('sku must not be blank; price must be >= 0');
  });

  it('uses the problem detail when there are no field errors', () => {
    const error = new HttpErrorResponse({
      status: 422,
      error: { detail: 'Insufficient stock for KB-1: requested 5, available 2' },
    });

    expect(errorMessage(error)).toBe('Insufficient stock for KB-1: requested 5, available 2');
  });

  it('explains network failures', () => {
    expect(errorMessage(new HttpErrorResponse({ status: 0 }))).toContain('Cannot reach the server');
  });

  it('falls back for unknown errors', () => {
    expect(errorMessage(new Error('boom'))).toBe('Something went wrong.');
  });
});
