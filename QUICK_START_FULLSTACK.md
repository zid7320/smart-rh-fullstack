# ⚡ Quick Start (2 Commands)

## Terminal 1: Backend
```powershell
docker-compose up --build
```

## Terminal 2: Frontend (after 30 seconds)
```powershell
cd frontend && npm install && npm start
```

## Access
- **URL:** http://localhost:4200
- **Login:** admin@smart-rh.com / admin123

## Services
| Service | URL |
|---------|-----|
| Frontend | http://localhost:4200 |
| Backend | http://localhost:8081 |
| Swagger API | http://localhost:8081/swagger-ui.html |
| Health Check | http://localhost:8081/api/health |

## Issues?

**Backend not responding:**
```powershell
docker logs smart-rh-backend
```

**Port in use:**
```powershell
netstat -ano | findstr :8081
```

**Reset everything:**
```powershell
docker-compose down -v
docker-compose up --build
```

Done. See START_HERE.md or README.md for more.
```

**Besoin d'aide?** → Consultez `FRONTEND_FULLSTACK.md` pour le guide complet.

---

**Version:** 1.0 | **Projet:** SMART RH 4.0
