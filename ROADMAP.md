# Brul2 — Development Roadmap

**Last updated:** 20 October 2026  
**Current progress:** Checkpoint 6.5D completed — **168 tests passed**.  
**Next checkpoint:** 6.6 — Authentication and Authorization Integration Tests and Security Review.

Brul2 is a REST API built with Kotlin, Ktor, JetBrains Amper (Kotlin Toolchain), JetBrains Exposed SQL DSL, and H2.

The application uses a feature-based architecture, separating HTTP routes, business logic, repository interfaces, database implementations, and shared technical components.

> **Verification note:** The developer confirmed that `kotlin test` reported 168 passing tests after implementing report ownership, protected report operations, user roles, and JWT role claims. GitHub commit and push status has not been independently verified. Checklist items distinguish implemented functionality from work still requiring verification.

---

## Project Principles

- Keep the implementation simple and maintainable (KISS).
- Prefer techniques taught at school when appropriate.
- Use established libraries for generic infrastructure, security, serialization, and persistence.
- Organize business functionality by feature.
- Separate HTTP routes, services, repository interfaces, and database implementations.
- Use constructor injection and repository interfaces to support testing.
- Keep Ktor plugin configuration in `plugins/` and reusable security components in `security/`.
- Write Kotlin code and comments in English.
- Use Kotlin null-safety and avoid unnecessary `!!`.
- Test normal behavior, invalid input, and edge cases throughout development.
- Preserve important functionality from the original Brul project.
- Keep secrets outside source control.
- Commit completed checkpoints and verify successful GitHub pushes.
- Do not implement an AI agent.

---

## Progress at a Glance

| Checkpoint | Scope | Status | Confirmed Cumulative Tests |
|---|---|---|---:|
| 1 | Ktor server and health route | Completed | — |
| 2 | JSON, configuration, error handling | Completed | — |
| 3 | Report domain hierarchy | Completed | — |
| 4 | H2 and Exposed report repository | Completed | — |
| 4.1 | Report categories and type validation | Completed | 19 |
| 5.1 | ReportService CRUD | Completed | 28 |
| 5.2A | Search, filtering, distance sorting | Completed | 44 |
| 5.2B | Report metadata and additional sorting | Completed | 54 |
| 5.3 | Report request/response DTOs | Completed | 66 |
| 5.4 | Report HTTP routes | Completed | 76 |
| 5.5 | API error handling and H2 integration tests | Completed | 84 |
| 6.1 | User model and persistence | Completed | 95 |
| 6.2 | BCrypt password hashing | Completed | 105 |
| Architecture refactor | Ktor plugins and shared security packages | Completed | — |
| 6.3A | Registration and login business logic | Completed | — |
| 6.3B | Authentication DTOs and HTTP routes | Completed | — |
| 6.3C | Authentication integration tests | Completed | — |
| 6.4A | JWT configuration and token service | Completed | 150 |
| 6.4B | Ktor JWT authentication and integration tests | Completed | 156 |
| 6.5A | Report ownership model and persistence | Completed | 156 |
| 6.5B | JWT-protected report operations | Completed | 162 |
| 6.5C | Ownership authorization | Completed | 166 |
| 6.5D | User roles and JWT role claims | Completed | **168** |
| 6.6 | Authorization integration tests and security review | Next | — |
| 7 | Report voting | Planned | — |
| 8 | Help participation | Planned | — |
| 9 | Testing and quality review | Planned | — |
| 10 | Documentation and delivery | Planned | — |

**Test counts are cumulative milestones, not separate groups to add together.**

---

## Phase 1 — Project Foundation

### Checkpoint 1 — Ktor Server & Routing

**Status: Completed**

- [x] Create Brul2 with JetBrains Amper.
- [x] Configure `module.yaml` and `resources/application.yaml`.
- [x] Create the application entry point in `Application.kt`.
- [x] Implement `GET /api/v1/health`.
- [x] Start the server and test the health response.
- [x] Create `HealthRoutesTest.kt`.
- [x] Establish initial README documentation and Git history.
- [ ] Verify the initial commit was pushed to GitHub.

**Current location:** Ktor routing configuration lives in `src/nl/brul/api/plugins/Routing.kt`.

**Original commit:** `chore: initialize Brul2 Ktor project`

### Checkpoint 2 — JSON, Configuration & Error Handling

**Status: Completed**

- [x] Install Ktor `ContentNegotiation` and configure kotlinx.serialization.
- [x] Reject unknown JSON fields (`ignoreUnknownKeys = false`).
- [x] Create `AppConfig.kt` and `ApiError.kt`.
- [x] Install Ktor `StatusPages` and return JSON errors.
- [x] Test valid health JSON and error responses.
- [x] Move plugin configuration into the `plugins/` package.
- [ ] Verify the checkpoint commit and GitHub push.

**Current locations:**

- `plugins/Serialization.kt`
- `plugins/ErrorHandling.kt`
- `config/AppConfig.kt`
- `common/error/ApiError.kt`

**Suggested commit:** `feat: configure JSON and error handling`

---

## Phase 2 — Domain & Database

### Checkpoint 3 — Report Domain Models

**Status: Completed**

- [x] Create abstract `Report` and its subclasses: `SafetyReport`, `NuisanceReport`, and `HelpRequest`.
- [x] Create `Location`, `ReportType`, and `ReportStatus`.
- [x] Implement domain validation and unit tests.
- [x] Demonstrate inheritance, polymorphism, encapsulation, enums, and null-safety.
- [ ] Verify the checkpoint commit and GitHub push.

**Suggested commit:** `feat: implement report domain models`

### Checkpoint 4 — Database & Repositories

**Status: Completed**

- [x] Add H2 and JetBrains Exposed SQL DSL.
- [x] Create `DatabaseFactory.kt` and `ReportsTable.kt`.
- [x] Create `NewReport`, the `ReportRepository` interface, and `ExposedReportRepository`.
- [x] Implement report creation, retrieval, listing, updating, and deletion.
- [x] Test persistence against isolated in-memory H2 databases.
- [ ] Verify the checkpoint commit and GitHub push.

**Database:** The application uses an H2 file database for development. Integration tests use isolated in-memory H2 databases.

**Suggested commit:** `feat: add Exposed database persistence`

### Checkpoint 4.1 — Report Categories & Type Validation

**Status: Completed — 19 cumulative tests passed**

- [x] Add `ReportCategory` to the report domain and persistence model.
- [x] Validate allowed type/category combinations.
- [x] Prevent changing an existing report's type during an update.
- [x] Update tests and the PlantUML report model.
- [ ] Verify database refactoring commit and GitHub push.

| Report Type | Allowed Categories |
|---|---|
| `SafetyReport` | THEFT, SUSPICIOUS_SITUATION, TRAFFIC_SAFETY |
| `NuisanceReport` | NOISE, WASTE, VANDALISM, PARKING |
| `HelpRequest` | GROCERIES, TRANSPORT, MOVING, CHORES, OTHER |

**Suggested commit:** `refactor: validate report categories and preserve report types`

---

## Phase 3 — REST API Features

### Checkpoint 5 — Reports API

**Status: Completed — 84 cumulative tests passed at the end of Checkpoint 5**

#### Checkpoint 5.1 — ReportService CRUD

**Status: Completed — 28 cumulative tests passed**

- [x] Create `ReportService.kt` with constructor-injected `ReportRepository`.
- [x] Implement create, find by ID, list, update, and delete operations.
- [x] Validate IDs and preserve report types during updates.
- [x] Test business logic with a fake repository.

#### Checkpoint 5.2A — Search, Filtering & Distance Sorting

**Status: Completed — 44 cumulative tests passed**

- [x] Create `ReportSearch`.
- [x] Search report titles and descriptions.
- [x] Filter by report type and category.
- [x] Filter by geographic radius.
- [x] Calculate distances with `Location.distanceTo()` using the Haversine formula.
- [x] Support `ID_ASC`, `ID_DESC`, and `