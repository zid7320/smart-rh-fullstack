# MASTER PROMPT — SMART RH 4.0 Frontend (Angular + Material) — FULL WORKING APP
You are a **senior Angular architect**. Build a **fully working, production-ready frontend** for **SMART RH 4.0 (GOOD GOV IT)** that connects to an already-complete backend.

Your output must be a complete runnable Angular project (not pseudo-code), with:
- Angular (latest stable) + TypeScript
- Angular Material UI
- Auth (login/register) using JWT
- RBAC (ADMIN / RH / EMPLOYEE) with route guards and role-based menus
- Full CRUD screens aligned to the required REST API
- Workflow screens: hire candidate → employee, leave approve/reject, payroll generate + PDF download
- Attendance module with real-time updates via WebSocket (STOMP)
- Clean architecture: core/services/interceptors/guards/shared/components
- Environment configs for dev/prod
- Dockerfile (optional) and README with run instructions

You must not leave TODOs in core features. The app must compile and run.

---

## 1) PROJECT SPECIFICATIONS (MUST FOLLOW)
### 1.1 Context & goal
SMART RH 4.0 is an intelligent HR Information System:
- HR administration management
- Automated processes
- AI module
- IoT attendance/clocking
- Scalable architecture

### 1.2 Objectives relevant to frontend
- Full HR lifecycle digitalization
- Process automation
- Facial recognition attendance module (frontend consumes history + realtime updates)
- Secure access via JWT + RBAC
- Traceability (frontend shows meaningful status changes, logs UX actions to backend via normal API calls)

### 1.3 Tech stack (frontend)
- Angular
- TypeScript
- Angular Material

Non-functional:
- Good UX, fast navigation (typical operations <2s response from backend)
- Role-based interface

---

## 2) DOMAIN MODEL (FROM CLASS DIAGRAM IMAGE) — USE IN UI + FORMS
Implement UI forms/tables for these entities:

### Entities & fields
**Candidate**
- idCandidat (int)
- nom (string)
- prenom (string)
- email (string)

**ResponsableRH**
- idResponsable (int)
- nom (string)

**Recrutement**
- idRecrutement (int)
- posteCible (string)
- statut (string)

**Employe**
- idEmploye (int)
- nom (string)
- prenom (string)
- poste (string)
- email (string)

**Poste**
- idPoste (int)
- titre (string)
- competencesRequises (string)

**Competence**
- idCompetence (int)
- nom (string)
- niveau (string)

**Contrat**
- idContrat (int)
- type (string)
- dateDebut (date)
- dateFin (date)
- salaire (number)

**DossierRH**
- idDossier (int)
- infosPerso (string)
- diplomes (string)
- documents (string)

**Planning**
- idPlanning (int)
- horaires (string)

**Conge**
- idConge (int)
- type (string)
- dateDebut (date)
- dateFin (date)
- (workflow fields from backend): status (PENDING/APPROVED/REJECTED), requestedAt, decidedAt, decidedBy

**Paie**
- idPaie (int)
- montant (number)
- bulletinPDF (string/path)
- (from backend): month/year, generatedAt

**Evaluation**
- idEval (int)
- objectifs (string)
- kpi (string)

**Formation**
- idFormation (int)
- titre (string)
- certification (string)

### Relationships (for UI navigation)
- Employe has: DossierRH, Poste, multiple Contrats, Conges, Paies, Evaluations, Formations, Planning
- Poste requires multiple Competence
- Recrutement managed by ResponsableRH
- Candidate applies to Recrutement
- Recrutement → Hire converts Candidate to Employe

---

## 3) BACKEND API CONTRACT (MUST MATCH EXACT PATHS)
All calls are under `environment.apiBaseUrl` + the paths below.

### AUTH
- `POST /api/auth/login`
- `POST /api/auth/register`
- `GET /api/auth/me`

### EMPLOYE
- `GET /api/employees` (support pagination if backend provides)
- `GET /api/employees/{id}`
- `POST /api/employees`
- `PUT /api/employees/{id}`
- `DELETE /api/employees/{id}`

### DOSSIER RH
- `GET /api/dossiers`
- `GET /api/dossiers/{id}`
- `PUT /api/dossiers/{id}`

### CONTRAT
- `GET /api/contracts`
- `POST /api/contracts`
- `PUT /api/contracts/{id}`
- `GET /api/contracts/byEmployee/{employeeId}`

### POSTE
- `GET /api/posts`
- `POST /api/posts`
- `PUT /api/posts/{id}`
- `DELETE /api/posts/{id}`

### COMPETENCE
- `GET /api/competences`
- `POST /api/competences`
- `PUT /api/competences/{id}`
Poste↔Competence relationship endpoints:
- `POST /api/posts/{postId}/competences/{competenceId}`
- `DELETE /api/posts/{postId}/competences/{competenceId}`

### RECRUTEMENT
- `GET /api/recruitments`
- `POST /api/recruitments`
- `PUT /api/recruitments/{id}`
Hire:
- `POST /api/recruitments/{id}/hire`

### CONGE
- `GET /api/leaves`
- `POST /api/leaves`
- `PUT /api/leaves/{id}/approve`
- `PUT /api/leaves/{id}/reject`

### PAIE
- `GET /api/payroll`
- `POST /api/payroll/generate`
- `GET /api/payroll/{id}`
- `GET /api/payroll/{id}/pdf`

### EVALUATION
- `GET /api/evaluations`
- `POST /api/evaluations`
- `PUT /api/evaluations/{id}`

### FORMATION
- `GET /api/trainings`
- `POST /api/trainings`
- `PUT /api/trainings/{id}`

### POINTAGE IA (ATTENDANCE)
- `POST /api/attendance/recognition` (frontend may not call; AI service does. But frontend must support viewing results.)
- `GET /api/attendance/history`
- `GET /api/attendance/byEmployee/{id}`

---

## 4) SECURITY (JWT + RBAC) — MUST IMPLEMENT IN FRONTEND
### 4.1 JWT storage & usage
- Store JWT securely (prefer in-memory + refresh on reload; if not possible, use localStorage with clear documentation).
- Add an `HttpInterceptor` that injects `Authorization: Bearer <token>` for API calls.
- Handle 401 by redirecting to login and clearing token.

### 4.2 Role-based access
Roles:
- ADMIN
- RH
- EMPLOYEE

Implement:
- `AuthGuard` (requires login)
- `RoleGuard` (requires role)
- A central `AuthService` that exposes current user and roles (fetched from `/api/auth/me`)

### 4.3 UI behavior by role
- ADMIN: sees everything
- RH: sees management modules (employees, posts, competences, recruitments, leaves approvals, payroll generate)
- EMPLOYEE: sees “My profile”, “My leaves”, “My payroll”, “My attendance”, “My trainings”, “My evaluations”

---

## 5) FRONTEND FEATURES & PAGES (MUST BUILD)
### 5.1 App layout
- Login/Register pages (public)
- Authenticated layout:
  - top bar + side nav
  - role-based menu items
  - logout button
- Use Angular Material theme.

### 5.2 Pages / modules
Create feature modules with routing:

**AuthModule**
- `/login`
- `/register`

**DashboardModule**
- `/dashboard` (role-based cards)

**EmployeesModule (RH/ADMIN)**
- list (table + search + pagination)
- create/edit form
- details page with tabs:
  - Dossier RH
  - Contracts
  - Leaves
  - Payroll
  - Evaluations
  - Trainings
  - Attendance

**PostsModule (RH/ADMIN)**
- list/create/edit/delete
- manage required competences:
  - attach/detach competence via relationship endpoints

**CompetencesModule (RH/ADMIN)**
- list/create/edit

**RecruitmentsModule (RH/ADMIN)**
- list/create/edit
- hire action:
  - UI flow: select recruitment → choose candidate (if backend provides) OR input candidate info / candidateId → call `/hire`
  - show success and created employee link

**LeavesModule**
- EMPLOYEE: create leave request + list own leaves
- RH/ADMIN: list leaves + approve/reject buttons + status badge

**PayrollModule**
- RH/ADMIN: generate payroll (month/year selection) + list payroll
- EMPLOYEE: list own payroll
- payroll details + “Download PDF” button calling `/api/payroll/{id}/pdf`

**EvaluationsModule**
- RH/ADMIN: create/update evaluations
- EMPLOYEE: read own evaluations

**TrainingsModule**
- RH/ADMIN: create/update trainings
- EMPLOYEE: read own trainings

**AttendanceModule**
- history list (table)
- by employee view
- realtime updates via WebSocket subscription to `/topic/attendance`

---

## 6) WEBSOCKET / REAL-TIME ATTENDANCE
Implement STOMP client (e.g., `@stomp/stompjs` + SockJS if backend uses it).
- Connect after login
- Subscribe to `/topic/attendance`
- On message: update attendance list and show Material snackbar notification.

If backend exposes WebSocket endpoint path (e.g., `/ws`), make it configurable in environment.

---

## 7) UI/UX REQUIREMENTS
- Use Angular Material components:
  - MatTable, MatPaginator, MatSort
  - MatDialog for confirmations (delete, approve/reject)
  - MatSnackBar for notifications
  - MatToolbar, MatSidenav
  - Reactive Forms with validation messages
- Provide loading spinners (MatProgressSpinner)
- Provide global error handler:
  - map backend validation errors into form fields
  - show human-friendly messages

---

## 8) DATA MODELS (FRONTEND INTERFACES)
Create `src/app/core/models/*.ts` for:
- auth: LoginRequest, AuthResponse, UserMe
- Candidate, Employe, Poste, Competence, Contrat, DossierRH, Planning, Conge, Paie, Evaluation, Formation
- AttendanceEvent / AttendanceRecord

---

## 9) FRONTEND ARCHITECTURE (REQUIRED)
Folder structure suggestion:

```
src/app/
  core/
    api/ (services)
    interceptors/
    guards/
    models/
    websocket/
    utils/
  shared/
    components/
    pipes/
  features/
    auth/
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
```

Use:
- Standalone components OR NgModules (choose one approach and be consistent)
- Lazy loading for feature modules

---

## 10) CONFIGURATION
- `environment.ts`:
  - `apiBaseUrl` (e.g., http://localhost:8080)
  - `wsBaseUrl` (e.g., ws://localhost:8080/ws)
- Document how to change for production.

---

## 11) DELIVERABLES
You must output:
1) Full Angular project code
2) `README.md` containing:
   - install: `npm install`
   - run: `ng serve`
   - configure API base URL
   - demo accounts (admin/rh/employee)
3) Optional: Dockerfile for frontend (nginx) if desired

---

## 12) ACCEPTANCE CRITERIA (MUST PASS)
- App compiles and runs.
- Login works; token stored; interceptor adds Authorization header.
- Role-based navigation works.
- All listed REST endpoints are callable from UI.
- Hire workflow works from UI.
- Leave approve/reject works from UI with status updates.
- Payroll generate works; PDF downloads.
- Attendance history shows; realtime updates via WebSocket work.
- Form validation matches backend constraints and shows errors properly.

---

## 13) START IMPLEMENTATION NOW
Generate the full working Angular frontend matching everything above. No missing pages for required modules. No placeholder TODO for critical flows.