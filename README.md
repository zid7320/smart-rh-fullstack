# HRMS Spring Boot Backend

A complete Spring Boot backend skeleton generated from a UML class diagram for a Human Resources Management System (HRMS).

## Project Structure

```
src/main/java/com/example/hrms/
├── model/           # JPA Entity classes (13 entities)
├── repository/      # JpaRepository interfaces
├── service/         # Business logic services
└── controller/      # REST API endpoints

src/main/resources/
├── application.properties  # Database configuration
```

## Entities (13 total)

- **Candidate** - Job applicants with many-to-many relationship to Recrutement
- **ResponsableRH** - HR manager with one-to-many relationship to Recrutement
- **Recrutement** - Recruitment process with relationships to Candidate, ResponsableRH, and Employe
- **Employe** - Employee with central role connecting to multiple entities
- **Poste** - Job position with many-to-many to Competence
- **Competence** - Skills/competencies (many-to-many with Poste and Employe)
- **Contrat** - Employment contract (one-to-many with Employe)
- **DossierRH** - HR file/dossier (one-to-one with Employe)
- **Planning** - Work schedule (one-to-many with Employe)
- **Conge** - Leave/vacation (one-to-many with Employe)
- **Paie** - Payroll (one-to-many with Employe)
- **Evaluation** - Performance evaluation (one-to-many with Employe)
- **Formation** - Training/formation (many-to-many with Employe)

## REST API Endpoints

### Candidates
- `GET /api/candidates` - List all candidates
- `POST /api/candidates` - Create new candidate
- `GET /api/candidates/{id}` - Get candidate by ID
- `PUT /api/candidates/{id}` - Update candidate
- `DELETE /api/candidates/{id}` - Delete candidate

### Employees
- `GET /api/employes` - List all employees
- `POST /api/employes` - Create new employee
- `GET /api/employes/{id}` - Get employee by ID
- `PUT /api/employes/{id}` - Update employee
- `DELETE /api/employes/{id}` - Delete employee

### Similar endpoints available for:
- `/api/responsables` - HR Managers
- `/api/recrutements` - Recruitment processes
- `/api/postes` - Job positions
- `/api/contrats` - Contracts
- `/api/dossiers` - HR dossiers
- `/api/plannings` - Work schedules
- `/api/conges` - Leave requests
- `/api/paies` - Payroll
- `/api/evaluations` - Performance evaluations
- `/api/formations` - Training programs
- `/api/competences` - Skills/competencies

## Database Configuration

Default: **H2 in-memory database**

To switch to MySQL:

Edit `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/hrmsdb?useSSL=false&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=your_password
spring.jpa.database-platform=org.hibernate.dialect.MySQL8Dialect
```

## Running the Application

```bash
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

## Testing the API

```bash
# Get all candidates
curl http://localhost:8080/api/candidates

# Create a new candidate
curl -X POST http://localhost:8080/api/candidates \
  -H "Content-Type: application/json" \
  -d '{"nom":"Dupont","prenom":"Jean","email":"jean@example.com"}'

# Get employee by ID
curl http://localhost:8080/api/employes/1

# Update employee
curl -X PUT http://localhost:8080/api/employes/1 \
  -H "Content-Type: application/json" \
  -d '{"nom":"Smith","prenom":"John","poste":"Developer","email":"john@example.com"}'
```

## Dependencies

- Spring Boot 2.7.12
- Spring Data JPA
- H2 Database (runtime)
- MySQL Connector (optional)
- Lombok (provided)

## Notes

- All entities use Lombok `@Data` and `@NoArgsConstructor` annotations to reduce boilerplate
- Relationships use appropriate fetch strategies (`LAZY` by default, `EAGER` for OneToOne)
- Cascade type `CascadeType.ALL` applied to maintain referential integrity
- `@GeneratedValue(strategy = GenerationType.IDENTITY)` for auto-increment IDs

## IDE Setup

Ensure Lombok support is enabled in your IDE:
- IntelliJ IDEA: Enable annotation processing in Settings → Build → Compiler → Annotation Processors
- VS Code: Install the Lombok Extension Pack
