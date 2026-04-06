# 🚀 SMART RH 4.0 - Guide Full-Stack (Backend + Frontend)

## 🎯 Vue d'Ensemble

```
SMART RH 4.0 Architecture
═══════════════════════════════════════════════════════════

┌─────────────────────────────────────┐
│      🌐 Frontend (Angular)          │
│      http://localhost:4200          │
│  • Port: 4200                       │
│  • Framework: Angular 21+           │
│  • UI: Angular Material             │
│  • WebSocket: STOMP/SockJS          │
└─────────────┬───────────────────────┘
              │ HTTP + WebSocket
              ↓
┌─────────────────────────────────────┐
│    🔗 API Backend (Spring Boot)     │
│     http://localhost:8081           │
│  • Port: 8081                       │
│  • Framework: Spring Boot 3.2       │
│  • Database: MySQL                  │
│  • Message Broker: MQTT             │
│  • Auth: JWT (JSON Web Token)       │
└─────────────┬───────────────────────┘
              │ JDBC
              ↓
┌─────────────────────────────────────┐
│  📦 Database (MySQL 8.0)            │
│    localhost:3307                   │
│  • Database: smart_rh_db            │
│  • User: smart_rh                   │
█──────────────────────────────────────┘
              + 
        MQTT Broker (1883)
        Redis Cache (optionnel)
```

---

## 📋 Prérequis Complets

### **Pour le Backend (Docker):**
- ✅ Docker Desktop v29+
- ✅ Docker Compose v5+
- ✅ Port 8081 disponible (API)
- ✅ Port 3307 disponible (MySQL)
- ✅ Port 1883 disponible (MQTT)

### **Pour le Frontend:**
- ✅ Node.js v20+ (vérifie: `node --version`)
- ✅ npm v11+ (vérifie: `npm --version`)
- ✅ Port 4200 disponible (Dev Server)
- ✅ Backend en cours d'exécution

---

## 🚀 Lancement Complet (2 Terminaux)

### **Terminal 1: Lancer le Backend avec Docker**

```powershell
# Ouvrir PowerShell
cd "C:\Users\MSI\Desktop\nouveau !!!"

# Option A: Lanceur automatique (Recommandé)
.\START_DOCKER.bat

# Option B: Ligne de commande
docker-compose up --build

# Option C: Script interactif
.\launch-docker.ps1
```

**Attendez jusqu'à voir:**
```
smart-rh-backend   | Started Application in X.XXX seconds
smart-rh-mysql     | [System] ready for connections
```

---

### **Terminal 2: Lancer le Frontend Angular**

Attendez que le backend soit prêt, puis:

```powershell
# Ouvrir une NOUVELLE fenêtre PowerShell

# Vérifier les versions
node --version        # Doit afficher v20+
npm --version         # Doit afficher v11+

# Naviguer au dossier frontend
cd "C:\Users\MSI\Desktop\nouveau !!!\frontend"

# Option A: Lanceur automatique (Recommandé)
..\launch-frontend.ps1   # Choisir option 3

# Option B: Ligne de commande
npm install
npm start

# Option C: À la main
ng serve
```

**Attendez jusqu'à voir:**
```
Application bundle generation complete. [X.XXX seconds]
server is listening on http://localhost:4200
```

---

## 🌐 Accès Complet à l'Application

Une fois **les deux services lancés**, ouvrez dans votre navigateur:

| Composant | URL | Description |
|-----------|-----|-------------|
| **Application Frontend** | http://localhost:4200 | Interface utilisateur Angular |
| **Backend API** | http://localhost:8081 | API REST Spring Boot |
| **Documentation API** | http://localhost:8081/swagger-ui.html | 📚 Tous les endpoints |
| **API Health** | http://localhost:8081/api/health | ✅ Vérifier santé API |
| **Base de Données** | localhost:3307 | MySQL (client externe) |

---

## ⚡ Workflow Rapide (5 minutes)

```bash
# == TERMINAL 1 == (Backend)
cd "C:\Users\MSI\Desktop\nouveau !!!"
docker-compose up --build
# Attendre "Application started"

# == TERMINAL 2 == (Frontend)
# ⏳ Attendre 30 secondes que le backend soit prêt
cd "C:\Users\MSI\Desktop\nouveau !!!\frontend"
npm install    # D'abord
npm start      # Démarre sur 4200
# Attendre "server is listening"

# == NAVIGATEUR ==
# Ouvrir http://localhost:4200
# Vous devriez voir la page de login
```

---

## 🔐 Authentification

### **Flux de Connexion Complet**

```
1️⃣  Frontend             User clique "Login"
         ↓
2️⃣  Angular Component    Recueille email + password
         ↓
3️⃣  HTTP Request         POST http://localhost:8081/api/auth/login
                         Body: { email, password }
         ↓
4️⃣  Backend API          Valide les identifiants en BD
         ↓
5️⃣  Response            Retourne JWT Token (JSON Web Token)
         ↓
6️⃣  Frontend localStorage Stocke le token
         ↓
7️⃣  Dashboard            Redirect vers dashboard
         ↓
8️⃣  Futures Requêtes     Inclure le token:
                         Header: Authorization: Bearer <token>
         ↓
9️⃣  Backend Guard        Vérifie la validité du token
         ↓
🔟  Réponse             Donnée retournée si valide
```

### **Identifiants de Test**

Consultez `BACKEND_VERIFICATION_REPORT.md` pour les vrais comptes.

Utilisateurs typiques:
```
Administrateur:
  Email: admin@smart-rh.com
  Password: admin123

Employé:
  Email: user@example.com
  Password: password123

Manager:
  Email: manager@smart-rh.com
  Password: manager123
```

---

## 🔄 Communication En Temps Réel (WebSocket)

### **Notifications WebSocket**

Le frontend se connecte automatiquement à:
```
ws://localhost:8081/ws
```

**Événements temps réel:**
- ✅ Congé approuvé
- ✅ Bulletin de paie généré
- ✅ Nouvelle candidature reçue
- ✅ Évaluation soumise

### **Exemple de Code (Angular)**

```typescript
// Service WebSocket
constructor(private stompService: StompService) {}

connect() {
  this.stompService.connect('ws://localhost:8081/ws', () => {
    // Subscribe à notificationstopics
    this.stompService.subscribe('/user/queue/notifications', (msg) => {
      console.log('Notification reçue:', msg);
      // Afficher toast ou alerte
    });
  });
}
```

---

## 🐛 Dépannage Full-Stack

### **Problème: Frontend affiche "Cannot find module"**

```powershell
# Solution
cd frontend
npm install --force
npm start
```

### **Problème: "Cannot connect to localhost:8081"**

```powershell
# Vérifier que le backend tourne
docker ps -a

# Si pas en cours:
docker-compose up --build

# Si une erreur appears:
docker-compose logs smart-rh-backend
```

### **Problème: Port 4200 ou 8081 déjà utilisé**

```powershell
# Voir quelle application utilise le port
netstat -ano | findstr :4200
netstat -ano | findstr :8081

# Soit arrêter l'autre app, soit utiliser un port différent:
ng serve --port 4201
```

### **Problème: l'application affiche "Connexion au serveur échouée"**

1. Vérifier le backend est bien lancé: http://localhost:8081/api/health
2. Vérifier la console du navigateur (F12) pour les erreurs
3. Vérifier les logs: `docker-compose logs -f smart-rh-backend`

---

## 📁 Structure Complète du Projet

```
c:\Users\MSI\Desktop\nouveau !!!
│
├── 🐳 DOCKER (Backend)
│   ├── docker-compose.yml           Configuration Docker
│   ├── Dockerfile                   Image Spring Boot
│   ├── START_DOCKER.bat             Lanceur simple
│   ├── launch-docker.ps1            Lanceur interactif
│   └── src/main/java/com/smart/     Code Java source
│
├── 🌐 FRONTEND (Angular)
│   ├── frontend/
│   │   ├── package.json             Dépendances npm
│   │   ├── angular.json             Config Angular
│   │   ├── src/
│   │   │   ├── app/                 Code source Angular
│   │   │   │   ├── core/            Services, guards
│   │   │   │   ├── features/        Modules métier
│   │   │   │   └── shared/          Composants partagés
│   │   │   └── assets/              Images, etc
│   │   ├── launch-frontend.ps1      Lanceur interactif
│   │   └── README.md                Documentation
│
├── 📚 DOCUMENTATION
│   ├── DOCKER_QUICKSTART.md         Guide backend
│   ├── FRONTEND_GUIDE.md            Guide frontend
│   ├── FRONTEND_FULLSTACK.md        ← Ce fichier
│   ├── DOCKER_COMMANDS_REFERENCE.md Commandes Docker
│   ├── README_DOCKER.md             Vue d'ensemble
│   ├── QUICK_START.md               Démarrage rapide
│   ├── INDEX_DOCKER.txt             Navigation
│   └── docs/DIAGRAMMES_SEQUENCES_VIEWER.html  📊 Diagrammes visuels
│
├── 📋 CONFIGURATION
│   ├── pom.xml                      Dépendances Maven
│   └── infra/                       Configuration infrastructure
│
└── 📊 DONNÉES
    ├── target/                      Build Maven
    └── docker/                      Images Docker
```

---

## 🎯 Cas d'Usage Typiques

### **Cas 1: Développement Complet**

```powershell
# Terminal 1: Backend
docker-compose up --build

# Terminal 2: Frontend
cd frontend && npm start

# Browser
http://localhost:4200

# Modifications au code:
# - Frontend (Angular): Rechargement automatique
# - Backend (Java): Redémarrage manual ou avec DevTools
```

---

### **Cas 2: Tester une Nouvelle Fonctionnalité Backend**

```powershell
# 1. Modifier le code Java
# 2. Redémarrer le backend
docker-compose restart smart-rh-backend

# 3. Vérifier le nouvel endpoint
curl http://localhost:8081/api/new-endpoint

# 4. Tester depuis Swagger
http://localhost:8081/swagger-ui.html

# 5. Adapter le frontend si besoin
# Modifier Angular, npm start recharge automatiquement
```

---

### **Cas 3: Déploiement Production**

```powershell
# Backend
npm run build
docker build -t smart-rh-backend .
docker push your-registry/smart-rh-backend

# Frontend
cd frontend
npm run build
# Copier dist/ vers CDN ou serveur web

# Configuration
# - Adapter URL API dans environment.prod.ts
# - Configurer CORS si domaines différents
```

---

## 📊 Performance & Best Practices

### **Frontend Performance**

```typescript
// ✅ BON - Chargement lazy des modules
const routes: Routes = [
  { 
    path: 'employees',
    loadChildren: () => import('./features/employees/employees.module')
      .then(m => m.EmployeesModule)
  }
];

// ❌ MAUVAIS - Charger tout au démarrage
import { EmployeesModule } from './features/employees/employees.module';
```

### **Backend Performance**

```java
// ✅ BON - Cache les données
@Cacheable("employees")
public List<Employee> getAllEmployees() {
  return employeeRepository.findAll();
}

// ✅ BON - Pagination pour grandes listes
Page<Employee> employees = employeeRepository.findAll(
  PageRequest.of(0, 20)
);
```

---

## 📞 Support & Ressources

### **Documentation**

- **Angular:** https://angular.io/docs
- **Spring Boot:** https://spring.io/projects/spring-boot
- **Docker:** https://docs.docker.com
- **MySQL:** https://dev.mysql.com/doc/
- **TypeScript:** https://www.typescriptlang.org/docs/

### **VS Code Extensions Recommandées**

- Angular Language Service
- Angular Schematics
- ES7+ React/Redux/React-Native snippets
- Spring Boot Extension Pack
- Docker
- MySQL

---

## ✅ Checklist Complète de Déploiement

```
BACKEND:
☑ Docker Desktop installé et en cours d'exécution
☑ docker-compose.yml correct
☑ MySQL healthy (3307)
☑ Spring Boot started (8081)
☑ API répond: http://localhost:8081/api/health
☑ Swagger UI accessible: http://localhost:8081/swagger-ui.html

FRONTEND:
☑ Node.js v20+ installé
☑ npm v11+ installé
☑ node_modules installé (npm install)
☑ À ng serve exécuté
☑ Application charge: http://localhost:4200
☑ Page de login affichée
☑ Authentification fonctionne

INTÉGRATION:
☑ Frontend se connecte à backend
☑ WebSocket fonctionne (notifications RTC)
☑ Tout fonctionne ensemble
☑ Aucune erreur dans la console
```

---

## 🎉 Résumé Final

**Pour lancer votre application SMART RH 4.0 complète:**

```powershell
### Terminal 1: Backend ###
cd "C:\Users\MSI\Desktop\nouveau !!!"
docker-compose up --build
# Attender "Application started"

### Terminal 2: Frontend ###
cd "C:\Users\MSI\Desktop\nouveau !!!\frontend"
npm install
npm start
# Attendre "server is listening"

### Navigateur ###
http://localhost:4200
# Connexion avec vos identifiants
# ✅ Vous êtes sur!
```

**L'application full-stack SMART RH 4.0 est maintenant opérationelle!** 🚀

---

**Version:** 1.0 | **Date:** Avril 2026 | **Projet:** SMART RH 4.0
