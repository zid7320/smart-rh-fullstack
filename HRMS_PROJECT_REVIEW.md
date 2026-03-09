# HRMS Project — Complete Technical Review

## Project Identity

- **Name:** Human Resource Management System (HRMS)
- **Artifact:** `com.example:hrms:0.0.1-SNAPSHOT`
- **Framework:** Spring Boot 2.7.12
- **Java Version:** 21
- **Architecture:** Layered REST API (Controller → Service → Repository → Entity)
- **Server Port:** 8080
- **Database:** H2 in-memory (MySQL driver present but commented out)

---

## Architecture Overview

The project follows a strict **5-layer architecture**:

```
HTTP Request
    ↓
Controller (@RestController)
    ↓ uses DTO (MapStruct mapper)
Service (@Service)
    ↓
Repository (JpaRepository)
    ↓
Entity (@Entity / JPA)
    ↓
H2 In-Memory Database
```

Supporting layers:
- `dto/` — flat Data Transfer Objects (no relationships exposed)
- `mapper/` — MapStruct interfaces for Entity ↔ DTO conversion
- `config/` — Jackson deserialization config
- `exception/` — Global error handler (`@ControllerAdvice`)

---

## Project Structure

```
src/
└── main/
    ├── java/com/example/hrms/
    │   ├── HrmsApplication.java
    │   ├── config/
    │   │   └── JacksonConfig.java
    │   ├── controller/
    │   │   ├── CandidateController.java
    │   │   ├── CompetenceController.java
    │   │   ├── CongeController.java
    │   │   ├── ContratController.java
    │   │   ├── DebugController.java
    │   │   ├── DossierRHController.java
    │   │   ├── EmployeController.java
    │   │   ├── EvaluationController.java
    │   │   ├── FormationController.java
    │   │   ├── PaieController.java
    │   │   ├── PlanningController.java
    │   │   ├── PosteController.java
    │   │   ├── RecrutementController.java
    │   │   └── ResponsableRHController.java
    │   ├── dto/
    │   │   ├── CandidateDTO.java
    │   │   ├── CompetenceDTO.java
    │   │   ├── CongeDTO.java
    │   │   ├── ContratDTO.java
    │   │   ├── DossierRHDTO.java
    │   │   ├── EmployeDTO.java
    │   │   ├── EvaluationDTO.java
    │   │   ├── FormationDTO.java
    │   │   ├── PaieDTO.java
    │   │   ├── PlanningDTO.java
    │   │   ├── PosteDTO.java
    │   │   ├── RecrutementDTO.java
    │   │   └── ResponsableRHDTO.java
    │   ├── exception/
    │   │   └── GlobalExceptionHandler.java
    │   ├── mapper/
    │   │   ├── CandidateMapper.java
    │   │   ├── CompetenceMapper.java
    │   │   ├── CongeMapper.java
    │   │   ├── ContratMapper.java
    │   │   ├── DossierRHMapper.java
    │   │   ├── EmployeMapper.java
    │   │   ├── EvaluationMapper.java
    │   │   ├── FormationMapper.java
    │   │   ├── PaieMapper.java
    │   │   ├── PlanningMapper.java
    │   │   ├── PosteMapper.java
    │   │   ├── RecrutementMapper.java
    │   │   └── ResponsableRHMapper.java
    │   ├── model/
    │   │   ├── Candidate.java
    │   │   ├── Competence.java
    │   │   ├── Conge.java
    │   │   ├── Contrat.java
    │   │   ├── DossierRH.java
    │   │   ├── Employe.java
    │   │   ├── Evaluation.java
    │   │   ├── Formation.java
    │   │   ├── Paie.java
    │   │   ├── Planning.java
    │   │   ├── Poste.java
    │   │   ├── Recrutement.java
    │   │   └── ResponsableRH.java
    │   ├── repository/
    │   │   ├── CandidateRepository.java
    │   │   ├── CompetenceRepository.java
    │   │   ├── CongeRepository.java
    │   │   ├── ContratRepository.java
    │   │   ├── DossierRHRepository.java
    │   │   ├── EmployeRepository.java
    │   │   ├── EvaluationRepository.java
    │   │   ├── FormationRepository.java
    │   │   ├── PaieRepository.java
    │   │   ├── PlanningRepository.java
    │   │   ├── PosteRepository.java
    │   │   ├── RecrutementRepository.java
    │   │   └── ResponsableRHRepository.java
    └── service/
        ├── CandidateService.java
        ├── CompetenceService.java
        ├── CongeService.java
        ├── ContratService.java
        ├── DossierRHService.java
        ├── EmployeService.java
        ├── EvaluationService.java
        ├── FormationService.java
        ├── PaieService.java
        ├── PlanningService.java
        ├── PosteService.java
        ├── RecruitmentService.java
        └── ResponsableRHService.java
```

---

## Domain Entities (13 total)

All entities use `@Entity`, `@Table`, Lombok `@Data`, `@NoArgsConstructor`, and `GenerationType.IDENTITY` for primary keys.

| Entity | Table | Key Fields |
|---|---|---|
| `Employe` | `employes` | idEmploye, nom, prenom, poste (String), email |
| `Candidate` | `candidates` | idCandidate, nom, prenom, email |
| `Competence` | `competences` | idCompetence, nom, niveau |
| `Conge` | `conges` | idConge, type, dateDebut, dateFin |
| `Contrat` | `contrats` | idContrat, type, dateDebut, dateFin, salaire |
| `DossierRH` | `dossiers_rh` | idDossier, infosPerso, diplomes, documents |
| `Evaluation` | `evaluations` | idEval, objectifs, kpi |
| `Formation` | `formations` | idFormation, titre, certification |
| `Paie` | `paies` | idPaie, montant, bulletinPDF |
| `Planning` | `plannings` | idPlanning, horaires |
| `Poste` | `postes` | idPoste, titre, competencesRequises |
| `Recrutement` | `recrutements` | idRecrutement, posteCible, statut |
| `ResponsableRH` | `responsables_rh` | idResponsable, nom |

### Entity Relationships

| From | Relation | To | Details |
|---|---|---|---|
| Employe | @OneToMany | Contrat | mappedBy="employe", CascadeAll, LAZY |
| Employe | @OneToOne | DossierRH | EAGER, CascadeAll, FK: dossier_id |
| Employe | @OneToMany | Planning | mappedBy="employe", CascadeAll, LAZY |
| Employe | @OneToMany | Conge | mappedBy="employe", CascadeAll, LAZY |
| Employe | @OneToMany | Paie | mappedBy="employe", CascadeAll, LAZY |
| Employe | @OneToMany | Evaluation | mappedBy="employe", CascadeAll, LAZY |
| Employe | @ManyToMany | Formation | join table: `employe_formation` |
| Employe | @ManyToMany | Competence | join table: `employe_competence` |
| Employe | @ManyToOne | Poste | FK: poste_id, LAZY |
| Employe | @OneToMany | Recrutement | mappedBy="employe", no cascade, LAZY |
| Candidate | @ManyToMany | Recrutement | join table: `candidate_recrutement`, CascadeAll |
| Poste | @ManyToMany | Competence | join table: `poste_competence`, CascadeAll |
| Recrutement | @ManyToOne | ResponsableRH | FK: responsable_id, LAZY |

**Auto-created JOIN tables:**
- `employe_formation` (employe_id, formation_id)
- `employe_competence` (employe_id, competence_id)
- `poste_competence` (poste_id, competence_id)
- `candidate_recrutement` (candidate_id, recrutement_id)

---

## DTOs (13 total)

All DTOs use Lombok `@Data`, `@NoArgsConstructor`, `@AllArgsConstructor`, and explicit `@JsonProperty` annotations. They are **flat** — no nested objects or foreign key IDs are exposed.

| DTO | Fields |
|---|---|
| `EmployeDTO` | idEmploye, nom (@NotBlank), prenom (@NotBlank), poste (@NotBlank), email (@Email, @NotBlank) |
| `CandidateDTO` | idCandidate, nom (@NotBlank), prenom (@NotBlank), email (@Email, @NotBlank) |
| `CompetenceDTO` | idCompetence, nom (@NotBlank), niveau (@NotBlank) |
| `CongeDTO` | idConge, type (@NotBlank), dateDebut (Date, @NotNull, @PastOrPresent), dateFin (Date, @NotNull) |
| `ContratDTO` | idContrat, type (@NotBlank), dateDebut (Date, @NotNull), dateFin (Date, @NotNull), salaire (Double, @Positive, @NotNull) |
| `DossierRHDTO` | idDossier, infosPerso (@NotBlank), diplomes (@NotBlank), documents (@NotBlank) |
| `EvaluationDTO` | idEval, objectifs (@NotBlank), kpi (@NotBlank) |
| `FormationDTO` | idFormation, titre (@NotBlank), certification (@NotBlank) |
| `PaieDTO` | idPaie, montant (Double, @Positive), bulletinPDF (@NotBlank) |
| `PlanningDTO` | idPlanning, horaires (@NotBlank) |
| `PosteDTO` | idPoste, titre (@NotBlank), competencesRequises (@NotBlank) |
| `RecrutementDTO` | idRecrutement, posteCible (@NotBlank), statut (@NotBlank) |
| `ResponsableRHDTO` | idResponsable, nom (@NotBlank) |

---

## Validation Rules

| Field | Constraint |
|---|---|
| `nom`, `prenom`, `titre`, `type`, `horaires`, `statut`, etc. | `@NotBlank` |
| `email` | `@Email`, `@NotBlank` |
| `salaire` (Contrat) | `@NotNull`, `@Positive` |
| `montant` (Paie) | `@Positive` |
| `dateDebut` (Conge) | `@NotNull`, `@PastOrPresent` |
| `dateFin` | `@NotNull` |

---

## Repositories (13 total)

All repositories extend `JpaRepository<Entity, Integer>`. No custom queries are defined.

| Repository | Entity | Custom Queries |
|---|---|---|
| `EmployeRepository` | `Employe` | None |
| `CandidateRepository` | `Candidate` | None |
| `CompetenceRepository` | `Competence` | None |
| `CongeRepository` | `Conge` | None |
| `ContratRepository` | `Contrat` | None |
| `DossierRHRepository` | `DossierRH` | None |
| `EvaluationRepository` | `Evaluation` | None |
| `FormationRepository` | `Formation` | None |
| `PaieRepository` | `Paie` | None |
| `PlanningRepository` | `Planning` | None |
| `PosteRepository` | `Poste` | None |
| `RecrutementRepository` | `Recrutement` | None |
| `ResponsableRHRepository` | `ResponsableRH` | None |

Inherited standard methods: `findAll`, `findById`, `save`, `deleteById`, `count`, `existsById`, etc.

---

## Services (13 total)

All services are `@Service` classes using constructor injection. Every service exposes exactly these 5 methods — thin delegates to the repository with no business logic.

| Method | Description |
|---|---|
| `create(Entity e)` | calls `repo.save(e)`, returns entity |
| `list()` | calls `repo.findAll()`, returns `List<Entity>` |
| `get(Integer id)` | calls `repo.findById(id)`, returns `Optional<Entity>` |
| `update(Entity e)` | calls `repo.save(e)`, returns entity |
| `delete(Integer id)` | calls `repo.deleteById(id)`, void |

No `@Transactional` annotations. No business logic.

---

## API Endpoints (65 total)

### Standard CRUD Pattern (applied to all 13 controllers)

| Method | Path | Request Body | Response | Description |
|---|---|---|---|---|
| GET | `/api/{resource}` | — | 200 `List<DTO>` | List all records |
| POST | `/api/{resource}` | `@Valid DTO` (JSON) | 201 `DTO` | Create new record |
| GET | `/api/{resource}/{id}` | — | 200 `DTO` / 404 | Get by ID |
| PUT | `/api/{resource}/{id}` | `@Valid DTO` (JSON) | 200 `DTO` / 404 | Full update |
| DELETE | `/api/{resource}/{id}` | — | 204 / 404 | Delete |

### Controller Base Paths

| Controller | Base Path |
|---|---|
| `EmployeController` | `/api/employes` |
| `CandidateController` | `/api/candidates` |
| `CompetenceController` | `/api/competences` |
| `CongeController` | `/api/conges` |
| `ContratController` | `/api/contrats` |
| `DossierRHController` | `/api/dossiers` |
| `EvaluationController` | `/api/evaluations` |
| `FormationController` | `/api/formations` |
| `PaieController` | `/api/paies` |
| `PlanningController` | `/api/plannings` |
| `PosteController` | `/api/postes` |
| `RecrutementController` | `/api/recrutements` |
| `ResponsableRHController` | `/api/responsables` |

### Debug Controller — `/api/debug`

| Method | Path | Body | Response | Description |
|---|---|---|---|---|
| POST | `/api/debug/test` | Raw String | String | Echoes raw request body |
| POST | `/api/debug/candidate` | CandidateDTO (no @Valid) | String | Returns DTO fields without validation |
| POST | `/api/debug/candidate-valid` | @Valid CandidateDTO | String | Returns DTO fields with validation |

> **Note:** This controller is a development artifact and should not be present in production.

---

## Mappers (13 total)

All mappers are MapStruct interfaces annotated with `@Mapper(componentModel = "spring")`, registered as Spring beans. No `@Mapping` customizations — auto-mapping by field name convention.

| Mapper | Entity | DTO | Methods |
|---|---|---|---|
| `EmployeMapper` | `Employe` | `EmployeDTO` | `toDTO(Employe)`, `toEntity(EmployeDTO)` |
| `CandidateMapper` | `Candidate` | `CandidateDTO` | `toDTO(Candidate)`, `toEntity(CandidateDTO)` |
| `CompetenceMapper` | `Competence` | `CompetenceDTO` | `toDTO(Competence)`, `toEntity(CompetenceDTO)` |
| `CongeMapper` | `Conge` | `CongeDTO` | `toDTO(Conge)`, `toEntity(CongeDTO)` |
| `ContratMapper` | `Contrat` | `ContratDTO` | `toDTO(Contrat)`, `toEntity(ContratDTO)` |
| `DossierRHMapper` | `DossierRH` | `DossierRHDTO` | `toDTO(DossierRH)`, `toEntity(DossierRHDTO)` |
| `EvaluationMapper` | `Evaluation` | `EvaluationDTO` | `toDTO(Evaluation)`, `toEntity(EvaluationDTO)` |
| `FormationMapper` | `Formation` | `FormationDTO` | `toDTO(Formation)`, `toEntity(FormationDTO)` |
| `PaieMapper` | `Paie` | `PaieDTO` | `toDTO(Paie)`, `toEntity(PaieDTO)` |
| `PlanningMapper` | `Planning` | `PlanningDTO` | `toDTO(Planning)`, `toEntity(PlanningDTO)` |
| `PosteMapper` | `Poste` | `PosteDTO` | `toDTO(Poste)`, `toEntity(PosteDTO)` |
| `RecrutementMapper` | `Recrutement` | `RecrutementDTO` | `toDTO(Recrutement)`, `toEntity(RecrutementDTO)` |
| `ResponsableRHMapper` | `ResponsableRH` | `ResponsableRHDTO` | `toDTO(ResponsableRH)`, `toEntity(ResponsableRHDTO)` |

---

## Configuration

### Database — `application.properties`

```properties
spring.datasource.url=jdbc:h2:mem:hrmsdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
spring.datasource.driverClassName=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.jpa.hibernate.ddl-auto=update
server.port=8080
```

- H2 in-memory database named `hrmsdb`
- Data persists only within the JVM session
- Hibernate auto-generates/alters tables on startup (`ddl-auto=update`)
- MySQL connector (8.0.33) is in the classpath but its config is commented out

### Jackson — `JacksonConfig.java`

- `FAIL_ON_UNKNOWN_PROPERTIES = false`
- Unknown JSON fields in incoming requests are silently ignored (lenient deserialization)

### Global Exception Handler — `GlobalExceptionHandler.java`

| Exception | HTTP Status | Response Body |
|---|---|---|
| `MethodArgumentNotValidException` | 400 | `{field: message}` map of all field errors |
| `ConstraintViolationException` | 400 | `{propertyPath: message}` map |
| `PersistenceException` | 400 | constraint map or `{error: raw message}` |
| Generic `Exception` (fallback) | 500 | `{error, message, status: "500"}` |

### Entry Point — `HrmsApplication.java`

```java
@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.example.hrms.repository")
```

`@EnableJpaRepositories` is explicit but technically redundant given `@SpringBootApplication` already scans the full base package.

---

## Dependencies

| Dependency | Version | Scope | Role |
|---|---|---|---|
| `spring-boot-starter-web` | managed (2.7.12) | compile | REST API, embedded Tomcat |
| `spring-boot-starter-data-jpa` | managed | compile | Hibernate ORM, repositories |
| `spring-boot-starter-validation` | managed | compile | Bean Validation |
| `h2` | managed | runtime | In-memory database |
| `mysql-connector-java` | 8.0.33 | runtime | MySQL JDBC (unused) |
| `lombok` | 1.18.30 | provided | Boilerplate reduction |
| `mapstruct` | 1.5.3.Final | compile | Entity ↔ DTO mapping |
| `spring-boot-starter-test` | managed | test | JUnit 5, Mockito, Spring Test |

**Build:** `maven-compiler-plugin` 3.11.0 with both MapStruct and Lombok configured as `annotationProcessorPaths` (correct order for compatibility).

---

## What Is Implemented vs What Is Missing

### Implemented

- Full CRUD REST API for all 13 HR domain entities
- JPA entity model with complete relationship mapping (OneToMany, ManyToMany, ManyToOne, OneToOne)
- DTO layer with Bean Validation constraints
- MapStruct entity ↔ DTO mapping
- Global exception handling with structured JSON error responses
- H2 in-memory database with automatic DDL generation
- Lenient Jackson deserialization

### Gaps and Missing Features

| Gap | Detail |
|---|---|
| **No Authentication / Authorization** | Zero security. All 65 endpoints are publicly accessible. No Spring Security, no JWT, no roles or permissions. |
| **No relationship management via API** | DTOs are flat — it is not possible to assign a `Contrat` to an `Employe` or link a `Candidate` to a `Recrutement` through the API. |
| **No PATCH support** | Only full PUT updates are available. Partial updates require sending the full object. |
| **No filtering, search, or pagination** | No query parameters, no `@RequestParam`, no `Pageable`. Only full list retrieval. |
| **No tests** | `spring-boot-starter-test` is included but zero test classes exist in the source tree. |
| **No `@Transactional`** | No transaction management on any service method. |
| **Debug endpoints in production code** | `/api/debug/*` are development artifacts that should be removed. |
| **Naming inconsistency** | `RecruitmentService` is English while all other classes use French names (`Recrutement`, `RecrutementController`, etc.). |
| **In-memory database only** | All data is lost on application restart. MySQL is configured but not activated. |
| **No audit fields** | No `createdAt`, `updatedAt`, `createdBy` tracking on any entity. |
| **No pagination/sorting on list endpoints** | All `GET /api/{resource}` return the full table with no limit. |

---

*Review generated: 2026-03-08*
