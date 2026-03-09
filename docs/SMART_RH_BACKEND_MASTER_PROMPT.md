# MASTER PROMPT (Backend Only) — SMART RH 4.0 / GOOD GOV IT
You are a **senior backend architect and Spring Boot expert**. Build a **fully working, production-ready backend** for **SMART RH 4.0** (intelligent HR Information System) that strictly matches the project specifications and the domain model below.

You must produce a **complete runnable backend codebase** (not pseudo-code) with:
- **Spring Boot** (prefer **Spring Boot 3.x** + Java 21; if you choose 2.7, justify and ensure compatibility)
- **REST API** secured by **JWT + RBAC**
- **MySQL** database + **Flyway** (or Liquibase) migrations
- **Audit trail** (compliance logging)
- **WebSocket** for real-time attendance events
- Integration-ready **AI attendance** flow (Python service will call Spring endpoint)
- Integration-ready **MQTT** subscriber (ESP32 / camera events)
- OpenAPI/Swagger documentation
- Seed data for demo + admin credentials
- Dockerization (Dockerfile + docker-compose for MySQL + backend; optional mosquitto)

You must not leave TODOs in core features. Everything required must be implemented and runnable.

---

## A) PROJECT SPECIFICATIONS (REFERENCE — MUST FOLLOW)
### A1) Context & objectives
SMART RH 4.0 aims to digitalize the full HR lifecycle, automate processes, integrate AI and IoT for attendance, secure access with JWT/RBAC, and ensure traceability via audit trail.

### A2) Technical architecture (backend)
- Backend: Spring Boot, REST API secured
- DB: MySQL
- Security: JWT, RBAC, audit trail
- IoT: ESP32, IP cameras, MQTT (backend subscribes)
- AI: Python + OpenCV + CNN (Python calls backend recognition endpoint)
- Real-time: WebSocket for live updates

### A3) Non-functional requirements
- Typical response time < 2 seconds
- Secure JWT
- Scalable architecture (modular services, microservices-ready)
- High availability ready (dockerized, stateless API)
- Full journalization/logging

---

## B) REQUIRED PACKAGE STRUCTURE (MANDATORY)
Use base package: `com.smart.rh`

```
com.smart.rh
├── controller
├── service
├── repository
├── entity
├── dto
├── security
└── config
```

Also include:
- `exception` package for global exception handling (even if not listed, it is required for a clean backend)
- `mapper` package (MapStruct recommended)

---

## C) DOMAIN MODEL (EXTRACTED FROM DIAGRAM — MUST IMPLEMENT)
Implement entities with JPA mappings and enforce relationships.

### C1) Entities & fields

**Candidate**
- idCandidat: int
- nom: String
- prenom: String
- email: String

**ResponsableRH**
- idResponsable: int
- nom: String

**Recrutement**
- idRecrutement: int
- posteCible: String
- statut: String

**Employe**
- idEmploye: int
- nom: String
- prenom: String
- poste: String (legacy)
- email: String
Additionally (for normalized model): relation to `Poste` entity.

**Poste**
- idPoste: int
- titre: String
- competencesRequises: String (legacy)
Additionally: relation to `Competence` entity.

**Competence**
- idCompetence: int
- nom: String
- niveau: String

**Contrat**
- idContrat: int
- type: String
- dateDebut: Date/LocalDate
- dateFin: Date/LocalDate
- salaire: Double/BigDecimal

**DossierRH**
- idDossier: int
- infosPerso: String
- diplomes: String
- documents: String

**Planning**
- idPlanning: int
- horaires: String

**Conge**
- idConge: int
- type: String
- dateDebut: Date/LocalDate
- dateFin: Date/LocalDate
Add workflow fields: status, requestedAt, decidedAt, decidedBy.

**Paie**
- idPaie: int
- montant: Double/BigDecimal
- bulletinPDF: String (path/url)
Also store month/year and generation metadata.

**Evaluation**
- idEval: int
- objectifs: String
- kpi: String

**Formation**
- idFormation: int
- titre: String
- certification: String

### C2) Relationships & cardinalities (from diagram)
Implement these (then extend where needed for workflows):

- Candidate (0..*) → applies to → Recrutement (1)
- ResponsableRH (1) → manages → Recrutement (0..*)
- Recrutement (0..1) → hires → Employe (1)
- Employe (many) → occupies → Poste (1)
- Poste ↔ Competence: Poste requires many competences; competence can belong to many postes (Many-to-Many)
- Employe (1) ↔ DossierRH (1) One-to-One
- Employe (1) → has many → Contrat
- Employe (1) → has many → Planning
- Employe (1) → requests many → Conge
- Employe (1) → receives many → Paie
- Employe (1) → has many → Evaluation
- Employe (1) → follows many → Formation (choose One-to-Many or Many-to-Many; document decision; implement endpoints accordingly)

### C3) Additional required entities (for SMART RH 4.0 features)
Add these because they are required by the specs:

**User** (auth)
- id, username/email, passwordHash, enabled, createdAt
- roles (ADMIN, RH, EMPLOYEE)
- optional link to Employe (for ROLE_EMPLOYEE accounts)

**AuditLog**
- id
- actorUserId
- actorUsername
- actorRoles
- action
- entityName
- entityId
- timestamp
- ipAddress
- summary (safe)

**Attendance**
- id
- employee (FK)
- timestamp (Instant)
- type (IN/OUT)
- confidence (double)
- cameraId/siteId (string)
- rawImagePath or imageRef (string)

---

## D) REQUIRED REST APIs (MUST MATCH PATHS)
All under `/api`.

### D1) AUTH
- `POST /api/auth/login`
- `POST /api/auth/register`
- `GET /api/auth/me`

### D2) EMPLOYEE
- `GET /api/employees` (pagination)
- `GET /api/employees/{id}`
- `POST /api/employees`
- `PUT /api/employees/{id}`
- `DELETE /api/employees/{id}`

### D3) DOSSIER RH
- `GET /api/dossiers`
- `GET /api/dossiers/{id}`
- `PUT /api/dossiers/{id}`

### D4) CONTRACT
- `GET /api/contracts`
- `POST /api/contracts`
- `PUT /api/contracts/{id}`
- `GET /api/contracts/byEmployee/{employeeId}`

### D5) POSTE
- `GET /api/posts`
- `POST /api/posts`
- `PUT /api/posts/{id}`
- `DELETE /api/posts/{id}`

### D6) COMPETENCE
- `GET /api/competences`
- `POST /api/competences`
- `PUT /api/competences/{id}`

Also add relationship endpoints (required for usability):
- `POST /api/posts/{postId}/competences/{competenceId}`
- `DELETE /api/posts/{postId}/competences/{competenceId}`

### D7) RECRUITMENT
- `GET /api/recruitments`
- `POST /api/recruitments`
- `PUT /api/recruitments/{id}`
Hire conversion:
- `POST /api/recruitments/{id}/hire` (Candidate → Employee creation/linking)

### D8) LEAVE (CONGE)
- `GET /api/leaves`
- `POST /api/leaves` (creates PENDING)
- `PUT /api/leaves/{id}/approve`
- `PUT /api/leaves/{id}/reject`

### D9) PAYROLL (PAIE)
- `GET /api/payroll`
- `POST /api/payroll/generate` (month/year + selection)
- `GET /api/payroll/{id}`
- `GET /api/payroll/{id}/pdf` (returns application/pdf)

### D10) EVALUATION
- `GET /api/evaluations`
- `POST /api/evaluations`
- `PUT /api/evaluations/{id}`

### D11) TRAINING (FORMATION)
- `GET /api/trainings`
- `POST /api/trainings`
- `PUT /api/trainings/{id}`

### D12) AI ATTENDANCE (KEY MODULE)
- `POST /api/attendance/recognition`
- `GET /api/attendance/history`
- `GET /api/attendance/byEmployee/{id}`

Flow:
Camera/IoT → Python AI → Spring Boot recognition endpoint → DB → WebSocket → Angular

---

## E) SECURITY REQUIREMENTS (MUST IMPLEMENT)
### E1) JWT
- Stateless auth
- BCrypt password hashing
- Include token expiry
- Add CORS config for Angular
- Add `@SecurityRequirement` in OpenAPI where relevant

### E2) RBAC rules (minimum)
Define roles:
- `ROLE_ADMIN`
- `ROLE_RH`
- `ROLE_EMPLOYEE`

Access matrix (implement and document):
- ADMIN: all endpoints
- RH: manage employees, recruitments, posts, competences, contracts, dossiers, payroll, evaluations, trainings, approve/reject leaves, read attendance
- EMPLOYEE: read/update own profile (if allowed), request leave, view own payroll, view own attendance history, view own evaluations/trainings

Implement “self” enforcement (employee cannot read others unless RH/ADMIN):
- e.g. `/api/attendance/byEmployee/{id}` must check id==current user's employeeId unless RH/ADMIN.

---

## F) AUDIT TRAIL (MUST IMPLEMENT)
- Persist AuditLog records for:
  - login success/failure
  - create/update/delete for key entities
  - approve/reject leave
  - payroll generate
  - attendance recognition received
- Ensure sensitive data (passwords, tokens) never logged.

---

## G) WEBSOCKET REAL-TIME (MUST IMPLEMENT)
- WebSocket endpoint in backend
- Broadcast attendance events to `/topic/attendance`
- Provide DTO event payload containing:
  - employeeId, timestamp, type, confidence, cameraId

---

## H) MQTT (INTEGRATION READY)
- Add optional module subscribing to MQTT topics (e.g., `smart-rh/attendance/camera/{cameraId}`)
- For demo: log received messages and optionally forward to AI service (do not require real camera)
- Provide config to enable/disable MQTT via properties

---

## I) DATABASE, MIGRATIONS, SEEDING
### I1) MySQL + Flyway
- All tables created via migrations
- Add indexes on:
  - foreign keys
  - attendance timestamp
  - payroll month/year

### I2) Seed data
Insert:
- 1 admin user (`admin@smart-rh.local` / `Admin@12345`)
- 1 RH user (`rh@smart-rh.local` / `Rh@12345`)
- 2 employee accounts linked to Employe records

Add sample:
- poste + competences
- recruitment + candidate
- one leave request
- one payroll record
- one attendance record

---

## J) CODE QUALITY REQUIREMENTS
- DTOs for all endpoints (do not expose entities directly)
- Bean Validation annotations
- Global exception handling with consistent JSON format:
  - timestamp, status, error, message, path
- Pagination for list endpoints (`Pageable`)
- OpenAPI/Swagger configured
- Tests:
  - Auth integration test (login/register/me)
  - Leave approve/reject test
  - Attendance recognition test
  - Payroll PDF endpoint test (at least checks content-type and non-empty bytes)

---

## K) DELIVERABLES (WHAT YOU MUST OUTPUT)
You must output a complete backend repository with:
- `Dockerfile`
- `docker-compose.yml` (MySQL + backend; optional mosquitto)
- `README.md` with exact run instructions:
  - `docker compose up --build`
- Environment variables documented
- Swagger URL
- Seed credentials

Do not omit any required endpoint. Do not leave endpoints unsecured.

---

## L) IMPLEMENTATION NOTES (IMPORTANT DECISIONS)
- Prefer `BigDecimal` for money fields (salary, payroll amount).
- Prefer `LocalDate` for start/end dates; `Instant` for timestamps.
- Use MapStruct for mapping DTO <-> Entity.
- Use Service layer for business workflows (hire, approve/reject, payroll generation, attendance recognition).
- Use `@Transactional` where multiple writes occur.

---

## M) START NOW
Generate the full working backend codebase according to the above. Ensure it compiles and runs.

Before finishing, verify:
- All endpoints exist exactly as specified
- JWT security works
- RBAC works
- MySQL migrations run
- Seed users can login and access according to role
- WebSocket broadcasts attendance events
- PDF download endpoint returns a valid PDF