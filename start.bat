@echo off
setlocal
title SentinelDesk AI Launcher
cd /d "%~dp0"

echo ===================================================
echo        Starting SentinelDesk AI Platform
echo ===================================================
echo.

:: 1. Free ports 5173 and 8080 from any lingering processes
for /f "tokens=5" %%a in ('netstat -aon ^| findstr :5173 ^| findstr LISTENING') do (
    echo Freeing port 5173 (PID %%a)...
    taskkill /F /PID %%a >nul 2>&1
)

for /f "tokens=5" %%a in ('netstat -aon ^| findstr :8080 ^| findstr LISTENING') do (
    echo Freeing port 8080 (PID %%a)...
    taskkill /F /PID %%a >nul 2>&1
)

echo.
echo [1/3] Launching Backend (Spring Boot 3.3.4, Java 21)...
if exist "backend\target\sentineldesk-backend-1.0.0.jar" (
    start "SentinelDesk AI - Backend" /d "backend" cmd /k "java -jar target\sentineldesk-backend-1.0.0.jar"
) else (
    start "SentinelDesk AI - Backend" /d "backend" cmd /k "mvnw.cmd spring-boot:run"
)

echo [2/3] Launching Frontend (React, Vite, TypeScript)...
start "SentinelDesk AI - Frontend" /d "frontend" cmd /k "npm run dev"

echo [3/3] Opening browser at http://localhost:5173 in 8 seconds...
timeout /t 8 /nobreak >nul
start http://localhost:5173

echo.
echo ===================================================
echo SentinelDesk AI is now running!
echo - Web UI:  http://localhost:5173
echo - Backend: http://localhost:8080
echo - Swagger: http://localhost:8080/swagger-ui.html
echo ===================================================
echo You can close this window.
timeout /t 5 >nul
