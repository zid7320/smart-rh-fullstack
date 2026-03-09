import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

/**
 * Usage:
 *   canActivate: [authGuard, roleGuard(['ADMIN', 'RH'])]
 */
export function roleGuard(allowedRoles: string[]): CanActivateFn {
  return () => {
    const role = inject(AuthService).currentUserRole();
    if (role && allowedRoles.includes(role)) return true;
    return inject(Router).parseUrl('/dashboard');
  };
}
