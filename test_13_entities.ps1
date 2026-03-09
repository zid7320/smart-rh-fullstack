$ProgressPreference = 'SilentlyContinue'

Write-Output "`n========== COMPREHENSIVE HRMS BACKEND TEST==========`n"
Write-Output "Date: $(Get-Date)`n"

function TestEntity {
    param([string]$name, [string]$endpoint, [hashtable]$data)
    
    try {
        $body = $data | ConvertTo-Json
        $post = Invoke-WebRequest -Uri "http://localhost:8080/api/$endpoint" -Method POST -Headers @{"Content-Type"="application/json"} -Body $body -UseBasicParsing
        $postStatus = $post.StatusCode
        $postData = $post.Content | ConvertFrom-Json
        $id = $postData | Get-Member -MemberType NoteProperty | Where-Object {$_.Name -match "id"} | ForEach-Object {$postData.$($_.Name)} | Select-Object -First 1
        
        $get = Invoke-RestMethod -Uri "http://localhost:8080/api/$endpoint" -Method GET
        $getStatus = 200
        $count = if ($get -is [array]) {$get.Count} else {1}
        
        Write-Output "✅ $name"
        Write-Output "   POST: $postStatus (Created)"
        Write-Output "   GET: $getStatus (Total: $count records)"
        Write-Output ""
    } catch {
        Write-Output "❌ $name - Error: $($_.Exception.Message)"
        Write-Output ""
    }
}

# Test all 13 entities
TestEntity "Candidates" "candidates" @{nom="Alice"; prenom="Wonder"; email="alice@test.com"}
TestEntity "Employes" "employes" @{nom="Bob"; prenom="Builder"; poste="Chef"; email="bob@test.com"}
TestEntity "Postes" "postes" @{titre="Manager"; competencesRequises="Leadership"}
TestEntity "Competences" "competences" @{nom="Java"; niveau="Expert"}
TestEntity "Conges" "conges" @{type="Vacation"; dateDebut="2026-01-01"; dateFin="2026-01-10"}
TestEntity "Contrats" "contrats" @{type="CDI"; dateDebut="2026-01-01"; dateFin="2027-01-01"; salaire=50000}
TestEntity "Dossiers RH" "dossiers" @{typeDocument="CV"; cheminFichier="/path/to/cv.pdf"}
TestEntity "Evaluations" "evaluations" @{objectifs="Reach 100% target"; kpi="Sales"; dateEvaluation="2026-01-01"}
TestEntity "Formations" "formations" @{titre="Spring Boot"; certification="Oracle"; dateDebut="2026-01-01"; dateFin="2026-02-01"}
TestEntity "Paies" "paies" @{mois="January"; montant=5000; datePaie="2026-01-31"}
TestEntity "Plannings" "plannings" @{horaires="9-17"; dateDebut="2026-01-01T09:00:00"; dateFin="2026-01-01T17:00:00"}
TestEntity "Recrutements" "recrutements" @{posteCible="Developer"; statut="Open"}
TestEntity "Responsables RH" "responsables" @{nom="Charlie"}

Write-Output "========== END OF TEST =========="
