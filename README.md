# ResolveHub

**Resident Repair & Complaint Operations Platform**

A production-style REST API for managing resident cases across accommodation providers. Residents report repair requests and complaints, property managers assign them to maintenance staff, staff resolve them through a controlled status workflow, and every action is logged in a case timeline. Built with Java 17 and Spring Boot 3 following layered architecture, JWT authentication, and role-based access control.

110 automated tests. Dockerised. CI via GitHub Actions.

---

## Context

ResolveHub is positioned for the accommodation and property management sector — student halls, HMOs, build-to-rent blocks, and housing associations. The domain maps naturally onto incident management: residents raise cases (repairs, complaints, queries), operations staff triage and assign them, maintenance staff resolve them, and managers track performance through dashboards.

> **Implementation note:** The backend uses generic entity names (`Incident`, `Agent`, `Manager`) internally. This is deliberate — the domain layer is sector-agnostic, making it reusable across verticals. The documentation and API responses present these in accommodation terminology. Renaming enum values (e.g. `TECHNICAL` → `MAINTENANCE`) is listed as a future domain adaptation task.

---

## Core Features

- **Resident case lifecycle** — create repair requests or complaints, assign to staff, transition through statuses, resolve, close
- **Role-based access control** — four roles (Resident, Maintenance Staff, Property Manager, Admin) with enforced permissions at the service layer
- **Status transition validation** — state machine prevents invalid moves (e.g. NEW cannot jump to CLOSED)
- **Rule-based urgency classification** — keyword matching auto-assigns urgency when not provided (e.g. "water leak" → CRITICAL, "lightbulb" → LOW)
- **Case comments** — threaded discussion per case with access control
- **Immutable case timeline** — every create, assign, status change, and comment is recorded as an audit trail
- **Operations dashboard APIs** — role-filtered statistics: counts by status, urgency, staff workload
- **Dynamic filtering** — JPA Specifications for status, category, urgency with pagination
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
Service (business logic, permissions, case timeline logging)
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

| Role | In-App Name | Permissions |
|------|-------------|------------|
| **USER** | Resident | Create cases, view/update own cases, add comments on own cases |
| **AGENT** | Maintenance Staff | View/update assigned cases, change status, add comments |
| **MANAGER** | Property Manager | View all cases, assign to staff, change any status, delete cases |
| **ADMIN** | Admin | All property manager permissions |

## Case Workflow

```
  NEW ──→ ASSIGNED ──→ IN_PROGRESS ──→ RESOLVED ──→ CLOSED
   │         │              │
   └────┬────┘──────────────┘
        ▼
    CANCELLED
```

- Assigning a case in NEW status auto-transitions to ASSIGNED
- Only valid transitions are allowed (enforced by `StatusTransitionValidator`)
- Each transition generates a case timeline entry

## Urgency Classification

When a resident submits a case without specifying urgency, the system auto-classifies based on keywords:

| Urgency | Example Keywords | Accommodation Example |
|---------|-----------------|----------------------|
| CRITICAL | outage, system down, data leak, breach, production down | Water leak flooding hallway, gas smell, no heating in winter |
| HIGH | security, unauthorized, failure | Broken front door lock, boiler failure, electrical fault |
| MEDIUM | *(default)* | Washing machine not draining, Wi-Fi intermittent |
| LOW | information, question, how to | Lightbulb replacement, bin collection query |

> **Domain-adapted keywords:** The classification service uses accommodation-specific keywords — "flood", "gas leak", "no heating" → CRITICAL; "mould", "broken lock", "boiler" → HIGH; "lightbulb", "minor" → LOW. The service uses the Strategy pattern, so swapping for an ML-backed implementation requires no caller changes.

## API Overview

### Authentication
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/auth/register` | Register a new resident |
| POST | `/api/auth/login` | Login, receive JWT token |
| GET | `/api/users/me` | Get current user profile |

### Resident Cases
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/incidents` | Create case (auto-classifies urgency if omitted) |
| GET | `/api/incidents` | List cases (filtered by role, paginated) |
| GET | `/api/incidents/{id}` | Get case detail |
| PUT | `/api/incidents/{id}` | Update case fields |
| PATCH | `/api/incidents/{id}/assign` | Assign to maintenance staff (Property Manager only) |
| PATCH | `/api/incidents/{id}/status` | Change status (validated transitions) |
| DELETE | `/api/incidents/{id}` | Delete case (Property Manager only) |

### Case Comments
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/incidents/{id}/comments` | Add comment to case |
| GET | `/api/incidents/{id}/comments` | List case comments |

### Case Timeline
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/incidents/{id}/audit-logs` | View full case timeline |

### Operations Dashboard
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/dashboard/summary` | Role-filtered summary counts |
| GET | `/api/dashboard/incidents-by-status` | Count per status |
| GET | `/api/dashboard/incidents-by-severity` | Count per urgency level |
| GET | `/api/dashboard/my-workload` | Personal workload + staff breakdown |

### Utility
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/health` | Health check |

> **Note:** API paths use the internal entity name (`incidents`, `audit-logs`). A future version could alias these to `/cases` and `/timeline` for domain consistency.

## Domain Model

```
User ──────────────── Incident ──────────── Comment
 │ (createdBy)           │                    │
 │ (assignedTo)          │                    │ (author)
 │                       │                    │
 └───────────────── AuditLog ────────────────┘
                    (actor)
```

**Entities:** User, Incident (resident case), Comment, AuditLog (case timeline entry)

**Enums:** Role (4), IncidentStatus (6), Priority (4 urgency levels), IncidentCategory (9), AuditAction (5)

> **Domain-adapted categories:** `IncidentCategory` uses accommodation-specific values: `MAINTENANCE`, `SAFETY`, `NOISE`, `INTERNET`, `BILLING`, `DEPOSIT`, `CLEANING`, `ACCESS`, `OTHER`. The SAFETY category auto-classifies as HIGH urgency regardless of keywords.

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

This walks through a resident reporting a heating fault, a property manager assigning it, and maintenance staff resolving it. Start the stack with `docker compose up -d` first.

```bash
API="http://localhost:8080/api"

# 1. Register three users
curl -s -X POST $API/auth/register -H "Content-Type: application/json" \
  -d '{"name":"Carol Perry","email":"carol@property.io","password":"Manage1!"}'

curl -s -X POST $API/auth/register -H "Content-Type: application/json" \
  -d '{"name":"Bob Torres","email":"bob@property.io","password":"Agent123!"}'

curl -s -X POST $API/auth/register -H "Content-Type: application/json" \
  -d '{"name":"Alice Chen","email":"alice@tenant.io","password":"User1234!"}'

# 2. Promote roles via database (no admin UI yet)
docker exec resolvehub-db psql -U resolvehub -c \
  "UPDATE users SET role='MANAGER' WHERE email='carol@property.io';"
docker exec resolvehub-db psql -U resolvehub -c \
  "UPDATE users SET role='AGENT' WHERE email='bob@property.io';"

# 3. Login as each role
MANAGER=$(curl -s -X POST $API/auth/login -H "Content-Type: application/json" \
  -d '{"email":"carol@property.io","password":"Manage1!"}' | grep -o '"token":"[^"]*' | cut -d'"' -f4)

STAFF=$(curl -s -X POST $API/auth/login -H "Content-Type: application/json" \
  -d '{"email":"bob@property.io","password":"Agent123!"}' | grep -o '"token":"[^"]*' | cut -d'"' -f4)

RESIDENT=$(curl -s -X POST $API/auth/login -H "Content-Type: application/json" \
  -d '{"email":"alice@tenant.io","password":"User1234!"}' | grep -o '"token":"[^"]*' | cut -d'"' -f4)

# 4. Resident reports a repair (urgency auto-classified as HIGH via keyword "not working")
curl -s -X POST $API/incidents -H "Content-Type: application/json" \
  -H "Authorization: Bearer $RESIDENT" \
  -d '{"title":"Heating not working in Flat B204","description":"Radiators are cold in all rooms. Thermostat shows no response. Temperature dropping.","category":"TECHNICAL"}'

# 5. Property manager assigns to maintenance staff (status auto-transitions to ASSIGNED)
# Replace <case-id> and <staff-id> with actual UUIDs from responses above
curl -s -X PATCH $API/incidents/<case-id>/assign \
  -H "Content-Type: application/json" -H "Authorization: Bearer $MANAGER" \
  -d '{"agentId":"<staff-id>"}'

# 6. Maintenance staff starts work
curl -s -X PATCH $API/incidents/<case-id>/status \
  -H "Content-Type: application/json" -H "Authorization: Bearer $STAFF" \
  -d '{"status":"IN_PROGRESS"}'

# 7. Staff adds update comment
curl -s -X POST $API/incidents/<case-id>/comments \
  -H "Content-Type: application/json" -H "Authorization: Bearer $STAFF" \
  -d '{"content":"Inspected boiler — pressure valve failed. Replacement part ordered, ETA tomorrow."}'

# 8. Staff resolves, property manager closes
curl -s -X PATCH $API/incidents/<case-id>/status \
  -H "Content-Type: application/json" -H "Authorization: Bearer $STAFF" \
  -d '{"status":"RESOLVED"}'

curl -s -X PATCH $API/incidents/<case-id>/status \
  -H "Content-Type: application/json" -H "Authorization: Bearer $MANAGER" \
  -d '{"status":"CLOSED"}'

# 9. Review case timeline
curl -s $API/incidents/<case-id>/audit-logs -H "Authorization: Bearer $MANAGER"

# 10. Check operations dashboard
curl -s $API/dashboard/summary -H "Authorization: Bearer $MANAGER"
```

### More example cases

These show the range of cases the platform handles:

```bash
# Water leak — auto-classified CRITICAL (keyword: "leak")
curl -s -X POST $API/incidents -H "Content-Type: application/json" \
  -H "Authorization: Bearer $RESIDENT" \
  -d '{"title":"Water leak under bathroom sink","description":"Constant dripping from pipe joint under sink. Water pooling on floor.","category":"TECHNICAL"}'

# Broken lock — auto-classified HIGH (keyword: "security")
curl -s -X POST $API/incidents -H "Content-Type: application/json" \
  -H "Authorization: Bearer $RESIDENT" \
  -d '{"title":"Broken front door lock","description":"Key turns but deadbolt does not engage. Door cannot be locked securely.","category":"SECURITY"}'

# Noise complaint
curl -s -X POST $API/incidents -H "Content-Type: application/json" \
  -H "Authorization: Bearer $RESIDENT" \
  -d '{"title":"Noise complaint after midnight","description":"Loud music from Flat C305 between 1am and 3am, three nights running.","category":"GENERAL"}'

# Wi-Fi issue
curl -s -X POST $API/incidents -H "Content-Type: application/json" \
  -H "Authorization: Bearer $RESIDENT" \
  -d '{"title":"Wi-Fi unavailable in Room C312","description":"No wireless signal detected. Router light is off. Other rooms on same floor also affected.","category":"TECHNICAL"}'

# Deposit dispute
curl -s -X POST $API/incidents -H "Content-Type: application/json" \
  -H "Authorization: Bearer $RESIDENT" \
  -d '{"title":"Deposit deduction dispute","description":"Charged £150 for carpet cleaning but carpet was professionally cleaned before checkout. Have receipt.","category":"BILLING"}'

# Mould issue
curl -s -X POST $API/incidents -H "Content-Type: application/json" \
  -H "Authorization: Bearer $RESIDENT" \
  -d '{"title":"Mould appearing near bedroom window","description":"Black mould patches spreading on wall beside window frame. Getting worse each week.","category":"TECHNICAL"}'

# Shared facility
curl -s -X POST $API/incidents -H "Content-Type: application/json" \
  -H "Authorization: Bearer $RESIDENT" \
  -d '{"title":"Washing machine broken in shared laundry room","description":"Machine stops mid-cycle with error code E3. Clothes stuck inside.","category":"TECHNICAL"}'
```

## Running Tests

```bash
# All 110 tests
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
| IncidentServiceTest | 26 | CRUD, read/write access per role, assignment, case timeline logging |
| IncidentClassificationServiceTest | 26 | Keyword urgency tiers, category overrides, precedence, domain categories |
| DashboardServiceTest | 8 | Role-filtered summary, status/urgency counts, workload |
| CommentServiceTest | 7 | Owner/staff/manager access, unauthorized rejection, timeline |
| AuditLogServiceTest | 4 | Log creation, value tracking, access control |
| IncidentControllerTest | 9 | HTTP status codes, validation errors, auth, JSON structure |
| CommentControllerTest | 4 | Create, blank rejection, list, access denied |
| DashboardControllerTest | 5 | All endpoints, auth check |
| **Total** | **110** | |

## CI/CD

GitHub Actions runs on every push to `main` and on pull requests:

1. **Test job** — starts PostgreSQL service container, installs JDK 17 with Maven caching, runs `./mvnw verify`
2. **Docker job** — builds the Docker image to verify the Dockerfile is valid (does not push to a registry)

See [`.github/workflows/ci.yml`](.github/workflows/ci.yml).

## Screenshots

This is a backend API project. The screenshots below show the API in use via Swagger UI and curl.

> To add screenshots: save images to a `docs/screenshots/` folder and update the paths below.

- [ ] **Swagger UI** — `http://localhost:8080/swagger-ui.html` showing all endpoint groups
- [ ] **POST /auth/register** — resident registration request and JWT response
- [ ] **POST /incidents** — resident creating a repair request with auto-classified urgency
- [ ] **PATCH /incidents/{id}/assign** — property manager assigning to maintenance staff
- [ ] **PATCH /incidents/{id}/status** — status transition (ASSIGNED to IN_PROGRESS)
- [ ] **GET /incidents/{id}/audit-logs** — full case timeline for a repair request
- [ ] **GET /dashboard/summary** — operations dashboard statistics response
- [ ] **Terminal** — `./mvnw test` showing 110 tests passing
- [ ] **Terminal** — `docker compose up` showing healthy startup

## Future Improvements

- **Resident portal** — React frontend for residents to submit and track cases
- **Email/SMS notifications** — Spring Events + async listeners for assignment and status change
- **SLA tracking** — scheduled task to flag overdue cases based on urgency thresholds (e.g. CRITICAL repairs within 4 hours)
- **File attachments** — S3-backed photo upload on cases (residents photograph the issue)
- **Richer property data model** — tenant-to-property mapping, building/floor/unit hierarchy for portfolio managers
- **Testcontainers** — repository-level tests against a real PostgreSQL to catch Hibernate query bugs
- **Full-text search** — Elasticsearch for searching across case titles and descriptions
- **Production deployment** — Fly.io or AWS ECS with environment-specific config profiles

---

## CV-Ready Project Description

### One paragraph

> Built a production-style resident case management REST API for accommodation providers with Java 17 and Spring Boot 3. Implemented JWT authentication, role-based access control for four user roles (Resident, Maintenance Staff, Property Manager, Admin), a validated status transition workflow, rule-based urgency classification, case timeline logging, and operations dashboard APIs. Wrote 110 automated tests (unit + integration with MockMvc). Containerised with a multi-stage Docker build and automated CI with GitHub Actions.

### Bullet points (for CV)

- Designed and built a RESTful case management API for property operations with Java 17, Spring Boot 3, Spring Security, and PostgreSQL
- Implemented stateless JWT authentication and role-based access control (Resident, Staff, Manager, Admin) enforced at the service layer
- Built validated status transition workflow, rule-based urgency classification, case timeline logging, and role-filtered operations dashboard APIs
- Wrote 110 automated tests across unit (Mockito) and integration (MockMvc) layers covering permissions, workflows, classification, and validation
- Containerised with Docker multi-stage build and set up GitHub Actions CI pipeline with PostgreSQL service containers

### Technical keywords

Java 17, Spring Boot 3, Spring Security, JWT, Spring Data JPA, PostgreSQL, Hibernate, REST API, RBAC, JPA Specifications, JUnit 5, Mockito, MockMvc, Docker, GitHub Actions, Maven, OpenAPI/Swagger

---

## What I Learned

**Layered architecture** — Keeping business logic strictly in the service layer means controllers stay thin (routing + validation) and repositories stay dumb (queries only). When I needed to reuse permission checks across services, package-private visibility solved it without creating public interfaces or utility classes.

**JWT authentication** — Building the full flow (registration, login, token generation, filter-based validation, SecurityContext population) from scratch taught me how stateless auth actually works. The token carries the role claim, so every request is self-contained — no session store needed.

**Role-based access control** — Enforcing permissions at the service layer (not with `@PreAuthorize` annotations) made the rules explicit and testable. Each role has clear boundaries: residents see their own cases, maintenance staff see their assignments, property managers see everything.

**Status transition validation** — Modelling allowed transitions as an immutable `Map<Status, Set<Status>>` made the state machine easy to test (21 parameterized tests cover every combination) and impossible to bypass — any code path that changes status goes through the validator.

**Testing strategy** — Unit tests with Mockito verify business logic in isolation (fast, no Spring context). Integration tests with `@WebMvcTest` + MockMvc verify HTTP routing, validation, security filters, and JSON serialisation. The two layers catch different classes of bugs.

**Docker multi-stage builds** — Separating the build stage (full JDK + Maven) from the runtime stage (JRE only) means the production image has no compiler, no source code, and runs as a non-root user. One Dockerfile, two concerns.

**CI pipeline** — GitHub Actions with a PostgreSQL service container means tests run against a real database in CI, not just in-memory mocks. Maven dependency caching keeps build times reasonable.

**Operations dashboard** — Pushing aggregation to the database (`COUNT` queries) instead of loading entities into memory is the difference between O(1) and O(n) for statistics endpoints. Role-based filtering at the query level means each user sees exactly what they should — a property manager sees all cases across the building, while a resident sees only their own.

---

## Project Status

- [x] Phase 1: Project foundation and architecture
- [x] Phase 2: Authentication and JWT
- [x] Phase 3: Incident CRUD
- [x] Phase 4: Role-based access control and assignment workflow
- [x] Phase 5: Comments and audit logs
- [x] Phase 6: Dashboard APIs and rule-based classification
- [x] Phase 7: Unit and integration tests (110 tests)
- [x] Phase 9: Docker multi-stage build and GitHub Actions CI
- [x] Phase 10: Documentation and project polish
- [x] Phase 11: Final verification, cleanup, and demo evidence
- [x] Phase 12A: Domain repositioning (accommodation operations)
- [x] Phase 13: Domain code adaptation (categories, classification, tests)
- [x] Phase 14: Final CV, GitHub, and interview presentation pack
