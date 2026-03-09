$ProgressPreference = 'SilentlyContinue'

Write-Output "======== Testing HRMS Backend - Complete CRUD ========`n"

# Test Candidates CRUD
Write-Output "TEST 1: Candidates"
$body1 = @{nom="Alice"; prenom="Wonder"; email="alice@test.com"} | ConvertTo-Json
$post1 = Invoke-WebRequest -Uri "http://localhost:8080/api/candidates" -Method POST -Headers @{"Content-Type"="application/json"} -Body $body1 -UseBasicParsing
Write-Output "POST: $($post1.StatusCode) Created - ID: $(($post1.Content | ConvertFrom-Json).idCandidate)"
$get1 = Invoke-RestMethod -Uri "http://localhost:8080/api/candidates"
Write-Output "GET ALL: 200 - Count: $($get1.Count)`n"

# Test Employes CRUD
Write-Output "TEST 2: Employes"
$body2 = @{nom="Bob"; prenom="Builder"; poste="Chef"; email="bob@test.com"} | ConvertTo-Json
try {
    $post2 = Invoke-WebRequest -Uri "http://localhost:8080/api/employes" -Method POST -Headers @{"Content-Type"="application/json"} -Body $body2 -UseBasicParsing
    Write-Output "POST: $($post2.StatusCode) Created"
    $get2 = Invoke-RestMethod -Uri "http://localhost:8080/api/employes"
    Write-Output "GET ALL: 200 - Count: $($get2.Count)`n"
} catch {
    Write-Output "ERROR: $($_.Exception.Message)`n"
}

# Test Postes CRUD
Write-Output "TEST 3: Postes"
$body3 = @{titre="Manager"; competencesRequises="Leadership"} | ConvertTo-Json
try {
    $post3 = Invoke-WebRequest -Uri "http://localhost:8080/api/postes" -Method POST -Headers @{"Content-Type"="application/json"} -Body $body3 -UseBasicParsing
    Write-Output "POST: $($post3.StatusCode) Created"
    $get3 = Invoke-RestMethod -Uri "http://localhost:8080/api/postes"
    Write-Output "GET ALL: 200 - Count: $($get3.Count)`n"
} catch {
    Write-Output "ERROR: $($_.Exception.Message)`n"
}

# Test Competences CRUD
Write-Output "TEST 4: Competences"
$body4 = @{nom="Java"; niveau="Expert"} | ConvertTo-Json
try {
    $post4 = Invoke-WebRequest -Uri "http://localhost:8080/api/competences" -Method POST -Headers @{"Content-Type"="application/json"} -Body $body4 -UseBasicParsing
    Write-Output "POST: $($post4.StatusCode) Created"
    $get4 = Invoke-RestMethod -Uri "http://localhost:8080/api/competences"
    Write-Output "GET ALL: 200 - Count: $($get4.Count)`n"
} catch {
    Write-Output "ERROR: $($_.Exception.Message)`n"
}

# Test Conges CRUD
Write-Output "TEST 5: Conges" 
$body5 = @{type="Vacation"; dateDebut="2026-01-01"; dateFin="2026-01-10"} | ConvertTo-Json
try {
    $post5 = Invoke-WebRequest -Uri "http://localhost:8080/api/conges" -Method POST -Headers @{"Content-Type"="application/json"} -Body $body5 -UseBasicParsing
    Write-Output "POST: $($post5.StatusCode) Created"
    $get5 = Invoke-RestMethod -Uri "http://localhost:8080/api/conges"
    Write-Output "GET ALL: 200 - Count: $($get5.Count)`n"
} catch {
    Write-Output "ERROR: $($_.Exception.Message)`n"
}

Write-Output "======== Test Complete ========"
