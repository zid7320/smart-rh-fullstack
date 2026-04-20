# START HERE - Quick Setup

## Prerequisites

- **Docker & Docker Compose**
- **Node.js v20+** and npm v11+

## Run in 2 Minutes

**Terminal 1** (Backend + Database):

```bash
docker compose up --build
```

Wait for: `✅ Container smart-rh-backend Up`

**Terminal 2** (Frontend, after Terminal 1 is healthy):

```bash
cd frontend
npm install
npm start
```

## Access

| Service      | URL                                   |
| ------------ | ------------------------------------- |
| **App**      | http://localhost:4200                 |
| **Login**    | admin@smart-rh.com / admin123         |
| **API Docs** | http://localhost:8081/swagger-ui.html |

## Next Steps

1. ✅ Backend loads → Check `http://localhost:8081/swagger-ui.html`
2. ✅ Frontend starts → Navigate to `http://localhost:4200`
3. ✅ Login with provided credentials
4. ✅ Explore Attendance Dashboard (Module 10)

## Troubleshooting

**Backend won't start?**

```bash
docker logs smart-rh-backend
docker compose down -v && docker compose up --build
```

**Frontend not loading?**

```bash
cd frontend && rm -rf node_modules/.angular && npm install && npm start
```

---

**For full documentation, see [README.md](./README.md)**
