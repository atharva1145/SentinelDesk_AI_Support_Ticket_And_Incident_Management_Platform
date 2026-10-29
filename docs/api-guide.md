# SentinelDesk AI — REST API Guide

All REST endpoints are versioned with the prefix `/api/v1`.
Interactive Swagger UI documentation is available at: `http://localhost:8080/swagger-ui.html`
OpenAPI JSON Schema specification: `http://localhost:8080/v3/api-docs`

---

## Authentication & Tokens

### `POST /api/v1/auth/login`
Authenticates a user and issues a signed JWT Bearer token.
```json
// Request
{
  "email": "agent1@sentineldesk.com",
  "password": "Password123!"
}

// Response (200 OK)
{
  "success": true,
  "message": "Login successful",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "type": "Bearer",
    "user": {
      "id": 3,
      "email": "agent1@sentineldesk.com",
      "fullName": "Alex Rivera",
      "role": "SUPPORT_AGENT",
      "department": "Tier 1 Service Desk"
    }
  }
}
```

### `POST /api/v1/auth/register`
Self-service registration for external requesters. Default role assigned: `REQUESTER`.

### `GET /api/v1/auth/me`
Retrieves current profile and permissions for the authenticated Bearer token.

---

## Ticket Management

### `POST /api/v1/tickets`
Submits a new ticket. Automatically initiates SLA tracker and AI triage.
```json
// Request
{
  "title": "VPN Gateway dropping connections in London branch",
  "description": "Multiple remote staff report intermittent disconnection every 10 minutes when connected to Gateway-EU2.",
  "category": "NETWORK",
  "priority": "P2"
}
```

### `GET /api/v1/tickets`
Searches, filters, and paginates tickets.
**Query Parameters**:
- `query` (text search in title, description, ticket number)
- `status` (`NEW`, `TRIAGED`, `OPEN`, `IN_PROGRESS`, `PENDING_REQUESTER`, `RESOLVED`, `CLOSED`, `CANCELLED`)
- `priority` (`P1`, `P2`, `P3`, `P4`)
- `category` (`SOFTWARE`, `HARDWARE`, `NETWORK`, `ACCESS`, `SECURITY`, `OUTAGE`, `OTHER`)
- `agentId`, `teamId`
- `unassigned` (`true`/`false`)
- `page` (default 0), `size` (default 15)
- `sortBy` (default `createdAt`), `sortDirection` (`asc`/`desc`)

*Security Rule: Requesters automatically have their query filtered to `requesterId = currentUserId`.*

### `GET /api/v1/tickets/{id}`
Returns full ticket details including SLA deadlines and latest AI suggestion.
*Security Rule: If current user is a Requester, accessing tickets owned by another user yields HTTP 403 Forbidden.*

### `PATCH /api/v1/tickets/{id}/status`
Transitions ticket along valid state machine.
```json
{
  "status": "IN_PROGRESS",
  "reason": "Started troubleshooting gateway network logs"
}
```

### `PATCH /api/v1/tickets/{id}/assign`
Assigns an agent or team. Advance `NEW` tickets to `OPEN`.

### `PATCH /api/v1/tickets/{id}/priority`
Updates priority with required audit reason. Recalculates SLA deadline automatically.

---

## Ticket Comments & Private Notes

### `GET /api/v1/tickets/{ticketId}/comments`
Retrieves conversation history.
*Security Rule: If requester requests comments, `is_internal = true` notes are strictly filtered from the response.*

### `POST /api/v1/tickets/{ticketId}/comments`
Adds a public reply or internal staff note.
```json
{
  "content": "Checking router interface eth0 error rates.",
  "isInternal": true
}
```
*Security Rule: If a Requester sets `isInternal: true`, the request is rejected with HTTP 403 AccessDeniedException.*

---

## AI Triage

### `GET /api/v1/tickets/{ticketId}/ai-triage`
Retrieves AI classification history and diagnostic prompts.

### `POST /api/v1/tickets/{ticketId}/ai-triage/{suggestionId}/accept`
Accepts AI recommendation (applies suggested category, priority, and team routing).

### `POST /api/v1/tickets/{ticketId}/ai-triage/{suggestionId}/override`
Allows human staff to override AI classification with mandatory rationale.

---

## SLA & Escalations

### `GET /api/v1/sla/policies`
Lists all active SLA policies and target response/resolution hours.

### `PUT /api/v1/sla/policies/{id}`
Updates SLA target thresholds (Manager / Admin only).

### `GET /api/v1/escalations`
Lists triggered SLA breach and warning escalation events.

### `PATCH /api/v1/escalations/{id}/status`
Updates escalation status to `ACKNOWLEDGED` or `RESOLVED`.

---

## Incident Management

### `GET /api/v1/incidents`
Lists all declared major incidents.

### `POST /api/v1/incidents`
Declares a new major incident.

### `POST /api/v1/incidents/{id}/link-tickets`
Correlates multiple support tickets to a shared incident investigation.

### `PATCH /api/v1/incidents/{id}/status`
Updates incident status (`INVESTIGATING`, `IDENTIFIED`, `MONITORING`, `RESOLVED`) and root cause summary.

---

## Analytics & Dashboard

### `GET /api/v1/dashboard/summary`
Returns high-level KPI cards (total open, unassigned, P1/P2 count, SLA compliance %, MTTR).

### `GET /api/v1/dashboard/charts`
Returns real aggregated datasets for Recharts visualization.
