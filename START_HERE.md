# START HERE

## Prerequisites
- Docker & Docker Compose
- Node.js v20+
- npm v11+

## Run Application

### Terminal 1: Backend + Database
```powershell
docker-compose up --build
```

### Terminal 2: Frontend (after backend is ready)
```powershell
cd frontend
npm install && npm start
```

## Access Application

- **URL:** http://localhost:4200
- **Email:** admin@smart-rh.com
- **Password:** admin123

## Services

| Service | URL | Port |
|---------|-----|------|
| Frontend | localhost:4200 | 4200 |
| Backend | localhost:8081 | 8081 |
| Swagger | localhost:8081/swagger-ui.html | 8081 |
| Database | localhost | 3307 |
| MQTT | localhost | 1883 |

## Troubleshooting

**Backend issues:**
```powershell
docker logs smart-rh-backend
```

**Reset:**
```powershell
docker-compose down -v
docker-compose up --build
```

See README.md for full documentation.
MySQL:     localhost:3307 (Docker)
MQTT:      localhost:1883 (Docker)
```

---

## 📊 System Components

| Component | Technology | How to Run | Port |
|-----------|-----------|-----------|------|
| Frontend | Angular 21+ | `npm start` | 4200 |
| Backend | Spring Boot 3.2 | `docker-compose` | 8081 |
| Database | MySQL 8.0 | `docker-compose` | 3307 |
| MQTT | Eclipse Mosquitto | `docker-compose` | 1883 |

---

## 🎮 Available Commands

### Start Everything
```powershell
# Option 1: Start with interactive menu
.\launch-fullstack.ps1

# Option 2: Manual (2 terminals)
# Terminal 1:
docker-compose up --build

# Terminal 2:
cd frontend
npm start
```

### Just Backend
```powershell
docker-compose up --build
```

### Just Frontend
```powershell
cd frontend
npm start
```

### Stop Everything
```powershell
docker-compose down
# Close npm terminal (Ctrl+C)
```

### Reset & Clean Start
```powershell
docker-compose down -v   # Remove volumes
docker-compose up --build # Fresh start
```

---

## 🧪 Verify Everything Works

```powershell
# Check backend health
curl http://localhost:8081/api/health

# Check Swagger UI
# Open: http://localhost:8081/swagger-ui.html

# Check frontend
# Open: http://localhost:4200
```

---

## 📖 Documentation Guide

| Need | Read This |
|------|-----------|
| Just start the app | GETTING_STARTED.md |
| 30-second start | QUICK_START_FULLSTACK.md |
| Understand architecture | FRONTEND_FULLSTACK.md |
| Frontend setup details | FRONTEND_GUIDE.md |
| Docker commands | DOCKER_COMMANDS_REFERENCE.md |
| Why separate frontend? | WHY_FRONTEND_SEPARATE.md |
| See all docs | INDEX_DOCUMENTATION.md |

---

## 🎯 Next Steps

1. **Start Backend:**
   ```powershell
   docker-compose up --build
   ```

2. **Start Frontend (new terminal):**
   ```powershell
   cd frontend
   npm start
   ```

3. **Access Application:**
   ```
   http://localhost:4200
   ```

4. **Login:**
   ```
   Email: admin@smart-rh.com
   Password: admin123
   ```

5. **Start Developing!**

---

## ✅ Checklist

```
✅ Docker containers cleaned
✅ Old files removed  
✅ Essential files kept
✅ Backend ready
✅ Database ready
✅ Frontend ready
✅ Documentation clean
✅ Project ready to run
```

---

## 🚀 You're All Set!

This is a **clean, minimal, production-ready** project setup.

```
Just run:
  docker-compose up --build
  
Then in another terminal:
  cd frontend && npm start

That's it! 🎉
```

---

**Version:** Clean | **Status:** Ready | **Date:** April 6, 2026
