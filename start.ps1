# SentinelDesk AI - PowerShell 1-Click Launcher
$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $scriptDir

Write-Host "===================================================" -ForegroundColor Cyan
Write-Host "        Starting SentinelDesk AI Platform         " -ForegroundColor Cyan
Write-Host "===================================================" -ForegroundColor Cyan
Write-Host ""

# 1. Clean up ports 5173 and 8080 if lingering
$p5173 = Get-NetTCPConnection -LocalPort 5173 -State Listen -ErrorAction SilentlyContinue
if ($p5173) {
    Write-Host "Stopping process on port 5173 (PID: $($p5173.OwningProcess))..." -ForegroundColor Yellow
    Stop-Process -Id $p5173.OwningProcess -Force -ErrorAction SilentlyContinue
}

$p8080 = Get-NetTCPConnection -LocalPort 8080 -State Listen -ErrorAction SilentlyContinue
if ($p8080) {
    Write-Host "Stopping process on port 8080 (PID: $($p8080.OwningProcess))..." -ForegroundColor Yellow
    Stop-Process -Id $p8080.OwningProcess -Force -ErrorAction SilentlyContinue
}

# 2. Launch Backend
Write-Host "[1/3] Launching Backend on http://localhost:8080..." -ForegroundColor Green
Start-Process powershell -ArgumentList "-NoExit", "-Command", "Set-Location '$scriptDir\backend'; .\mvnw.cmd spring-boot:run"

# 3. Launch Frontend
Write-Host "[2/3] Launching Frontend on http://localhost:5173..." -ForegroundColor Green
Start-Process powershell -ArgumentList "-NoExit", "-Command", "Set-Location '$scriptDir\frontend'; npm run dev"

# 4. Wait and Open Browser
Write-Host "[3/3] Waiting for servers to initialize..." -ForegroundColor Green
Start-Sleep -Seconds 6
Start-Process "http://localhost:5173"

Write-Host ""
Write-Host "SentinelDesk AI is now running!" -ForegroundColor Cyan
Write-Host " - Web UI:  http://localhost:5173" -ForegroundColor White
Write-Host " - Backend: http://localhost:8080" -ForegroundColor White
Write-Host " - Swagger: http://localhost:8080/swagger-ui.html" -ForegroundColor White
