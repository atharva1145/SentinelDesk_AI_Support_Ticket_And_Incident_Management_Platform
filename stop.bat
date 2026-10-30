@echo off
title Stop SentinelDesk AI
echo ===================================================
echo        Stopping SentinelDesk AI Platform
echo ===================================================
echo.

for /f "tokens=5" %%a in ('netstat -aon ^| findstr :5173 ^| findstr LISTENING') do (
    echo Stopping frontend process (PID %%a)...
    taskkill /F /PID %%a >nul 2>&1
)

for /f "tokens=5" %%a in ('netstat -aon ^| findstr :8080 ^| findstr LISTENING') do (
    echo Stopping backend process (PID %%a)...
    taskkill /F /PID %%a >nul 2>&1
)

echo.
echo All SentinelDesk AI services have been stopped.
timeout /t 3 >nul
