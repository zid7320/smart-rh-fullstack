# SMART RH 4.0 — Backend

Spring Boot 3.2.4 · Java 21 · JWT · WebSocket · MQTT (optional) · iTextPDF · Docker

Full-featured Human Resources API with JWT authentication, role-based access control, leave workflow, payroll PDF generation, AI attendance recognition, real-time WebSocket broadcasting, and optional MQTT subscriber for IoT/edge integration.

---

## Table of Contents

1. [Prerequisites](#prerequisites)
2. [Quick Start — Local (H2)](#quick-start--local-h2)
3. [Quick Start — Docker Compose (MySQL)](#quick-start--docker-compose-mysql)
4. [Default Credentials](#default-credentials)
5. [API Overview](#api-overview)
6. [Authentication Flow](#authentication-flow)
7. [WebSocket](#websocket)
8. [MQTT Subscriber](#mqtt-subscriber)
9. [Running Tests](#running-tests)
10. [Project Structure](#project-structure)

---

## Prerequisites

| Tool | Minimum version |
|------|----------------|
| JDK  | 21             |
| Maven | 3.9+          |
| Docker + Compose | 24+ (optional, for MySQL mode) |

---

## Quick Start — Local (H2)

The default profile uses an **H2 in-memory database** — no external services needed.

```bash
# Clone and build
git clone <repo-url>
cd smart-rh-backend

# Run (H2, port 8080)
mvn spring-boot:run
```

Swagger UI: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
Health check: [http://localhost:8080/actuator/health](http://localhost:8080/actuator/health)

---

## Quick Start — Docker Compose (MySQL)

```bash
# Start all services: backend + MySQL + (optional) Mosquitto MQTT broker
docker compose up --build

# Stop
docker compose down -v
```

Services started:

| Service    | Port  | Description              |
|------------|-------|--------------------------|
| `backend`  | 8080  | Spring Boot API          |
| `db`       | 3306  | MySQL 8                  |
| `mqtt`     | 1883  | Mosquitto (MQTT broker)  |

### Environment overrides (`.env` or shell)

```bash
# MySQL
MYSQL_ROOT_PASSWORD=secret
MYSQL_DATABASE=smartrh

# MQTT (disabled by default)
MQTT_ENABLED=true
MQTT_BROKER_URL=tcp://mqtt:1883
MQTT_FORWARD_RECOGNITION=true
```

---

## Default Credentials

The application has **no built-in seed data**. Create users via the registration endpoint:

```bash
# Register an admin user (role defaults to ROLE_EMPLOYEE — promote manually via DB or set role in request)
curl -s -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","email":"admin@smartrh.io","password":"Admin@2024","role":"ROLE_ADMIN"}'
```

### Integration test credentials (H2 test profile only)

These are created dynamically per test class and destroyed after each test:

| Test class        | Username       | Password        | Role           |
|-------------------|----------------|-----------------|----------------|
| AuthIntegrationTest | `testadmin`  | `Admin@2024`    | `ROLE_ADMIN`   |
| AuthIntegrationTest | `testemployee` | `Employee@2024` | `ROLE_EMPLOYEE` |
| LeaveWorkflowIT   | `lv_admin`     | `Admin@2024`    | `ROLE_ADMIN`   |
| LeaveWorkflowIT   | `lv_rh`        | `Rh@Password1`  | `ROLE_RH`      |
| LeaveWorkflowIT   | `lv_employee`  | `Employee@2024` | `ROLE_EMPLOYEE` |
| PayrollIT         | `pr_rh`        | `Rh@Password1`  | `ROLE_RH`      |
| AttendanceIT      | `at_rh`        | `Rh@Password1`  | `ROLE_RH`      |
| ErrorResponseIT   | `er_admin`     | `Admin@2024`    | `ROLE_ADMIN`   |

---

## API Overview

All endpoints are prefixed with `/api`. Protected endpoints require `Authorization: Bearer <token>`.

### Auth — `/api/auth`

| Method | Path | Roles | Description |
|--------|------|-------|-------------|
| `POST` | `/register` | Public | Register a new user. Returns JWT. |
| `POST` | `/login`    | Public | Login with username or email. Returns JWT. |
| `GET`  | `/me`       | Any authenticated | Current user profile. |

### Employees — `/api/employees`

| Method | Path | Roles | Description |
|--------|------|-------|-------------|
| `GET`  | `/`           | ADMIN, RH | Paginated list. |
| `GET`  | `/{id}`       | Authenticated | Get by ID. |
| `POST` | `/`           | ADMIN, RH | Create. |
| `PUT`  | `/{id}`       | ADMIN, RH | Update. |
| `DELETE` | `/{id}`    | ADMIN | Delete. |

### Leaves (Congés) — `/api/leaves`

| Method | Path | Roles | Description |
|--------|------|-------|-------------|
| `GET`  | `/`                       | ADMIN, RH | Paginated list. |
| `GET`  | `/{id}`                   | Authenticated | Get by ID. |
| `GET`  | `/byEmployee/{employeId}` | Authenticated | By employee. |
| `POST` | `/`                       | Authenticated | Submit leave request. |
| `PUT`  | `/{id}/approve`           | ADMIN, RH | Approve. |
| `PUT`  | `/{id}/reject`            | ADMIN, RH | Reject. |

### Payroll — `/api/payroll`

| Method | Path | Roles | Description |
|--------|------|-------|-------------|
| `GET`  | `/`                       | ADMIN, RH | Paginated list. |
| `GET`  | `/{id}`                   | Authenticated | Get by ID. |
| `GET`  | `/byEmployee/{employeId}` | Authenticated | By employee. |
| `POST` | `/generate`               | ADMIN, RH | Generate for all employees, given month/year. |
| `GET`  | `/{id}/pdf`               | Authenticated | Download payslip PDF. |

### Attendance — `/api/attendance`

| Method | Path | Roles | Description |
|--------|------|-------|-------------|
| `POST` | `/recognition`            | ADMIN, RH | Submit face-recognition event. Broadcasts to WebSocket `/topic/attendance`. |
| `GET`  | `/history`                | ADMIN, RH | Global history, paginated, newest first. |
| `GET`  | `/byEmployee/{employeId}` | ADMIN, RH, or self | Self-service access for EMPLOYEE role. |

### Health / Ping

| Method | Path | Roles | Description |
|--------|------|-------|-------------|
| `GET` | `/api/health`    | Public    | Liveness check. |
| `GET` | `/api/admin/ping` | ADMIN    | Admin role check. |
| `GET` | `/api/rh/ping`    | ADMIN, RH | RH role check. |

---

## Authentication Flow

```
POST /api/auth/login
Body: { "usernameOrEmail": "admin", "password": "Admin@2024" }

Response 200:
{
  "token":     "eyJhbGciOiJIUzUxMiJ9...",
  "tokenType": "Bearer",
  "expiresIn": 86400,
  "userId":    1,
  "username":  "admin",
  "email":     "admin@smartrh.io",
  "role":      "ROLE_ADMIN"
}
```

Use the token in every subsequent request:
```
Authorization: Bearer eyJhbGciOiJIUzUxMiJ9...
```

### Error envelope (all errors)

```json
{
  "timestamp": "2025-03-08T10:30:00Z",
  "status":    404,
  "error":     "Not Found",
  "message":   "Employe not found with id : '99'",
  "path":      "/api/employees/99"
}
```

Validation errors also include `fieldErrors`:
```json
{
  "timestamp":   "2025-03-08T10:30:00Z",
  "status":      400,
  "error":       "Validation Failed",
  "message":     "Request validation failed",
  "path":        "/api/leaves",
  "fieldErrors": {
    "type":      "must not be blank",
    "dateDebut": "must not be null"
  }
}
```

---

## WebSocket

Real-time attendance events are broadcast via STOMP over SockJS.

**Endpoint:** `http://localhost:8080/ws` (SockJS fallback enabled)

**Topic:** `/topic/attendance`

Angular example:
```typescript
const socket = new SockJS('http://localhost:8080/ws');
const client = Stomp.over(socket);
client.connect({}, () => {
  client.subscribe('/topic/attendance', (msg) => {
    console.log(JSON.parse(msg.body));
  });
});
```

---

## MQTT Subscriber

Disabled by default. Enable in `application.properties` or via environment variable:

```properties
app.mqtt.enabled=true
app.mqtt.broker-url=tcp://localhost:1883
app.mqtt.client-id=smart-rh-backend
app.mqtt.topics=smartrh/attendance/recognition,smartrh/attendance/raw,smartrh/system/heartbeat
app.mqtt.qos=1
app.mqtt.forward-recognition=true
```

| Property | Default | Description |
|----------|---------|-------------|
| `app.mqtt.enabled` | `false` | Master switch — no MQTT beans created when false. |
| `app.mqtt.broker-url` | `tcp://localhost:1883` | Broker URL. |
| `app.mqtt.client-id` | `smart-rh-backend` | MQTT client identifier. |
| `app.mqtt.topics` | (3 topics) | Comma-separated list of topics to subscribe to. |
| `app.mqtt.forward-recognition` | `true` | Forward `attendance/recognition` messages to the recognition pipeline. |

When `forward-recognition=true`, JSON messages on `smartrh/attendance/recognition` are automatically parsed and saved as `Attendance` records, then broadcast to the WebSocket topic.

---

## Running Tests

Tests use an **H2 in-memory database** and run fully standalone — no external services needed.

```bash
# Run all tests
mvn test

# Run a specific test class
mvn test -Dtest=LeaveWorkflowIT

# Run with verbose output
mvn test -Dsurefire.failIfNoSpecifiedTests=false
```

### Test coverage

| Test class | What it covers |
|------------|----------------|
| `AuthIntegrationTest` | Registration, login (username + email), `/me`, 401/403 RBAC |
| `LeaveWorkflowIT` | Create → approve → reject → double-approve (400) → employee-approves (403) → 404 → pagination |
| `PayrollIT` | Generate → list → get-by-ID → PDF content-type → idempotent run → 403 → 404 |
| `AttendanceIT` | Recognition (201) → employee-blocked (403) → history → self-access → cross-employee (403) |
| `ErrorResponseIT` | 400 fieldErrors shape, 404 envelope, 401 status, 403 status, Content-Type: application/json |

---

## Project Structure

```
src/
├── main/java/com/smart/rh/
│   ├── config/          # SecurityConfig, WebSocketConfig, OpenApiConfig
│   ├── controller/      # REST controllers
│   ├── dto/             # Request/response records (per domain package)
│   ├── entity/          # JPA entities + enums
│   ├── exception/       # GlobalExceptionHandler, ErrorResponse, custom exceptions
│   ├── mapper/          # MapStruct mappers
│   ├── mqtt/            # Optional MQTT subscriber (MqttConfig, MqttMessageRouter)
│   ├── repository/      # Spring Data JPA repositories
│   ├── security/        # JwtTokenProvider, JwtAuthenticationFilter, UserDetailsImpl
│   └── service/         # Business logic (one per domain)
│
├── main/resources/
│   ├── application.properties          # Default (H2) configuration
│   ├── application-mysql.properties    # MySQL profile
│   └── db/migration/                   # Flyway scripts (if enabled)
│
└── test/
    ├── java/com/smart/rh/
    │   ├── auth/        AuthIntegrationTest
    │   ├── attendance/  AttendanceIT
    │   ├── conge/       LeaveWorkflowIT
    │   ├── error/       ErrorResponseIT
    │   └── paie/        PayrollIT
    └── resources/
        └── application-test.properties  # H2 + MQTT disabled
```

---

## Roles

| Role | Description |
|------|-------------|
| `ROLE_ADMIN` | Full access to all endpoints |
| `ROLE_RH` | HR operations: employees, leaves, payroll, attendance history |
| `ROLE_EMPLOYEE` | Read own profile, submit leave, view own payslips, view own attendance |
