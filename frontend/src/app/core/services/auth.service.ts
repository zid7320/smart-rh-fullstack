import { Injectable, signal, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable } from 'rxjs';
import { tap } from 'rxjs/operators';
import { environment } from '../../../environments/environment';
import {
  LoginRequest, RegisterRequest, AuthResponse, UserProfile, AppRole, toAppRole
} from '../models/user.model';

const TOKEN_KEY   = 'smart_rh_token';
const PROFILE_KEY = 'smart_rh_profile';

// ─────────────────────────────────────────────────────────────────────────────
// TOKEN STORAGE STRATEGY: localStorage
//   Pros: survives page reload — user stays logged in.
//   Cons: accessible to JS → XSS risk. Mitigate with strict CSP in production.
// Stored keys:
//   smart_rh_token   → raw JWT string
//   smart_rh_profile → { id, username, email, role } (ROLE_ prefix stripped)
// ─────────────────────────────────────────────────────────────────────────────

interface StoredProfile {
  id: number;
  username: string;
  email: string;
  role: AppRole;
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  private http   = inject(HttpClient);
  private router = inject(Router);

  private _profile = signal<StoredProfile | null>(this.loadProfile());
  private _token   = signal<string | null>(localStorage.getItem(TOKEN_KEY));

  /** Read-only: currently logged-in user or null */
  readonly currentUser  = this._profile.asReadonly();
  /** Read-only: JWT string or null */
  readonly currentToken = this._token.asReadonly();

  /** Normalised role ('ADMIN' | 'RH' | 'EMPLOYEE') or null */
  currentUserRole(): AppRole | null {
    return this._profile()?.role ?? null;
  }

  // ── POST /api/auth/login ──────────────────────────────────────────────────
  login(credentials: LoginRequest): Observable<AuthResponse> {
    return this.http
      .post<AuthResponse>(`${environment.apiBaseUrl}/api/auth/login`, credentials)
      .pipe(tap((res) => this.persist(res)));
  }

  // ── POST /api/auth/register ───────────────────────────────────────────────
  register(req: RegisterRequest): Observable<AuthResponse> {
    return this.http
      .post<AuthResponse>(`${environment.apiBaseUrl}/api/auth/register`, req)
      .pipe(tap((res) => this.persist(res)));
  }

  // ── GET /api/auth/me ──────────────────────────────────────────────────────
  me(): Observable<UserProfile> {
    return this.http
      .get<UserProfile>(`${environment.apiBaseUrl}/api/auth/me`)
      .pipe(
        tap((profile) => {
          const stored: StoredProfile = {
            id:       profile.id,
            username: profile.username,
            email:    profile.email,
            role:     toAppRole(profile.role)
          };
          localStorage.setItem(PROFILE_KEY, JSON.stringify(stored));
          this._profile.set(stored);
        })
      );
  }

  // ── Logout ────────────────────────────────────────────────────────────────
  logout(): void {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(PROFILE_KEY);
    this._token.set(null);
    this._profile.set(null);
    this.router.navigate(['/login']);
  }

  isAuthenticated(): boolean {
    return !!this._token();
  }

  // ── Helpers ───────────────────────────────────────────────────────────────
  private persist(res: AuthResponse): void {
    const profile: StoredProfile = {
      id:       res.userId,
      username: res.username,
      email:    res.email,
      role:     toAppRole(res.role)
    };
    localStorage.setItem(TOKEN_KEY,   res.token);
    localStorage.setItem(PROFILE_KEY, JSON.stringify(profile));
    this._token.set(res.token);
    this._profile.set(profile);
  }

  private loadProfile(): StoredProfile | null {
    try {
      const raw = localStorage.getItem(PROFILE_KEY);
      return raw ? (JSON.parse(raw) as StoredProfile) : null;
    } catch {
      return null;
    }
  }
}
