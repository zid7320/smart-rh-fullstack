import { HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, throwError } from 'rxjs';
import { MatSnackBar } from '@angular/material/snack-bar';
import { AuthService } from '../services/auth.service';

export const jwtInterceptor: HttpInterceptorFn = (req, next) => {
  const auth  = inject(AuthService);
  const snack = inject(MatSnackBar);
  const token = auth.currentToken();
  if (token) {
    const newReq = req.clone({ setHeaders: { Authorization: 'Bearer ' + token } });
    req = newReq;
  }
  return next(req).pipe(
    catchError((err: HttpErrorResponse) => {
      if (err.status === 401) {
        auth.logout();
      } else if (err.status === 403) {
        snack.open('Permission refusée.', 'OK', { duration: 4000 });
      } else if (err.status >= 500) {
        snack.open('Erreur serveur — veuillez réessayer.', 'OK', { duration: 5000 });
      }
      return throwError(() => err);
    })
  );
};
