# Documentation Index

## Essential Files

| File | Purpose | Read Time |
|------|---------|-----------|
| **README.md** | Project overview | 2 min |
| **START_HERE.md** | Setup instructions | 3 min |
| **QUICK_START_FULLSTACK.md** | Quick reference (2 commands) | 1 min |
| **FRONTEND_FULLSTACK.md** | Complete architecture & workflow | 20 min |
| **FRONTEND_GUIDE.md** | Angular module details | 15 min |

## Quick Navigation

### Just Start (5 min)
1. Read: `QUICK_START_FULLSTACK.md`
2. Run: `.\launch-fullstack.ps1`
3. Open: http://localhost:4200

### Understand Architecture (30 min)
1. Read: `START_HERE.md`
2. Read: `FRONTEND_FULLSTACK.md`
3. Read: `FRONTEND_GUIDE.md`
4. Run: `.\launch-fullstack.ps1`

### Development Setup
1. Read: `FRONTEND_GUIDE.md` (Angular modules)
2. Read: `FRONTEND_FULLSTACK.md` (Backend communication)
3. Start: backend with `docker-compose up --build`
4. Start: frontend with `cd frontend && npm start`

## Infrastructure Files

- **docker-compose.yml** - 3 services (MySQL, Backend, MQTT)
- **Dockerfile** - Spring Boot image build
- **pom.xml** - Maven dependencies
- **launch-fullstack.ps1** - One-command launcher
- **docs/** - Additional documentation

## Services

| Service | URL | Port |
|---------|-----|------|
| Frontend | http://localhost:4200 | 4200 |
| Backend API | http://localhost:8081 | 8081 |
| Swagger UI | http://localhost:8081/swagger-ui.html | 8081 |
| Database | localhost | 3307 |
| MQTT | localhost | 1883 |

## Login Credentials

- **Email:** admin@smart-rh.com
- **Password:** admin123

Done. Start with README.md or START_HERE.md.

### **Chemin 3: "Je vais développer" (Développeur)**
1. Lire: `FRONTEND_FULLSTACK.md` (20 min) - Vue d'ensemble
2. Lire: `FRONTEND_GUIDE.md` (15 min) - Setup Angular
3. Pour le backend: `DOCKER_QUICKSTART.md` (15 min)
4. Installer dépendances:
   ```powershell
   cd frontend
   npm install    # 3-5 min
   ```
5. Lancer: `.\launch-fullstack.ps1` (3 min)
6. Explorer le code: `frontend/src/app/`
7. Faire des modifications et voir les changements en temps réel

**Temps total: 60 minutes** ⏱️

---

### **Chemin 4: "Je veux maîtriser DevOps" (DevOps Engineer)**
1. Lire: `DOCKER_QUICKSTART.md` (20 min)
2. Lire: `DOCKER_COMMANDS_REFERENCE.md` (20 min)
3. Consulter: `FRONTEND_FULLSTACK.md` - Section "Production Deployment" (10 min)
4. Exécuter les commandes Docker progressivement:
   ```powershell
   docker-compose build
   docker-compose up
   docker ps
   docker-compose logs -f
   docker-compose exec smart-rh-backend sh
   ```

**Temps total: 90 minutes** ⏱️

---

## 🎓 Concepts Clés

### **1. Architecture Générale**
```
Client (Angular sur 4200)
    ↓ HTTP + WebSocket
API Backend (Spring Boot sur 8081)
    ↓ JDBC
Database MySQL (3307)
    +
Message Broker MQTT (1883)
```

### **2. Authentification**
- **Protocole**: JWT (JSON Web Token)
- **Flow**: Email + Password → JWT Token → Storage → Authorization Header
- **Endpoints**: `/api/auth/login`, `/api/auth/refresh`

### **3. Communication Temps Réel**
- **Protocole**: WebSocket (STOMP over SockJS)
- **Endpoint**: `ws://localhost:8081/ws`
- **Topics**: `/user/queue/notifications`

### **4. Deploy Options**
- **Développement**: `docker-compose up` + `npm start`
- **Production**: Docker containers + CDN pour frontend

---

## 🆘 FAQ Rapide

### Q: Où trouver les identifiants de test?
→ A: Dans `BACKEND_VERIFICATION_REPORT.md` ou utilisez admin@smart-rh.com

### Q: Comment voir tous les endpoints?
→ A: http://localhost:8081/swagger-ui.html (doit avoir backend lancé)

### Q: Comment modifier le code Angular et voir les changements?
→ A: `npm start` a le live-reload activé. Sauvegardez, le navigateur se recharge automatiquement.

### Q: Comment voir les logs du backend?
→ A: `docker-compose logs -f smart-rh-backend`

### Q: Comment arrêter tout?
→ A: `docker-compose down` + Fermer la fenêtre npm

### Q: Quel navigateur utiliser?
→ A: Chrome, Firefox ou Edge récent. Pas d'IE.

### Q: Port 4200 déjà utilisé?
→ A: `ng serve --port 4201` ou tuer le processus qui utilise 4200

---

## 📞 Support & Ressources Externes

### **Documentations Officielles**
- Angular: https://angular.io/docs
- Spring Boot: https://spring.io/projects/spring-boot
- Docker: https://docs.docker.com/
- MySQL: https://dev.mysql.com/doc/
- TypeScript: https://www.typescriptlang.org/docs/

### **Tutoriels Utiles**
- JWT Auth: https://jwt.io/introduction
- WebSocket with Spring: https://spring.io/guides/gs/messaging-stomp-websocket/
- Angular & REST: https://angular.io/guide/http

### **Outils Pratiques**
- VS Code: https://code.visualstudio.com
- Postman: https://www.postman.com (tester les APIs)
- DBeaver: https://dbeaver.io (gérer MySQL)

---

## ✅ Vérification d'Installation

Exécutez cet script pour vérifier que tout est prêt:

```powershell
# PowerShell

# Check Node.js
node --version          # Doit afficher v20+
npm --version          # Doit afficher v11+

# Check Docker
docker --version       # Doit afficher docker version
docker-compose --version  # Doit afficher docker-compose version

# Check directories exist
Test-Path "C:\Users\MSI\Desktop\nouveau !!!\frontend"     # $True
Test-Path "C:\Users\MSI\Desktop\nouveau !!!\docker-compose.yml"  # $True

Write-Host "✅ All checks passed!" -ForegroundColor Green
```

---

## 📈 Prochaines Étapes

Une fois l'application en cours d'exécution:

1. **Explorez l'interface**: Dashboard, Employees, Leaves, etc.
2. **Testez les APIs**: http://localhost:8081/swagger-ui.html
3. **Lire le code source**: `frontend/src/app/`
4. **Modifiez et testez**: Tout changement se verra en temps réel
5. **Consultez les diagrammes**: `DIAGRAMMES_SEQUENCES_VIEWER.html`

---

## 🎉 Conclusion

**Tout ce dont vous avez besoin est documenté ici.**

- ✅ Démarrage: `QUICK_START_FULLSTACK.md`
- ✅ Compréhension: `FRONTEND_FULLSTACK.md`
- ✅ Développement: `FRONTEND_GUIDE.md` + `DOCKER_QUICKSTART.md`
- ✅ Référence: `DOCKER_COMMANDS_REFERENCE.md`
- ✅ Visualisation: `DIAGRAMMES_SEQUENCES_VIEWER.html`

**Happy coding! 🚀**

---

**Index Document** | Version. 1.0 | SMART RH 4.0 Project | Avril 2026
