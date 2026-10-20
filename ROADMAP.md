
# Brul2 — Development Roadmap

**Last updated:** 20 October 2026  
**Current progress:** Checkpoint 6.4B completed — **156 tests passed**.  
**Next checkpoint:** 6.5 — Authorization and Report Ownership.

Brul2 is a REST API built with Kotlin, Ktor, JetBrains Amper (Kotlin Toolchain), JetBrains Exposed SQL DSL, and H2.

The application uses a feature-based architecture, separating HTTP routes, business logic, repository interfaces, database implementations, and shared technical components.

> **Verification note:** The developer confirmed that `kotlin test` reported **156 passing tests** after completing JWT authentication and fixing the remaining serialization issue. GitHub commit and push status has not been independently verified. Checklist items distinguish implemented functionality from work still requiring verification.

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
| 6.2 | BCrypt password hashing | Completed | 105 |
| Architecture refactor | Ktor plugins and shared security packages | Completed | 105 |
| 6.3A | Registration and login business logic | Completed | — |
| 6.3B | Authentication DTOs and HTTP routes | Completed | — |
| 6.3C | Authentication integration tests | Completed | — |
| 6.4A | JWT configuration and token service | Completed | 150 |
| 6.4B | Ktor JWT authentication and integration tests | Completed | **156** |
| 6.5 | Authorization and report ownership | Next | — |
| 6.6 | Authorization integration tests and security review | Planned | — |
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

**Not yet implemented:** `MOST_LIKED` depends on voting functionality in Checkpoint 7.

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
- [x] Support search, filter, and sort query parameters.
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

**Important:** The implemented update method is `PUT`, not `PATCH`.

#### Checkpoint 5.5 — Error Handling & API Integration Tests

**Status: Completed — 84 cumulative tests passed**

- [x] Handle malformed JSON and invalid request payloads with JSON error responses.
- [x] Return appropriate 400, 404, 405, and 500 responses.
- [x] Test report routes against real Exposed repositories and isolated in-memory H2 databases.
- [x] Test persistence through POST/GET, updates, deletion, invalid categories, missing fields, invalid data types, and invalid nearest-sort requests.

**Suggested overall commit:** `feat: implement reports REST API`

---

## Checkpoint 6 — Users, Authentication & Authorization

**Status: In progress — Checkpoints 6.1 through 6.4B completed**

### Checkpoint 6.1 — User Domain & Database

**Status: Completed — 95 cumulative tests passed**

- [x] Create `User.kt` with `id`, `username`, `email`, `passwordHash`, and `createdAt`.
- [x] Create `NewUser.kt` for user creation input.
- [x] Create the `UserRepository` interface.
- [x] Create `UsersTable.kt` and `ExposedUserRepository.kt`.
- [x] Persist users in H2 and enforce unique email addresses.
- [x] Normalize email addresses for lookup and storage.
- [x] Update `DatabaseFactory` to create both report and user tables.
- [x] Add user model and repository tests.
- [x] Verify 95 cumulative passing tests.
- [x] Move `NewUser.kt` into `features/user/model/` and update imports.
- [ ] Verify the checkpoint commit and GitHub push.

**Current user fields:** `id`, `username`, `email`, `passwordHash`, `createdAt`.

**Not implemented:** A `Role` enum or role-based authorization.

### Checkpoint 6.2 — Password Hashing

**Status: Completed — 105 cumulative tests passed**

- [x] Add `org.mindrot:jbcrypt:0.4`.
- [x] Define the `PasswordHasher` interface.
- [x] Implement `BCryptPasswordHasher`.
- [x] Use BCrypt salts and configurable work factors.
- [x] Reject blank passwords and passwords exceeding 72 UTF-8 bytes before hashing.
- [x] Verify correct passwords and reject incorrect or invalid inputs.
- [x] Add password-hashing tests.
- [x] Verify 105 cumulative passing tests.
- [ ] Verify the checkpoint commit and GitHub push.

**Current source location:** `src/nl/brul/api/security/`

**Security rule:** Never store or return plaintext passwords. Never expose `passwordHash` in public API response DTOs.

### Architecture Refactor — Ktor Plugins & Security

**Status: Completed**

- [x] Move `Routing.kt` into `plugins/`.
- [x] Move `ErrorHandling.kt` into `plugins/`.
- [x] Extract JSON configuration into `plugins/Serialization.kt`.
- [x] Keep `Application.kt` responsible for startup orchestration.
- [x] Move password hashing into the top-level `security/` package.
- [x] Update test imports and package references.
- [x] Confirm the refactor with passing tests.
- [ ] Verify the refactor commit and GitHub push.

**Suggested commit:** `refactor: organize Ktor plugins and security`

### Checkpoint 6.3A — AuthService

**Status: Completed**

- [x] Create `features/auth/service/AuthService.kt`.
- [x] Inject `UserRepository` and `PasswordHasher`.
- [x] Validate registration input.
- [x] Normalize registration and login input.
- [x] Reject duplicate email registrations.
- [x] Hash passwords before persistence.
- [x] Authenticate login attempts using `PasswordHasher.verify`.
- [x] Reject invalid credentials.
- [x] Add service unit tests using fake repositories.
- [x] Verify functionality through the automated test suite.

### Checkpoint 6.3B — Authentication DTOs & Routes

**Status: Completed**

- [x] Create registration and login request DTOs.
- [x] Create safe response DTOs excluding `passwordHash`.
- [x] Implement `POST /api/v1/auth/register`.
- [x] Implement `POST /api/v1/auth/login`.
- [x] Register authentication routes in `plugins/Routing.kt`.
- [x] Add route tests for successful and invalid requests.
- [x] Integrate authentication errors with HTTP error handling.
- [x] Verify functionality through the automated test suite.

**Scope note:** Login initially validated credentials without issuing a JWT. Token issuance was added in Checkpoint 6.4B.

### Checkpoint 6.3C — Registration & Login Integration Tests

**Status: Completed**

- [x] Test registration against a real Exposed user repository and isolated H2 database.
- [x] Confirm password hashes are stored instead of plaintext passwords.
- [x] Test successful login after registration.
- [x] Test invalid login credentials and duplicate registrations.
- [x] Test invalid authentication requests.
- [x] Confirm public responses do not expose `passwordHash`.
- [x] Run the complete test suite.
- [ ] Verify the checkpoint commit and GitHub push.

### Checkpoint 6.4A — JWT Configuration & Token Service

**Status: Completed — 150 cumulative tests passed**

- [x] Add Auth0 `java-jwt` dependency.
- [x] Add Ktor authentication dependencies.
- [x] Create `security/JwtConfig.kt`.
- [x] Create `security/JwtService.kt`.
- [x] Generate JWT access tokens using HMAC-SHA256.
- [x] Include issuer, audience, subject, issue time, and expiration.
- [x] Validate signing secret length and configuration.
- [x] Reject invalid or expired tokens.
- [x] Reject invalid user IDs in token subjects.
- [x] Add JWT unit tests.
- [x] Verify 150 cumulative passing tests.

**JWT claims:**

| Claim | Purpose |
|---|---|
| `iss` | Token issuer |
| `aud` | Intended audience |
| `sub` | Authenticated user ID |
| `iat` | Token issue time |
| `exp` | Token expiration time |

**Security note:** JWT signing secrets must not be hardcoded in production source code.

### Checkpoint 6.4B — Ktor JWT Authentication & Integration Tests

**Status: Completed — 156 cumulative tests passed**

- [x] Create `plugins/Security.kt`.
- [x] Configure Ktor Authentication with the `auth-jwt` provider.
- [x] Configure JWT signature, issuer, and audience verification.
- [x] Validate token issue time and expiration.
- [x] Return a JWT access token after successful login.
- [x] Add `LoginResponse.kt` with `accessToken` and `tokenType`.
- [x] Ensure `tokenType` is explicitly serialized as `"Bearer"`.
- [x] Implement `GET /api/v1/auth/me`.
- [x] Require a valid bearer token for the profile endpoint.
- [x] Resolve the authenticated user through `UserRepository`.
- [x] Return HTTP 401 for missing or invalid tokens.
- [x] Return HTTP 401 when a token references a nonexistent user.
- [x] Keep registration publicly accessible.
- [x] Add six JWT HTTP integration tests.
- [x] Verify 156 cumulative passing tests.
- [ ] Verify the checkpoint commit and GitHub push.

**Implementation note:** `AuthRoutes.kt` currently allows optional JWT dependencies for compatibility with existing standalone route tests. This can be simplified during a future test refactor.

### Local Environment Configuration — dotenv-kotlin

**Status: Implemented**

- [x] Add the `dotenv-kotlin` dependency.
- [x] Load local JWT configuration from `.env`.
- [x] Configure `JWT_SECRET`, `JWT_ISSUER`, `JWT_AUDIENCE`, and `JWT_VALIDITY_MILLIS`.
- [x] Keep `.env` outside Git through `.gitignore`.
- [x] Document the required variables in `.env.example`.
- [x] Avoid manually setting `JWT_SECRET` before local startup.
- [ ] Verify the `.env` file is not tracked by Git.
- [ ] Verify application startup using `kotlin run` if not already manually checked.

**Example `.env.example`:**

```dotenv id="a1jpfu"
JWT_SECRET=replace-with-a-random-secret-of-at-least-32-bytes
JWT_ISSUER=brul-api
JWT_AUDIENCE=brul-client
JWT_VALIDITY_MILLIS=3600000
```

**Important:** The example secret is a placeholder. Use a securely generated random secret for real deployments.

### Current Authentication Endpoints

| Method | Path | Authentication | Status |
|---|---|---|---|
| `POST` | `/api/v1/auth/register` | Public | Implemented |
| `POST` | `/api/v1/auth/login` | Public | Implemented |
| `GET` | `/api/v1/auth/me` | Bearer JWT | Implemented |

**Important:** The authenticated profile endpoint is `/api/v1/auth/me`, not the previously planned `/api/v1/users/me`.

### Checkpoint 6.5 — Authorization & Report Ownership

**Status: Next — Not started**

**Goal:** Associate reports with their creators and enforce authorization rules using the existing JWT authentication system.

#### 6.5A — Report Ownership Model & Persistence

- [ ] Add `ownerId` to the report domain model.
- [ ] Update report subclasses and constructors where required.
- [ ] Add `ownerId` to `ReportsTable`.
- [ ] Define a relationship between reports and users.
- [ ] Update `NewReport` and repository mapping.
- [ ] Ensure ownership survives report updates.
- [ ] Determine how existing development data should be handled.
- [ ] Update domain, repository, and integration tests.
- [ ] Verify database behavior with isolated H2 tests.

#### 6.5B — JWT-Protected Report Operations

- [ ] Require authentication for report creation.
- [ ] Require authentication for report updates.
- [ ] Require authentication for report deletion.
- [ ] Extract the authenticated user ID from the JWT principal.
- [ ] Assign the authenticated user as the report owner during creation.
- [ ] Prevent clients from choosing or changing `ownerId` directly.
- [ ] Preserve public report listing and detail retrieval where intended.
- [ ] Return HTTP 401 when authentication is missing or invalid.

#### 6.5C — Ownership Authorization

- [ ] Allow report owners to update their own reports.
- [ ] Allow report owners to delete their own reports.
- [ ] Reject modification attempts by other authenticated users.
- [ ] Return HTTP 403 for forbidden ownership operations.
- [ ] Keep ownership checks in the service layer where appropriate.
- [ ] Prevent changes to ownership through update DTOs.
- [ ] Add service tests for ownership and authorization rules.
- [ ] Add HTTP tests for allowed and forbidden operations.

**Expected behavior:**

| Scenario | Expected result |
|---|---|
| Anonymous user lists reports | HTTP 200 |
| Anonymous user retrieves a report | HTTP 200 |
| Anonymous user creates a report | HTTP 401 |
| Authenticated user creates a report | HTTP 201 |
| Report owner updates their report | Success |
| Report owner deletes their report | Success |
| Different authenticated user updates a report | HTTP 403 |
| Different authenticated user deletes a report | HTTP 403 |
| Invalid or expired token accesses a protected route | HTTP 401 |

These are planned expectations, not currently implemented behavior.

**Suggested commit:** `feat: add report ownership and authorization`

### Checkpoint 6.6 — Authorization Integration & Security Review

**Status: Planned**

- [ ] Test registration → login → token → report creation.
- [ ] Verify that newly created reports belong to the authenticated user.
- [ ] Test updating and deleting reports as their owner.
- [ ] Test ownership violations using a different authenticated user.
- [ ] Test missing, invalid, and expired tokens.
- [ ] Verify correct HTTP 401 and 403 behavior.
- [ ] Confirm users cannot change report ownership.
- [ ] Review authentication error messages.
- [ ] Review JWT secret handling and `.env` configuration.
- [ ] Confirm passwords and password hashes are never exposed.
- [ ] Verify database and API behavior together.
- [ ] Run the complete test suite and document the result.
- [ ] Verify checkpoint commits and GitHub pushes.

**Suggested overall commit:** `feat: complete authentication and authorization`

---

## Checkpoint 7 — Voting

**Status: Planned**

- [ ] Create vote domain models, DTOs, and `VotesTable.kt`.
- [ ] Implement the vote repository, `VoteService.kt`, and `VoteRoutes.kt`.
- [ ] Support likes, dislikes, and changing or removing a vote.
- [ ] Prevent duplicate active votes.
- [ ] Prevent users from voting on their own reports.
- [ ] Calculate vote totals and expose the required response fields.
- [ ] Add `MOST_LIKED` sorting after vote counts exist.
- [ ] Require authentication for voting.
- [ ] Test valid votes, duplicate votes, vote changes, removal, ownership rules, and unauthorized requests.
- [ ] Add HTTP examples, run tests, and verify the GitHub push.

**Suggested commit:** `feat: implement report voting`

---

## Checkpoint 8 — Help Participation

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

**School requirements to verify:** At least three meaningful unit tests and three meaningful integration tests, plus any individual portfolio-specific requirements.

Brul2 already exceeds the basic numerical threshold, but final test coverage and assignment compliance still need review.

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

**Existing documentation:** README, development roadmap, checkpoint notes, and PlantUML diagrams. Their final completeness has not been independently verified.

**Suggested commit:** `docs: finalize Brul2 documentation`

---

## Current Architecture

Brul2 uses a feature-based structure with dedicated technical packages.

```text id="f7fw9m"
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
│   ├── Security.kt
│   └── Serialization.kt
├── security/
│   ├── BCryptPasswordHasher.kt
│   ├── PasswordHasher.kt
│   ├── JwtConfig.kt
│   └── JwtService.kt
└── features/
    ├── auth/
    │   ├── dto/
    │   ├── routes/
    │   └── service/
    ├── report/
    │   ├── dto/
    │   ├── model/
    │   ├── repository/
    │   ├── routes/
    │   └── service/
    └── user/
        ├── model/
        │   ├── User.kt
        │   └── NewUser.kt
        └── repository/
            ├── UserRepository.kt
            ├── ExposedUserRepository.kt
            └── UsersTable.kt
```

### Responsibilities

| Package | Responsibility |
|---|---|
| `plugins` | Install and configure Ktor serialization, routing, error handling, and JWT authentication |
| `security` | Provide BCrypt password hashing and JWT token functionality |
| `features/report` | Implement report models, DTOs, business rules, persistence, and HTTP routes |
| `features/user` | Model users and persist user records |
| `features/auth` | Implement registration, login, and authenticated profile access |
| `database` | Connect to H2 and initialize tables |
| `config` | Read application configuration |
| `common/error` | Define shared API error response data |

### Request Flow

```text id="a7b7bc"
HTTP client
    |
    v
Ktor plugins
(JSON, error handling, JWT authentication)
    |
    v
Feature route
    |
    v
Request DTO / mapper
    |
    v
Service (business logic)
    |
    v
Repository interface
    |
    v
Exposed repository implementation
    |
    v
H2 database
```

Responses travel back through the layers.

The repository interface defines a contract; it is not a separate network request or database operation.

### Authentication Flow

```text id="6ebt8r"
POST /api/v1/auth/login
    |
    v
AuthRoutes
    |
    v
AuthService
    |
    +--> UserRepository
    |
    +--> PasswordHasher.verify()
    |
    v
JwtService.generateToken()
    |
    v
HTTP 200 + Bearer access token
```

Protected endpoint flow:

```text id="30uvb3"
GET /api/v1/auth/me
Authorization: Bearer <token>
    |
    v
Ktor JWT Authentication
    |
    v
Validate signature and claims
    |
    v
Extract user ID
    |
    v
UserRepository.findById()
    |
    v
HTTP 200 + public user profile
```

### Startup Flow

`Application.kt`:

1. Loads application configuration.
2. Loads JWT settings using `dotenv-kotlin`.
3. Creates `JwtConfig` and `JwtService`.
4. Configures JSON serialization.
5. Configures centralized error handling.
6. Installs Ktor JWT authentication.
7. Initializes the database.
8. Registers health, report, and authentication routes.

This setup runs at application startup, not for each HTTP request.

### Current API Base URL

Local development:

```text id="rq1brp"
http://localhost:8080
```

### Current Implemented Endpoints

| Method | Path | Authentication |
|---|---|---|
| `GET` | `/api/v1/health` | Public |
| `GET` | `/api/v1/reports` | Public |
| `GET` | `/api/v1/reports/{id}` | Public |
| `POST` | `/api/v1/reports` | Public |
| `PUT` | `/api/v1/reports/{id}` | Public |
| `DELETE` | `/api/v1/reports/{id}` | Public |
| `POST` | `/api/v1/auth/register` | Public |
| `POST` | `/api/v1/auth/login` | Public |
| `GET` | `/api/v1/auth/me` | Bearer JWT |

Report ownership restrictions will be introduced in Checkpoint 6.5.

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

```powershell id="pcbrha"
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

## Next Action — Checkpoint 6.5A

Implement **Report Ownership Model & Persistence**.

Start by examining the existing report domain hierarchy, `ReportsTable`, `NewReport`, repository interfaces, and DTO mappings.

Introduce an `ownerId` that links each report to its creator while preserving the existing report functionality.

Update all affected constructors, mappings, repositories, and tests together.

After ownership persistence is working, continue with JWT-protected report operations and authorization rules.

**Baseline:** 156 passing tests before Checkpoint 6.5.

**Next goal:** Preserve all existing tests while adding coverage for report ownership and authorization.
