# Interview Preparation — ResolveHub

## 30-Second Pitch

ResolveHub is a resident case management REST API I built with Java 17 and Spring Boot 3. It's designed for accommodation providers — student halls, HMOs, build-to-rent blocks. Residents report repair requests and complaints, property managers assign them to maintenance staff, staff work through a validated status workflow, and every action is logged in a case timeline. It has four roles with enforced permissions, auto-classification of urgency using keyword matching, and operations dashboard APIs for real-time statistics. I wrote 110 automated tests, containerised it with Docker Compose, and set up CI with GitHub Actions.

## 1-Minute Interview Explanation

**Why I built it:** I wanted a portfolio project that demonstrates backend engineering skills recruiters actually look for — REST APIs, authentication, access control, testing, Docker, CI — inside a domain that's easy to explain in interviews. Accommodation management is a real problem space I understand as a tenant.

**What problem it solves:** Accommodation providers handle repair requests and complaints by email, WhatsApp, or in person. Nothing is tracked centrally, response times are invisible, and managers can't see staff workload. ResolveHub gives each case a structured lifecycle — reported, assigned, in progress, resolved, closed — with a full audit trail and role-filtered dashboards.

**Main workflow:** A resident submits a case (e.g. "Heating not working in Flat B204"). The system auto-classifies its urgency using keyword matching. A property manager assigns it to maintenance staff. Staff progress through statuses — ASSIGNED → IN_PROGRESS → RESOLVED — leaving comments along the way. The manager closes the case. Every action is recorded in an immutable case timeline.

**Core backend features:** JWT stateless authentication, four-role RBAC enforced at the service layer, a validated status transition state machine, rule-based urgency classification, immutable audit logging, operations dashboard with COUNT queries pushed to PostgreSQL, JPA Specifications for dynamic filtering, and consistent error handling with `@RestControllerAdvice`.

**Technical decisions:** Permissions live in the service layer (not `@PreAuthorize` annotations) because access rules depend on data relationships. Status transitions use an immutable `Map<Status, Set<Status>>` so the state machine is declarative and impossible to bypass. Audit logging injects the repository directly to break a circular dependency. Classification uses the Strategy pattern so it can be swapped for ML without changing callers.

**What I learned:** How stateless JWT auth actually works end-to-end. Why service-layer permission checks are more testable than annotation-based security. How to design a state machine that's both declarative and enforced. How two-layer testing (Mockito unit + MockMvc integration) catches different classes of bugs. How Docker multi-stage builds reduce the attack surface of production images.

---

## CV-Ready Project Descriptions

### Short CV version

> Built a resident case management REST API for accommodation providers with Java 17, Spring Boot 3, Spring Security (JWT), and PostgreSQL. Implemented role-based access control, status workflow validation, rule-based urgency classification, audit logging, and dashboard APIs. 110 automated tests, Docker Compose, GitHub Actions CI.

### Backend-focused CV version

> Designed and built a production-style REST API for managing resident repair requests and complaints across accommodation properties. Implemented stateless JWT authentication, four-role RBAC enforced at the service layer, a validated status transition state machine, rule-based keyword classification for case urgency, immutable audit logging, and role-filtered operations dashboard APIs using COUNT queries pushed to PostgreSQL. Wrote 110 automated tests across unit (Mockito) and integration (MockMvc) layers. Containerised with a Docker multi-stage build and automated CI with GitHub Actions.

### CV Bullet Points (Graduate Software Engineer)

- Designed and built a RESTful case management API for property operations with Java 17, Spring Boot 3, Spring Security, and PostgreSQL
- Implemented stateless JWT authentication and four-role RBAC (Resident, Staff, Manager, Admin) enforced at the service layer with 26 permission tests
- Built a validated status transition state machine, rule-based urgency classification (9 accommodation categories), and immutable case timeline audit logging
- Created role-filtered operations dashboard APIs using COUNT queries pushed to PostgreSQL for O(1) statistics
- Wrote 110 automated tests across unit (Mockito) and integration (MockMvc) layers covering permissions, workflows, classification, and validation
- Containerised with Docker multi-stage build (non-root JRE runtime) and Docker Compose, automated CI with GitHub Actions and PostgreSQL service containers

### Technical Keywords

Java 17, Spring Boot 3, Spring Security, JWT, Spring Data JPA, PostgreSQL, Hibernate, REST API, RBAC, JPA Specifications, JUnit 5, Mockito, MockMvc, Docker, Docker Compose, GitHub Actions, Maven, OpenAPI/Swagger

---

## GitHub Repository Presentation

### GitHub repo description (one line)

Resident repair & complaint management REST API — Spring Boot 3, JWT auth, RBAC, status workflow, audit logging, 110 tests, Docker, CI

### Suggested GitHub topics

`java` `spring-boot` `rest-api` `jwt` `spring-security` `postgresql` `docker` `github-actions` `rbac` `property-management`

### README intro (first paragraph)

> A production-style REST API for managing resident cases across accommodation providers. Residents report repair requests and complaints, property managers assign them to maintenance staff, staff resolve them through a controlled status workflow, and every action is logged in a case timeline. Built with Java 17 and Spring Boot 3 following layered architecture, JWT authentication, and role-based access control. 110 automated tests. Dockerised. CI via GitHub Actions.

---

## Interview Q&A

### Why did you build this project?

I wanted a portfolio project that demonstrates the backend skills UK graduate roles actually require — REST APIs, authentication, role-based access control, testing, Docker, CI — in a domain that's easy to explain. Property management is a real problem space I understand, and incident management patterns (cases, assignment, status progression, audit trails) map cleanly onto it.

### What real-world problem does it solve?

Accommodation providers handle repair requests and complaints informally — by email, WhatsApp, or in person. Nothing is tracked centrally, so response times are invisible, cases get lost, and managers can't see staff workload. ResolveHub provides a structured workflow: every case has a status, an owner, a timeline, and dashboard visibility.

### What is the architecture?

Standard layered architecture. Controllers handle HTTP routing and input validation. Services contain all business logic and permission checks. Repositories handle database queries using JPA Specifications for dynamic filtering. Entities map to PostgreSQL tables via Hibernate. DTOs decouple the database schema from the API contract — entities are never exposed.

### How does JWT authentication work?

When a user logs in with email and password, the server generates an HMAC-SHA256 signed JWT containing the user's ID, email, and role. Every subsequent request includes this token in the `Authorization: Bearer` header. A `JwtAuthenticationFilter` extracts the token, validates the signature and expiry, loads the user from the database, and populates the `SecurityContext`. No session store is needed — every request is self-contained.

### How did you implement RBAC?

Four roles: Resident (USER), Maintenance Staff (AGENT), Property Manager (MANAGER), and Admin. Permissions are enforced at the service layer, not with `@PreAuthorize` annotations, because the access rules depend on data relationships — "is this the resident who raised this case?" or "is this staff member assigned to this case?". This makes the rules explicit and testable with Mockito. 26 tests in `IncidentServiceTest` cover all permission combinations.

### How do status transitions work?

An immutable `Map<Status, Set<Status>>` defines every valid transition (e.g. ASSIGNED → IN_PROGRESS, IN_PROGRESS → RESOLVED). The `StatusTransitionValidator` checks every status change against this map and throws an exception for invalid moves. 21 parameterized tests cover every valid and invalid combination. Assigning a case in NEW status auto-transitions to ASSIGNED.

### How does rule-based classification work?

When a resident submits a case without specifying urgency, the `IncidentClassificationService` scans the title and description for keywords. "Flood", "gas leak", "no heating" → CRITICAL. "Broken lock", "mould", "boiler" → HIGH. "Lightbulb", "minor", "query" → LOW. SAFETY category always returns HIGH regardless of keywords. The service uses the Strategy pattern — it can be swapped for an ML-backed implementation without changing any callers. 26 tests cover all keyword tiers, category overrides, precedence, and case insensitivity.

### How do audit logs / case timeline work?

Every significant action (case created, assigned, status changed, comment added) writes an immutable `AuditLog` entity with the actor, action type, old/new values, and a human-readable message. The audit log entity is append-only — no update or delete operations exist. The `GET /api/incidents/{id}/audit-logs` endpoint returns the full timeline, filtered by the same role-based access rules as the incident itself.

### How did you test permissions and workflows?

Two layers. Unit tests (92) use Mockito with no Spring context — sub-millisecond execution. They test business logic in isolation: each role's read/write/status-update permissions, every valid and invalid status transition, all classification keyword tiers, and audit log creation. Integration tests (18) use `@WebMvcTest` with MockMvc — they load a Spring context slice (controller + security filters + validation) and test HTTP concerns: status codes, JSON structure, validation error messages, authentication rejection. The two layers catch different classes of bugs.

### How does Docker Compose help reviewers?

`docker compose up -d` starts the full stack — PostgreSQL with a healthcheck and the backend — in one command. The Dockerfile is a multi-stage build: stage 1 compiles with the full JDK, stage 2 copies only the JAR into a JRE-only image running as a non-root user. Reviewers can test the API immediately without installing Java, Maven, or PostgreSQL locally.

### What does GitHub Actions CI do?

Two jobs run on every push and pull request. The `test` job starts a PostgreSQL service container, installs JDK 17 with Maven dependency caching, and runs `./mvnw verify` — all 110 tests against a real database. The `docker` job builds the Docker image to verify the Dockerfile is valid. Neither job pushes to a registry — it's a validation pipeline.

### What would you improve next?

1. **React resident portal** — frontend consuming the existing API
2. **Email/SMS notifications** — Spring Events + async listeners for assignment and status changes
3. **SLA tracking** — scheduled task to flag overdue cases based on urgency thresholds (CRITICAL within 4 hours)
4. **Testcontainers** — repository-level tests against a real PostgreSQL to catch Hibernate query bugs
5. **File attachments** — S3-backed photo upload so residents can photograph the issue
6. **Multi-property support** — tenant-to-property mapping for portfolio managers
7. **Production deployment** — Fly.io or AWS ECS with environment-specific config profiles

---

## Screenshot Checklist

These screenshots demonstrate the working API. Save to `docs/screenshots/` and update paths in README.

| # | Screenshot | What to capture |
|---|-----------|----------------|
| 1 | **Swagger UI overview** | `http://localhost:8080/swagger-ui.html` — all endpoint groups expanded |
| 2 | **POST /api/auth/register** | Swagger or curl — resident registration with JWT response |
| 3 | **POST /api/auth/login** | Swagger or curl — login returning token |
| 4 | **POST /api/incidents** | Create a repair case ("Heating not working in Flat B204") — show auto-classified urgency |
| 5 | **Rule-based classification** | Create a case with "water leak" → CRITICAL, then "lightbulb" → LOW — show both responses |
| 6 | **PATCH /api/incidents/{id}/assign** | Property manager assigning to maintenance staff |
| 7 | **PATCH /api/incidents/{id}/status** | Status transition ASSIGNED → IN_PROGRESS |
| 8 | **GET /api/incidents/{id}/audit-logs** | Full case timeline showing create → assign → status change → comment |
| 9 | **GET /api/dashboard/summary** | Operations dashboard response with counts |
| 10 | **Terminal: ./mvnw test** | All 110 tests passing — BUILD SUCCESS |
| 11 | **Terminal: docker compose up** | Both containers healthy — PostgreSQL + backend |
| 12 | **GitHub Actions** | CI pipeline passing (green checkmark on main) |

---

## Project Status

- [x] Phase 1: Project foundation and architecture
- [x] Phase 2: Authentication and JWT
- [x] Phase 3: Incident CRUD
- [x] Phase 4: Role-based access control and assignment workflow
- [x] Phase 5: Comments and audit logs
- [x] Phase 6: Dashboard APIs and rule-based classification
- [x] Phase 7: Unit and integration tests (101 → 110 tests)
- [x] Phase 9: Docker multi-stage build and GitHub Actions CI
- [x] Phase 10: Documentation and project polish
- [x] Phase 11: Final verification, cleanup, and demo evidence
- [x] Phase 12A: Domain repositioning (accommodation operations)
- [x] Phase 13: Domain code adaptation (categories, classification, tests)
- [x] Phase 14: Final CV, GitHub, and interview presentation pack
