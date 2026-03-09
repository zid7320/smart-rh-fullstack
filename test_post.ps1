Add-Type -AssemblyName "System.Net.Http"

$handler = New-Object System.Net.Http.HttpClientHandler
$client = New-Object System.Net.Http.HttpClient($handler)
$client.DefaultRequestHeaders.Add("Content-Type", "application/json")

$candidate = @{
    nom = "Smith"
    prenom = "Marie"
    email = "marie@example.com"
} | ConvertTo-Json

Write-Host "Request Body: $candidate" -ForegroundColor Cyan

$content = New-Object System.Net.Http.StringContent($candidate, [System.Text.Encoding]::UTF8, "application/json")
$response = $client.PostAsync("http://localhost:8080/api/candidates", $content).Result

Write-Host "Status Code: $($response.StatusCode)" -ForegroundColor Yellow
Write-Host "Status: $($response.StatusCode.ToString())" -ForegroundColor Yellow

$responseBody = $response.Content.ReadAsStringAsync().Result
Write-Host "Response Body: $responseBody" -ForegroundColor Green

$client.Dispose()
