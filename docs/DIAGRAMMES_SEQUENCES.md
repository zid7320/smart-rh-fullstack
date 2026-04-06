# Diagrammes de Séquences - SMART RH 4.0

## Vue d'ensemble
Ce document regroupe les diagrammes de séquence illustrant les interactions entre les différents composants du système SMART RH 4.0 pour les processus clés.

---

## 1. Authentification et Connexion

```mermaid
sequenceDiagram
    actor Utilisateur
    participant Frontend as Frontend<br/>(Angular)
    participant Backend as Backend<br/>(Spring Boot)
    participant DB as Base de Données<br/>(MySQL)
    participant Auth as Service Auth<br/>(JWT)

    Utilisateur->>Frontend: 1. Saisir identifiants
    Frontend->>Backend: 2. POST /api/auth/login<br/>(email, mot de passe)
    Backend->>DB: 3. Rechercher utilisateur
    DB-->>Backend: 4. Retour données utilisateur
    Backend->>Auth: 5. Générer JWT Token
    Auth-->>Backend: 6. Token généré
    Backend-->>Frontend: 7. Réponse 200 (Token JWT)
    Frontend->>Frontend: 8. Stocker token
    Frontend-->>Utilisateur: 9. Redirection Dashboard
    Frontend->>Backend: 10. GET /api/user/profile<br/>(Header: Authorization)
    Backend->>DB: 11. Récupérer profil
    DB-->>Backend: 12. Profil utilisateur
    Backend-->>Frontend: 13. Données profil
    Frontend-->>Utilisateur: 14. Affichage Dashboard
```

**Description :**
- L'utilisateur fournit ses identifiants
- Le backend valide les informations en base de données
- Un JWT token est généré pour les futures requêtes
- Le token est stocké côté client (localStorage)
- Le dashboard de l'utilisateur est chargé et affiché

**Endpoints impliqués :**
- `POST /api/auth/login` - Authentification
- `GET /api/user/profile` - Récupération du profil

---

## 2. Gestion des Congés avec Workflow d'Approbation

```mermaid
sequenceDiagram
    actor Employé
    actor Responsable
    participant Frontend as Frontend<br/>(Angular)
    participant Backend as Backend<br/>(Spring Boot)
    participant DB as Base de Données
    participant WebSocket as WebSocket<br/>STOMP

    Employé->>Frontend: 1. Cliquer "Demander congé"
    Frontend->>Frontend: 2. Ouvrir formulaire
    Employé->>Frontend: 3. Remplir: type, dates, motif
    Frontend->>Backend: 4. POST /api/leave/request<br/>(conge object)
    Backend->>DB: 5. Créer demande (statut: EN_ATTENTE)
    DB-->>Backend: 6. ID demande créé
    Backend->>WebSocket: 7. Notifier responsable
    WebSocket-->>Responsable: 8. Notification: Nouvelle demande
    Backend-->>Frontend: 9. Réponse 201 (succès)
    Frontend-->>Employé: 10. Confirmation: "Demande envoyée"
    
    Responsable->>Frontend: 11. Accéder module "Approbations"
    Frontend->>Backend: 12. GET /api/leave/pending
    Backend->>DB: 13. Récupérer demandes en attente
    DB-->>Backend: 14. Liste demandes
    Backend-->>Frontend: 15. Retour liste
    Frontend-->>Responsable: 16. Afficher demandes
    
    Responsable->>Frontend: 17. Cliquer "Approuver"
    Frontend->>Backend: 18. PUT /api/leave/{id}/approve<br/>(commentaire)
    Backend->>DB: 19. MAJ statut: APPROUVÉE
    DB-->>Backend: 20. Confirmation MAJ
    Backend->>WebSocket: 21. Notifier employé
    WebSocket-->>Employé: 22. Notification: "Congé approuvé"
    Backend-->>Frontend: 23. Réponse 200 (succès)
    Frontend-->>Responsable: 24. Confirmation affichée
```

**Description :**
- L'employé remplit une demande de congé avec les dates et le type
- La demande est créée et le responsable reçoit une notification en temps réel
- Le responsable accède à la liste des demandes en attente
- Il peut approuver, rejeter ou demander des modifications
- L'employé reçoit une notification de la décision

**Endpoints impliqués :**
- `POST /api/leave/request` - Créer une demande
- `GET /api/leave/pending` - Récupérer demandes
- `PUT /api/leave/{id}/approve` - Approuver

---

## 3. Pointage par Reconnaissance Faciale

```mermaid
sequenceDiagram
    actor Employé
    participant Kiosk as Kiosk<br/>Pointage
    participant Backend as Backend<br/>(Spring Boot)
    participant DB as Base de Données
    participant FaceAPI as API<br/>Reconnaissance<br/>Faciale

    Employé->>Kiosk: 1. Se présenter devant<br/>la caméra
    Kiosk->>Kiosk: 2. Capturer photo visage
    Kiosk->>FaceAPI: 3. POST /recognize<br/>(image binaire)
    FaceAPI-->>Kiosk: 4. Retour ID + confiance
    Kiosk->>Backend: 5. POST /api/attendance/checkin<br/>(employee_id, timestamp)
    Backend->>DB: 6. Créer enregistrement<br/>EntrySortie
    DB-->>Backend: 7. Confirmation (ID record)
    Backend->>DB: 8. Vérifier absence/retard
    DB-->>Backend: 9. Absence/Retard détecté?
    
    alt Retard détecté
        Backend->>Backend: 10. Générer alerte
        Backend-->>Kiosk: 11. Notification "Retard!"
        Kiosk-->>Employé: 12. Affichage écran
    else Absent attendu
        Backend->>Backend: 13. Générer alerte
        Backend-->>Employé: 14. Notif Admin
    else Normal
        Backend-->>Kiosk: 15. Réponse 200 OK
    end
    
    Kiosk-->>Employé: 16. "Pointage validé"
    Backend->>DB: 17. Incrémenter compteur<br/>présence du jour
    DB-->>Backend: 18. Confirmé
```

**Description :**
- L'employé se présente devant le kiosque
- Recognition faciale pour identifier l'employé
- Le pointage (check-in/check-out) est enregistré en base
- Détection automatique des retards ou absences
- Alertes générées pour les anomalies

**Endpoints impliqués :**
- `POST /api/attendance/checkin` - Pointage
- `GET /api/attendance/records` - Historique présence

---

## 4. Génération et Validation de la Paie

```mermaid
sequenceDiagram
    actor RH as Responsable RH
    participant Frontend as Frontend<br/>(Angular)
    participant Backend as Backend<br/>(Spring Boot)
    participant PayrollEngine as Moteur<br/>de Paie
    participant DB as Base de Données
    participant Stockage as Stockage<br/>Fichiers

    RH->>Frontend: 1. Accéder "Gestion Paie"
    Frontend->>Backend: 2. GET /api/payroll/config
    Backend->>DB: 3. Récupérer config paie
    DB-->>Backend: 4. Retour config
    Backend-->>Frontend: 5. Afficher formulaire
    Frontend-->>RH: 6. Formulaire mois/année
    
    RH->>Frontend: 7. Sélectionner mois<br/>+ valider
    Frontend->>Backend: 8. POST /api/payroll/generate<br/>(month, year)
    Backend->>DB: 9. Récupérer employés actifs
    DB-->>Backend: 10. Liste employés
    Backend->>DB: 11. GET données paie<br/>(salaires, retenues, primes)
    DB-->>Backend: 12. Données paie
    Backend->>PayrollEngine: 13. Calculer bulletins<br/>(données paie)
    PayrollEngine-->>Backend: 14. Bulletins calculés
    Backend->>DB: 15. Stocker bulletins<br/>(statut: GÉNÉRÉ)
    DB-->>Backend: 16. Confirmé
    Backend->>Stockage: 17. Générer fichiers PDF
    Stockage-->>Backend: 18. Fichiers PDF créés
    Backend-->>Frontend: 19. Réponse 201
    Frontend-->>RH: 20. "Bulletins générés"
    
    RH->>Frontend: 21. Cliquer "Valider paie"
    Frontend->>Backend: 22. PUT /api/payroll/{id}/validate
    Backend->>DB: 23. MAJ statut: VALIDÉE
    DB-->>Backend: 24. Confirmé
    Backend->>DB: 25. Mettre à jour solde<br/>employés
    DB-->>Backend: 26. Confirmé
    Backend-->>Frontend: 27. Réponse 200
    Frontend-->>RH: 28. Affichage "Paie validée"
```

**Description :**
- Le responsable RH accède au module de gestion de paie
- Sélection du mois et de l'année
- Calcul automatique des bulletins selon les données (salaires, retenues, primes)
- Génération des fichiers PDF des bulletins
- Validation de la paie
- Mise à jour des soldes des employés

**Endpoints impliqués :**
- `GET /api/payroll/config` - Configuration paie
- `POST /api/payroll/generate` - Génération des bulletins
- `PUT /api/payroll/{id}/validate` - Validation paie
- `GET /api/payroll/export` - Export fichiers

---

## 5. Gestion du Recrutement

```mermaid
sequenceDiagram
    actor Candidat
    actor RH as Responsable<br/>Recrutement
    participant Website as Site Web<br/>(Frontend)
    participant Backend as Backend<br/>(Spring Boot)
    participant DB as Base de Données
    participant Email as Service Email
    participant WebSocket as WebSocket

    Candidat->>Website: 1. Accéder "Offres"
    Website->>Backend: 2. GET /api/jobs/open
    Backend->>DB: 3. Récupérer offres
    DB-->>Backend: 4. Liste offres
    Backend-->>Website: 5. Afficher offres
    Website-->>Candidat: 6. Consulter détails
    
    Candidat->>Website: 7. Cliquer "Postuler"
    Website->>Website: 8. Ouvrir formulaire
    Candidat->>Website: 9. Télécharger CV + LM
    Website->>Backend: 10. POST /api/applications<br/>(cv, cover_letter, données)
    Backend->>DB: 11. Créer candidature<br/>(statut: NEW)
    DB-->>Backend: 12. Confirmé
    Backend->>Email: 13. Envoyer confirmation<br/>au candidat
    Email-->>Candidat: 14. Email reçu
    Backend->>WebSocket: 15. Notifier RH
    WebSocket-->>RH: 16. "Nouvelle candidature"
    Backend-->>Website: 17. Réponse 201
    Website-->>Candidat: 18. "Candidature reçue"
    
    RH->>Website: 19. Accéder module<br/>"Candidatures"
    Website->>Backend: 20. GET /api/applications/pending
    Backend->>DB: 21. Récupérer candidatures
    DB-->>Backend: 22. Liste candidatures
    Backend-->>Website: 23. Afficher candidatures
    Website-->>RH: 24. Consulter CV
    
    RH->>Website: 25. Cliquer "Entretien"
    Website->>Backend: 26. PUT /api/applications/{id}<br/>(statut: INTERVIEW)
    Backend->>DB: 27. MAJ statut
    DB-->>Backend: 28. Confirmé
    Backend->>Email: 29. Envoyer invite entretien
    Email-->>Candidat: 30. Email avec détails
    Backend-->>Website: 31. Réponse 200
    Website-->>RH: 32. "Candidat contacté"
```

**Description :**
- Les candidats consultent les offres d'emploi
- Soumission de candidature avec CV et lettre de motivation
- Confirmation et notification au candidat
- Notification en temps réel au responsable RH
- RH accède aux candidatures et peut les progresser dans le pipeline
- Communication automatique par email avec les candidats

**Endpoints impliqués :**
- `GET /api/jobs/open` - Offres ouvertes
- `POST /api/applications` - Soumettre candidature
- `GET /api/applications/pending` - Candidatures en attente
- `PUT /api/applications/{id}` - Mettre à jour statut

---

## 6. Dashboard Employé avec Notifications Temps Réel

```mermaid
sequenceDiagram
    actor Salarié
    participant Frontend as Frontend<br/>(Angular)
    participant Backend as Backend<br/>(Spring Boot)
    participant DB as Base de Données
    participant Cache as Cache Redis
    participant WebSocket as WebSocket<br/>STOMP

    Salarié->>Frontend: 1. Se connecter
    Frontend->>Backend: 2. POST /api/auth/login
    Backend-->>Frontend: 3. JWT Token
    Frontend->>Backend: 4. GET /api/dashboard/profile
    Backend->>Cache: 5. Chercher données<br/>en cache
    
    alt Données en cache
        Cache-->>Backend: 6. Retour données
    else Cache vide
        Backend->>DB: 6b. Récupérer données
        DB-->>Backend: 7b. Données BD
        Backend->>Cache: 8b. Stocker en cache (TTL)
    end
    
    Backend-->>Frontend: 8. Données profil
    Frontend->>Frontend: 9. Afficher Dashboard
    Frontend-->>Salarié: 10. Dashboard affiché
    
    Frontend->>Backend: 11. Ouvrir WebSocket<br/>(stomp connect)
    Backend->>WebSocket: 12. Abonnement aux<br/>notifs du salarié
    
    par Récupération données
        Frontend->>Backend: 13. GET /api/my-leaves
        Backend->>DB: 14. Récupérer congés
        DB-->>Backend: 15. Congés du salarié
        Backend-->>Frontend: 16. Retour congés
    and Récupération paie
        Frontend->>Backend: 17. GET /api/my-payslips
        Backend->>Cache: 18. Chercher paies
        Cache-->>Backend: 19. Paies en cache
        Backend-->>Frontend: 20. Retour paies
    and Récupération présence
        Frontend->>Backend: 21. GET /api/my-attendance
        Backend->>DB: 22. Récupérer présence
        DB-->>Backend: 23. Présence
        Backend-->>Frontend: 24. Retour présence
    end
    
    Frontend-->>Salarié: 25. Afficher données
    
    Backend->>WebSocket: 26. Nouveau bulletin<br/>généré (notification)
    WebSocket-->>Frontend: 27. Message: "Bulletin"
    Frontend->>Frontend: 28. Afficher notification
    Frontend-->>Salarié: 29. Toast: "Bulletin<br/>disponible"
```

**Description :**
- L'employé se connecte et accède au dashboard
- Chargement des données avec utilisation du cache pour les performances
- Les données sont chargées en parallèle (congés, paie, présence)
- Ouverture d'une connexion WebSocket pour les notifications temps réel
- Affichage des notifications (ex: nouveau bulletin disponible)

**Points techniques :**
- Utilisation de **Redis** pour le cache
- Requêtes parallèles pour optimiser les performances
- **WebSocket/STOMP** pour les notifications en temps réel
- Temps de chargement optimisé (< 2 secondes objectif)

---

## 7. Gestion des Évaluations de Performance

```mermaid
sequenceDiagram
    actor Responsable as Manager/<br/>Responsable
    actor Employé
    participant Frontend as Frontend<br/>(Angular)
    participant Backend as Backend<br/>(Spring Boot)
    participant DB as Base de Données
    participant Email as Email Service
    participant WebSocket as WebSocket

    Responsable->>Frontend: 1. Accéder "Évaluations"
    Frontend->>Backend: 2. GET /api/evaluations/pending
    Backend->>DB: 3. Récupérer équipe<br/>à évaluer
    DB-->>Backend: 4. Liste équipe
    Backend-->>Frontend: 5. Afficher employés
    Frontend-->>Responsable: 6. Liste affichée
    
    Responsable->>Frontend: 7. Cliquer "Commencer<br/>évaluation"
    Frontend->>Backend: 8. GET /api/evaluations/<br/>template
    Backend->>DB: 9. Récupérer formulaire
    DB-->>Backend: 10. Questions/critères
    Backend-->>Frontend: 11. Afficher formulaire
    Frontend-->>Responsable: 12. Formulaire affiché
    
    Responsable->>Frontend: 13. Remplir évaluation<br/>(scores, commentaires)
    Frontend->>Backend: 14. POST /api/evaluations<br/>(evaluation_data)
    Backend->>DB: 15. Créer évaluation<br/>(statut: DRAFT)
    DB-->>Backend: 16. Confirmé
    Backend-->>Frontend: 17. Réponse 201
    Frontend-->>Responsable: 18. "Évaluation sauvegardée"
    
    Responsable->>Frontend: 19. Cliquer "Soumettre"
    Frontend->>Backend: 20. PUT /api/evaluations/{id}<br/>(statut: SUBMITTED)
    Backend->>DB: 21. MAJ statut: SUBMITTED
    DB-->>Backend: 22. Confirmé
    Backend->>Email: 23. Envoyer notification
    Email-->>Employé: 24. Email: Évaluation reçue
    Backend->>WebSocket: 25. Notif WebSocket
    WebSocket-->>Employé: 26. Notification temps réel
    Backend-->>Frontend: 27. Réponse 200
    Frontend-->>Responsable: 28. "Évaluation envoyée"
    
    Employé->>Frontend: 29. Consulter évaluation
    Frontend->>Backend: 30. GET /api/evaluations/{id}
    Backend->>DB: 31. Récupérer évaluation
    DB-->>Backend: 32. Données évaluation
    Backend-->>Frontend: 33. Afficher résultats
    Frontend-->>Employé: 34. Consultation évaluation
```

**Description :**
- Le responsable accède à la liste des employés à évaluer
- Récupération du formulaire/template d'évaluation
- Remplissage avec scores et commentaires détaillés
- Sauvegarde en brouillon (DRAFT) puis soumission
- L'employé reçoit une notification (email + WebSocket)
- L'employé peut consulter son évaluation

**Endpoints impliqués :**
- `GET /api/evaluations/pending` - Évaluations en attente
- `GET /api/evaluations/template` - Récupérer le formulaire
- `POST /api/evaluations` - Créer une évaluation
- `PUT /api/evaluations/{id}` - Mettre à jour

---

## Synthèse des Technologies

| Composant | Technologie | Rôle |
|-----------|-------------|------|
| **Frontend** | Angular 17+ | Interface utilisateur |
| **Backend** | Spring Boot 3.2 | Logique métier |
| **BD** | MySQL 8.0 | Persistance des données |
| **Auth** | JWT + Spring Security 6 | Authentification & Autorisation |
| **Cache** | Redis | Optimisation performances |
| **Notifications** | WebSocket + STOMP | Temps réel |
| **Email** | Spring Mail | Notifications emails |

---

## Flux de Sécurité

Tous les diagrammes incluent les principes de sécurité suivants :
1. ✅ **JWT Token** - Authentification stateless
2. ✅ **Authorization header** - Sécurité des requêtes
3. ✅ **HTTPS/TLS** - Chiffrement en transit
4. ✅ **RGPD compliance** - Logs d'audit
5. ✅ **Role-Based Access Control (RBAC)** - Contrôle d'accès

---

**Document généré pour le projet SMART RH 4.0**  
**Version:** 1.0  
**Date:** Avril 2026
