# SentinelDesk AI — Complete Engineering Delivery Report

## Executive Summary
**SentinelDesk AI** is an enterprise-grade AI-powered Support Ticket & Incident Management Platform engineered with **Java 21 LTS**, **Spring Boot 3.3.4**, **PostgreSQL / Flyway**, **React 18**, **TypeScript**, and **Tailwind CSS**. 

The platform features an automated multi-priority SLA engine with ITIL clock pause/resume capabilities, idempotent background escalations, customer satisfaction (CSAT) ratings, AI agent copilot auto-drafting, pre-approved canned reply templates, multi-provider AI triage (OpenAI & Google Gemini) with deterministic fallback, real-time Server-Sent Events (SSE) toast alerts, IT knowledge base runbooks with semantic recommendations, inbound email webhook ingestion, and downloadable SLA compliance audit reports.

---

## 1. Core Architecture & Implemented Capabilities

### 1.1 ITIL-Compliant SLA Clock Pause / Resume Engine
- **State-Aware Timers**: When a ticket transitions to `PENDING_REQUESTER`, the SLA resolution timer is automatically frozen (`isPaused = true`, `pausedAt = now`).
- **Deadline Extension**: Upon transitioning back to active work (`IN_PROGRESS` or `RESOLVED`), the elapsed duration (`totalPausedMinutes`) is added to `resolutionDeadline` and synchronized to `ticket.slaDeadline`.
- **Escalation Suppression**: The background escalation scheduler checks `tracker.getIsPaused()` and actively suppresses SLA breach checks or warning events while waiting for the customer.
- **Visual Feedback**: Paused tickets display an animated amber `SLA Paused` badge in ticket tables and an explicit warning banner on the ticket detail view.

### 1.2 AI Agent Copilot & Thread Auto-Drafting
- **Dynamic Endpoint**: `POST /api/v1/tickets/{id}/ai-triage/copilot` accepting actions:
  - `DRAFT_RESOLUTION`: Formulates professional, courteous public replies detailing resolution steps and verification instructions.
  - `SUGGEST_STEPS`: Generates internal diagnostic checklists and step-by-step troubleshooting actions.
  - `SUMMARIZE`: Synthesizes complex multi-comment discussions into a concise executive brief.
- **Integrated Agent Toolbar**: Accessible above the comment reply box in `TicketDetails.tsx` for support agents and managers.

### 1.3 Customer Satisfaction (CSAT) Survey & Reopening Workflow
- **Requester Feedback Loop**: When tickets reach `RESOLVED`, requesters are presented with an interactive 5-star rating selector and feedback textarea.
- **Automated Lifecycle Completion**: Submitting a CSAT rating automatically transitions the ticket from `RESOLVED` to `CLOSED`.
- **Reopen Counter**: Tracks repeated reopen transitions (`reopenedCount`) when resolved tickets are reopened by users.
- **Executive Analytics**: Average CSAT score and verified rating counts are computed and visualized on the KPI dashboard.

### 1.4 Pre-Approved Canned Response Templates
- **Template Repository & REST API**: Stored in `canned_responses` with database schema migration (`V4__advanced_sla_csat_and_copilot.sql`).
- **Standard ITIL Macros**: Includes pre-seeded templates:
  - `/diagnostics` (Initial Diagnostics & Log Request)
  - `/vpn-reset` (VPN Gateway Connection Reset Instructions)
  - `/pwd-unlocked` (Account Password Reset Confirmation)
  - `/maintenance` (Scheduled Maintenance Advisory)
- **One-Click Agent Injection**: Instant template insertion into the comment text area with shortcut preview.

### 1.5 Multi-Provider AI (Google Gemini & OpenAI) + Diagnostics
- **Dual LLM Provider Support**: Dynamically routes structured classification to **OpenAI** (`gpt-4o-mini`) or **Google Gemini** (`gemini-1.5-flash`), with prompt injection sanitization.
- **Fail-Safe Offline Mode**: Zero runtime crashes when offline or unconfigured—instantly switches to an internal rule-based classifier.
- **Live Diagnostics Console**: Integrated into the Application Settings page (`/api/v1/ai/test-connection`) with real-time latency (ms) and JSON schema validation.

### 1.6 Real-Time Live Updates via Server-Sent Events (SSE)
- **Spring Web `SseEmitter` Pipeline**: Reactive client stream at `GET /api/v1/notifications/stream`.
- **System Event Broadcasting**: Pushes live events for:
  - `TICKET_CREATED` (auto-alerts agents when new tickets arrive)
  - `SLA_ALERT` (warns commanders immediately upon 80% SLA threshold or breach)
  - `NOTIFICATION` (delivers instant in-app alerts directly to assignees)
- **Frontend Live Toast Stack**: Animated floating alert toasts in `LiveAlertToast.tsx` with direct navigation links.

### 1.7 Knowledge Base & Runbook Solution Recommender (RAG)
- **Pre-Seeded Enterprise Playbooks**: Includes real runbooks for Cisco AnyConnect VPN, Active Directory account lockouts, PostgreSQL/HikariCP pool saturation, Phishing incident containment, and Hardware failures.
- **Smart Recommendation Engine**: In `TicketDetails.tsx`, queries `/api/v1/knowledge-base/suggest` and auto-recommends relevant troubleshooting playbooks tailored to the active ticket's category and title.

### 1.8 Inbound Email Webhook Ingestion
- **SendGrid / Mailgun / SES Compatible**: Webhook endpoint at `POST /api/v1/email/inbound`.
- **Intelligent Subject Parsing**:
  - Appends comments to existing tickets matching subject patterns (`Re: [SD-2026-000001]`).
  - Provisions users and creates new tickets (`source = EMAIL`) with automated AI triage and SLA tracking.
- **Interactive Webhook Simulator**: Built into `ApplicationSettings.tsx` to test sending inbound customer emails.

### 1.9 Exportable SLA Compliance & Audit Reports
- **RFC 4180 CSV Export**: Streamed from `GET /api/v1/reports/sla/export` with headers, deadlines, and breach markers.
- **Operational Metrics**: Aggregates Mean Time to Acknowledge (MTTA), Mean Time to Resolve (MTTR), CSAT scores, and priority compliance breakdowns on the Reports & Analytics page.

---

## 2. Automated Test Matrix & Execution Results

```powershell
mvn test -f backend/pom.xml
```

| Test Suite | Tests | Result | Focus Area |
| :--- | :---: | :---: | :--- |
| `AiTriageSafetyTest` | 3 | **PASSED** | Fallback execution, malformed JSON tolerance, human overrides |
| `ConcurrentTicketNumberTest` | 1 (50 threads) | **PASSED** | Thread-safe sequential ticket numbering (`SD-2026-XXXXXX`) |
| `SecurityAcceptanceTest` | 2 | **PASSED** | Requester ticket isolation & private internal notes protection |
| `SlaEngineTest` | 3 | **PASSED** | Accurate deadlines, duplicate escalation prevention, ITIL SLA pause/resume |
| `TicketWorkflowTest` | 3 | **PASSED** | Legal status state transitions & rejection of invalid jumps |
| `SentinelDeskApplicationTests` | 1 | **PASSED** | Full Spring context, Flyway V1-V4 migrations, JPA verification |
| **Total Automated Tests** | **13** | **13 / 13 PASSED** | **BUILD SUCCESS (0 Failures, 0 Errors)** |

### Frontend Build Verification
```powershell
cd frontend
npm run build
```
- **TypeScript Static Verification**: `0 errors`
- **Vite Production Bundler**: Compiled 2,446 modules cleanly into `frontend/dist/` (`dist/assets/index-DtBGdE2P.js` - 818 kB minified)

---

## 3. Quickstart & Service URLs

### Local Run Commands
- **Backend**:
  ```powershell
  cd backend
  .\mvnw.cmd spring-boot:run
  ```
  *API: `http://localhost:8080/api/v1` | Swagger UI: `http://localhost:8080/swagger-ui.html`*
- **Frontend**:
  ```powershell
  cd frontend
  npm run dev
  ```
  *App: `http://localhost:5173`*

### Seed User Credentials (Password: `password123` for all)
- **Admin**: `admin@sentineldesk.internal`
- **Support Manager**: `manager@sentineldesk.internal`
- **Support Agent**: `agent@sentineldesk.internal`
- **Requester**: `requester@sentineldesk.internal`
