# Complete Full-Stack SMART RH 4.0 Launcher
# Starts both Backend (Docker) and Frontend (Angular) in one command

param(
    [switch]$SkipDocker,
    [switch]$SkipFrontend,
    [switch]$Rebuild,
    [ValidateSet("quick", "detailed")][string]$Mode = "quick"
)

# Colors
$SUCCESS = "Green"
$ERROR = "Red"
$INFO = "Cyan"
$WARNING = "Yellow"

function Write-Info($msg) {
    Write-Host "ℹ️  $msg" -ForegroundColor $INFO
}

function Write-Success($msg) {
    Write-Host "✅ $msg" -ForegroundColor $SUCCESS
}

function Write-Error-Custom($msg) {
    Write-Host "❌ $msg" -ForegroundColor $ERROR
}

function Write-Warning-Custom($msg) {
    Write-Host "⚠️  $msg" -ForegroundColor $WARNING
}

# Get workspace root
$ROOT = "C:\Users\MSI\Desktop\nouveau !!!"
$BACKEND_DIR = $ROOT
$FRONTEND_DIR = "$ROOT\frontend"

Clear-Host
Write-Host "╔════════════════════════════════════════════════════╗" -ForegroundColor Cyan
Write-Host "║  🚀 SMART RH 4.0 - Full-Stack Launcher             ║" -ForegroundColor Cyan
Write-Host "║     Backend (Docker) + Frontend (Angular)          ║" -ForegroundColor Cyan
Write-Host "╚════════════════════════════════════════════════════╝" -ForegroundColor Cyan
Write-Host ""

# === VALIDATION ===
Write-Info "Validating environment..."

# Check Docker
try {
    $dockerVersion = docker --version 2>$null
    if (-not $dockerVersion) {
        throw "Docker not found"
    }
    Write-Success "Docker detected: $dockerVersion"
} catch {
    Write-Warning-Custom "Docker not found. Backend will NOT start."
    $SkipDocker = $true
}

# Check Docker Desktop running
try {
    $ps = Get-Process "Docker Desktop" -ErrorAction SilentlyContinue
    if (-not $ps) {
        Write-Warning-Custom "Docker Desktop is not running. Starting Docker Desktop..."
        Start-Process "C:\Program Files\Docker\Docker\Docker.exe" -WindowStyle Minimized
        Write-Info "⏳ Waiting 30 seconds for Docker Desktop to start..."
        Start-Sleep -Seconds 30
    } else {
        Write-Success "Docker Desktop is running"
    }
} catch {
    Write-Warning-Custom "Could not verify Docker Desktop status"
}

# Check Node.js
try {
    $nodeVersion = node --version 2>$null
    if (-not $nodeVersion) {
        throw "Node.js not found"
    }
    Write-Success "Node.js detected: $nodeVersion"
} catch {
    Write-Error-Custom "Node.js not found. Cannot start frontend."
    $SkipFrontend = $true
}

# Check npm
try {
    $npmVersion = npm --version 2>$null
    if (-not $npmVersion) {
        throw "npm not found"
    }
    Write-Success "npm detected: $npmVersion"
} catch {
    Write-Error-Custom "npm not found"
    $SkipFrontend = $true
}

# Check directories
if (-not (Test-Path $BACKEND_DIR)) {
    Write-Error-Custom "Backend directory not found: $BACKEND_DIR"
    exit 1
}
Write-Success "Backend directory found"

if (-not (Test-Path $FRONTEND_DIR)) {
    Write-Error-Custom "Frontend directory not found: $FRONTEND_DIR"
    $SkipFrontend = $true
}
Write-Success "Frontend directory found"

Write-Host ""

# === STARTING DOCKER (if not skipped) ===
if (-not $SkipDocker) {
    Write-Host "╔════════════════════════════════════════════════════╗" -ForegroundColor Yellow
    Write-Host "║  🐳 Starting Backend (Docker Compose)              ║" -ForegroundColor Yellow
    Write-Host "╚════════════════════════════════════════════════════╝" -ForegroundColor Yellow
    
    # Check if containers already running
    try {
        $running = docker-compose -f "$BACKEND_DIR\docker-compose.yml" ps -q 2>$null
        if ($running) {
            Write-Warning-Custom "Docker containers already running."
            $choice = Read-Host "Restart them? (y/n)"
            if ($choice -eq 'y') {
                Write-Info "Stopping existing containers..."
                docker-compose -f "$BACKEND_DIR\docker-compose.yml" down
                Start-Sleep -Seconds 3
            }
        }
    } catch {
        Write-Info "No existing containers found"
    }
    
    # Build and start
    Write-Info "Building and starting Docker containers..."
    Write-Info "This may take 2-5 minutes on first run..."
    
    $buildCmd = if ($Rebuild) {
        "docker-compose -f `"$BACKEND_DIR\docker-compose.yml`" up --build -d"
    } else {
        "docker-compose -f `"$BACKEND_DIR\docker-compose.yml`" up -d"
    }
    
    $output = Invoke-Expression $buildCmd 2>&1
    
    if ($LASTEXITCODE -eq 0) {
        Write-Success "Docker containers started successfully"
        
        # Wait for services to be healthy
        Write-Info "⏳ Waiting for services to be ready (up to 60 seconds)..."
        $maxAttempts = 60
        $attempts = 0
        $healthy = $false
        
        while ($attempts -lt $maxAttempts) {
            try {
                $health = docker-compose -f "$BACKEND_DIR\docker-compose.yml" ps 2>$null | Select-String "healthy|running"
                if ($health -match "healthy") {
                    $healthy = $true
                    break
                }
                $attempts++
                Start-Sleep -Seconds 1
            } catch {
                $attempts++
                Start-Sleep -Seconds 1
            }
        }
        
        if ($healthy) {
            Write-Success "All services are healthy!"
        } else {
            Write-Warning-Custom "Services may still be starting. Check logs if needed."
        }
        
        # Show service status
        if ($Mode -eq "detailed") {
            Write-Info "Service Status:"
            docker-compose -f "$BACKEND_DIR\docker-compose.yml" ps
        }
        
        Write-Success "Backend is available at: http://localhost:8081"
        Write-Info "API Health Check: http://localhost:8081/api/health"
        Write-Info "Swagger UI: http://localhost:8081/swagger-ui.html"
    } else {
        Write-Error-Custom "Failed to start Docker containers"
        Write-Info "Last error output:"
        Write-Host $output -ForegroundColor Red
    }
    Write-Host ""
}

# === STARTING FRONTEND (if not skipped) ===
if (-not $SkipFrontend) {
    Write-Host "╔════════════════════════════════════════════════════╗" -ForegroundColor Magenta
    Write-Host "║  🌐 Starting Frontend (Angular)                    ║" -ForegroundColor Magenta
    Write-Host "╚════════════════════════════════════════════════════╝" -ForegroundColor Magenta
    
    # Check if node_modules needs installing
    $nodeModulesPath = "$FRONTEND_DIR\node_modules"
    if (-not (Test-Path $nodeModulesPath)) {
        Write-Warning-Custom "node_modules not found. Running npm install..."
        Write-Info "This will take 3-5 minutes on first run..."
        
        Push-Location $FRONTEND_DIR
        npm install --legacy-peer-deps 2>&1
        Pop-Location
        
        if ($LASTEXITCODE -eq 0) {
            Write-Success "npm install completed successfully"
        } else {
            Write-Error-Custom "npm install failed"
            $SkipFrontend = $true
        }
    } else {
        Write-Success "node_modules already exist"
    }
    
    if (-not $SkipFrontend) {
        Write-Info "Starting Angular development server on http://localhost:4200..."
        
        # Start npm in a new process (visible window)
        $npmStartScript = {
            param($FrontendDir)
            Push-Location $FrontendDir
            npm start
        }
        
        # This will open a new window
        Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd `'$FRONTEND_DIR`'; npm start" `
            -WindowStyle Normal
        
        Write-Success "Frontend starting in new window..."
        Write-Warning-Custom "Frontend window is opening. Please wait for 'server is listening' message"
        Start-Sleep -Seconds 5
    }
    Write-Host ""
}

# === SUMMARY ===
Write-Host "╔════════════════════════════════════════════════════╗" -ForegroundColor Green
Write-Host "║  📋 Full-Stack Launcher Summary                    ║" -ForegroundColor Green
Write-Host "╚════════════════════════════════════════════════════╝" -ForegroundColor Green
Write-Host ""

if (-not $SkipDocker) {
    Write-Success "✅ Backend (Docker):              http://localhost:8081"
    Write-Host "   └─ MySQL Database:           localhost:3307"
    Write-Host "   └─ MQTT Broker:              localhost:1883"
    Write-Host "   └─ Swagger UI:               http://localhost:8081/swagger-ui.html"
} else {
    Write-Error-Custom "❌ Backend (Docker):              SKIPPED"
}

Write-Host ""

if (-not $SkipFrontend) {
    Write-Success "✅ Frontend (Angular):           http://localhost:4200"
    Write-Host "   └─ Development Server:      Check new PowerShell window"
    Write-Host "   └─ Hot Reload:               Enabled"
} else {
    Write-Error-Custom "❌ Frontend (Angular):            SKIPPED"
}

Write-Host ""
Write-Host "═══════════════════════════════════════════════════" -ForegroundColor Cyan
Write-Host ""

# === NEXT STEPS ===
Write-Info "Next Steps:"
Write-Host "1. Wait for both services to start completely"
Write-Host "2. Open browser: http://localhost:4200"
Write-Host "3. Login with your credentials"
Write-Host "4. Start developing!"
Write-Host ""

# === MONITORING ===
Write-Host "═══════════════════════════════════════════════════" -ForegroundColor Cyan
Write-Host "Monitor Backend (Docker) Logs:" -ForegroundColor Yellow
Write-Host "  docker-compose logs -f smart-rh-backend" -ForegroundColor Gray
Write-Host ""
Write-Host "Monitor MySQL Logs:" -ForegroundColor Yellow
Write-Host "  docker-compose logs -f smart-rh-mysql" -ForegroundColor Gray
Write-Host ""
Write-Host "Monitor All Services:" -ForegroundColor Yellow
Write-Host "  docker-compose logs -f" -ForegroundColor Gray
Write-Host ""
Write-Host "Stop All Services:" -ForegroundColor Yellow
Write-Host "  docker-compose down" -ForegroundColor Gray
Write-Host ""
Write-Host "═══════════════════════════════════════════════════" -ForegroundColor Cyan
Write-Host ""

# Keep terminal open
Write-Host "Press any key to continue monitoring..." -ForegroundColor Gray
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")

# Show live logs
if (-not $SkipDocker) {
    Write-Info "Showing live Docker logs. Press Ctrl+C to stop."
    Start-Sleep -Seconds 2
    docker-compose -f "$BACKEND_DIR\docker-compose.yml" logs -f
}
