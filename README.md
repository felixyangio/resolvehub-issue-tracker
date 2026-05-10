# ResolveHub

**Incident Response & Issue Resolution Platform**

A production-style REST API for managing incidents across an organisation. Users report issues, managers assign them to agents, agents resolve them through a controlled status workflow, and every action is audit-logged. Built with Java 17 and Spring Boot 3 following layered architecture, JWT authentication, and role-based access control.

101 automated tests. Dockerised. CI via GitHub Actions.

---

## Core Features

- **Incident lifecycle management** — create, assign, transition through statuses, resolve, close
- **Role-based access control** — four roles (User, Agent, Manager, Admin) with enforced permissions at the service layer
- **Status transition validation** — state machine prevents invalid moves (e.g. NEW cannot jump to CLOSED)
- **Rule-based priority classification** — keyword matching auto-assigns priority when not provided
- **Incident comments** — threaded discussion per incident with access control
- **Immutable audit logs** — every create, assign, status change, and comment is recorded
- **Dashboard APIs** — role-filtered statistics: counts by status, priority, agent workload
- **Dynamic filtering** — JPA Specifications for status, category, priority with pagination
- **JWT authentication** — stateless, HMAC-SHA signed tokens with configurable expiry
- **Input validation** — Hibernate Validator on all request DTOs
- **Consistent error handling** — `@RestControllerAdvice` with structured error responses
- **OpenAPI documentation** — Swagger UI at `/swagger-ui.html`

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Language | Java 17 |
| Framework | Spring Boot 3.2.5 |
| Security | Spring Security + JWT (jjwt 0.12.5) |
| Data | Spring Data JPA + Hibernate |
| Database | PostgreSQL 15 |
| Validation | Hibernate Validator (Jakarta) |
| API Docs | springdoc-openapi 2.5.0 |
| Testing | JUnit 5 + Mockito + MockMvc |
| Build | Maven 3.9.6 (wrapper included) |
| Containerisation | Docker (multi-stage build) |
| CI | GitHub Actions |

## Architecture

```
Controller (HTTP routing, validation)
    │
Service (business logic, permissions, audit logging)
    │
Repository (JPA queries, Specifications)
    │
Entity (JPA domain model)
```

**Design principles:**
- Business logic lives in the service layer, never in controllers or repositories
- DTOs for all requests and responses — entities are never exposed to the API
- `@MappedSuperclass` BaseEntity provides UUID primary keys with `createdAt`/`updatedAt`
- Package-private method visibility for cross-service reuse without public interfaces
- Circular dependencies resolved by injecting repositories directly where needed

## User Roles

| Role | Permissions |
|------|------------|
| **USER** | Create incidents, view/update own incidents, add comments on own incidents |
| **AGENT** | View/update assigned incidents, change status, add comments |
| **MANAGER** | View all incidents, assign to agents, change any status, delete incidents |
| **ADMIN** | All manager permissions |

## Incident Workflow

```
  NEW ──→ ASSIGNED ──→ IN_PROGRESS ──→ RESOLVED ──→ CLOSED
   │         │              │
   └────┬────┘──────────────┘
        ▼
    CANCELLED
```

- Assigning an incident in NEW status auto-transitions to ASSIGNED
- Only valid transitions are allowed (enforced by `StatusTransitionValidator`)
- Each transition generates an audit log entry

## API Overview

### Authentication
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/auth/register` | Register a new user |
| POST | `/api/auth/login` | Login, receive JWT token |
| GET | `/api/users/me` | Get current user profile |

### Incidents
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/incidents` | Create incident (auto-classifies priority if omitted) |
| GET | `/api/incidents` | List incidents (filtered by role, paginated) |
| GET | `/api/incidents/{id}` | Get incident detail |
| PUT | `/api/incidents/{id}` | Update incident fields |
| PATCH | `/api/incidents/{id}/assign` | Assign to agent (Manager/Admin only) |
| PATCH | `/api/incidents/{id}/status` | Change status (validated transitions) |
| DELETE | `/api/incidents/{id}` | Delete incident (Manager/Admin only) |

### Comments
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/incidents/{id}/comments` | Add comment |
| GET | `/api/incidents/{id}/comments` | List comments |

### Audit Logs
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/incidents/{id}/audit-logs` | View audit trail |

### Dashboard
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/dashboard/summary` | Role-filtered summary counts |
| GET | `/api/dashboard/incidents-by-status` | Count per status |
| GET | `/api/dashboard/incidents-by-severity` | Count per priority |
| GET | `/api/dashboard/my-workload` | Personal workload + agent breakdown |

### Utility
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/health` | Health check |

## Domain Model

```
User ──────────────── Incident ──────────── Comment
 │ (createdBy)           │                    │
 │ (assignedTo)          │                    │ (author)
 │                       │                    │
 └───────────────── AuditLog ────────────────┘
                    (actor)
```

**Entities:** User, Incident, Comment, AuditLog

**Enums:** Role (4), IncidentStatus (6), Priority (4), IncidentCategory (5), AuditAction (5)

## Running Locally

### Prerequisites
- Java 17+
- Docker

### Option 1: Docker Compose (recommended)

```bash
# Start backend + PostgreSQL
docker compose up -d

# Verify
curl http://localhost:8080/api/health

# View logs
docker compose logs -f backend

# Stop
docker compose down
```

### Option 2: Local development

```bash
# Start PostgreSQL only
docker compose up -d postgres

# Run with dev profile (auto-creates tables)
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

The API is available at `http://localhost:8080`.
Swagger UI is at `http://localhost:8080/swagger-ui.html`.

### Demo flow

This walks through the full incident lifecycle. Start the stack with `docker compose up -d` first.

```bash
API="http://localhost:8080/api"

# 1. Register three users
curl -s -X POST $API/auth/register -H "Content-Type: application/json" \
  -d '{"name":"Carol Manager","email":"carol@demo.io","password":"Manage1!"}'

curl -s -X POST $API/auth/register -H "Content-Type: application/json" \
  -d '{"name":"Bob Agent","email":"bob@demo.io","password":"Agent123!"}'

curl -s -X POST $API/auth/register -H "Content-Type: application/json" \
  -d '{"name":"Alice User","email":"alice@demo.io","password":"User1234!"}'

# 2. Promote roles via database (no admin UI yet)
docker exec resolvehub-db psql -U resolvehub -c \
  "UPDATE users SET role='MANAGER' WHERE email='carol@demo.io';"
docker exec resolvehub-db psql -U resolvehub -c \
  "UPDATE users SET role='AGENT' WHERE email='bob@demo.io';"

# 3. Login as each role (tokens include updated roles)
MANAGER=$(curl -s -X POST $API/auth/login -H "Content-Type: application/json" \
  -d '{"email":"carol@demo.io","password":"Manage1!"}' | grep -o '"token":"[^"]*' | cut -d'"' -f4)

AGENT=$(curl -s -X POST $API/auth/login -H "Content-Type: application/json" \
  -d '{"email":"bob@demo.io","password":"Agent123!"}' | grep -o '"token":"[^"]*' | cut -d'"' -f4)

USER=$(curl -s -X POST $API/auth/login -H "Content-Type: application/json" \
  -d '{"email":"alice@demo.io","password":"User1234!"}' | grep -o '"token":"[^"]*' | cut -d'"' -f4)

# 4. Create incident as user (priority auto-classified as CRITICAL)
curl -s -X POST $API/incidents -H "Content-Type: application/json" \
  -H "Authorization: Bearer $USER" \
  -d '{"title":"Production API outage","description":"All endpoints returning 503","category":"TECHNICAL"}'

# 5. Manager assigns to agent (status auto-transitions to ASSIGNED)
# Replace <incident-id> and <agent-id> with actual UUIDs from responses above
curl -s -X PATCH $API/incidents/<incident-id>/assign \
  -H "Content-Type: application/json" -H "Authorization: Bearer $MANAGER" \
  -d '{"agentId":"<agent-id>"}'

# 6. Agent progresses through statuses
curl -s -X PATCH $API/incidents/<incident-id>/status \
  -H "Content-Type: application/json" -H "Authorization: Bearer $AGENT" \
  -d '{"status":"IN_PROGRESS"}'

# 7. Agent adds comment
curl -s -X POST $API/incidents/<incident-id>/comments \
  -H "Content-Type: application/json" -H "Authorization: Bearer $AGENT" \
  -d '{"content":"Root cause identified: database connection pool exhausted."}'

# 8. Agent resolves, manager closes
curl -s -X PATCH $API/incidents/<incident-id>/status \
  -H "Content-Type: application/json" -H "Authorization: Bearer $AGENT" \
  -d '{"status":"RESOLVED"}'

curl -s -X PATCH $API/incidents/<incident-id>/status \
  -H "Content-Type: application/json" -H "Authorization: Bearer $MANAGER" \
  -d '{"status":"CLOSED"}'

# 9. Review audit trail
curl -s $API/incidents/<incident-id>/audit-logs -H "Authorization: Bearer $MANAGER"

# 10. Check dashboard
curl -s $API/dashboard/summary -H "Authorization: Bearer $MANAGER"
```

## Running Tests

```bash
# All 101 tests
./mvnw test

# Unit tests only (service layer)
./mvnw test -Dtest="*ServiceTest,*ValidatorTest"

# Integration tests only (MockMvc controller tests)
./mvnw test -Dtest="*ControllerTest"
```

### Test breakdown

| Test class | Count | What it covers |
|-----------|-------|---------------|
| StatusTransitionValidatorTest | 21 | All valid/invalid state transitions (parameterized) |
| IncidentServiceTest | 26 | CRUD, read/write access per role, assignment, audit logging |
| IncidentClassificationServiceTest | 17 | Keyword priority tiers, precedence, case insensitivity |
| DashboardServiceTest | 8 | Role-filtered summary, status/priority counts, workload |
| CommentServiceTest | 7 | Owner/agent/manager access, unauthorized rejection, audit |
| AuditLogServiceTest | 4 | Log creation, value tracking, access control |
| IncidentControllerTest | 9 | HTTP status codes, validation errors, auth, JSON structure |
| CommentControllerTest | 4 | Create, blank rejection, list, access denied |
| DashboardControllerTest | 5 | All endpoints, auth check |
| **Total** | **101** | |

## CI/CD

GitHub Actions runs on every push to `main` and on pull requests:

1. **Test job** — starts PostgreSQL service container, installs JDK 17 with Maven caching, runs `./mvnw verify`
2. **Docker job** — builds the Docker image to verify the Dockerfile is valid (does not push to a registry)

See [`.github/workflows/ci.yml`](.github/workflows/ci.yml).

## Screenshots

This is a backend API project. The screenshots below show the API in use via Swagger UI and curl.

> To add screenshots: save images to a `docs/screenshots/` folder and update the paths below.

- [ ] **Swagger UI** — `http://localhost:8080/swagger-ui.html` showing all endpoint groups
- [ ] **POST /auth/register** — registration request and JWT response
- [ ] **POST /incidents** — creating an incident with auto-classified priority
- [ ] **PATCH /incidents/{id}/assign** — manager assigning to agent
- [ ] **PATCH /incidents/{id}/status** — status transition (ASSIGNED to IN_PROGRESS)
- [ ] **GET /incidents/{id}/audit-logs** — full audit trail for an incident
- [ ] **GET /dashboard/summary** — dashboard statistics response
- [ ] **Terminal** — `./mvnw test` showing 101 tests passing
- [ ] **Terminal** — `docker compose up` showing healthy startup

## Future Improvements

- Frontend dashboard (React or Thymeleaf)
- Email/Slack notifications on assignment and status change
- SLA tracking with overdue alerts
- File attachments on incidents
- Full-text search with Elasticsearch
- Pagination metadata in dashboard responses
- Rate limiting on public endpoints
- Integration tests with Testcontainers
- Deployment to a cloud platform (AWS/Render/Fly.io)

---

## CV-Ready Project Description

### One paragraph

> Built a production-style incident management REST API with Java 17 and Spring Boot 3. Implemented JWT authentication, role-based access control for four user roles, a validated status transition workflow, rule-based priority classification, audit logging, and dashboard statistics APIs. Wrote 101 automated tests (unit + integration with MockMvc). Containerised with a multi-stage Docker build and automated CI with GitHub Actions.

### Bullet points (for CV)

- Designed and built a RESTful incident management API with Java 17, Spring Boot 3, Spring Security, and PostgreSQL
- Implemented stateless JWT authentication and role-based access control (User, Agent, Manager, Admin) enforced at the service layer
- Built validated status transition workflow, rule-based incident classification, audit logging, and role-filtered dashboard APIs
- Wrote 101 automated tests across unit (Mockito) and integration (MockMvc) layers covering permissions, workflows, and validation
- Containerised with Docker multi-stage build and set up GitHub Actions CI pipeline with PostgreSQL service containers

### Technical keywords

Java 17, Spring Boot 3, Spring Security, JWT, Spring Data JPA, PostgreSQL, Hibernate, REST API, RBAC, JPA Specifications, JUnit 5, Mockito, MockMvc, Docker, GitHub Actions, Maven, OpenAPI/Swagger

---

## What I Learned

**Layered architecture** — Keeping business logic strictly in the service layer means controllers stay thin (routing + validation) and repositories stay dumb (queries only). When I needed to reuse permission checks across services, package-private visibility solved it without creating public interfaces or utility classes.

**JWT authentication** — Building the full flow (registration, login, token generation, filter-based validation, SecurityContext population) from scratch taught me how stateless auth actually works. The token carries the role claim, so every request is self-contained — no session store needed.

**Role-based access control** — Enforcing permissions at the service layer (not with `@PreAuthorize` annotations) made the rules explicit and testable. Each role has clear boundaries: users see their own data, agents see their assignments, managers see everything.

**Status transition validation** — Modelling allowed transitions as an immutable `Map<Status, Set<Status>>` made the state machine easy to test (21 parameterized tests cover every combination) and impossible to bypass — any code path that changes status goes through the validator.

**Testing strategy** — Unit tests with Mockito verify business logic in isolation (fast, no Spring context). Integration tests with `@WebMvcTest` + MockMvc verify HTTP routing, validation, security filters, and JSON serialisation. The two layers catch different classes of bugs.

**Docker multi-stage builds** — Separating the build stage (full JDK + Maven) from the runtime stage (JRE only) means the production image has no compiler, no source code, and runs as a non-root user. One Dockerfile, two concerns.

**CI pipeline** — GitHub Actions with a PostgreSQL service container means tests run against a real database in CI, not just in-memory mocks. Maven dependency caching keeps build times reasonable.

**Dashboard APIs** — Pushing aggregation to the database (`COUNT` queries) instead of loading entities into memory is the difference between O(1) and O(n) for statistics endpoints. Role-based filtering at the query level means each user sees exactly what they should.

---

## Project Status

- [x] Phase 1: Project foundation and architecture
- [x] Phase 2: Authentication and JWT
- [x] Phase 3: Incident CRUD
- [x] Phase 4: Role-based access control and assignment workflow
- [x] Phase 5: Comments and audit logs
- [x] Phase 6: Dashboard APIs and rule-based classification
- [x] Phase 7: Unit and integration tests (101 tests)
- [x] Phase 9: Docker multi-stage build and GitHub Actions CI
- [x] Phase 10: Documentation and project polish
- [x] Phase 11: Final verification, cleanup, and demo evidence
