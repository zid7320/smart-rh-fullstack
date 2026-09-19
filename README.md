# SMART RH 4.0 - Full-Stack HR Management System

**Status:** Module 10 (Facial Recognition Dashboard) ✅ Complete | Ready for Evaluation

A modern, production-ready HR management system with real-time facial recognition attendance tracking, IoT device integration, and comprehensive admin dashboards.

---

## 🚀 Quick Start

### Prerequisites

- **Docker & Docker Compose** (latest)
- **Node.js v20+** and npm v11+
- **4GB RAM minimum** for containers

### Start Everything (2 minutes)

**Terminal 1 - Backend + Database:**

```bash
docker compose up --build
```

**Terminal 2 - Frontend (after "Healthy" appears in Terminal 1):**

```bash
cd frontend
npm install
npm start
```

### Access Application

| Service          | URL                                   | Credentials                   |
| ---------------- | ------------------------------------- | ----------------------------- |
| **Frontend**     | http://localhost:4200                 | admin@smartrh.com / Admin@2024 |
| **Backend API**  | http://localhost:8081/api             | Auto-verified via JWT         |
| **Swagger Docs** | http://localhost:8081/swagger-ui.html | No auth required              |
| **Database**     | localhost:3307                        | root / mysql123 (MySQL 8.0)   |
| **MQTT Broker**  | localhost:1883                        | Anonymous (TLS: 8883)         |

---

## 📋 Project Status

### Completed Modules ✅

| Module | Component                     | Status      | Notes                                                 |
| ------ | ----------------------------- | ----------- | ----------------------------------------------------- |
| **10** | Facial Recognition Attendance | ✅ Complete | Real-time dashboard, fraud alerts, HR verification    |
| **5**  | Device Management             | ✅ Complete | IoT registration, MQTT credentials, health monitoring |
| **8**  | Real-time Notifications       | ✅ Complete | WebSocket connection, attendance events broadcast     |
| **3**  | User Authentication           | ✅ Complete | JWT + Role-based access (ADMIN, RH, EMPLOYEE)         |

### In Progress / Planned 📅

| Module   | Component             | Target | Effort                                    |
| -------- | --------------------- | ------ | ----------------------------------------- |
| **19**   | BI & Reporting        | Week 3 | Attendance trends, fraud metrics, exports |
| **17**   | Employee Self-Service | Week 3 | View own attendance, download reports     |
| **MQTT** | Device Integration    | Week 2 | Full device simulation, TLS certificates  |

---

## 🏗️ System Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                    SMART RH 4.0 Stack                       │
├─────────────────────────────────────────────────────────────┤
│                                                              │
│  Frontend                  Backend                Services   │
│  ──────────────────────────────────────────────────────────  │
│  Angular 21+              Spring Boot 3.2     MQTT Broker   │
│  - Attendance             - REST APIs         (Mosquitto)   │
│  - Devices                - WebSockets                       │
│  - Analytics              - Auth (JWT)        MySQL 8.0     │
│  - Reports                - IoT Integration   Database      │
│  - Settings               - MQTT Handler                     │
│                                                              │
│  Port: 4200               Port: 8081          Port: 1883    │
│                                                              │
└─────────────────────────────────────────────────────────────┘
```

### Core Services

**Backend (Spring Boot)**

- REST API: Device management, attendance, users, analytics
- WebSocket: Real-time attendance event streaming
- MQTT Handler: IoT device communication
- JWT Security: Role-based access control
- Database: MySQL 8.0 with optimized indexes

**Frontend (Angular)**

- Attendance Dashboard: Real-time facial recognition feed
- Fraud Alert Center: HR verification workflow
- Device Management: Register & monitor IoT devices
- Analytics & Reports: Attendance trends, fraud metrics
- User Settings: Profile, preferences

---

## 📁 Project Structure

```
smart-rh-fullstack/
├── src/                          # Spring Boot application (backend code)
│   ├── main/java/com/smart/rh/
│   │   ├── controller/           # REST endpoints
│   │   ├── service/              # Business logic
│   │   ├── entity/               # JPA entities
│   │   ├── repository/           # Database queries
│   │   ├── config/               # Spring config (security, CORS, WebSocket, OpenAPI)
│   │   ├── security/             # JWT authentication
│   │   ├── mqtt/                 # MQTT broker integration
│   │   ├── dto/                  # Data Transfer Objects
│   │   ├── mapper/               # Entity <-> DTO mappers
│   │   ├── schedule/             # Scheduled jobs (device health, sensors)
│   │   └── exception/            # Custom exceptions & global handler
│   ├── main/resources/
│   │   ├── application.properties        # Configuration (default: H2)
│   │   ├── application-mysql.properties  # MySQL profile (used by Docker)
│   │   └── db/migration/         # Flyway migrations
│   └── test/                     # Backend tests
│
├── frontend/                     # Angular application
│   ├── src/
│   │   ├── app/
│   │   │   ├── features/         # Feature modules
│   │   │   │   ├── attendance/   # Module 10 - Facial recognition
│   │   │   │   ├── devices/      # Module 5 - Device management
│   │   │   │   ├── users/        # User management
│   │   │   │   ├── analytics/    # Module 19 - Reports & BI
│   │   │   │   └── auth/         # Authentication
│   │   │   ├── core/             # Singleton services
│   │   │   │   ├── api/          # HTTP services
│   │   │   │   ├── websocket/    # Real-time updates
│   │   │   │   └── auth/         # JWT interceptors
│   │   │   └── shared/           # Shared components & utilities
│   │   ├── assets/               # Images, fonts
│   │   └── styles/               # Global CSS/Tailwind
│   ├── angular.json              # Angular CLI config
│   ├── tailwind.config.js        # Tailwind CSS config
│   └── package.json              # npm dependencies
│
├── docs/                         # Documentation
│   ├── CHAPITRE_1_CADRE_DU_PROJET.md    # Project context (French)
│   ├── DIAGRAMMES_SEQUENCES.md          # Architecture diagrams
│   └── SMART_RH_*_MASTER_PROMPT.md      # Development guidelines
│
├── docker-compose.yml            # Services orchestration
├── pom.xml                       # Maven dependencies
├── Dockerfile                    # Backend image
└── START_HERE.md                 # Quick setup instructions
```

---

## 🔑 Key Features

### ✅ Module 10: Facial Recognition Attendance (COMPLETE)

**Dashboard**

- Real-time employee check-in/out feed with confidence scores
- Live WebSocket updates (no page refresh needed)
- Fraud detection indicators (mask detected, spoofing alerts)
- Summary statistics (check-ins, check-outs, suspicious events)

**Fraud Detection & Verification**

- Confidence score display (highlighted if <70%)
- Fraud reason classification (MASK_DETECTED, SPOOFING, etc.)
- HR verification workflow (Approve/Block with notes)
- Photo evidence viewing (Base64 images from MQTT)

**Responsive Design**

- Mobile-first layout with Tailwind CSS
- Desktop 3-column grid (summary + feed + alerts)
- Touch-friendly buttons and interactions
- Loading states and empty states

### ✅ Module 5: IoT Device Management (COMPLETE)

**Device Registration**

- Unique MQTT credentials generation (bcrypt hashed)
- Device types: DOOR_SENSOR, CAMERA, KIOSK, MOBILE
- Status tracking (ACTIVE, INACTIVE, OFFLINE, FAILED)
- Firmware version management

**Health Monitoring**

- Uptime percentage tracking
- Error rate aggregation
- Consecutive failure detection
- Automatic alert generation

**MQTT Integration**

- Topic structure: `smartrh/{deviceId}/{eventType}`
- Payload validation and parsing
- Automatic DeviceHealth record creation
- Dead-letter queue for failed events

### ✅ Module 8: Real-Time Notifications (COMPLETE)

**WebSocket Streaming**

- Attendance events: `/topic/attendance`
- Device alerts: `/topic/devices/{deviceId}`
- System notifications: `/topic/notifications`
- Auto-reconnect with exponential backoff

---

## 🛠️ Development Commands

### Docker Operations

```bash
# Start all services (build if needed)
docker compose up --build

# Start without rebuild
docker compose up

# Stop all services
docker compose down

# Remove all data (fresh start)
docker compose down -v

# View service logs
docker logs smart-rh-backend
docker logs smart-rh-mysql
docker logs smart-rh-mqtt

# Execute commands in container
docker exec smart-rh-backend sh
```

### Frontend Development

```bash
cd frontend

# Install dependencies
npm install

# Start dev server (port 4200)
npm start

# Build for production
npm run build

# Run tests
npm test

# Lint & fix code
npm run lint -- --fix
```

### Backend Development

```bash
# View logs (running in Docker)
docker logs -f smart-rh-backend

# Access Spring Boot actuator
curl http://localhost:8081/actuator/health

# View Swagger UI
# Open: http://localhost:8081/swagger-ui.html
```

---

## 🔒 Authentication & Security

### User Roles & Permissions

| Role         | Features                                       | Endpoints        |
| ------------ | ---------------------------------------------- | ---------------- |
| **ADMIN**    | Full system access, user management            | /api/admin/\*    |
| **RH**       | Attendance verification, fraud alerts, reports | /api/rh/\*       |
| **EMPLOYEE** | Own attendance, self-service portal            | /api/employee/\* |

### Default Credentials

```
Email: admin@smartrh.com
Password: Admin@2024
```

Other demo accounts (RH manager, employees) are created on startup by `UserPasswordInitializer` (MySQL / Docker profile) and `DataInitializer` (default H2 profile) in `src/main/java/com/smart/rh/config/`. With the MySQL profile their passwords are reset on every startup.

### JWT Token Flow

1. POST `/api/auth/login` → Get JWT token
2. Include token in `Authorization: Bearer {token}` header
3. Token validity: 24 hours (configurable)
4. Auto-refresh on refresh token endpoint

---

## 📊 Database Schema

### Core Tables

| Table              | Purpose                   | Columns                                                          |
| ------------------ | ------------------------- | ---------------------------------------------------------------- |
| `users`            | Employee & admin accounts | id, email, password (bcrypt), role, status                       |
| `devices`          | IoT device registry       | id, deviceId, type, mqtt_username, mqtt_password, status         |
| `sensor_readings`  | Time-series IoT data      | id, device_id, reading_type, value, timestamp                    |
| `attendance_event` | Facial recognition events | id, employee_id, device_id, event_type, confidence, fraud_reason |
| `alerts`           | System & device alerts    | id, device_id, severity, message, acknowledged                   |

### Migrations

Located in `src/main/resources/db/migration/`:

- V1-V7: Core schema setup
- V8: IoT foundation (devices, sensor_readings, device_health)
- V9: Attendance system (attendance_event, fraud_threshold)
- V10+: Upcoming modules

---

## 🧪 API Examples

### Get Attendance Feed

```bash
curl -H "Authorization: Bearer {token}" \
  http://localhost:8081/api/attendance/events?page=0&size=20
```

Response:

```json
{
  "content": [
    {
      "id": 1,
      "employeeId": 5,
      "eventType": "CHECK_IN",
      "confidence": 0.95,
      "fraudReason": null,
      "timestamp": "2026-04-20T10:30:00Z",
      "verified": false
    }
  ],
  "totalElements": 150,
  "currentPage": 0
}
```

### Verify Fraud Alert (HR Only)

```bash
curl -X POST \
  -H "Authorization: Bearer {token}" \
  -H "Content-Type: application/json" \
  -d '{"isValid": false, "notes": "Spoofing detected"}' \
  http://localhost:8081/api/attendance/events/1/verify
```

### Register IoT Device

```bash
curl -X POST \
  -H "Authorization: Bearer {adminToken}" \
  -H "Content-Type: application/json" \
  -d '{
    "deviceId": "camera-001",
    "deviceType": "CAMERA",
    "location": "Building A - Floor 1"
  }' \
  http://localhost:8081/api/devices/register
```

---

## 🐛 Troubleshooting

### Backend Won't Start

```bash
# Check logs
docker logs smart-rh-backend

# Verify database is ready
docker logs smart-rh-mysql

# Rebuild container
docker compose down -v
docker compose up --build
```

### Frontend Not Loading

```bash
# Clear Angular cache
rm -rf frontend/node_modules/.angular

# Reinstall dependencies
cd frontend && npm install && npm start
```

### WebSocket Connection Failing

1. Verify backend is running: `http://localhost:8081/swagger-ui.html`
2. Check browser console for errors
3. Ensure port 8081 is not blocked by firewall

### MQTT Not Connected

```bash
# Check Mosquitto logs
docker logs smart-rh-mqtt

# Test MQTT connection
docker run -it --rm eclipse-mosquitto:2.0 mosquitto_sub -h host.docker.internal -t "#"
```

### Database Connection Issues

```bash
# Verify MySQL is healthy
docker exec smart-rh-mysql mysql -u root -pmysql123 -e "SELECT 1"

# Check migrations ran
docker exec smart-rh-mysql mysql -u root -pmysql123 smartrh -e "SHOW TABLES;"
```

---

## 📝 Configuration

### Backend Configuration (`application.yml`)

- JWT secret & expiration
- Database connection pooling
- MQTT broker settings
- WebSocket STOMP configuration
- Logging levels

### Frontend Configuration (`environment.ts`)

- API base URL
- WebSocket URL
- Feature flags
- Dev/prod settings

### Docker Compose Overrides

Edit `docker-compose.yml` to:

- Change port mappings
- Adjust memory limits
- Add environment variables
- Mount local volumes

---

## 🎯 Next Steps

### Immediate (Week 2)

1. **Photo Evidence Modal** - View Base64 attendance photos with HR annotations
2. **Module 19: BI & Reporting** - Attendance trends, fraud metrics, export reports
3. **MQTT Device Simulation** - Test with actual device publishing

### Week 3+

1. **Module 17: Employee Self-Service** - View own attendance, download reports
2. **Security Hardening** - Rate limiting, input validation, GDPR compliance
3. **Performance Testing** - Load testing with 50-200+ devices

---

## 📚 Documentation

- **`START_HERE.md`** - Quick setup instructions
- **`docs/CHAPITRE_1_CADRE_DU_PROJET.md`** - Project context & objectives (French)
- **`docs/DIAGRAMMES_SEQUENCES.md`** - Architecture & sequence diagrams
- **`docs/SMART_RH_BACKEND_MASTER_PROMPT.md`** - Backend development guide
- **`docs/SMART_RH_FRONTEND_MASTER_PROMPT.md`** - Frontend development guide

---

## 📞 Support & Contribution

For issues or questions:

1. Check logs: `docker logs smart-rh-backend`
2. Review Swagger docs: `http://localhost:8081/swagger-ui.html`
3. Check frontend console (F12)

---

**Last Updated:** 2026-04-20 | **Version:** 1.0.0-BETA | **Author:** Aymen Zid — github.com/zid7320
