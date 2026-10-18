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

## Phase 1 — Project Foundation

### Checkpoint 1 — Ktor Server & Routing

**Status:** In progress

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
- [ ] Update `README.md`.
- [x] Commit and push to GitHub.

Tests:
- Health endpoint returns HTTP 200.
- Health endpoint returns the expected response.

Commit:
`chore: initialize Brul2 Ktor project`

### Checkpoint 2 — JSON, Configuration & Error Handling

**Status:** Not started

Tasks:
- [ ] Configure Ktor ContentNegotiation.
- [ ] Configure kotlinx.serialization.
- [ ] Create `AppConfig.kt`.
- [ ] Create `ApiError.kt`.
- [ ] Create `ErrorHandling.kt`.
- [ ] Configure Ktor StatusPages.
- [ ] Return JSON from the health endpoint.
- [ ] Add relevant tests.
- [ ] Update documentation.
- [ ] Commit and push to GitHub.

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

**Status:** Not started

Tasks:
- [ ] Create abstract `Report` class.
- [ ] Create `SafetyReport`.
- [ ] Create `NuisanceReport`.
- [ ] Create `HelpRequest`.
- [ ] Create `Location`.
- [ ] Create `ReportType`.
- [ ] Create `ReportCategory`.
- [ ] Define domain validation rules.
- [ ] Write unit tests.
- [ ] Update documentation.
- [ ] Commit and push to GitHub.

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

**Status:** Not started

Tasks:
- [ ] Add H2 dependency.
- [ ] Add Exposed DSL dependencies.
- [ ] Create `DatabaseFactory.kt`.
- [ ] Create `ReportsTable.kt`.
- [ ] Create `ReportRepository` interface.
- [ ] Create `ExposedReportRepository`.
- [ ] Configure database initialization.
- [ ] Write database integration tests.
- [ ] Update documentation.
- [ ] Commit and push to GitHub.

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

**Status:** Not started

Tasks:
- [ ] Create report request and response DTOs.
- [ ] Create `ReportService.kt`.
- [ ] Create `ReportRoutes.kt`.
- [ ] Implement report creation.
- [ ] Implement report retrieval.
- [ ] Implement report listing.
- [ ] Implement searching and filtering.
- [ ] Implement report updates.
- [ ] Implement report deletion.
- [ ] Implement validation and error responses.
- [ ] Write unit and integration tests.
- [ ] Add HTTP example requests.
- [ ] Commit and push to GitHub.

Endpoints (proposed):
- `POST /api/v1/reports`
- `GET /api/v1/reports`
- `GET /api/v1/reports/{id}`
- `PATCH /api/v1/reports/{id}`
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

Note:
- Start with access tokens only.
- Do not implement refresh tokens unless required.

Commit:
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
- [ ] Prevent duplicate votes.
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

Commit:
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
- [ ] Handle help request status.
- [ ] Prevent duplicate participation.
- [ ] Require authentication.
- [ ] Write unit and integration tests.
- [ ] Add HTTP example requests.
- [ ] Commit and push to GitHub.

Tests:
- Join a help request.
- Prevent duplicate participation.
- Reject participation when capacity is full.
- Leave a help request.
- Reject participation in a closed help request.

Commit:
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

Commit:
`test: complete API test coverage`

### Checkpoint 10 — Documentation & Delivery

**Status:** Not started

Tasks:
- [ ] Complete `README.md`.
- [ ] Document project structure.
- [ ] Document installation and startup.
- [ ] Document API endpoints.
- [ ] Create HTTP request examples.
- [ ] Create UML class diagram.
- [ ] Create UML package diagram.
- [ ] Create at least 3 relevant sequence diagrams.
- [ ] Explain OOP and FP usage.
- [ ] Complete the required report.
- [ ] Prepare the demonstration video.
- [ ] Verify school assignment requirements.
- [ ] Run final tests.
- [ ] Commit and push to GitHub.

Commit:
`docs: finalize Brul2 documentation`

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
- `features/help` — Help request participation.

Features are added only when needed.

---

## Current Next Step

**Checkpoint 1 — Complete the Ktor project foundation.**

The Ktor server is running and the health endpoint works.

Next actions:
1. Create `HealthRoutesTest.kt`.
2. Run `kotlin test`.
3. Update `README.md`.
4. Create the first GitHub commit.

After completing Checkpoint 1, continue with Checkpoint 2.
