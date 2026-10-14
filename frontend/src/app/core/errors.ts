import { HttpErrorResponse } from '@angular/common/http';
import { ProblemDetail } from './models';

/** Turns any HTTP error into a single human-readable sentence for the UI. */
export function errorMessage(error: unknown): string {
  if (error instanceof HttpErrorResponse) {
    if (error.status === 0) {
      return 'Cannot reach the server. Check your connection and try again.';
    }
    const problem = error.error as ProblemDetail | null;
    if (problem?.errors && Object.keys(problem.errors).length > 0) {
      return Object.entries(problem.errors)
        .map(([field, message]) => `${field} ${message}`)
        .join('; ');
    }
    if (problem?.detail) {
      return problem.detail;
    }
    return `Request failed (${error.status})`;
  }
  return 'Something went wrong.';
}
