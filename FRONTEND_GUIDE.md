# 🌐 SMART RH 4.0 - Guide du Frontend (Angular)

## 📋 Vue d'ensemble

Le frontend SMART RH 4.0 est construire avec:
- **Framework:** Angular 21+
- **UI Components:** Angular Material
- **Communication Temps Réel:** WebSocket (STOMP/SockJS)
- **Langage:** TypeScript
- **Gestionnaire Packages:** npm

---

## 🚀 Démarrage Rapide du Frontend

### **Prérequis**

Assurez-vous que vous avez:
- ✅ **Node.js 20+** installé (vérifie avec `node --version`)
- ✅ **npm 11+** installé (vérifie avec `npm --version`)
- ✅ **Backend en cours d'exécution** sur http://localhost:8081

Télécharger Node.js: https://nodejs.org/

### **Étape 1: Vérifier Node.js et npm**

```powershell
node --version
npm --version
```

Résultat attendu:
```
v20.x.x
11.6.x
```

### **Étape 2: Naviguer vers le dossier Frontend**

```powershell
cd "C:\Users\MSI\Desktop\nouveau !!!\frontend"
```

### **Étape 3: Installer les Dépendances**

```powershell
npm install
```

Cela va télécharger et installer toutes les dépendances Angular (~5-10 minutes la première fois)

### **Étape 4: Lancer le Serveur de Développement**

```powershell
npm start
```

ou

```powershell
ng serve
```

Cela va:
- ✅ Compiler le code Angular
- ✅ Lancer un serveur local sur http://localhost:4200
- ✅ Activer le "hot reload" (rechargement automatique quand vous modifiez le code)

### **Étape 5: Ouvrir l'Application**

Ouvrez votre navigateur et allez à:
```
http://localhost:4200
```

---

## 🌐 Accès à l'Application

### **Ensemble des URLs**

| Service | URL | Port | Statut |
|---------|-----|------|--------|
| **Frontend Angular** | http://localhost:4200 | 4200 | Dev Server |
| **Backend API** | http://localhost:8081 | 8081 | Spring Boot |
| **API Documentation** | http://localhost:8081/swagger-ui.html | 8081 | Swagger |
| **Base de Données** | localhost:3307 | 3307 | MySQL |

---

## 📦 Installation Complète (Étape par Étape)

Si c'est votre première fois ou si npm install a échoué:

```powershell
# 1. Aller au dossier frontend
cd "C:\Users\MSI\Desktop\nouveau !!!\frontend"

# 2. Supprimer les dépendances anciennes (si problème)
Remove-Item node_modules -Force -Recurse -ErrorAction SilentlyContinue
Remove-Item package-lock.json -Force -ErrorAction SilentlyContinue

# 3. Réinstaller
npm install

# 4. Démarrer
npm start

# 5. Ouvrir le navigateur
start http://localhost:4200
```

---

## 👤 Connexion à l'Application

### **Identifiants par Défaut** (si disponibles)

```
Email:    user@example.com
Password: password123
```

ou

```
Email:    admin@smart-rh.com
Password: admin123
```

> Vérifiez `BACKEND_VERIFICATION_REPORT.md` pour les vrais identifiants

---

## 🛠️ Commandes npm Utiles

```powershell
# Démarrer le serveur de développement
npm start

# Construire pour la production
npm build

# Construire avec watch (auto-rechargement)
npm run watch

# Lancer les tests unitaires
npm test

# Voir version Angular
ng version

# Générer un composant
ng generate component nom-composant

# Générer un service
ng generate service nom-service
```

---

## 🔧 Configuration API

Le frontend se connecte au backend via un **proxy**.

### **Fichier de Configuration**

Vérifiez: `frontend/proxy.conf.json` ou `frontend/angular.json`

Les requêtes API sont automatiquement redirigées vers:
```
http://localhost:8081/api
```

---

## 📡 Communication Temps Réel (WebSocket)

Le frontend utilise **WebSocket** (STOMP) pour les notifications en temps réel:

- ✅ Notifications de congés approuvés
- ✅ Bulletins de paie générés
- ✅ Nouvelles candidatures reçues
- ✅ Évaluations soumises
- ✅ Alertes de pointage

La connexion WebSocket se fait automatiquement à:
```
ws://localhost:8081/ws
```

---

## 🏗️ Structure du Projet Frontend

```
frontend/
├── src/
│   ├── index.html                 # Point d'entrée HTML
│   ├── main.ts                    # Point d'entrée Angular
│   ├── styles.scss                # Styles globaux
│   ├── app/
│   │   ├── app.component.ts       # Composant racine
│   │   ├── app.routes.ts          # Routes
│   │   ├── core/
│   │   │   ├── api/               # Services API
│   │   │   ├── interceptors/      # HTTP interceptors
│   │   │   ├── services/          # Services
│   │   │   ├── guards/            # Route guards
│   │   │   └── models/            # Modèles TypeScript
│   │   ├── features/              # Modules métier
│   │   │   ├── auth/              # Authentification
│   │   │   ├── employees/         # Gestion employés
│   │   │   ├── attendance/        # Pointage
│   │   │   ├── leaves/            # Congés
│   │   │   ├── payroll/           # Paie
│   │   │   ├── recruitments/      # Recrutement
│   │   │   ├── evaluations/       # Évaluations
│   │   │   └── dashboard/         # Tableau de bord
│   │   ├── shared/                # Composants partagés
│   │   │   ├── components/        # Composants réutilisables
│   │   │   ├── pipes/             # Pipes personnalisés
│   │   └── layout/                # Layout principal
│   └── environments/              # Configuration par environnement
│       ├── environment.ts         # Développement
│       └── environment.prod.ts    # Production
├── package.json                   # Dépendances npm
├── angular.json                   # Configuration Angular
├── tsconfig.json                  # Configuration TypeScript
└── README.md                      # Documentation
```

---

## 🚨 Dépannage Frontend

### **Erreur: "npm: command not found"**

**Solution:** Node.js n'est pas installé
```powershell
# Télécharger et installer Node.js
# https://nodejs.org/

# Vérifier l'installation
node --version
npm --version
```

### **Erreur: "Cannot find module '@angular/core'"**

**Solution:** Les dépendances ne sont pas installées
```powershell
cd frontend
npm install
```

### **Port 4200 déjà utilisé**

**Solution:** Utilisez un port différent
```powershell
ng serve --port 4201
# puis ouvrez http://localhost:4201
```

### **Le frontend ne peut pas communiquer avec le backend**

**Vérifiez:**
1. Backend est en cours d'exécution: `http://localhost:8081/api/health`
2. Le proxy est bien configuré dans `angular.json`
3. Allez à l'onglet "Network" dans les DevTools du navigateur

### **Le style ne charge pas correctement**

**Solution:** Videz le cache et rechargez
```
Ctrl + Shift + R  (rechargement dur)
```

---

## 🎯 Workflow Typique de Développement

```powershell
# Terminal 1: Backend
cd "C:\Users\MSI\Desktop\nouveau !!!"
docker-compose up --build

# Terminal 2: Frontend (une fois le backend prêt)
cd "C:\Users\MSI\Desktop\nouveau !!!\frontend"
npm install          # Première fois uniquement
npm start            # Démarre sur http://localhost:4200
```

**Résultat:**
- Backend API: http://localhost:8081
- Documentation API: http://localhost:8081/swagger-ui.html
- Frontend: http://localhost:4200
- Base de données: localhost:3307

---

## 🔐 Authentification

### **Flux de Login**

1. L'utilisateur saisit ses identifiants dans le login
2. Le frontend envoie: `POST /api/auth/login`
3. Le backend retourne un **JWT Token**
4. Le frontend stocke le token en localStorage
5. Toutes les requêtes futures incluent le token dans l'header:
   ```
   Authorization: Bearer <jwt_token>
   ```

### **Déconnexion**

- Le token est supprimé de localStorage
- L'utilisateur est redirigé vers la page de login

---

## 📊 Modules Principaux du Frontend

### **1. Authentication Module**
- Login/Logout
- Gestion des tokens JWT
- Route guards

### **2. Employee Module**
- Gestion des employés
- Profils avec photo
- Historique des employés

### **3. Attendance Module**
- Pointage facial
- Historique présence
- Alertes retard/absence

### **4. Leave Module**
- Demandes de congé
- Workflow d'approbation
- Notifications en temps réel

### **5. Payroll Module**
- Consultation des bulletins
- Historique paie
- Téléchargement PDF

### **6. Dashboard Module**
- Vue d'ensemble des données
- Graphiques et statistiques
- Notifications temps réel

---

## 🌍 Variables d'Environnement Frontend

Créez un fichier `.env` dans le dossier `frontend/`:

```env
# API Configuration
NG_APP_API_URL=http://localhost:8081
NG_APP_API_TIMEOUT=30000

# WebSocket
NG_APP_WS_URL=ws://localhost:8081/ws

# Environment
NG_APP_ENVIRONMENT=development
NG_APP_DEBUG=true
```

Accédez-les dans le code:
```typescript
import { environment } from './environments/environment';
const apiUrl = environment.apiUrl; // http://localhost:8081
```

---

## 🏁 Build pour la Production

### **Compiler pour Production**

```powershell
cd frontend
npm run build
```

Cela crée un dossier `dist/` avec:
- Code optimisé et minifié
- Assets compressés
- Source maps (pour debugging)

### **Taille du Build**

Vérifiez la taille:
```powershell
Get-ChildItem dist/ -Recurse | Measure-Object -Property Length -Sum
```

---

## 📚 Documentation Angular

- **Guide Officiel:** https://angular.io/guide/setup-local
- **Material Design:** https://material.angular.io
- **CLI:** https://angular.io/cli
- **Routing:** https://angular.io/guide/routing-overview
- **HTTP Client:** https://angular.io/guide/http

---

## ✅ Checklist de Lancement du Frontend

```
□ Node.js 20+ installé
□ npm 11+ disponible
□ Backend en cours d'exécution (http://localhost:8081)
□ Naviguez vers le dossier frontend
□ npm install exécuté avec succès
□ npm start lancé sans erreurs
□ http://localhost:4200 s'ouvre dans le navigateur
□ La page de login s'affiche
□ Vous pouvez vous connecter avec vos identifiants
```

---

## 🎓 Ressources

- **Repo Frontend:** `c:\Users\MSI\Desktop\nouveau !!!\frontend\`
- **Angular Docs:** https://angular.io
- **TypeScript:** https://www.typescriptlang.org
- **npm:** https://www.npmjs.com

---

**Version:** 1.0 | **Date:** Avril 2026 | **Projet:** SMART RH 4.0

---

## 🚀 TL;DR (Texte Ultra-Court)

```powershell
# Terminal 1: Vérifier que le backend tourne
# (docker-compose up --build doit être en cours)

# Terminal 2: Lancer le frontend
cd frontend
npm install  # D'abord si besoin
npm start

# Accès
Frontend:  http://localhost:4200
Backend:   http://localhost:8081
```

**Puis ouvrez http://localhost:4200 dans votre navigateur!** 🎉
