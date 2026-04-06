# SMART RH 4.0

Full-stack HR management system: Angular + Spring Boot + MySQL.

## Quick Start

```powershell
# Terminal 1
docker-compose up --build

# Terminal 2 (after 30s)
cd frontend && npm install && npm start
```

**Access:** http://localhost:4200 | **Login:** admin@smart-rh.com / admin123

## Architecture

- **Frontend:** Angular 21+ (http://localhost:4200)
- **Backend:** Spring Boot 3.2 (http://localhost:8081)
- **Database:** MySQL 8.0 (localhost:3307)
- **MQTT:** Eclipse Mosquitto (localhost:1883)

## Files

- `START_HERE.md` - Setup instructions (start here)
- `FRONTEND_GUIDE.md` - Angular module details
- `FRONTEND_FULLSTACK.md` - Full system architecture
- `INDEX_DOCUMENTATION.md` - Documentation index
- `launch-fullstack.ps1` - One-command launcher
- `docker-compose.yml` - Docker services

## Commands

```powershell
# Start everything
docker-compose up --build

# Stop everything
docker-compose down

# View backend logs
docker logs smart-rh-backend

# Check health
curl http://localhost:8081/api/health
```

That's it. Start developing.
