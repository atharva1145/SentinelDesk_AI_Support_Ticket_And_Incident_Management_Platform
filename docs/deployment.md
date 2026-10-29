# SentinelDesk AI — Deployment & Operations Guide

## 1. Local Development Startup

### Prerequisites
- Java 21 LTS
- Node.js 20+ & npm

### Backend Startup (H2 PostgreSQL Compatibility Mode)
No external database is required for local rapid prototyping.
```bash
# In project root:
mvn spring-boot:run -f backend/pom.xml
```
The backend initializes on port `8080`.
Flyway migrations `V1` and `V2` execute automatically, seeding default SLA policies and demo users.

### Frontend Startup
```bash
cd frontend
npm install
npm run dev
```
The frontend Vite server boots on `http://localhost:5173`.
Proxy rules route all `/api/*` requests automatically to `http://localhost:8080`.

---

## 2. Docker & Containerized Deployment

### Full Stack Startup via Docker Compose
```bash
# Copy template and customize credentials if desired
cp .env.example .env

# Build and start PostgreSQL, Spring Boot, and Nginx React containers
docker compose up -d --build
```

### Container Stack Architecture
- `postgres`: PostgreSQL 16 Alpine on internal `sentinel-net`. Port `5432` mapped. Data stored in `postgres_data` volume.
- `backend`: Spring Boot 3.3.4 running on Eclipse Temurin 21 JRE. Healthchecked via `/actuator/health`.
- `frontend`: React 18 production build served by Nginx Alpine. Port `3000` mapped.

### Verifying Service Health
```bash
# Check running containers
docker compose ps

# Check backend health
curl http://localhost:8080/actuator/health
```

---

## 3. Demo Credentials

| Role | Email | Password |
|---|---|---|
| **System Administrator** | `admin@sentineldesk.com` | `Password123!` |
| **Support Manager** | `manager@sentineldesk.com` | `Password123!` |
| **Senior Support Agent** | `agent1@sentineldesk.com` | `Password123!` |
| **Network Support Agent** | `agent2@sentineldesk.com` | `Password123!` |
| **Customer Requester** | `user@sentineldesk.com` | `Password123!` |

*(One-click instant login buttons are also provided directly on the `/login` screen).*
