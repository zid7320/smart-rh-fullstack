# Chapitre 1 : Cadre du projet

## Introduction

Dans ce premier chapitre, nous présentons le cadre général du projet SMART RH 4.0. Tout d'abord, nous introduisons le contexte du projet et les technologies appropriées. Ensuite, nous comparons notre solution à celles déjà disponibles sur le marché. Enfin, nous définissons la méthodologie de développement adoptée. Ce chapitre pose les bases en fournissant une vue d'ensemble du projet, en décrivant les enjeux métier, en analysant l'existant et en spécifiant les besoins fonctionnels et non fonctionnels.

---

## 1.1 Contexte général du projet

### 1.1.1 Cadre du projet

Le stage constitue une opportunité précieuse pour enrichir nos connaissances académiques et acquérir une expérience professionnelle concrète dans le domaine du développement logiciel et de la gestion des ressources humaines. Dans le cadre de l'obtention du diplôme national d'ingénieur en Génie Logiciel, nous avons réalisé un projet au sein d'une entreprise leader en solutions digitales. Ce projet consiste en le développement d'une **plateforme intégrée de gestion des ressources humaines**, intitulée **« SMART RH 4.0 »**, destinée à moderniser les processus RH des entreprises.

L'objectif principal de SMART RH 4.0 est de fournir un système d'information complet et centralisé permettant aux entreprises de gérer efficacement :
- La gestion des employés et de leurs profils
- Le suivi des présences et de l'assiduité
- La gestion des congés et des absences
- La gestion de la paie et des bulletins de salaire
- Le recrutement et la sélection de candidats
- Les évaluations de performance
- Les formations et certifications
- La planification des horaires et des tâches

### 1.1.2 Présentation de la société d'accueil et du contexte métier

#### 1.1.2.1 Enjeux métier

Les entreprises modernes font face à des défis croissants dans la gestion de leurs ressources humaines. Les systèmes RH traditionnels, souvent fragmentés et peu intuitifs, présentent plusieurs limitations :

- **Fragmentation des données** : L'information RH est dispersée entre plusieurs applications déconnectées
- **Absence de centralisation** : Aucun point unique de référence pour l'ensemble des opérations RH
- **Manque de réactivité** : Les processus manuels ralentissent les décisions et augmentent les délais
- **Expérience utilisateur limitée** : Les interfaces peu ergonomiques découragent l'utilisation courante
- **Manque de traçabilité** : L'absence de logs d'audit complique la conformité et la responsabilité

#### 1.1.2.2 Technologies adoptées

Les technologies utilisées dans SMART RH 4.0 se résument dans des compétences techniques modernes et éprouvées :

• **Back-End** : développe et gère principalement la logique fonctionnelle de base et les opérations de la plateforme RH à l'aide de **Spring Boot 3.2**, **Java 21**, **Spring Data JPA** pour la gestion avancée des données, et **Spring Security 6** pour l'authentification robuste basée sur JWT.

• **Front-End** : est la pratique de convertir les données métier RH en interface graphique conviviale pour permettre à l'utilisateur de visualiser et d'interagir avec les données par interaction numérique à l'aide d'**Angular 17+**, **Angular Material** pour les composants UI professionnels et accessibles, et **TypeScript** pour une meilleure qualité de code.

• **Persistance des données** : gestion de la couche données et migrations versionnées en utilisant **MySQL 8.0** comme base de données relationnelle robuste et scalable, et **Flyway** pour la gestion sécurisée des migrations de schéma.

• **Communication temps réel** : implémentation d'une infrastructure de synchronisation en temps réel entre le serveur et les clients via **WebSocket/SockJS** pour les connexions bidirectionnelles et **STOMP** comme protocole de messagerie, permettant les mises à jour instantanées des opérations RH.

• **Infrastructure et déploiement DevOps** : utilisation de **Docker & Docker Compose** pour la conteneurisation des applications garantissant des déploiements cohérents et reproductibles, ainsi que **Swagger/OpenAPI** pour la documentation interactive des APIs REST, associée aux **JWT (JSON Web Tokens)** pour une authentification stateless et sécurisée.

Ces technologies constituent un choix particulièrement adapté pour la scalabilité, la sécurité, la maintenabilité et la performance, avec des standards éprouvés dans le secteur bancaire et gouvernemental.

---

## 1.2 Idée émergente et étude de l'existant

### 1.2.1 Idée émergente

Consciente de l'importance croissante de la transformation numérique, notre entreprise, un acteur majeur dans le secteur des solutions digitales et de la gestion des ressources humaines, a pris la décision stratégique de développer une plateforme RH intégrée. Cette initiative vise à répondre aux besoins changeants des entreprises en leur offrant une solution innovante et pratique pour gérer l'ensemble de leurs opérations de ressources humaines. En développant cette plateforme SMART RH 4.0, l'entreprise s'engage à offrir une expérience utilisateur optimale et à renforcer son positionnement en tant que leader de l'industrie RH, tout en restant à la pointe des avancées technologiques.

L'objectif principal de SMART RH 4.0 est d'augmenter l'interaction entre les équipes RH, les responsables et les employés, et de maximiser l'efficacité opérationnelle grâce à une application intégrée conviviale et intuitive.

### 1.2.2 Analyse de l'existant

Une bonne étude de l'existant est essentielle et apporte de nombreux avantages. En examinant attentivement les solutions RH actuellement disponibles sur le marché, nous avons pu dégager les tendances et les bonnes pratiques.

Les systèmes RH existants se divisent en deux catégories :

#### Solutions on-premise coûteuses
- **SAP SuccessFactors**, **Oracle HCM Cloud**, **Workday**
- Coût : 10 000€ à 100 000€+ par mois pour petite à moyenne entreprise
- Complexité : Configuration complexe, courbe d'apprentissage longue
- Avantages : Robustes, complètes, supportées
- Inconvénients : Peu flexibles, temps de déploiement long, coût de maintenance élevé

#### Solutions SaaS légères
- **BambooHR**, **Zoho People**, **Factorial**
- Coût : 50 à 500€ par mois selon les modules
- Simplicité : Interfaces intuitives, déploiement rapide
- Avantages : Abordables, faciles à utiliser
- Inconvénients : Fonctionnalités limitées, peu de personnalisation, dépendance au fournisseur

### 1.2.3 Critique de l'existant

### 1.2.3 Critique de l'existant

#### 1.2.3.1 Critique du système actuel

En étudiant les solutions RH existantes, nous avons constaté qu'elles se limitent à des fonctionnalités basiques. Certaines fonctionnalités ne bénéficient pas d'une expérience utilisateur immersive et adaptée aux spécificités des besoins métier moderne.

— Il n'est pas possible d'avoir une vue d'ensemble unifiée des opérations RH sur une seule plateforme centralisée.
— Pour accéder aux bulletins de paie, les employés doivent naviguer dans des systèmes externes déconnectés, sans intégration fluide.
— Les notifications pour les demandes de congé et les mises à jour administratives ne sont pas interactives, ce qui entraîne une réactivité faible de la part des utilisateurs.
— L'utilisateur ne reçoit pas de notifications en temps réel pour les alertes critiques, les approbations requises ou les événements RH importants.
— Il existe une latence tant au niveau du temps d'exécution des requêtes que de la navigation entre les différents modules et écrans.
— L'absence de tableaux de bord analytiques en temps réel limite la capacité des responsables RH à prendre des décisions données.
— Les workflows d'approbation sont peu flexibles et ne s'adaptent pas aux processus métier spécifiques de chaque entreprise.

#### 1.2.3.2 État de l'art

Afin de proposer une solution complète et compétitive, nous avons étudié les meilleures pratiques du marché :

**Workday (Leader de marché)**
- Points forts :
  - Cloud-native, très sécurisée
  - Dashboards analytiques avancés
  - Intégrations nombreuses (200+ applications)
  - Expérience utilisateur excellente
- Points faibles :
  - Coût très élevé (5 000€ à 50 000€/mois)
  - Temps d'implémentation très long
  - Configuration complexe

**BambooHR (Solution légère populaire)**
- Points forts :
  - Interface très conviviale et intuitive
  - Rapport coût-performance avantageux
  - Déploiement rapide (quelques jours)
  - Applicatif mobile natif bien développé
- Points faibles :
  - Fonctionnalités limitées en gestion de paie
  - Absence de véritable traçabilité d'audit
  - Personnalisation très limitée
  - Performance limitée pour gros volumes

**Microsoft Dynamics 365 Human Resources**
- Points forts :
  - Intégration complète avec l'écosystème Microsoft
  - Extensibilité via Power Platform
  - Gestion complète de la paie et des avantages
- Points faibles :
  - Courbe d'apprentissage très importante
  - Coûts variés et souvent inattendus
  - Nécessite expertise technique importante

### 1.2.4 Solution proposée : SMART RH 4.0

Nous proposons une solution open-source, moderne et hautement personnalisable qui remédie à toutes les limitations précédemment mentionnées. SMART RH 4.0 garantit toutes les fonctionnalités essentielles d'une plateforme RH complète tout en offrant une flexibilité maximale et des coûts maîtrisés.

**Spécificités de SMART RH 4.0 :**

1. **Gestion complète des employés**
   - Profils détaillés avec photo et identification faciale
   - Historique complet des changements
   - Gestion des responsables et dépendances hiérarchiques

2. **Gestion de la présence et assiduité**
   - Pointage facial (reconnaissance faciale intégrée)
   - Historique complet des entrées/sorties
   - Rapports d'assiduité en temps réel
   - Alertes sur absences ou retards

3. **Gestion des congés et absences**
   - Demandes de congé avec workflow d'approbation
   - Historique des congés par type et par employé
   - Intégration avec planning
   - Notifications automatiques

4. **Gestion de la paie**
   - Calcul automatique des bulletins de salaire
   - Génération de rapports et fichiers de paie
   - Suivi des déductions et primes
   - Archivage des bulletins électroniques

5. **Recrutement et candidatures**
   - Gestion des offres d'emploi
   - Pipeline de candidatures
   - Suivi du statut des candidats
   - Intégration avec tests de compétences

6. **Évaluations de performance**
   - Formulaires d'évaluation personnalisables
   - KPIs et objectifs à atteindre
   - Historique des performances
   - Feedbacks 360°

7. **Formations et développement**
   - Catalogue de formations
   - Suivi des formations et certifications
   - Attestations numériques
   - Rapports de compétences

8. **Tableau de bord analytique**
   - Dashboards en temps réel
   - Rapports personnalisables
   - Alertes automatiques
   - Export de données (PDF, Excel)

9. **Audit et conformité**
   - Traçabilité complète de toutes les opérations
   - Logs d'audit détaillés avec IP et timestamps
   - Respect des normes de sécurité RGPD
   - Signatures numériques et certifiées

10. **Accessibilité et performance**
    - Interface responsive (desktop, tablette, mobile)
    - Temps de charge optimisé (< 2 secondes)
    - Accessibilité WCAG 2.1 AAA
    - Support multi-langues (minimum 4 langues)

---

## 1.3 Méthodologie de développement

### 1.3.1 Étude des méthodes existantes

Devant le nombre de méthodes disponibles, le choix parmi elles devient difficile. Nous étudions à ce propos quelques méthodes de développement pour pouvoir choisir la méthode la plus adéquate à ce projet.

**Scrum**

Scrum est une framework agile particulièrement destinée à la gestion de projets informatiques. Le principe de Scrum est de pouvoir modifier la direction prise par le projet au fur et à mesure de son avancement. Les développements sont organisés en sprints (itérations) de durée fixe (généralement 2 semaines).

Avantages :
- Flexibilité et adaptabilité au changement
- Feedback régulier des utilisateurs
- Progrès visible et mesurable
- Bonne collaboration d'équipe

Inconvénients :
- Peut être inefficace pour les petites équipes
- Nécessite engagement du client constant

---

**Extreme Programming (XP)**

Le principe fondamental de la méthode XP est de faire collaborer étroitement tous les acteurs du projet et d'opter pour des itérations de développement très courtes. L'Extreme Programming préconise également le travail en binôme des développeurs et des tests rigoureux.

Avantages :
- Qualité de code très élevée
- Réduction des bugs et risques
- Collaboration intense

Inconvénients :
- Coûteux en ressources (2 développeurs par tâche)
- Pas toujours adapté aux projets de grande taille

---

**Rational Unified Process (RUP)**

RUP est un processus itératif et incrémental. Chaque itération respecte un cycle comprenant quatre phases : lancement, conception, réalisation et livraison. Les développements sont guidés par des cas d'utilisation.

Avantages :
- Équilibre entre discipline et flexibilité
- Bien structuré et documenté
- Gestion des risques efficace

Inconvénients :
- Complexe et lourd pour petits projets
- Nécessite expertise importante

---

**Two Track Unified Process (2TUP)**

Le 2TUP propose un cycle de développement en Y, qui dissocie les aspects techniques des aspects fonctionnels. Il combine une branche fonctionnelle et une branche technique qui convergent vers la réalisation.

Avantages :
- Séparation claire entre design fonctionnel et architecture technique
- Idéal pour projets d'envergure moyenne
- Flexibilité et adaptabilité
- Bonne traçabilité des besoins

Inconvénients :
- Peut être lourd administrativement
- Nécessite bonne documentation

---

### 1.3.2 Tableau comparatif des méthodologies

| Critère | Scrum | XP | RUP | 2TUP |
|---------|-------|-----|------|------|
| **Complexité du projet** | Moyenne | Petite | Grande | Moyenne-Grande |
| **Taille de l'équipe** | 3-9 personnes | 2-4 personnes | 20+ personnes | 5-15 personnes |
| **Flexibilité au changement** | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐⭐ | ⭐⭐ | ⭐⭐⭐ |
| **Documentation** | Légère | Minimale | Très importante | Importante |
| **Courbe d'apprentissage** | Facile | Moyenne | Difficile | Moyenne |
| **Gestion des risques** | Bonne | Bonne | Excellente | Excellente |
| **Qualité du code** | Bonne | Excellente | Très bonne | Très bonne |
| **Coût** | Moyen | Élevé | Élevé | Moyen |
| **Adaptation projet RH** | ⭐⭐⭐⭐ | ⭐⭐⭐ | ⭐⭐⭐ | ⭐⭐⭐⭐⭐ |

### 1.3.3 Choix de la méthodologie et justification

Après analyse comparative, nous avons choisi la méthodologie **2TUP (Two Track Unified Process)** comme approche de développement pour SMART RH 4.0.

**Justification du choix :**

La méthodologie 2TUP s'avère particulièrement appropriée pour notre projet pour les raisons suivantes :

1. **Clarté entre métier et technique**
   - Les besoins métier complexes du RH sont isolés sur une branche dédiée
   - L'architecture technique peut évoluer indépendamment
   - Meilleure traçabilité des besoins vers la solution

2. **Flexibilité adaptée**
   - Permet d'ajuster les priorités fonctionnelles rapidement
   - Permet d'optimiser l'architecture technique en parallèle
   - Convergence progressive des deux branches

3. **Gestion des risques**
   - Identification précoce des risques techniques et fonctionnels
   - Validation progressive des choix architecturaux
   - Réduction des découvertes tardives

4. **Équipe de taille moyenne**
   - Notre équipe peut être subdivisée en branche fonctionnelle et technique
   - Collaboration efficace sans surcharge administrative

5. **Domaine du RH complexe**
   - Les processus RH sont bien définis mais peuvent évoluer
   - L'architecture technique doit supporter l'évolutivité
   - 2TUP permet de gérer cette dualité

**Structure de 2TUP adoptée :**

**Branche gauche (fonctionnelle) :**
- Capture des besoins métier RH
- Cas d'utilisation détaillés par domaine (paie, congés, etc.)
- Modélisation des processus métier
- Validation avec les utilisateurs finaux

**Branche droite (technique) :**
- Définition de l'architecture microservices/monolithique
- Choix technologiques et justification
- Framework et dépendances
- Design patterns et conventions

**Branche du milieu (réalisation) :**
- Conception préliminaire : fusion des besoins et architecture
- Conception détaillée : modèles de données, interfaces
- Implémentation itérative
- Intégration et tests

---

## 1.4 Planification

La planification est cruciale pour assurer le bon déroulement d'un projet. Elle consiste à identifier et organiser les tâches de manière séquentielle et logique. Pour cette étude, nous avons établi un calendrier detaillé couvrant l'ensemble des phases de développement.

### 1.4.1 Phases principales du projet

**Phase 1 : Étude et spécification (Semaines 1-2)**
- Analyse détaillée des besoins
- Définition des cas d'utilisation
- Architecture logicielle

**Phase 2 : Conception (Semaines 3-4)**
- Modélisation des données
- Design des interfaces utilisateur
- Architecture technique affinée

**Phase 3 : Implémentation - Sprint 1 (Semaines 5-6)**
- Backend : Authentification et autorisation
- Backend : Gestion des utilisateurs et employés
- Frontend : Infrastructure et authentification

**Phase 4 : Implémentation - Sprint 2 (Semaines 7-8)**
- Backend : Gestion des congés et assiduité
- Frontend : Module employés et pointage
- Intégration des APIs

**Phase 5 : Implémentation - Sprint 3 (Semaines 9-10)**
- Backend : Gestion de la paie et recrutement
- Frontend : Modules paie et recrutement
- Tests unitaires et intégration

**Phase 6 : Implémentation - Sprint 4 (Semaines 11-12)**
- Backend : Évaluations et formations
- Frontend : Dashboards analytiques
- Tests de performance

**Phase 7 : Tests et qualité (Semaines 13-14)**
- Tests de régression complets
- Tests de charge et performance
- Audit de sécurité

**Phase 8 : Déploiement et documentation (Semaines 15-16)**
- Préparation de l'environnement de production
- Rédaction de la documentation utilisateur
- Formation des utilisateurs

### 1.4.2 Jalons importants

| Jalon | Semaine | Livrables |
|-------|---------|-----------|
| Spécifications validées | 2 | Requirements, Use cases, Maquettes |
| Design architecture approuvé | 4 | Modèles de données, APIs, UI Kit |
| MVP backend opérationnel | 6 | Auth, Users, BD de base |
| Frontend prototype | 8 | Interface de base, routes |
| Fonctionnalités cœur | 10 | Paie, congés, pointage |
| Version candidate | 12 | Tous les modules, pas de critiques |
| Tests complets | 14 | Rapports de tests, corrections |
| Production ready | 16 | Documentation complète, déploiement |

---

## 1.5 Conclusion

Ce chapitre a abordé le contexte général du projet SMART RH 4.0, les enjeux métier auxquels il répond, et la méthodologie adoptée pour sa réalisation. Nous avons démontré comment notre solution innove par rapport aux alternatives existantes en offrant flexibilité, coûts maîtrisés et fonctionnalités complètes.

La méthodologie 2TUP choisie permet de gérer efficacement la complexité du domaine RH tout en maintenant une architecture technique solide et évolutive. La planification établie assure une progression cohérente du projet vers la livraison d'une solution de première qualité.

Le chapitre suivant approfondira les spécifications fonctionnelles du projet en détaillant les différents acteurs, leurs rôles respectifs et les actions spécifiques qu'ils effectueront au sein de la plateforme SMART RH 4.0.

---

## Références

[1] Agile Manifesto - Beck K., et al. (2001)  
[2] 2TUP - "Two Track Unified Process" - Orange Consulting  
[3] RUP - Rational Unified Process - IBM  
[4] SCRUM Guide - Schwaber K., Sutherland J. (2020)  

---

**Document généré pour le projet SMART RH 4.0**  
**Version:** 1.0  
**Date:** Avril 2026
