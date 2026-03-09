import { HttpErrorResponse } from '@angular/common/http';
import { FormGroup } from '@angular/forms';

/**
 * Applies backend validation errors (field-level) onto the given FormGroup.
 * Returns a human-readable summary message.
 *
 * Backend error shape expected:
 *   { message?: string; errors?: Record<string, string> }
 */
export function applyServerErrors(err: HttpErrorResponse, form: FormGroup): string {
  if (err.status === 400 && err.error && typeof err.error === 'object') {
    const body = err.error as { message?: string; errors?: Record<string, string> };
    if (body.errors) {
      Object.entries(body.errors).forEach(([field, msg]) => {
        const ctrl = form.get(field);
        if (ctrl) { ctrl.setErrors({ server: msg }); }
      });
    }
    return body.message ?? 'Données invalides.';
  }
  return 'Une erreur est survenue.';
}
