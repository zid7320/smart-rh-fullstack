// ─────────────────────────────────────────────────────────────────────────────
// Auth DTOs — exact shapes from backend:
//   LoginRequest.java   → usernameOrEmail (NOT username)
//   RegisterRequest.java→ username, email, password  (no nom/prenom)
//   AuthResponse.java   → flat: token, tokenType, expiresIn, userId, username, email, role
//   UserProfileDto.java → id, username, email, role, enabled  (no nom/prenom)
// ─────────────────────────────────────────────────────────────────────────────

/** POST /api/auth/login — accepts username OR email in 'usernameOrEmail' */
export interface LoginRequest {
  usernameOrEmail: string;
  password: string;
}

/** POST /api/auth/register — only these 3 required fields from RegisterRequest.java */
export interface RegisterRequest {
  username: string;
  email: string;
  password: string;   // min 8 chars (backend @Size constraint)
  role?: string;      // omit → defaults to ROLE_EMPLOYEE
}

/** Flat response for login AND register (AuthResponse.java) — NO nested user object */
export interface AuthResponse {
  token: string;
  tokenType: string;   // always "Bearer"
  expiresIn: number;   // milliseconds, from app.jwt.expiration-ms=86400000
  userId: number;
  username: string;
  email: string;
  role: string;        // e.g. "ROLE_ADMIN" — strip prefix before storing
}

/** GET /api/auth/me → UserProfileDto.java */
export interface UserProfile {
  id: number;
  username: string;
  email: string;
  role: string;       // e.g. "ROLE_ADMIN"
  enabled: boolean;
}

/** Normalised role used in guards/menus (ROLE_ prefix stripped) */
export type AppRole = 'ADMIN' | 'RH' | 'EMPLOYEE';

/** Convert backend "ROLE_ADMIN" → "ADMIN" */
export function toAppRole(backendRole: string): AppRole {
  return backendRole.replace('ROLE_', '') as AppRole;
}
