# MASTER PROMPT — SMART RH 4.0 (GOOD GOV IT)
You are a **senior solution architect + full‑stack lead**. Build a **fully working production‑ready project** called **SMART RH 4.0**: an **intelligent HR Information System** with **Angular (frontend)**, **Spring Boot (backend)**, **MySQL (DB)**, plus an **AI attendance module (Python/OpenCV/CNN)** and an **IoT-ready integration (ESP32 + IP camera + MQTT)**.

Your output must be a **complete, runnable repository** containing:
- **Backend (Spring Boot)** with secure REST APIs (JWT + RBAC + audit trail)
- **Frontend (Angular + Angular Material)** consuming the APIs
- **Database schema + migrations** (prefer Flyway or Liquibase)
- **Python AI microservice** for face recognition attendance
- **Docker Compose** to run MySQL + backend + frontend + python service (and optional MQTT broker like Mosquitto)
- Seed data + admin account + demo scenario scripts
- Clean README with setup steps

Use best practices: validation, DTOs, exception handling, tests, logging, OpenAPI/Swagger, CORS, pagination, and role-based route guards.

---

## 1) PROJECT SPECIFICATIONS (from cahier des charges)
### 1.1 Context & Goal
Organizations need a modern, secure, intelligent HR platform automating HR processes. SMART RH 4.0 integrates:
- HR administrative management
- Process automation
- AI
- IoT for attendance/clocking
- Scalable architecture

### 1.2 Problems with legacy HR systems
- Manual processes
- Attendance fraud risk
- No predictive intelligence
- Multi-site management difficulty
- Lack of traceability

### 1.3 Objectives
- Fully digitalize HR lifecycle
- Automate business processes
- Integrate facial recognition for attendance
- Secure access with **JWT** and **RBAC**
- Ensure traceability and compliance (**audit trail**)

### 1.4 Technical Architecture
**Frontend**
- Angular
- TypeScript
- Angular Material

**Backend**
- Spring Boot
- Secure REST API

**Database**
- MySQL

**AI**
- Python
- OpenCV
- CNN

**IoT**
- ESP32
- IP Cameras
- MQTT

**Security**
- JWT
- RBAC
- Audit trail

### 1.5 Non-functional requirements
- Response time < 2 seconds (typical operations)
- JWT security
- Scalable architecture
- High availability (design for it; dockerized)
- Full logging/journalization

### 1.6 Engineering points to highlight
- Microservices-ready architecture
- IoT + MQTT integration
- Real-time AI (CNN)
- RBAC security
- WebSocket for real-time

---

## 2) DOMAIN MODEL (EXTRACTED FROM DIAGRAM IMAGE)
Implement these entities + relations. Use proper JPA mappings.

### 2.1 Entities & Attributes

#### Candidate
- idCandidat: int
- nom: String
- prenom: String
- email: String

#### ResponsableRH
- idResponsable: int
- nom: String

#### Recrutement
- idRecrutement: int
- posteCible: String
- statut: String

#### Employe
- idEmploye: int
- nom: String
- prenom: String
- poste: String  *(keep as string for legacy; ALSO link to Poste entity for normalized design)*
- email: String

#### Poste
- idPoste: int
- titre: String
- competencesRequises: String *(legacy string; ALSO use relation to Competence)*

#### Competence
- idCompetence: int
- nom: String
- niveau: String

#### Contrat
- idContrat: int
- type: String
- dateDebut: Date
- dateFin: Date
- salaire: Double

#### DossierRH
- idDossier: int
- infosPerso: String
- diplomes: String
- documents: String

#### Planning
- idPlanning: int
- horaires: String

#### Conge
- idConge: int
- type: String
- dateDebut: Date
- dateFin: Date

#### Paie
- idPaie: int
- montant: Double
- bulletinPDF: String *(store path/URL; also generate real PDF bytes in endpoint)*

#### Evaluation
- idEval: int
- objectifs: String
- kpi: String

#### Formation
- idFormation: int
- titre: String
- certification: String

### 2.2 Relationships & Cardinalities (from diagram)
Implement exactly, then improve where needed.

- **Candidate (0..*) — postule — (1) Recrutement**
  - Many candidates can apply to one recruitment.
  - Each Candidate is linked to exactly one Recrutement in the diagram.

- **ResponsableRH (1) — gère — (0..*) Recrutement**
  - One HR manager manages many recruitments.
  - Each recruitment has one HR manager.

- **Recrutement (0..1) — embauche — (1) Employe**
  - A recruitment may hire at most one employee.
  - Each employee is associated with exactly one recruitment in diagram.  
  - In implementation: allow nullable `recrutement` in employee and ensure hire operation creates/links employee.

- **Employe — occupe — Poste**
  - Diagram shows: Employe side **0..*** and Poste side **1** (interpreted: many employees occupy one poste; each employee has exactly one poste).
  - Implement: `Employe -> Poste (ManyToOne)`, `Poste -> Employes (OneToMany)`.

- **Poste (1) — requiert — (0..*) Competence**
  - A poste requires multiple competences.
  - Competence can be required by multiple postes (diagram shows 0..* at competence side too).
  - Implement: Many-to-Many (`PosteCompetence` join table) + optional `niveauRequis` on join.

- **Employe (1) — lié à / possède — (1) DossierRH**
  - One employee has exactly one HR file; one dossier belongs to one employee.
  - Implement: One-to-One.

- **Employe (1) — suit — (0..*) Planning**
  - One employee has multiple planning entries.
  - Implement: One-to-Many.

- **Employe (1) — demande — (0..*) Conge**
  - One employee requests many leaves.
  - Implement: One-to-Many.

- **Employe (1) — reçoit — (0..*) Paie**
  - One employee receives many payroll records.
  - Implement: One-to-Many.

- **Employe (1) — subit — (0..*) Evaluation**
  - One employee has many evaluations.
  - Implement: One-to-Many.

- **Employe (1) — suit — (0..*) Formation**
  - One employee attends many trainings.
  - Implement: One-to-Many (or Many-to-Many if you choose; but diagram suggests 0..* formations per employee and (implicitly) each formation per employee; simplest: One-to-Many where Formation belongs to one employee).

---

## 3) BACKEND REQUIRED PACKAGE STRUCTURE (MANDATORY)
Create Spring Boot project with base package `com.smart.rh`:

- com.smart.rh.controller
- com.smart.rh.service
- com.smart.rh.repository
- com.smart.rh.entity
- com.smart.rh.dto
- com.smart.rh.security
- com.smart.rh.config

Include:
- Global exception handler
- Validation annotations
- Mapper layer (MapStruct recommended) or manual mapping

---

## 4) SECURITY REQUIREMENTS (JWT + RBAC + AUDIT)
### 4.1 Authentication
- Use JWT access tokens (and optional refresh tokens).
- Password hashing with BCrypt.
- Endpoints:
  - `POST /api/auth/login`
  - `POST /api/auth/register`
  - `GET /api/auth/me`

### 4.2 Roles (RBAC)
Implement at least:
- `ROLE_ADMIN`
- `ROLE_RH` (HR manager)
- `ROLE_EMPLOYEE`

Enforce role access:
- Admin: full access
- RH: manage HR resources (employees, recruitments, contracts, payroll, evaluations, trainings)
- Employee: read own profile, request leave, view own payroll, own evaluations/trainings, view attendance history

### 4.3 Audit Trail
Implement an audit log table capturing:
- actorUserId
- actorRole(s)
- action (CREATE/UPDATE/DELETE/LOGIN/APPROVE/REJECT/etc.)
- entityName + entityId
- timestamp
- ipAddress (if possible)
- payload summary (safe; do not log passwords)
Also log security events (login success/failure).

---

## 5) REST APIS TO IMPLEMENT (AS GIVEN)
All endpoints under `/api`.

### 5.1 AUTHENTIFICATION
- `POST /api/auth/login`
- `POST /api/auth/register`
- `GET /api/auth/me`

### 5.2 EMPLOYE
- `GET /api/employees`
- `GET /api/employees/{id}`
- `POST /api/employees`
- `PUT /api/employees/{id}`
- `DELETE /api/employees/{id}`

Relations:
- has one DossierRH
- occupies a Poste
- has many Contrats
- has many Conges
- has many Paies
- has many Evaluations
- follows many Formations

### 5.3 DOSSIER RH
- `GET /api/dossiers`
- `GET /api/dossiers/{id}`
- `PUT /api/dossiers/{id}`

### 5.4 CONTRAT
- `GET /api/contracts`
- `POST /api/contracts`
- `PUT /api/contracts/{id}`
- `GET /api/contracts/byEmployee/{employeeId}`

### 5.5 POSTE
- `GET /api/posts`
- `POST /api/posts`
- `PUT /api/posts/{id}`
- `DELETE /api/posts/{id}`

### 5.6 COMPETENCE
- `GET /api/competences`
- `POST /api/competences`
- `PUT /api/competences/{id}`

Rule:
- A poste requires several competences (implement endpoints to attach/detach competences to postes).

### 5.7 RECRUTEMENT
- `GET /api/recruitments`
- `POST /api/recruitments`
- `PUT /api/recruitments/{id}`

Hire conversion (Candidate -> Employee):
- `POST /api/recruitments/{id}/hire`
  - should create an Employee from a selected Candidate
  - mark recruitment status accordingly
  - assign poste, create dossier, etc.

### 5.8 CONGE
- `GET /api/leaves`
- `POST /api/leaves`
- `PUT /api/leaves/{id}/approve`
- `PUT /api/leaves/{id}/reject`
Add fields in entity (even if not in diagram) to support workflow:
- status: PENDING/APPROVED/REJECTED
- requestedAt, decidedAt, decidedBy

### 5.9 PAIE
- `GET /api/payroll`
- `POST /api/payroll/generate`
- `GET /api/payroll/{id}`
- `GET /api/payroll/{id}/pdf`
Implementation:
- payroll generation creates payroll records for a month/year and employees
- pdf endpoint returns actual PDF file (generated server-side)

### 5.10 EVALUATION
- `GET /api/evaluations`
- `POST /api/evaluations`
- `PUT /api/evaluations/{id}`

### 5.11 FORMATION
- `GET /api/trainings`
- `POST /api/trainings`
- `PUT /api/trainings/{id}`

### 5.12 POINTAGE IA (KEY MODULE)
- `POST /api/attendance/recognition`
- `GET /api/attendance/history`
- `GET /api/attendance/byEmployee/{id}`

Flow:
**Camera → Python AI Service → Spring Boot → Database → Angular**

Implement an `Attendance` entity (not in diagram but required):
- id
- employee (FK)
- timestamp
- type (IN/OUT)
- confidence
- cameraId / siteId
- rawImagePath (optional)

Recognition endpoint:
- Spring Boot receives recognition payload from Python service:
  - recognizedEmployeeId OR embedding match result
  - confidence score
  - timestamp
  - image reference
- Save attendance record and push real-time update to frontend via WebSocket.

---

## 6) FRONTEND REQUIREMENTS (ANGULAR + MATERIAL)
Build Angular app with:
- Auth pages (login/register)
- Layout with role-based menu
- Modules/pages:
  - Employees (list, details, create/edit)
  - Dossiers RH (view/edit)
  - Contracts (list, by employee)
  - Posts & competences (manage + link)
  - Recruitments (list/create/update + hire action)
  - Leaves (employee requests + RH approves/rejects)
  - Payroll (generate, list, details, download PDF)
  - Evaluations
  - Trainings
  - Attendance (history, by employee, real-time updates via WebSocket)
- Angular Material tables with pagination, sorting, filtering
- Route guards by role
- Interceptors: JWT token injection, error handling

---

## 7) PYTHON AI SERVICE (FACE RECOGNITION)
Create separate service:
- FastAPI (recommended) or Flask
- Endpoint: `/recognize` that accepts an image frame (base64 or multipart)
- Use OpenCV for face detection + CNN-based recognition (you can use a lightweight approach for demo):
  - For a working demo, allow either:
    1) a simple embedding-based recognizer (e.g., face_recognition library) OR
    2) OpenCV DNN face detector + a placeholder CNN classifier model
- Maintain dataset of employee faces:
  - enrollment endpoint `/enroll` to register employeeId + face images
- When recognition happens:
  - python calls Spring Boot `POST /api/attendance/recognition` with employeeId/confidence/timestamp/imageRef
- Provide clear instructions to run and test locally.

---

## 8) IOT + MQTT INTEGRATION (READY-TO-USE)
Add an MQTT broker service in docker-compose (Eclipse Mosquitto).
Implement in Spring Boot:
- MQTT client subscribing to topics like:
  - `smart-rh/attendance/camera/{cameraId}`
  - `smart-rh/iot/esp32/{deviceId}/status`
- Messages can carry:
  - camera snapshot URL or base64 frame
  - device heartbeats
- For demo, allow simulated publishing scripts.

---

## 9) WEBSOCKET REAL-TIME
Implement WebSocket (Spring):
- Broadcast new attendance events to:
  - `/topic/attendance`
- Angular subscribes and updates attendance table in real time.

---

## 10) DATABASE & MIGRATIONS
Use MySQL.
- Provide migration scripts for all tables
- Include indices on foreign keys and timestamps (attendance, payroll)
- Provide seed data:
  - Admin user + RH user + 2 employees
  - Sample poste + competences
  - Sample recruitment + candidate

---

## 11) QUALITY REQUIREMENTS
- OpenAPI/Swagger at `/swagger-ui` (or `/swagger-ui.html`)
- Unit tests for services
- Integration tests for auth and at least one key module
- Centralized error format:
  - timestamp, status, error, message, path
- DTOs for requests/responses (do not expose entities directly)
- Pagination for list endpoints
- CORS configured for Angular dev server

---

## 12) DELIVERABLE FORMAT REQUIRED FROM YOU (THE AI MODEL)
Produce:
1) Repo structure with directories:
   - `/backend` (Spring Boot)
   - `/frontend` (Angular)
   - `/ai-service` (Python)
   - `/infra` (docker-compose, mqtt configs, scripts)
2) Exact commands to run everything:
   - `docker compose up --build`
3) Credentials for seed users
4) Screenshots are optional, but at least provide a demo walkthrough.

---

## 13) IMPORTANT IMPLEMENTATION DECISIONS (MAKE THEM EXPLICIT)
When implementing, decide and document:
- Date types (LocalDate vs Instant)
- Employee/Poste modeling: keep `Employe.poste` string **and** a FK to `Poste`
- Training relation: keep simple `Formation -> Employe` ManyToOne unless you decide ManyToMany (then implement join)
- Store PDF payroll in filesystem with DB reference, or store bytes in DB (filesystem recommended)
- Attendance: IN/OUT logic (toggle per day or explicit type passed from python)

---

## 14) ACCEPTANCE CRITERIA (MUST PASS)
- All listed endpoints exist and work with JWT security.
- RBAC rules enforced.
- MySQL schema created automatically via migrations.
- Frontend can login and perform CRUD operations.
- Hire endpoint converts a candidate to employee and links recruitment.
- Leave approve/reject works with status updates and audit log entry.
- Payroll generation creates records and PDF endpoint downloads a PDF.
- Attendance recognition endpoint stores events; WebSocket pushes updates; Angular receives them.
- Dockerized setup runs successfully.

---

## 15) NOW START BUILDING
Proceed to generate the full codebase with the above constraints.
Do not leave TODOs for core functionality. Provide working implementations.

(End of master prompt)