import { HttpErrorResponse } from '@angular/common/http';

import { ApiError } from './models';

/** A readable message from a failed API call, including field errors. */
export function errorMessage(error: unknown): string {
  if (error instanceof HttpErrorResponse) {
    const body = error.error as Partial<ApiError> | null;
    if (body && typeof body === 'object' && body.message) {
      const details = body.details
        ? Object.entries(body.details)
            .map(([field, problem]) => `${field}: ${String(problem)}`)
            .join('; ')
        : '';
      return details ? `${body.message} (${details})` : body.message;
    }
    if (error.status === 0) {
      return 'The server cannot be reached.';
    }
    return `The request failed (${error.status}).`;
  }
  return 'Something went wrong.';
}

/** Today as yyyy-mm-dd, for date inputs. */
export function today(): string {
  const now = new Date();
  const offset = now.getTimezoneOffset() * 60000;
  return new Date(now.getTime() - offset).toISOString().slice(0, 10);
}
