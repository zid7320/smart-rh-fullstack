# SMART RH 4.0 — Frontend

Angular 17+ standalone frontend for the **SMART RH 4.0** HR Information System.
Connects to a Spring Boot 3.2 / Java 21 backend (see `/src`).

---

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Framework | Angular (latest stable) |
| UI | Angular Material |
| Auth | JWT (stored in localStorage) |
| Realtime | STOMP over SockJS |
| Build | Angular CLI |

---

## Prerequisites

- Node.js 18+
- npm 9+ (or pnpm / yarn)
- Backend running on `http://localhost:8080`

---

## Setup

```bash
cd frontend
npm install
```

---

## Configuration

Edit `frontend/src/environments/environment.ts` (development) or
`frontend/src/environments/environment.prod.ts` (production):

```typescript
export const environment = {
  production: false,

  // REST API base URL — no trailing slash
  apiBaseUrl: 'http://localhost:8080',

  // WebSocket endpoint — must be HTTP (SockJS), NOT ws://
  wsBaseUrl: 'http://localhost:8080/ws',

  // STOMP topic for real-time attendance events
  stompTopic: '/topic/attendance',

  stompAppPrefix: '/app'
};
```

> For Docker, change `apiBaseUrl` / `wsBaseUrl` to point at your container host.

---

## Run (Development)

```bash
cd frontend
npm start
# → http://localhost:4200
```

Both backend and frontend must be running. The Angular dev server proxies nothing — calls go
directly to `apiBaseUrl` (CORS is enabled on the backend).

---

## Build (Production)

```bash
cd frontend
npm run build
# Output: frontend/dist/smart-rh-frontend/
```

Serve the `dist/` folder with any static file server (nginx, Apache, `serve`, etc.).

---

## Demo Credentials

| Role | Username | Password |
|------|----------|----------|
| Admin | `admin` (admin@smartrh.com) | `Admin@2024` |
| Responsable RH | `rhmanager` (rh@smartrh.com) | `Rh@2024` |
| Employe (Alice) | `alice.dubois` (alice@smartrh.com) | `Employee@2024` |
| Employe (Bob) | `bob.martin` (bob@smartrh.com) | `Employee@2024` |

> Credentials are seeded by the backend's `DataInitializer` on first run.

---

## Features by Role

| Feature | ADMIN | RH | EMPLOYEE |
|---------|-------|----|----------|
| Tableau de bord | ✓ | ✓ | ✓ |
| Employés (CRUD) | ✓ | ✓ | — |
| Postes + Compétences | ✓ | ✓ | — |
| Recrutements + Candidats | ✓ | ✓ | — |
| Congés (créer + liste) | ✓ | ✓ | ✓ (own) |
| Congés (approuver/rejeter) | ✓ | ✓ | — |
| Paie (générer + PDF) | ✓ | ✓ | ✓ (own) |
| Évaluations (créer/modifier) | ✓ | ✓ | ✓ (read-own) |
| Formations (créer/modifier) | ✓ | ✓ | ✓ (read-own) |
| Présences temps-réel (WS) | ✓ | ✓ | ✓ (own) |
| Dossiers RH | ✓ | ✓ | — |
| Planning | placeholder | placeholder | — |

---

## Real-time Attendance

The app connects to the backend WebSocket on layout init (`LayoutComponent.ngOnInit`).
Events on `/topic/attendance` appear instantly in the Présences table without page reload.

Reconnection is automatic (5-second delay, handled by `WebsocketService`).

---

## Architecture

```
frontend/src/app/
  core/
    api/           ← one service per backend endpoint group
    guards/        ← authGuard, roleGuard
    interceptors/  ← jwtInterceptor (attach token, handle 401/403/5xx)
    models/        ← TypeScript interfaces mirroring backend DTOs
    services/      ← AuthService
    utils/         ← form-error.util (map backend validation errors to FormGroup)
    websocket/     ← WebsocketService (STOMP/SockJS)
  features/        ← one folder per route / domain
  layout/          ← sidenav + toolbar, WS connect/disconnect
  shared/
    components/
      confirm-dialog/  ← reusable ConfirmDialogComponent
```
