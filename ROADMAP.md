# Brul2 — Development Roadmap

Brul2 is a REST API built with Kotlin, Ktor, JetBrains Amper, Exposed, and H2.

The project follows a feature-based architecture and focuses on simplicity, maintainability, object-oriented programming (OOP), functional programming (FP), and the requirements of the school assignment.

**Current progress:** Checkpoint 5.1 completed — 28 tests passed.

## Project Principles

- Keep It Simple (KISS).
- Use libraries and techniques covered in the school lessons.
- Organize code by feature.
- Separate routes, services, and repositories.
- Use repository interfaces for testability.
- Write code and comments in English.
- Use Kotlin null-safety and avoid `!!`.
- Write tests throughout development.
- Preserve the important functionality of the original Brul project.
- Complete each checkpoint with a GitHub commit.
- Do not implement an AI agent.

---

## Phase 1 — Project Foundation

### Checkpoint 1 — Ktor Server & Routing

**Status:** Completed

Tasks:

- [x] Create Brul2 using JetBrains Amper.
- [x] Configure `module.yaml`.
- [x] Configure `application.yaml`.
- [x] Create `Application.kt`.
- [x] Create `Routing.kt`.
- [x] Implement `GET /api/v1/health`.
- [x] Start the server successfully.
- [x] Create `HealthRoutesTest.kt`.
- [x] Run `kotlin test`.
- [x] Create project README documentation.
- [x] Create the initial Git commit.
- [ ] Verify the initial commit is pushed to GitHub.

Tests:

- Health endpoint returns HTTP 200.
- Health endpoint returns the expected response.

Commit:

`chore: initialize Brul2 Ktor project`

### Checkpoint 2 — JSON, Configuration & Error Handling

**Status:** Completed

Tasks:

- [x] Configure Ktor ContentNegotiation.
- [x] Configure kotlinx.serialization.
- [x] Create `AppConfig.kt`.
- [x] Create `ApiError.kt`.
- [x] Create `ErrorHandling.kt`.
- [x] Configure Ktor StatusPages.
- [x] Return JSON from the health endpoint.
- [x] Add relevant tests.
- [x] Update project documentation.
- [ ] Verify checkpoint changes are committed and pushed to GitHub.

Tests:

- Health endpoint returns valid JSON.
- Health endpoint returns HTTP 200.
- Invalid requests return an appropriate error response.
- Unexpected errors return HTTP 500 without exposing internal details.

Suggested commit:

`feat: configure JSON and error handling`

---

## Phase 2 — Domain & Database

### Checkpoint 3 — Report Domain Models

**Status:** Completed

Tasks:

- [x] Create abstract `Report` class.
- [x] Create `SafetyReport`.
- [x] Create `NuisanceReport`.
- [x] Create `HelpRequest`.
- [x] Create `Location`.
- [x] Create `ReportType`.
- [x] Create `ReportStatus`.
- [x] Define initial domain validation rules.
- [x] Write unit tests.
- [x] Update documentation.
- [ ] Verify checkpoint changes are committed and pushed to GitHub.

School concepts:

- Abstract classes.
- Inheritance.
- Polymorphism.
- Encapsulation.
- Data classes.
- Enums.
- Null safety.

Tests:

- Valid report creation.
- Invalid report title.
- Invalid location coordinates.
- Polymorphic behavior.
- Help-request volunteer validation.

Suggested commit:

`feat: implement report domain models`

### Checkpoint 4 — Database & Repositories

**Status:** Completed

Tasks:

- [x] Add H2 dependency.
- [x] Add Exposed DSL dependencies.
- [x] Create `DatabaseFactory.kt`.
- [x] Create `ReportsTable.kt`.
- [x] Create `NewReport.kt`.
- [x] Create `ReportRepository` interface.
- [x] Create `ExposedReportRepository`.
- [x] Configure database initialization.
- [x] Write database integration tests.
- [x] Update documentation.
- [ ] Verify checkpoint changes are committed and pushed to GitHub.

Tests:

- Save a report.
- Retrieve a report.
- Retrieve all three report types.
- Update a report.
- Delete a report.
- Return `null` for an unknown report.

Database:

- H2 database.
- JetBrains Exposed SQL DSL.
- One `reports` table for all three report types.
- In-memory H2 databases for repository integration tests.

Suggested commit:

`feat: add Exposed database persistence`

### Checkpoint 4.1 — Report Categories & Database Refactoring

**Status:** Completed

This checkpoint improves the domain and repository implementation from Checkpoints 3 and 4.

Tasks:

- [x] Add `ReportCategory` enum.
- [x] Add a required category to the abstract `Report` class.
- [x] Replace the artificial `summary()` method with category validation.
- [x] Implement category validation in `SafetyReport`.
- [x] Implement category validation in `NuisanceReport`.
- [x] Implement category validation in `HelpRequest`.
- [x] Update `NewReport` to use `ReportCategory`.
- [x] Update the database schema to require a category.
- [x] Update Exposed enum conversion.
- [x] Prevent changing an existing report's type during updates.
- [x] Update domain unit tests.
- [x] Update repository integration tests.
- [x] Update checkpoint documentation.
- [x] Update PlantUML diagrams.
- [x] Run the complete test suite.
- [ ] Verify checkpoint changes are committed and pushed to GitHub.

Allowed categories:

| Report Type | Categories |
|---|---|
| SafetyReport | THEFT, SUSPICIOUS_SITUATION, TRAFFIC_SAFETY |
| NuisanceReport | NOISE, WASTE, VANDALISM, PARKING |
| HelpRequest | GROCERIES, TRANSPORT, MOVING, CHORES, OTHER |

School concepts:

- Abstract classes.
- Method overriding.
- Inheritance.
- Polymorphism.
- Enums.
- Kotlin `require()`.
- Kotlin `when`.
- Null safety.
- Exposed SQL expressions.

Tests:

- Valid category combinations.
- Invalid category combinations.
- Report type validation.
- Database category persistence.
- Prevention of report type changes.

Test result:

**19 tests passed after Checkpoint 4.1.**

Suggested commit:

`refactor: validate report categories and preserve report types`

---

## Phase 3 — REST API Features

### Checkpoint 5 — Reports API

**Status:** In progress

The report API is divided into smaller checkpoints to keep implementation and testing manageable.

#### Checkpoint 5.1 — ReportService CRUD

**Status:** Completed

Tasks:

- [x] Create `ReportService.kt`.
- [x] Inject the `ReportRepository` interface through the constructor.
- [x] Implement report creation.
- [x] Implement report retrieval by ID.
- [x] Implement report listing.
- [x] Implement report updates.
- [x] Implement report deletion.
- [x] Validate positive IDs for retrieval and deletion.
- [x] Create `ReportServiceTest.kt`.
- [x] Create a fake repository for service unit tests.
- [x] Test prevention of report type changes.
- [x] Run the complete test suite.
- [x] Update README documentation.
- [ ] Verify checkpoint changes are committed and pushed to GitHub.

School concepts:

- Interfaces.
- Constructor dependency injection.
- Separation of responsibilities.
- Kotlin `require()`.
- Collections and mutable maps.
- Kotlin `when`.
- Lambdas and `sortedBy`.
- Unit testing with a fake repository.

Tests:

- Create a safety report.
- Find all reports.
- Find a report by ID.
- Return `null` for an unknown report.
- Update an existing report.
- Delete an existing report.
- Reject invalid IDs.
- Prevent report type changes.

Test result:

**28 tests passed.**

Test breakdown:

| Test Category | Passed |
|---|---:|
| Ktor integration tests | 3 |
| Report domain unit tests | 10 |
| Repository integration tests | 6 |
| Report service unit tests | 9 |
| **Total** | **28** |

Suggested commit:

`feat: add report service with CRUD tests`

#### Checkpoint 5.2 — Search, Filtering & Sorting

**Status:** Not started — Next checkpoint

Tasks:

- [ ] Define report search and filter parameters.
- [ ] Implement text search.
- [ ] Implement filtering by report type.
- [ ] Implement filtering by category.
- [ ] Implement location-based filtering.
- [ ] Implement distance calculation where needed.
- [ ] Implement sorting supported by the current data model.
- [ ] Prepare for sorting by likes and views when those fields become available.
- [ ] Write service unit tests.
- [ ] Run the complete test suite.
- [ ] Update documentation.
- [ ] Commit and push to GitHub.

Planned search parameters:

- Search text.
- Report type.
- Report category.
- Latitude and longitude.
- Search radius.
- Sort order.

Planned sorting options from the original Brul project:

- `newest`
- `mostLiked`
- `mostViewed`
- `nearest`

Some sorting options depend on functionality that has not been implemented yet, such as voting, view counts, and creation timestamps.

These dependencies must be handled explicitly rather than implementing placeholder behavior.

Tests:

- Search reports by title or description.
- Filter reports by type.
- Filter reports by category.
- Combine multiple filters.
- Filter reports by distance.
- Verify sorting behavior.
- Handle empty search results.
- Reject invalid search parameters.

#### Checkpoint 5.3 — Request & Response DTOs

**Status:** Not started

Tasks:

- [ ] Create report request DTOs.
- [ ] Create report response DTOs.
- [ ] Use kotlinx.serialization.
- [ ] Map domain objects to response DTOs.
- [ ] Map valid request DTOs to domain input.
- [ ] Define request validation rules.
- [ ] Use existing Ktor validation functionality where appropriate.
- [ ] Write DTO and validation tests.
- [ ] Run the complete test suite.
- [ ] Update documentation.
- [ ] Commit and push to GitHub.

#### Checkpoint 5.4 — Ktor Report Routes

**Status:** Not started

Tasks:

- [ ] Create `ReportRoutes.kt`.
- [ ] Connect routes to `ReportService`.
- [ ] Implement report creation endpoint.
- [ ] Implement report listing endpoint.
- [ ] Implement report retrieval endpoint.
- [ ] Implement report update endpoint.
- [ ] Implement report deletion endpoint.
- [ ] Connect search and filtering parameters.
- [ ] Write HTTP integration tests.
- [ ] Add HTTP example requests.
- [ ] Run the complete test suite.
- [ ] Update documentation.
- [ ] Commit and push to GitHub.

Endpoints (proposed):

- `POST /api/v1/reports`
- `GET /api/v1/reports`
- `GET /api/v1/reports/{id}`
- `PATCH /api/v1/reports/{id}`
- `DELETE /api/v1/reports/{id}`

#### Checkpoint 5.5 — Error Handling & API Tests

**Status:** Not started

Tasks:

- [ ] Review request validation.
- [ ] Return HTTP 400 for invalid input.
- [ ] Return HTTP 404 for unknown reports.
- [ ] Return appropriate responses for invalid updates.
- [ ] Verify JSON error responses.
- [ ] Test report CRUD endpoints.
- [ ] Test searching and filtering through HTTP.
- [ ] Verify database integration.
- [ ] Run the complete test suite.
- [ ] Update API documentation.
- [ ] Commit and push to GitHub.

Tests:

- Create a valid report.
- Reject invalid report data.
- Retrieve an existing report.
- Return 404 for an unknown report.
- Search and filter reports.
- Update a report.
- Delete a report.
- Return appropriate error responses.

Overall Checkpoint 5 suggested commit:

`feat: implement reports REST API`

### Checkpoint 6 — Users & Authentication

**Status:** Not started

Tasks:

- [ ] Create `User` model.
- [ ] Create `Role` enum.
- [ ] Create `UsersTable.kt`.
- [ ] Create user repository.
- [ ] Create authentication DTOs.
- [ ] Implement user registration.
- [ ] Implement login.
- [ ] Add BCrypt password hashing.
- [ ] Configure JWT authentication.
- [ ] Protect endpoints requiring authentication.
- [ ] Implement authorization rules.
- [ ] Write unit and integration tests.
- [ ] Add HTTP example requests.
- [ ] Commit and push to GitHub.

Endpoints (proposed):

- `POST /api/v1/auth/register`
- `POST /api/v1/auth/login`
- `GET /api/v1/users/me`

Tests:

- Register a valid user.
- Reject duplicate registration.
- Login with valid credentials.
- Reject incorrect passwords.
- Reject requests without authentication.
- Verify authorization rules.

Notes:

- Start with access tokens only.
- Do not implement refresh tokens unless required.
- Add report ownership and authorization after authentication is available.

Suggested commit:

`feat: implement user authentication`

### Checkpoint 7 — Voting

**Status:** Not started

Tasks:

- [ ] Create vote model and DTOs.
- [ ] Create `VotesTable.kt`.
- [ ] Create vote repository.
- [ ] Create `VoteService.kt`.
- [ ] Create `VoteRoutes.kt`.
- [ ] Implement voting on reports.
- [ ] Implement changing or removing votes.
- [ ] Prevent duplicate active votes.
- [ ] Prevent voting on your own report.
- [ ] Implement vote counting.
- [ ] Require authentication.
- [ ] Write unit and integration tests.
- [ ] Add HTTP example requests.
- [ ] Commit and push to GitHub.

Tests:

- Create a vote.
- Prevent duplicate votes.
- Change a vote.
- Remove a vote.
- Calculate vote totals.
- Reject unauthorized voting.
- Reject voting on your own report.

Suggested commit:

`feat: implement report voting`

### Checkpoint 8 — Help Participation

**Status:** Not started

Tasks:

- [ ] Create help participation model.
- [ ] Create `HelpParticipationsTable.kt`.
- [ ] Create help repository.
- [ ] Create `HelpService.kt`.
- [ ] Create `HelpRoutes.kt`.
- [ ] Implement joining a help request.
- [ ] Implement leaving a help request.
- [ ] Validate participant capacity.
- [ ] Handle help-request status.
- [ ] Prevent duplicate active participation.
- [ ] Prevent owners from joining their own help requests.
- [ ] Implement closing help requests.
- [ ] Require authentication.
- [ ] Write unit and integration tests.
- [ ] Add HTTP example requests.
- [ ] Commit and push to GitHub.

Planned help-request behavior:

- Open requests accept participants.
- Full requests reject additional participants.
- Closed requests reject participation.
- Users cannot join the same request twice while already participating.
- Owners cannot join their own requests.
- Participants can leave requests.
- Capacity cannot be reduced below the active participant count.

Tests:

- Join a help request.
- Prevent duplicate participation.
- Reject participation when capacity is full.
- Leave a help request.
- Reject participation in a closed help request.
- Reject owner participation.
- Validate capacity changes.

Suggested commit:

`feat: implement help participation`

---

## Phase 4 — Quality & Delivery

### Checkpoint 9 — Testing & Quality

**Status:** Not started

Tasks:

- [ ] Review unit test coverage.
- [ ] Review integration test coverage.
- [ ] Verify all API endpoints.
- [ ] Test invalid input.
- [ ] Test authentication and authorization.
- [ ] Test database behavior.
- [ ] Check error handling.
- [ ] Remove unnecessary code.
- [ ] Review Kotlin null-safety.
- [ ] Verify all tests pass.
- [ ] Commit and push to GitHub.

School requirements:

- At least 3 meaningful unit tests.
- At least 3 meaningful integration tests.
- Account for individual portfolio test requirements.
- Verify tests for normal behavior, invalid input, and edge cases.

Suggested commit:

`test: complete API test coverage`

### Checkpoint 10 — Documentation & Delivery

**Status:** Not started

Tasks:

- [ ] Complete `README.md`.
- [ ] Document project structure.
- [ ] Document installation and startup.
- [ ] Document API endpoints.
- [ ] Create HTTP request examples.
- [x] Create initial UML class diagram.
- [x] Create initial UML package diagram.
- [x] Create initial report creation sequence diagram.
- [ ] Update UML diagrams after later features.
- [ ] Create at least 3 relevant sequence diagrams.
- [ ] Explain OOP and FP usage.
- [ ] Complete the required report.
- [ ] Prepare the demonstration video.
- [ ] Verify school assignment requirements.
- [ ] Run final tests.
- [ ] Commit and push to GitHub.

Documentation already started:

- README.
- Development roadmap.
- Checkpoint documentation.
- PlantUML diagrams for Checkpoint 4.1.

Suggested commit:

`docs: finalize Brul2 documentation`

---

## Development Workflow

Follow these steps for every checkpoint:

1. Discuss the functionality and relevant school concepts.
2. Define test scenarios before implementation.
3. Create or modify the required files.
4. Run automated tests using `kotlin test`.
5. Manually verify relevant API endpoints when they exist.
6. Update the README and project documentation.
7. Check Git status.
8. Commit and push to GitHub.

## GitHub Workflow

```bash
git status
kotlin test
git add .
git commit -m "type: describe completed checkpoint"
git push
```

Commit naming conventions:

- `chore:` Project setup and configuration.
- `feat:` New functionality.
- `fix:` Bug fixes.
- `test:` Tests and test improvements.
- `refactor:` Code improvements without changing behavior.
- `docs:` Documentation updates.

Only commit a checkpoint when:

- The project compiles.
- Relevant tests pass.
- Existing functionality still works.
- No secrets or unnecessary generated files are staged.
- Documentation is updated where needed.

---

## Architecture

Brul2 uses a feature-based project structure.

Each feature follows this flow:

`Routes → Services → Repository Interfaces → Exposed Repositories → H2`

### Current Packages

- `config` — Application configuration.
- `common` — Shared technical functionality and