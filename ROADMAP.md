# Brul2 — Development Roadmap

Brul2 is a REST API built with Kotlin, Ktor, JetBrains Amper, Exposed and H2.

The project follows a feature-based architecture and focuses on simplicity, maintainability, object-oriented programming (OOP), functional programming (FP), and the requirements of the school assignment.

## Project Principles

- Keep It Simple (KISS).
- Use libraries and techniques covered in the school lessons.
- Organize code by feature.
- Separate routes, services and repositories.
- Use repository interfaces for testability.
- Write code and comments in English.
- Use Kotlin null-safety and avoid `!!`.
- Write tests throughout development.
- Complete each checkpoint with a GitHub commit.
- Do not implement an AI agent.

---

## Progress Overview

| Checkpoint | Feature | Status |
|---|---|---|
| 1–5 | Foundation, domain, database, reports API | Implemented |
| 6 | Users and JWT authentication | Implemented and tested |
| 7 | Voting, sorting, cascade deletion | Implemented and tested; 239 confirmed |
| 8 | Help participation | Implemented and tested |
| 9 | Report status management | Implemented and tested |
| 10 | Quality review | Pending |
| 11 | Documentation and delivery | Pending |

**Test status:** All tests passed after Checkpoint 9; expected 263 total, exact number unconfirmed.

**Principle:** Prefer meaningful tests and KISS over unnecessary complexity or test count.

---

## Phase 1 — Project Foundation

### Checkpoint 1 — Ktor Server & Routing

**Status:** Implemented

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
- [ ] Verify `README.md` is up to date.
- [ ] Verify commit and push to GitHub.

Tests:
- Health endpoint returns HTTP 200.
- Health endpoint returns the expected response.

Commit:
`chore: initialize Brul2 Ktor project`

### Checkpoint 2 — JSON, Configuration & Error Handling

**Status:** Implemented and tested

Tasks:
- [x] Configure Ktor ContentNegotiation.
- [x] Configure kotlinx.serialization.
- [x] Create `AppConfig.kt`.
- [x] Create `ApiError.kt`.
- [x] Create `ErrorHandling.kt`.
- [x] Configure Ktor StatusPages.
- [x] Return JSON from the health endpoint.
- [x] Add relevant tests.
- [x] Update documentation.
- [ ] Verify commit and push to GitHub.

Tests:
- Health endpoint returns valid JSON.
- Health endpoint returns HTTP 200.
- Invalid requests return an appropriate error response.
- Unexpected errors return HTTP 500 without exposing internal details.

Commit:
`feat: configure JSON and error handling`

---

## Phase 2 — Domain & Database

### Checkpoint 3 — Report Domain Models

**Status:** Implemented and tested

Tasks:
- [x] Create abstract `Report` class.
- [x] Create `SafetyReport`.
- [x] Create `NuisanceReport`.
- [x] Create `HelpRequest`.
- [x] Create `Location`.
- [x] Create `ReportType`.
- [x] Create `ReportCategory`.
- [x] Define domain validation rules.
- [x] Write unit tests.
- [x] Update documentation.
- [ ] Verify commit and push to GitHub.

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
- Invalid category validation.
- Invalid location validation.
- Polymorphic behavior.
- Help request business rules.

Commit:
`feat: implement report domain models`

### Checkpoint 4 — Database & Repositories

**Status:** Implemented and tested

Tasks:
- [x] Add H2 dependency.
- [x] Add Exposed DSL dependencies.
- [x] Create `DatabaseFactory.kt`.
- [x] Create `ReportsTable.kt`.
- [x] Create `ReportRepository` interface.
- [x] Create `ExposedReportRepository`.
- [x] Configure database initialization.
- [x] Write database integration tests.
- [x] Update documentation.
- [ ] Verify commit and push to GitHub.

Tests:
- Save a report.
- Retrieve a report.
- Delete a report.
- Verify database persistence.

Commit:
`feat: add Exposed database persistence`

---

## Phase 3 — REST API Features

### Checkpoint 5 — Reports API

**Status:** Implemented and tested

Tasks:
- [x] Create report request and response DTOs.
- [x] Create `ReportService.kt`.
- [x] Create `ReportRoutes.kt`.
- [x] Implement report creation.
- [x] Implement report retrieval.
- [x] Implement report listing.
- [x] Implement searching and filtering.
- [x] Implement report updates.
- [x] Implement report deletion.
- [x] Implement validation and error responses.
- [x] Write unit and integration tests.
- [x] Add HTTP example requests.
- [ ] Verify commit and push to GitHub.

Endpoints (proposed):
- `POST /api/v1/reports`
- `GET /api/v1/reports`
- `GET /api/v1/reports/{id}`
- `PUT /api/v1/reports/{id}`
- `DELETE /api/v1/reports/{id}`

Tests:
- Create a valid report.
- Reject invalid report data.
- Retrieve an existing report.
- Return 404 for an unknown report.
- Search and filter reports.
- Update a report.
- Delete a report.

Commit:
`feat: implement reports REST API`

### Checkpoint 6 — Users & Authentication

**Status:** Implemented and tested

Tasks:
- [x] Create `User` model.
- [x] Create `Role` enum.
- [x] Create `UsersTable.kt`.
- [x] Create user repository.
- [x] Create authentication DTOs.
- [x] Implement user registration.
- [x] Implement login.
- [x] Add BCrypt password hashing.
- [x] Configure JWT authentication.
- [x] Protect endpoints requiring authentication.
- [x] Implement authorization rules.
- [x] Write unit and integration tests.
- [x] Add HTTP example requests.
- [ ] Verify commit and push to GitHub.

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

Note:
- Start with access tokens only.
- Do not implement refresh tokens unless required.

Commit:
`feat: implement user authentication`

### Checkpoint 7 — Voting

**Status:** Implemented and tested

Tasks:
- [x] Create vote model and DTOs.
- [x] Create `VotesTable.kt`.
- [x] Create vote repository.
- [x] Create `VoteService.kt`.
- [x] Create `VoteRoutes.kt`.
- [x] Implement voting on reports.
- [x] Implement changing or removing votes.
- [x] Prevent duplicate votes.
- [x] Implement vote counting.
- [x] Require authentication.
- [x] Write unit and integration tests.
- [x] Add HTTP example requests.
- [ ] Verify commit and push to GitHub.

Tests:
- Create a vote.
- Prevent duplicate votes.
- Change a vote.
- Remove a vote.
- Calculate vote totals.
- Reject unauthorized voting.

Commit:
`feat: implement report voting`

### Checkpoint 8 — Help Participation

**Status:** Implemented and tested

Tasks:
- [x] Create help participation model.
- [x] Create `HelpParticipationsTable.kt`.
- [x] Create help repository.
- [x] Create `HelpService.kt`.
- [x] Create `HelpRoutes.kt`.
- [x] Implement joining a help request.
- [x] Implement leaving a help request.
- [x] Validate participant capacity.
- [x] Handle help request status.
- [x] Prevent duplicate participation.
- [x] Require authentication.
- [x] Write unit and integration tests.
- [x] Add HTTP example requests.
- [ ] Verify commit and push to GitHub.

Tests:
- Join a help request.
- Prevent duplicate participation.
- Reject participation when capacity is full.
- Leave a help request.
- Reject participation in a closed help request.

Commit:
`feat: implement help participation`

---

### Checkpoint 9 — Report Status Management

**Status:** Implemented and tested

Tasks:
- [x] Reuse existing `PUT /api/v1/reports/{id}` and owner authorization.
- [x] Add `ReportStatusRules` and validate transitions in `ReportService.update`.
- [x] Allow OPEN → IN_PROGRESS, OPEN → RESOLVED, and IN_PROGRESS → RESOLVED.
- [x] Reject reopening a RESOLVED report and returning IN_PROGRESS to OPEN.
- [x] Allow unchanged status.
- [x] Add six focused status-rule tests.
- [ ] Manually verify status updates through the running server.
- [ ] Verify commit and push to GitHub.

Note: Current status rules apply to **all report types**, not only help requests. Confirm that this matches the assignment.

Suggested commit: `feat: validate report status transitions`

---

## Phase 4 — Quality & Delivery

### Checkpoint 10 — Testing & Quality

**Status:** Pending final review

Tasks:
- [ ] Review unit and integration test coverage for meaningful behavior.
- [ ] Manually verify all API endpoints in the running application.
- [ ] Verify authentication, authorization, invalid input and error handling.
- [ ] Verify database initialization and cascade deletion.
- [ ] Prevent lowering `volunteersNeeded` below the number of existing participants.
- [ ] Decide whether status rules should apply to all report types.
- [ ] Review Kotlin null-safety and remove unnecessary code.
- [ ] Record the exact final test count.
- [ ] Verify commit and push to GitHub.

School requirements:
- At least 3 meaningful unit tests.
- At least 3 meaningful integration tests.
- Check individual portfolio requirements.

Suggested commit: `test: complete API quality review`

### Checkpoint 11 — Documentation & Delivery

**Status:** Pending

Tasks:
- [ ] Complete `README.md`.
- [ ] Document project structure, installation, configuration and startup.
- [ ] Document all actual API endpoints and JWT authentication.
- [ ] Add HTTP request examples.
- [ ] Create UML class diagram and UML package diagram.
- [ ] Create at least 3 relevant sequence diagrams.
- [ ] Explain OOP and FP usage with concrete examples.
- [ ] Complete the school report and demonstration video.
- [ ] Verify the school assignment requirements.
- [ ] Run final tests and check Git status.
- [ ] Commit and push to GitHub.

Suggested commit: `docs: finalize Brul2 documentation`

---

## Implemented API Summary

- `GET /api/v1/health`
- `POST /api/v1/auth/register`, `POST /api/v1/auth/login`
- `GET /api/v1/reports`, `GET /api/v1/reports/{id}`
- `POST /api/v1/reports` (JWT)
- `PUT /api/v1/reports/{id}` (owner)
- `DELETE /api/v1/reports/{id}` (owner or ADMIN)
- `GET /api/v1/reports/{id}/votes`
- `PUT /api/v1/reports/{id}/vote`, `GET /api/v1/reports/{id}/vote/me`, `DELETE /api/v1/reports/{id}/vote` (JWT)
- `GET /api/v1/reports/{id}/participants`, `GET /api/v1/reports/{id}/participants/list`
- `POST /api/v1/reports/{id}/participants`, `DELETE /api/v1/reports/{id}/participants/me` (JWT)

Checkpoint 8 uses `HelpParticipationService` and `HelpParticipationRoutes`, not the original proposed `HelpService` / `HelpRoutes` names. Help request participation status is derived as OPEN, FULL or CLOSED, while stored report status remains OPEN, IN_PROGRESS or RESOLVED. The database enforces one active participation per report/user and cascading deletion.

---

## Development Workflow

Follow these steps for every checkpoint:

1. Discuss the functionality and relevant school concepts.
2. Define test scenarios before implementation.
3. Create or modify the required files.
4. Run automated tests using `kotlin test`.
5. Manually verify relevant API endpoints.
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

Main packages:

- `config` — Application configuration.
- `common` — Shared technical functionality.
- `database` — Database connection and initialization.
- `security` — Shared authentication configuration.
- `features/report` — Report management and domain models.
- `features/user` — User management.
- `features/auth` — Registration and login.
- `features/vote` — Report voting.
- `features/help` — Help request participation and capacity summaries.

Features are added only when needed.

---

## Current Next Step

**Checkpoint 10 — Testing & Quality Review.**

1. Add validation to prevent reducing help request capacity below the current participant count.
2. Confirm whether status transitions apply to all reports or only help requests.
3. Manually test the registered endpoints in the running application.
4. Record the exact final test count, then proceed to Checkpoint 11 documentation.

**Progress:** Checkpoints 1–9 are implemented. The user confirmed all tests passing after Checkpoint 9. The expected total is **263 tests** (239 after Checkpoint 7 + 18 in Checkpoint 8 + 6 in Checkpoint 9), but the exact count was not explicitly confirmed. Git commits, README updates and manual API verification have not been independently confirmed.