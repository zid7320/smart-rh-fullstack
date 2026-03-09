# SMART RH 4.0 — Angular Frontend

Angular 21 + Angular Material frontend for the SMART RH 4.0 HR management system.

---

## Quick Start

```bash
# Install dependencies
npm install

# Start development server (Angular devkit)
npx ng serve
# → App available at http://localhost:4200
```


---

## How to Login

### Step 1 — Start the backend

**Option A — Local H2 (no Docker, no seed data)**
```bash
# From the repo root (Java 21 + Maven required)
mvn spring-boot:run
# Backend starts at http://localhost:8080
```
Then register a test account via the UI (`/register`) or Swagger at http://localhost:8080/swagger-ui.html.

**Option B — Docker with MySQL + seeded accounts**
```bash
docker compose up --build
# Backend available at http://localhost:8081  (host 8081 → container 8080)
```
Use the seeded credentials below (Flyway V5 seed runs on first start).

### Step 2 — Start the Angular app
```bash
# Inside /frontend
npm install
npx ng serve
# → http://localhost:4200
```

### Step 3 — Configure the backend URL
Edit `src/environments/environment.ts`:
```typescript
apiBaseUrl: 'http://localhost:8080',   // local run
// apiBaseUrl: 'http://localhost:8081', // Docker
```

### Step 4 — Login
Open http://localhost:4200
You are redirected to **/login**.
Enter **username or email** + **password** → click **Se connecter**.

| Field | Value |
|---|---|
| Nom d'utilisateur ou e-mail | `admin` OR `admin@smartrh.com` |
| Mot de passe | `Admin@2024` |

After successful login:
- JWT is stored in localStorage (`smart_rh_token`)
- `GET /api/auth/me` is called to refresh profile
- You are redirected to **/dashboard**
- The sidenav shows only the items your role can access

---

## Token Storage

| Key | Value stored | When cleared |
|---|---|---|
| `smart_rh_token` | Raw JWT string | logout / 401 response |
| `smart_rh_profile` | `{id, username, email, role}` JSON | logout / 401 response |

**Storage: localStorage** — survives page reload so the user stays logged in.
Security note: localStorage is vulnerable to XSS. In production, add a strict Content-Security-Policy header on the server.

On every page load, `APP_INITIALIZER` calls `GET /api/auth/me` if a token is present. This validates the token server-side and refreshes the role before any route activates. If the backend returns 401, the `jwtInterceptor` calls `logout()` automatically.

---

## Authorization Flow

```
Browser                    Angular                        Backend
  │  navigate /dashboard       │                              │
  │ ─────────────────────────► │                              │
  │                            │  APP_INITIALIZER             │
  │                            │ ──GET /api/auth/me──────────►│
  │                            │ ◄── {id, role, ...} ─────── │
  │                            │  authGuard: token? yes       │
  │                            │  activate LayoutComponent    │
  │                            │  ws.connect() → SockJS /ws   │
  │  ◄── render dashboard ──── │                              │
  │                            │                              │
  │  any API call              │                              │
  │ ─────────────────────────► │                              │
  │                            │ ──Authorization: Bearer ────►│
  │                            │ ◄──────── 200 OK ─────────── │
```

---

## Backend Integration

### API Base URL

| Mode | URL | How to run backend |
|---|---|---|
| Local (H2, default) | `http://localhost:8080` | `mvn spring-boot:run` |
| Docker (MySQL) | `http://localhost:8081` | `docker compose up --build` |

Set in `src/environments/environment.ts` (dev) or `environment.prod.ts` (Docker).

### WebSocket / Real-time Attendance

| Property | Value |
|---|---|
| STOMP endpoint | `/ws` (registered in `WebSocketConfig.java`) |
| SockJS | Yes — backend registered with `.withSockJS()` |
| **wsBaseUrl (dev)** | `http://localhost:8080/ws` |
| **wsBaseUrl (Docker)** | `http://localhost:8081/ws` |
| Subscribe topic | `/topic/attendance` |
| App prefix | `/app` |

> **Important:** SockJS requires an **HTTP URL**, not `ws://`.
> The Angular `WebsocketService` passes `environment.wsBaseUrl` directly to `new SockJS(url)`.

### Auth API

| Endpoint | Method | Body |
|---|---|---|
| `/api/auth/login` | POST | `{ usernameOrEmail, password }` |
| `/api/auth/register` | POST | `{ username, email, password }` |
| `/api/auth/me` | GET | (Bearer token required) |

Login body field is `usernameOrEmail` (accepts username OR email address).

---

## Demo Credentials

Seeded by Flyway migration `V5__seed_data.sql`.
Only available when running with the **MySQL profile** (`docker compose up`).
In H2 dev mode the seed does not run (Flyway disabled); use `POST /api/auth/register` to create accounts.

| Role | Username | Password |
|---|---|---|
| ADMIN | `admin` | `Admin@2024` |
| RH | `rhmanager` | `Rh@2024` |
| EMPLOYEE | `alice.dubois` | `Employee@2024` |
| EMPLOYEE | `bob.martin` | `Employee@2024` |

---

## Configure API URL

Edit `src/environments/environment.ts`:

```typescript
export const environment = {
  production: false,
  apiBaseUrl: 'http://localhost:8080',   // ← change this
  wsBaseUrl:  'http://localhost:8080/ws' // ← and this (keep HTTP, not ws://)
};
```

---

## Role-Based Access

| Role | Access |
|---|---|
| ADMIN | Everything |
| RH | Employees, Posts, Competences, Recruitments, Leaves (approve/reject), Payroll generate |
| EMPLOYEE | Own profile, own leaves, own payroll, attendance history, trainings, evaluations |

---

## Project Structure

```
src/app/
  core/
    api/           # Thin HTTP wrappers per entity (11 services)
    guards/        # authGuard, roleGuard
    interceptors/  # jwtInterceptor (adds Bearer token; handles 401 → logout)
    models/        # TypeScript interfaces matching backend DTOs
    services/      # AuthService (login/register/me/logout)
    websocket/     # WebsocketService (STOMP + SockJS)
  shared/
    components/    # ConfirmDialogComponent
    pipes/
  features/        # Lazy-loaded feature modules
    auth/          # /login, /register
    dashboard/
    employees/
    posts/
    competences/
    recruitments/
    leaves/
    payroll/
    evaluations/
    trainings/
    attendance/
    planning/
    dossiers-rh/
    candidats/
  layout/          # Shell: MatToolbar + MatSidenav + <router-outlet>
```
