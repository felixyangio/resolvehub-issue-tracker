# Interview Preparation — ResolveHub

## 30-Second Pitch

ResolveHub is an incident management REST API I built with Java 17 and Spring Boot 3. It handles the full incident lifecycle: users report issues, managers assign them to agents, agents work through a validated status workflow, and every action is audit-logged. It has four roles with enforced permissions, auto-classification of priority using keyword matching, and dashboard APIs for real-time statistics. I wrote 101 automated tests, containerised it with Docker, and set up CI with GitHub Actions.

## Architecture Explanation

The project follows a standard layered architecture: controllers handle HTTP routing and validation, services contain all business logic and permission checks, repositories handle database queries, and entities map to PostgreSQL tables via JPA.

Key decisions:
- **DTOs everywhere** — entities are never exposed to the API, which decouples the database schema from the API contract
- **Permissions in the service layer** — not using `@PreAuthorize` annotations because the access rules depend on data (e.g. "is this agent assigned to this incident?"), which annotation-based security can't express cleanly
- **Package-private visibility** — `checkReadAccess()` and `getIncidentOrThrow()` are package-private so `CommentService` and `AuditLogService` can reuse them without making them public API

## Key Technical Decisions

### Why service-layer permissions instead of @PreAuthorize?

`@PreAuthorize` checks happen before the method body runs, so you can only check roles, not data conditions like "is this the incident's creator?" or "is this agent assigned to this incident?". Putting permission logic in the service layer makes it explicit, testable with Mockito, and flexible enough for complex rules.

### Why an immutable status transition map?

Modelling transitions as `Map<Status, Set<Status>>` makes the state machine declarative and testable. 21 parameterized tests cover every combination. Any code path that changes status goes through the validator — there's no way to bypass it.

### Why inject AuditLogRepository directly into IncidentService?

IncidentService needs to write audit logs, but AuditLogService also depends on IncidentService for access checks. Injecting the repository directly breaks the circular dependency without `@Lazy` hacks or Spring events.

### Why keyword-based classification instead of ML?

For a v1, keyword matching is deterministic, testable (17 tests), and easy to explain. It demonstrates the Strategy pattern — the `IncidentClassificationService` can be swapped for an ML-backed implementation without changing any callers.

### Why COUNT queries instead of loading entities?

Dashboard statistics use `countByStatus()`, `countByPriority()`, etc. — all pushed to PostgreSQL. Loading all incidents into memory and counting in Java would be O(n) per request. The database does it in O(1) with index scans.

## Testing Strategy

**Unit tests (83)** — Mockito, no Spring context, sub-millisecond execution. These test business logic in isolation: permission checks, status transitions, classification rules, audit log creation. Each test creates exactly the state it needs and asserts one behaviour.

**Integration tests (18)** — `@WebMvcTest` with MockMvc. These load a Spring context slice (controller + security filters + validation) and test HTTP concerns: correct status codes, JSON response structure, validation error messages, authentication rejection. Services are mocked.

**Why two layers?** Unit tests catch logic bugs fast. Integration tests catch wiring bugs (wrong HTTP method, missing `@Valid`, broken JSON serialisation) that unit tests can't see. Together they give confidence without the cost of full-stack integration tests.

**What I'd add in production:** Testcontainers for repository-level tests against a real PostgreSQL, contract tests if a frontend team consumes the API, and load tests on dashboard queries.

## Docker / CI Explanation

**Dockerfile** — Multi-stage build. Stage 1 uses the full JDK to compile with Maven. Stage 2 copies only the JAR into a JRE-only image and runs as a non-root user. The final image has no compiler, no source code, and a smaller attack surface.

**docker-compose.yml** — Two services: PostgreSQL with a healthcheck (`pg_isready`), and the backend that waits for PostgreSQL to be healthy before starting. Environment variables override `application.yml` so the same config works locally and in Docker.

**GitHub Actions** — Two jobs. `test` starts a PostgreSQL service container and runs `./mvnw verify`. `docker` builds the image without pushing. Maven dependencies are cached across runs to keep build times fast.

## Future Improvements

1. **Email notifications** — Spring Events + async listeners for assignment and status change
2. **SLA tracking** — scheduled task to flag overdue incidents based on priority thresholds
3. **Testcontainers** — repository-level tests against a real database to catch JPA query bugs
4. **File attachments** — S3-backed file upload on incidents
5. **Full-text search** — Elasticsearch for searching across incident titles and descriptions
6. **Frontend** — React dashboard consuming the existing API
7. **Deployment** — Fly.io or AWS ECS with environment-specific config profiles

## Common Interview Questions

**"What's the most complex part of this project?"**
> The permission system. Four roles, each with different visibility rules that depend on the relationship between the user and the incident (creator? assigned agent? manager?). Getting this right required careful test coverage — 26 tests in `IncidentServiceTest` alone.

**"How would you scale this?"**
> The API is stateless (JWT, no sessions), so horizontal scaling is straightforward — put multiple instances behind a load balancer. The database is the bottleneck; I'd add read replicas for dashboard queries and connection pooling (HikariCP is already the default). For very high throughput, I'd move audit logging to an async event queue.

**"What would you do differently if starting over?"**
> I'd add Testcontainers from day one — some bugs only appear when Hibernate generates real SQL against a real database. I'd also consider Spring Events for audit logging to decouple the audit concern from the business logic.

**"Why not use Spring Security's built-in role annotations?"**
> They work well for simple "only ADMIN can access this endpoint" rules. But our rules are data-dependent: "users can only view incidents they created, agents can only view incidents assigned to them." That requires reading the entity from the database first, which means the check has to happen inside the method, not as a pre-condition annotation.
