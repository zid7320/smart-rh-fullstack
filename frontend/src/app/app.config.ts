import { APP_INITIALIZER, ApplicationConfig, provideZoneChangeDetection } from '@angular/core';
import { provideRouter, withComponentInputBinding } from '@angular/router';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';
import { Observable, of } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { routes } from './app.routes';
import { jwtInterceptor } from './core/interceptors/jwt.interceptor';
import { AuthService } from './core/services/auth.service';

// ─────────────────────────────────────────────────────────────────────────────
// Startup strategy — APP_INITIALIZER
//
// Token found → GET /api/auth/me → refresh profile signal (role in UI updates).
// Token expired → 401 → jwtInterceptor calls logout() → navigate /login.
// No token → of(null) → app boots instantly → authGuard redirects to /login.
// ─────────────────────────────────────────────────────────────────────────────
function initAuth(auth: AuthService): () => Observable<unknown> {
  return (): Observable<unknown> =>
    auth.isAuthenticated()
      ? auth.me().pipe(catchError(() => of(null)))
      : of(null);
}

export const appConfig: ApplicationConfig = {
  providers: [
    provideZoneChangeDetection({ eventCoalescing: true }),
    provideRouter(routes, withComponentInputBinding()),
    provideHttpClient(withInterceptors([jwtInterceptor])),
    provideAnimationsAsync(),
    {
      provide:    APP_INITIALIZER,
      useFactory: initAuth,
      deps:       [AuthService],
      multi:      true
    }
  ]
};
