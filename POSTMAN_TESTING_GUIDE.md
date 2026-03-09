# HRMS Backend - Postman Test Guide

**Backend URL:** `http://localhost:8080`

---

## 📋 CRUD Test Examples by Entity

### 1. CANDIDATES

#### GET - List All Candidates
```
Method: GET
URL: http://localhost:8080/api/candidates
```

#### GET - Get Specific Candidate
```
Method: GET
URL: http://localhost:8080/api/candidates/1
```

#### POST - Create New Candidate
```
Method: POST
URL: http://localhost:8080/api/candidates
Content-Type: application/json

{
  "nom": "Dupont",
  "prenom": "Marie",
  "email": "marie.dupont@example.com"
}
```

#### PUT - Update Candidate
```
Method: PUT
URL: http://localhost:8080/api/candidates/1
Content-Type: application/json

{
  "nom": "Dupont",
  "prenom": "Marie",
  "email": "marie.dupont.updated@example.com"
}
```

#### DELETE - Delete Candidate
```
Method: DELETE
URL: http://localhost:8080/api/candidates/1
```

---

### 2. EMPLOYEES (Employes)

#### GET - List All Employees
```
Method: GET
URL: http://localhost:8080/api/employes
```

#### GET - Get Specific Employee
```
Method: GET
URL: http://localhost:8080/api/employes/1
```

#### POST - Create New Employee
```
Method: POST
URL: http://localhost:8080/api/employes
Content-Type: application/json

{
  "nom": "Martin",
  "prenom": "Pierre",
  "poste": "Software Engineer",
  "email": "pierre.martin@example.com"
}
```

#### PUT - Update Employee
```
Method: PUT
URL: http://localhost:8080/api/employes/1
Content-Type: application/json

{
  "nom": "Martin",
  "prenom": "Pierre",
  "poste": "Senior Software Engineer",
  "email": "pierre.martin@example.com"
}
```

#### DELETE - Delete Employee
```
Method: DELETE
URL: http://localhost:8080/api/employes/1
```

---

### 3. LEAVE REQUESTS (Conges)

#### POST - Create Leave Request
```
Method: POST
URL: http://localhost:8080/api/conges
Content-Type: application/json

{
  "type": "Vacances",
  "dateDebut": "2026-03-10",
  "dateFin": "2026-03-20"
}
```

#### GET - List All Leave Requests
```
Method: GET
URL: http://localhost:8080/api/conges
```

---

### 4. CONTRACTS (Contrats)

#### POST - Create Contract
```
Method: POST
URL: http://localhost:8080/api/contrats
Content-Type: application/json

{
  "type": "CDI",
  "dateDebut": "2026-01-01",
  "dateFin": "2027-01-01"
}
```

#### GET - List All Contracts
```
Method: GET
URL: http://localhost:8080/api/contrats
```

---

### 5. POSITIONS (Postes)

#### POST - Create Position
```
Method: POST
URL: http://localhost:8080/api/postes
Content-Type: application/json

{
  "titre": "Développeur Java",
  "competencesRequises": "Java, Spring, SQL"
}
```

#### GET - List All Positions
```
Method: GET
URL: http://localhost:8080/api/postes
```

---

### 6. COMPETENCIES (Competences)

#### POST - Create Competency
```
Method: POST
URL: http://localhost:8080/api/competences
Content-Type: application/json

{
  "nom": "Java",
  "niveau": "Avancé"
}
```

#### GET - List All Competencies
```
Method: GET
URL: http://localhost:8080/api/competences
```

---

### 7. TRAINING (Formations)

#### POST - Create Training
```
Method: POST
URL: http://localhost:8080/api/formations
Content-Type: application/json

{
  "titre": "Spring Boot Advanced",
  "duree": 40
}
```

#### GET - List All Training
```
Method: GET
URL: http://localhost:8080/api/formations
```

---

### 8. PAYROLL (Paies)

#### POST - Create Payroll
```
Method: POST
URL: http://localhost:8080/api/paies
Content-Type: application/json

{
  "montant": 3500.00,
  "mois": "Mars"
}
```

#### GET - List All Payroll
```
Method: GET
URL: http://localhost:8080/api/paies
```

---

### 9. PLANNING (Plannings)

#### POST - Create Planning
```
Method: POST
URL: http://localhost:8080/api/plannings
Content-Type: application/json

{
  "date": "2026-03-10",
  "heures": 8
}
```

#### GET - List All Planning
```
Method: GET
URL: http://localhost:8080/api/plannings
```

---

### 10. EVALUATION (Evaluations)

#### POST - Create Evaluation
```
Method: POST
URL: http://localhost:8080/api/evaluations
Content-Type: application/json

{
  "note": 8,
  "commentaires": "Bon travail"
}
```

#### GET - List All Evaluations
```
Method: GET
URL: http://localhost:8080/api/evaluations
```

---

### 11. HR DOSSIER (DossierRH)

#### POST - Create HR Dossier
```
Method: POST
URL: http://localhost:8080/api/dossiers
Content-Type: application/json

{
  "numeroControl": "DOS-001"
}
```

#### GET - List All HR Dossiers
```
Method: GET
URL: http://localhost:8080/api/dossiers
```

---

### 12. RECRUITMENT (Recrutements)

#### POST - Create Recruitment
```
Method: POST
URL: http://localhost:8080/api/recrutements
Content-Type: application/json

{
  "poste": "Senior Developer",
  "datePublication": "2026-03-01"
}
```

#### GET - List All Recruitments
```
Method: GET
URL: http://localhost:8080/api/recrutements
```

---

### 13. HR MANAGERS (ResponsableRH)

#### POST - Create HR Manager
```
Method: POST
URL: http://localhost:8080/api/responsables
Content-Type: application/json

{
  "nom": "Durrand",
  "prenom": "Alice",
  "email": "alice.durrand@example.com"
}
```

#### GET - List All HR Managers
```
Method: GET
URL: http://localhost:8080/api/responsables
```

---

## ✅ Expected HTTP Status Codes

| Operation | Status | Description |
|-----------|--------|-------------|
| GET (success) | 200 | Resource found and returned |
| GET (fail) | 404 | Resource not found |
| POST (success) | 201 | Resource created successfully |
| POST (fail) | 400 | Bad request (validation error) |
| PUT (success) | 200 | Resource updated successfully |
| PUT (fail) | 404 | Resource not found |
| DELETE (success) | 204 | No content (resource deleted) |
| DELETE (fail) | 404 | Resource not found |

---

## 🚨 Validation Error Responses

When validation fails, you'll receive HTTP 405/400 with error details:

```json
{
  "timestamp": "2026-03-02T15:30:00.000+00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed for object='candidateDTO'",
  "path": "/api/candidates"
}
```

---

## 📝 Common Issues & Solutions

### Issue: 404 Error on Endpoint
- **Cause:** Incorrect URL or missing `/api/` prefix
- **Solution:** Check spelling and ensure `/api/` is included

### Issue: 400 Bad Request
- **Cause:** Invalid JSON or missing required fields
- **Solution:** Verify JSON syntax and required fields are included

### Issue: 201 vs 200 on POST
- **201:** Correct - resource created
- **200:** May indicate update instead of create

### Issue: Connection Refused
- **Cause:** Backend not running
- **Solution:** Ensure `mvn spring-boot:run` is executed and server is on port 8080

---

## 🔧 Quick Postman Setup

1. **Create New Collection:** "HRMS API"
2. **Set Base URL Variable:**
   - Variable Name: `{{baseUrl}}`
   - Value: `http://localhost:8080`

3. **Use in Requests:**
   - Instead of full URL, use: `{{baseUrl}}/api/candidates`

4. **Test All CRUD for Each Entity:**
   - Create request for each HTTP method
   - Group by entity folder
   - Run tests sequentially

---

## 📊 Sample Testing Workflow

```
1. POST /api/candidates → Create candidate (save ID = 1)
2. GET /api/candidates → Verify candidate in list
3. GET /api/candidates/1 → Get specific candidate
4. PUT /api/candidates/1 → Update candidate
5. GET /api/candidates/1 → Verify update
6. DELETE /api/candidates/1 → Delete candidate
7. GET /api/candidates/1 → Verify 404 (not found)
```

Repeat this workflow for all 13 entities to ensure full CRUD compliance.

---

## 🎯 Validation Constraints by Entity

### Candidate
- `nom`: @NotBlank (required)
- `prenom`: @NotBlank (required)
- `email`: @Email, @NotBlank (required, valid email)

### Employe
- `nom`: @NotBlank (required)
- `prenom`: @NotBlank (required)
- `poste`: @NotBlank (required)
- `email`: @Email, @NotBlank (required, valid email)

### Conge
- `type`: @NotBlank (required)
- `dateDebut`: @NotNull, @PastOrPresent (required, must be past or today)
- `dateFin`: @NotNull (required)

### Competence
- `nom`: @NotBlank (required)
- `niveau`: @NotBlank (required)

### Poste
- `titre`: @NotBlank (required)
- `competencesRequises`: @NotBlank (required)

### ResponsableRH
- `nom`: @NotBlank (required)
- `prenom`: @NotBlank (required)
- `email`: @Email, @NotBlank (required, valid email)

---

## 🚀 Performance Testing Tips

- Start with small payloads
- Test with 100+ records to check list performance
- Monitor database connections
- Check response time for GET operations
- Verify cascading deletes work correctly

---

**Backend is Ready for Full CRUD Testing via Postman** ✅
