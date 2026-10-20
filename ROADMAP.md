# Brul2 — Development Roadmap

**Last updated:** 20 October 2026  
**Current progress:** Checkpoint 6.2 completed; **105 tests passed**.  
**Next checkpoint:** 6.3A — AuthService (registration and login business logic).

Brul2 is a REST API built with Kotlin, Ktor, JetBrains Amper (Kotlin Toolchain), JetBrains Exposed SQL DSL, and H2. We organize the application by feature and keep Ktor plugin configuration and security mechanisms separate from business logic.

> **Verification note:** The developer confirmed that `kotlin test` compiled both the application and test sources and reported **105 passing tests** after the project-structure refactor. We have not independently verified GitHub push status or every documentation update. Checklist items distinguish confirmed implementation from work that still needs verification.

## Project Principles

- Keep the implementation simple and maintainable (KISS).
- Prefer techniques taught at school when appropriate, but use established libraries for generic infrastructure, security, serialization, and persistence rather than reinventing them.
- Organize business functionality by feature.
- Separate HTTP routes, services, repository interfaces, and database implementations.
- Use constructor injection and repository interfaces to support testing.
- Keep Ktor plugin configuration in `plugins/` and technical security components in `security/`.
- Write Kotlin code and comments in English.
- Use Kotlin null-safety; avoid unnecessary `!!`.
- Test normal behavior, invalid input, and edge cases throughout development.
- Preserve important functionality from the original Brul project.
- Commit each completed checkpoint and verify that the changes reach GitHub.
- Do not implement an AI agent.

---

## Progress at a Glance

| Checkpoint | Scope | Status | Confirmed cumulative tests |
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
| 6.2 | BCrypt password hashing | Completed | **105** |
| Architecture refactor | Ktor `plugins/` and top-level `security/` | Completed | **105** |
| 6.3A–C | Registration, login, routes, integration tests | Next | — |
| 6.4 | JWT access tokens | Planned | — |
| 6.5 | Authentication and authorization rules | Planned | — |
| 6.6 | End-to-end authentication tests | Planned | — |
| 7 | Voting | Planned | — |
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

**Current location:** Ktor routing configuration now lives in `src/nl/brul/api/plugins/Routing.kt` after the architecture refactor.

**Original commit:** `chore: initialize Brul2 Ktor project`

### Checkpoint 2 — JSON, Configuration & Error Handling

**Status: Completed**

- [x] Install Ktor `ContentNegotiation` and configure kotlinx.serialization.
- [x] Reject unknown JSON fields (`ignoreUnknownKeys = false`).
- [x] Create `AppConfig.kt` and `ApiError.kt`.
- [x] Install Ktor `StatusPages` and return JSON errors.
- [x] Test valid health JSON and error responses.
- [x] Move plugin configuration into the `plugins/` package during the later refactor.
- [ ] Verify the checkpoint commit and GitHub push.

**Current locations:** `plugins/Serialization.kt`, `plugins/ErrorHandling.kt`, `config/AppConfig.kt`, and `common/error/ApiError.kt`.

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
- [ ] Verify the checkpoint commit and GitHub push.

| Report type | Allowed categories |
|---|---|
| `SafetyReport` | `THEFT`, `SUSPICIOUS_SITUATION`, `TRAFFIC_SAFETY` |
| `NuisanceReport` | `NOISE`, `WASTE`, `VANDALISM`, `PARKING` |
| `HelpRequest` | `GROCERIES`, `TRANSPORT`, `MOVING`, `CHORES`, `OTHER` |

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

**Suggested commit:** `feat: add report service with CRUD tests`

#### Checkpoint 5.2A — Search, Filtering & Distance Sorting

**Status: Completed — 44 cumulative tests passed**

- [x] Create `ReportSearch`.
- [x] Search report titles and descriptions.
- [x] Filter by report type and category.
- [x] Filter by geographic radius.
- [x] Calculate distances with `Location.distanceTo()` using the Haversine formula.
- [x] Support `ID_ASC`, `ID_DESC`, and `NEAREST` sorting.
- [x] Test filters, combined queries, invalid parameters, and distance calculations.

#### Checkpoint 5.2B — Report Metadata & Sorting

**Status: Completed — 54 cumulative tests passed**

- [x] Add `createdAt: Long` and `viewCount: Int` to the report model and database mapping.
- [x] Preserve metadata during updates.
- [x] Support `NEWEST` and `MOST_VIEWED` sorting.
- [x] Test metadata persistence and sorting.

**Not yet implemented:** `MOST_LIKED` depends on voting functionality in Checkpoint 7. Do not advertise it as an available sort order.

#### Checkpoint 5.3 — Request & Response DTOs

**Status: Completed — 66 cumulative tests passed**

- [x] Create `CreateReportRequest.kt`, `UpdateReportRequest.kt`, and `ReportResponse.kt`.
- [x] Implement `ReportMapper.kt` with mapping extensions.
- [x] Use kotlinx.serialization for HTTP JSON payloads.
- [x] Test request validation and response mapping.

#### Checkpoint 5.4 — Ktor Report Routes

**Status: Completed — 76 cumulative tests passed**

- [x] Implement `ReportRoutes.kt`.
- [x] Register report endpoints under `/api/v1`.
- [x] Connect routes to `ReportService`.
- [x] Support search/filter/sort query parameters.
- [x] Add HTTP route tests.

**Implemented endpoints:**

| Method | Path | Purpose |
|---|---|---|
| `GET` | `/api/v1/health` | Check API health |
| `GET` | `/api/v1/reports` | List, search, filter, and sort reports |
| `GET` | `/api/v1/reports/{id}` | Retrieve a report |
| `POST` | `/api/v1/reports` | Create a report |
| `PUT` | `/api/v1/reports/{id}` | Update a report |
| `DELETE` | `/api/v1/reports/{id}` | Delete a report |

**Important:** The implemented update method is **`PUT`**, not the previously proposed `PATCH`.

#### Checkpoint 5.5 — Error Handling & API Integration Tests

**Status: Completed — 84 cumulative tests passed**

- [x] Handle malformed JSON and invalid request payloads with JSON error responses.
- [x] Return appropriate 400, 404, 405, and 500 responses.
- [x] Test report routes against real Exposed repositories and isolated in-memory H2 databases.
- [x] Test persistence through POST/GET, updates, deletion, invalid categories, missing fields, invalid data types, and invalid nearest-sort requests.

**Suggested overall commit:** `feat: implement reports REST API`

---

### Checkpoint 6 — Users & Authentication

**Status: In progress — 6.1 and 6.2 completed; 6.3 next**

#### Checkpoint 6.1 — User Domain & Database

**Status: Completed — 95 cumulative tests passed**

- [x] Create `User.kt` with `id`, `username`, `email`, `passwordHash`, and `createdAt`.
- [x] Create `NewUser.kt` for user creation input.
- [x] Create the `UserRepository` interface.
- [x] Create `UsersTable.kt` and `ExposedUserRepository.kt`.
- [x] Persist users in H2 and enforce unique email addresses.
- [x] Normalize email addresses for lookup and storage.
- [x] Update `DatabaseFactory` to create both report and user tables.
- [x] Add 11 user model and repository tests.
- [x] Verify 95 cumulative passing tests.
- [ ] Verify the checkpoint commit and GitHub push.

**Current user fields:** `id`, `username`, `email`, `passwordHash`, `createdAt`.

**Not yet implemented:** A `Role` enum or role-based authorization. The earlier roadmap listed roles as planned work; they are not part of the completed 6.1 model.

#### Checkpoint 6.2 — Password Hashing

**Status: Completed — 105 cumulative tests passed**

- [x] Add the `org.mindrot:jbcrypt:0.4` dependency.
- [x] Define the `PasswordHasher` interface.
- [x] Implement `BCryptPasswordHasher`.
- [x] Use BCrypt salts and configurable work factors.
- [x] Reject blank passwords and passwords exceeding 72 UTF-8 bytes before hashing.
- [x] Verify correct passwords and reject incorrect or invalid inputs.
- [x] Add 10 password-hashing tests.
- [x] Verify 105 cumulative passing tests.
- [ ] Verify the checkpoint commit and GitHub push.

**Current source location:** `src/nl/brul/api/security/`  
**Current test location:** `test/nl/brul/api/security/`

**Security rule:** Never store or return plaintext passwords. Never expose `passwordHash` in public API response DTOs.

#### Architecture Refactor — Ktor Plugins & Security

**Status: Completed — 105 tests passed after refactoring**

- [x] Move `Routing.kt` to `src/nl/brul/api/plugins/`.
- [x] Move `ErrorHandling.kt` to `src/nl/brul/api/plugins/`.
- [x] Extract JSON configuration into `plugins/Serialization.kt`.
- [x] Simplify `Application.kt` to orchestrate application setup.
- [x] Move password-hashing components from `features/auth/security/` to top-level `security/`.
- [x] Move the BCrypt test to the corresponding `test/.../security/` package.
- [x] Update outdated imports in route and error-handling tests.
- [x] Confirm application and test compilation with 105 passing tests.
- [ ] Verify the refactor commit and GitHub push.

**Suggested commit:** `refactor: organize Ktor plugins and security`

#### Checkpoint 6.3A — AuthService

**Status: Next — Not started**

- [ ] Create `features/auth/service/AuthService.kt`.
- [ ] Inject `UserRepository` and `PasswordHasher` through the constructor.
- [ ] Validate registration input and enforce a clear password policy.
- [ ] Normalize emails consistently with repository behavior.
- [ ] Reject duplicate email registrations with a well-defined error.
- [ ] Hash passwords before passing `NewUser` to the repository.
- [ ] Authenticate login attempts using `PasswordHasher.verify`.
- [ ] Reject invalid credentials without revealing whether an email exists.
- [ ] Return safe user information without password hashes.
- [ ] Add unit tests with fake repositories and a fake password hasher where useful.
- [ ] Run `kotlin test`.

#### Checkpoint 6.3B — Authentication DTOs & Routes

**Status: Planned**

- [ ] Create registration and login request DTOs.
- [ ] Create safe response DTOs that exclude `passwordHash`.
- [ ] Implement `POST /api/v1/auth/register`.
- [ ] Implement `POST /api/v1/auth/login`.
- [ ] Register authentication routes in `plugins/Routing.kt`.
- [ ] Map validation failures to HTTP 400, duplicate registration to HTTP 409, and invalid credentials to HTTP 401.
- [ ] Add route tests for valid and invalid requests.
- [ ] Document example HTTP requests.
- [ ] Run `kotlin test`.

**Scope note:** Login in 6.3 validates credentials but does not yet issue a JWT. Token issuance belongs to 6.4.

#### Checkpoint 6.3C — Registration & Login Integration Tests

**Status: Planned**

- [ ] Test registration against a real Exposed user repository and isolated H2 database.
- [ ] Confirm that the database stores a BCrypt hash rather than plaintext.
- [ ] Test successful login after registration.
- [ ] Test duplicate emails, wrong passwords, unknown accounts, and malformed JSON.
- [ ] Confirm that responses never contain `passwordHash`.
- [ ] Run the complete test suite and update documentation.
- [ ] Verify the checkpoint commit and GitHub push.

#### Checkpoint 6.4 — JWT Authentication

**Status: Planned**

- [ ] Select and configure Ktor's JWT authentication support.
- [ ] Implement token creation with appropriate signing, expiration, issuer, and audience settings.
- [ ] Keep signing secrets outside source control.
- [ ] Configure JWT verification in `plugins/Authentication.kt`.
- [ ] Return an access token after successful login.
- [ ] Validate expired, malformed, missing, and invalidly signed tokens.
- [ ] Start with access tokens only; do not add refresh tokens without a clear requirement.
- [ ] Add unit and HTTP tests.

#### Checkpoint 6.5 — Authentication & Authorization

**Status: Planned**

- [ ] Add `GET /api/v1/users/me` for authenticated users.
- [ ] Introduce report ownership (`ownerId`) and database support.
- [ ] Define which report operations require authentication.
- [ ] Enforce ownership checks on protected update and delete operations.
- [ ] Add role-based authorization only if the requirements justify roles.
- [ ] Test unauthorized and forbidden requests separately (401 versus 403).
- [ ] Preserve existing public behavior only where explicitly intended.

#### Checkpoint 6.6 — Authentication Integration & Security Review

**Status: Planned**

- [ ] Test registration → login → token → protected endpoint.
- [ ] Test invalid and expired tokens.
- [ ] Test unauthorized access and ownership violations.
- [ ] Review error messages, secret handling, and password-hash exposure.
- [ ] Verify database and API behavior together.
- [ ] Run the complete test suite and document results.
- [ ] Verify checkpoint commits and GitHub pushes.

**Planned endpoints (not implemented as of the last confirmed test run):**

| Method | Path | Planned checkpoint |
|---|---|---|
| `POST` | `/api/v1/auth/register` | 6.3B |
| `POST` | `/api/v1/auth/login` | 6.3B; token response added in 6.4 |
| `GET` | `/api/v1/users/me` | 6.5 |

**Suggested overall commit:** `feat: implement user authentication`

---

### Checkpoint 7 — Voting

**Status: Planned**

- [ ] Create vote domain models, DTOs, and `VotesTable.kt`.
- [ ] Implement the vote repository, `VoteService.kt`, and `VoteRoutes.kt`.
- [ ] Support likes/dislikes and changing or removing a vote.
- [ ] Prevent duplicate active votes.
- [ ] Prevent users from voting on their own reports.
- [ ] Calculate vote totals and expose the required response fields.
- [ ] Add `MOST_LIKED` sorting after vote counts exist.
- [ ] Require authentication for voting.
- [ ] Test valid votes, duplicate votes, vote changes, removal, ownership rules, and unauthorized requests.
- [ ] Add HTTP examples, run tests, and verify the GitHub push.

**Suggested commit:** `feat: implement report voting`

### Checkpoint 8 — Help Participation

**Status: Planned**

- [ ] Create participation models and `HelpParticipationsTable.kt`.
- [ ] Implement the help repository, `HelpService.kt`, and `HelpRoutes.kt`.
- [ ] Support joining and leaving help requests.
- [ ] Track active participants and enforce request capacity.
- [ ] Model and enforce `OPEN`, `FULL`, and `CLOSED` behavior.
- [ ] Reject duplicate active participation.
- [ ] Prevent request owners from joining their own requests.
- [ ] Support closing help requests.
- [ ] Prevent capacity reductions below the active participant count.
- [ ] Require authentication and test authorization.
- [ ] Test valid participation, capacity limits, closed requests, owner restrictions, and leave/rejoin behavior.
- [ ] Add HTTP examples, run tests, and verify the GitHub push.

**Suggested commit:** `feat: implement help participation`

---

## Phase 4 — Quality & Delivery

### Checkpoint 9 — Testing & Quality

**Status: Planned**

- [ ] Review unit, route, repository, and end-to-end test coverage.
- [ ] Verify every API endpoint and relevant HTTP status code.
- [ ] Test malformed input, boundary values, missing records, and duplicate operations.
- [ ] Review authentication, authorization, ownership, and password handling.
- [ ] Check database behavior and isolated test setup.
- [ ] Remove unnecessary code and review Kotlin null-safety.
- [ ] Run the full test suite and record the final test count.
- [ ] Verify the checkpoint commit and GitHub push.

**School requirements to verify:** At least three meaningful unit tests and three meaningful integration tests, plus any individual portfolio-specific requirements. Brul2 already exceeds the basic numerical threshold, but final coverage and assignment compliance still need review.

**Suggested commit:** `test: complete API test coverage`

### Checkpoint 10 — Documentation & Delivery

**Status: Planned; initial documentation exists**

- [ ] Complete and verify `README.md`.
- [ ] Document the current architecture and project structure.
- [ ] Document installation, configuration, and startup.
- [ ] Document implemented endpoints, DTOs, query parameters, and error responses.
- [ ] Add tested HTTP request examples.
- [x] Create the initial UML class diagram.
- [x] Create the initial UML package diagram.
- [x] Create the initial report-creation sequence diagram.
- [ ] Update UML diagrams to reflect the final code.
- [ ] Produce at least three relevant sequence diagrams and verify their accuracy.
- [ ] Explain the project's OOP and functional-programming choices.
- [ ] Complete the school report and individual portfolio requirements.
- [ ] Prepare and record the demonstration video.
- [ ] Run final tests, verify requirements, and push the final commit.

**Existing documentation:** README, development roadmap, checkpoint notes, and PlantUML diagrams. Their current completeness has not been independently verified.

**Suggested commit:** `docs: finalize Brul2 documentation`

---

## Current Architecture

Brul2 uses a feature-based structure with dedicated technical packages.

```text
src/nl/brul/api/
├── Application.kt
├── common/
│   └── error/
│       └── ApiError.kt
├── config/
│   └── AppConfig.kt
├── database/
│   └── DatabaseFactory.kt
├── plugins/
│   ├── ErrorHandling.kt
│   ├── Routing.kt
│   └── Serialization.kt
├── security/
│   ├── BCryptPasswordHasher.kt
│   └── PasswordHasher.kt
└── features/
    ├── report/
    │   ├── dto/
    │   ├── model/
    │   ├── repository/
    │   ├── routes/
    │   └── service/
    └── user/
        ├── model/
        └── repository/
```

**Note:** Empty directories such as `features/auth/` may exist locally; they do not represent implemented registration or login functionality. The tree above lists meaningful implemented packages, not every empty directory.

### Responsibilities

| Package | Responsibility |
|---|---|
| `plugins` | Install and configure Ktor serialization, routing, and error handling; later JWT authentication |
| `security` | Provide reusable security mechanisms such as BCrypt hashing and future JWT support |
| `features/report` | Implement report models, DTOs, business rules, persistence, and HTTP routes |
| `features/user` | Model users and persist user records |
| `features/auth` | **Planned:** registration and login workflows |
| `database` | Connect to H2 and initialize tables |
| `config` | Read application configuration |
| `common/error` | Define shared API error response data |

### Request Flow

```text
HTTP client
    ↓
Ktor plugins (JSON parsing, error handling; JWT later)
    ↓
Feature route
    ↓
Request DTO / mapper
    ↓
Service (business logic)
    ↓
Repository interface
    ↓
Exposed repository implementation
    ↓
H2 database
```

Responses travel back through the layers. The repository interface defines a contract; it is not a separate network request or database operation.

### Startup Flow

`Application.kt` loads configuration, configures serialization and error handling, initializes the database, and registers routes. It performs this setup at startup rather than for each HTTP request.

### Current API Base URL

Local development: `http://localhost:8080`

Currently implemented endpoints: the health endpoint and the five report CRUD/list routes documented in Checkpoint 5.4. Authentication routes are not implemented yet.

---

## Development Workflow

For each checkpoint:

1. Define the feature scope and relevant school concepts.
2. Identify normal, invalid, and edge-case test scenarios.
3. Implement the required code without changing unrelated features.
4. Run `kotlin test` and resolve failures.
5. Manually verify relevant HTTP endpoints when available.
6. Update the roadmap, README, API examples, and UML where necessary.
7. Review `git status` and ensure generated files and secrets stay out of the commit.
8. Commit the checkpoint and verify the push to GitHub.

### GitHub Workflow

```powershell
git status
kotlin test
git add .
git diff --cached --stat
git commit -m "type: describe completed checkpoint"
git push
```

Commit prefixes:

- `chore:` Setup and configuration.
- `feat:` New functionality.
- `fix:` Bug fixes.
- `test:` Tests and test improvements.
- `refactor:` Structural improvements without changing behavior.
- `docs:` Documentation changes.

Only mark a GitHub-related checkbox complete after confirming the relevant commit and successful push.

---

## Next Action — Checkpoint 6.3A

Implement `AuthService` in `features/auth/service/` using the existing `UserRepository` and `PasswordHasher` interfaces. Start with registration validation, BCrypt hashing, login credential verification, and unit tests. Keep HTTP DTOs/routes for 6.3B, database-backed authentication tests for 6.3C, and JWT token issuance for 6.4.

**Baseline:** 105 passing tests before Checkpoint 6.3A.
