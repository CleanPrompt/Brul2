import pypandoc
from pathlib import Path

content = """# Brul2 — Development Roadmap

Brul2 is a REST API built with Kotlin, Ktor, JetBrains Amper, Exposed and H2.

The project uses a feature-based architecture and focuses on KISS, maintainability, object-oriented programming (OOP), functional programming (FP), and the school assignment requirements.

## Project Principles

- Keep It Simple (KISS), appropriate for second-year HBO Informatica.
- Use libraries and techniques covered in the school lessons.
- Organize code by feature; separate routes, services and repositories.
- Use repository interfaces for testability.
- Write code and comments in English.
- Use Kotlin null-safety and avoid `!!`.
- Write meaningful tests rather than maximizing test count.
- Run `kotlin test` after each checkpoint.
- Commit and push checkpoints after reviewing changes.
- Do not implement an AI agent.
- Avoid unnecessary concurrency, locking and architectural complexity.

## Progress Overview

| Checkpoint | Topic | Status |
|---|---|---|
| 1 | Ktor Server & Routing | Implemented |
| 2 | JSON, Configuration & Error Handling | Implemented |
| 3 | Report Domain Models | Implemented |
| 4 | Database & Repositories | Implemented |
| 5 | Reports REST API | Implemented |
| 6 | Users & Authentication | Implemented and tested |
| 7 | Voting | Implemented and tested |
| 8 | Help Participation | Implemented and tested |
| 9 | Report Status Management | Implemented and tested |
| 10 | Testing & Quality Review | Pending final review |
| 11 | Documentation & Delivery | Pending |

**Latest reported result:** All tests passed after Checkpoint 9. The expected count is **263** (239 after Checkpoint 7, plus 18 in Checkpoint 8 and 6 in Checkpoint 9); the final numerical total has not been independently confirmed.

**Important:** Implementation and passing tests do not prove that README updates, manual API verification, Git commits or school deliverables have been completed. Those tasks remain unchecked unless explicitly confirmed.

---

## Phase 1 — Project Foundation

### Checkpoint 1 — Ktor Server & Routing

**Status:** Implemented

Tasks:
- [x] Create Brul2 using JetBrains Amper.
- [x] Configure `module.yaml` and `application.yaml`.
- [x] Create `Application.kt` and `Routing.kt`.
- [x] Implement `GET /api/v1/health`.
- [x] Start the server successfully.
- [x] Create health endpoint tests and run `kotlin test`.
- [ ] Verify README setup instructions are current.
- [ ] Verify checkpoint changes were committed and pushed.

Tests:
- Health endpoint returns HTTP 200 and the expected response.

Suggested commit: `chore: initialize Brul2 Ktor project`

### Checkpoint 2 — JSON, Configuration & Error Handling

**Status:** Implemented

Tasks:
- [x] Configure Ktor ContentNegotiation and kotlinx.serialization.
- [x] Create application configuration and API error handling.
- [x] Configure StatusPages.
- [x] Return JSON from the health endpoint.
- [x] Handle invalid requests and unexpected errors.
- [x] Add relevant tests.
- [ ] Review documentation and commit history.

Tests:
- JSON responses, invalid requests, error status codes and unexpected errors.

Suggested commit: `feat: configure JSON and error handling`

---

## Phase 2 — Domain & Database

### Checkpoint 3 — Report Domain Models

**Status:** Implemented

Tasks:
- [x] Create abstract `Report` and concrete `SafetyReport`, `NuisanceReport` and `HelpRequest`.
- [x] Create `Location`, `ReportType`, `ReportCategory` and `ReportStatus`.
- [x] Implement domain validation and help request volunteer capacity validation.
- [x] Write unit tests.
- [ ] Review documentation and commit history.

School concepts:
- Abstract classes, inheritance, polymorphism, encapsulation, data classes, enums and null-safety.

Suggested commit: `feat: implement report domain models`

### Checkpoint 4 — Database & Repositories

**Status:** Implemented

Tasks:
- [x] Configure H2 and Exposed DSL.
- [x] Create database initialization and `ReportsTable`.
- [x] Create `ReportRepository` and `ExposedReportRepository`.
- [x] Implement report persistence.
- [x] Write database integration tests.
- [ ] Verify the database schema initializes correctly on a fresh installation.
- [ ] Review documentation and commit history.

Suggested commit: `feat: add Exposed database persistence`

---

## Phase 3 — REST API Features

### Checkpoint 5 — Reports API

**Status:** Implemented

Tasks:
- [x] Create request and response DTOs.
- [x] Create `ReportService` and `ReportRoutes`.
- [x] Implement report creation, retrieval, listing, updating and deletion.
- [x] Implement searching, filtering and sorting.
- [x] Implement validation and error responses.
- [x] Write unit and integration tests.
- [ ] Add or verify HTTP request examples.
- [ ] Review documentation and commit history.

Implemented endpoints:
- `POST /api/v1/reports` — JWT required.
- `GET /api/v1/reports` — public; supports search, filters and sorting.
- `GET /api/v1/reports/{id}` — public.
- `PUT /api/v1/reports/{id}` — report owner only.
- `DELETE /api/v1/reports/{id}` — report owner or ADMIN.

Note: The implemented update endpoint uses **PUT**, not the PATCH endpoint originally proposed.

Supported sorting options:
- `ID_ASC`, `ID_DESC`, `NEAREST`, `NEWEST`, `MOST_VIEWED`, `MOST_LIKED`.

Suggested commit: `feat: implement reports REST API`

### Checkpoint 6 — Users & Authentication

**Status:** Implemented and tested

Tasks:
- [x] Create user and role models, database table and repository.
- [x] Implement registration and login.
- [x] Add BCrypt password hashing.
- [x] Configure JWT authentication.
- [x] Protect report creation, updates and other restricted endpoints.
- [x] Implement USER/ADMIN roles and report ownership checks.
- [x] Add unit, integration and authorization tests.
- [ ] Verify HTTP request examples and documentation.
- [ ] Verify checkpoint commit history.

Endpoints:
- `POST /api/v1/auth/register`
- `POST /api/v1/auth/login`
- `GET /api/v1/users/me` — verify this endpoint exists in the current application before documenting it as implemented.

Tests:
- Valid registration, duplicate registration, valid and invalid login, missing JWT, invalid JWT and authorization restrictions.

Notes:
- Use access tokens only unless refresh tokens are required by the assignment.
- Checkpoint 6 included sub-checkpoints 6.1–6.6D.

Suggested commit: `feat: implement user authentication`

### Checkpoint 7 — Voting

**Status:** Implemented and tested

Tasks:
- [x] Create vote model, DTOs, table and repository.
- [x] Implement `VoteService` and `VoteRoutes`.
- [x] Support LIKE and DISLIKE.
- [x] Support changing and removing a vote.
- [x] Prevent duplicate votes using one vote per report/user.
- [x] Prevent voting on one's own report.
- [x] Provide public vote totals and authenticated personal vote lookup.
- [x] Add `MOST_LIKED` report sorting.
- [x] Add cascading deletion of votes when a report is deleted.
- [x] Add service, repository, API and cascade tests.
- [ ] Verify HTTP examples and commit history.

Endpoints:
- `GET /api/v1/reports/{id}/votes` — public.
- `PUT /api/v1/reports/{id}/vote` — JWT required.
- `GET /api/v1/reports/{id}/vote/me` — JWT required.
- `DELETE /api/v1/reports/{id}/vote` — JWT required.

**Confirmed test result at the end of Checkpoint 7:** 239 passed.

Suggested commit: `feat: implement report voting`

### Checkpoint 8 — Help Participation

**Status:** Implemented and tested

Tasks:
- [x] Create help participation model.
- [x] Create `HelpParticipationsTable` with a unique report/user constraint.
- [x] Create participation repository interface and Exposed implementation.
- [x] Implement `HelpParticipationService` and `HelpParticipationRoutes`.
- [x] Allow authenticated users to join and leave help requests.
- [x] Prevent owners from joining their own help requests.
- [x] Prevent duplicate participation.
- [x] Reject new participants when capacity is full.
- [x] Reject new participants when the report is not OPEN.
- [x] Expose participant count, available places and derived participation status.
- [x] Cascade-delete participations when a report is deleted.
- [x] Register help routes in the application's main `Routing.kt`.
- [x] Add repository, service and HTTP integration tests.
- [ ] Verify HTTP examples and commit history.

Endpoints:
- `GET /api/v1/reports/{id}/participants` — public summary.
- `GET /api/v1/reports/{id}/participants/list` — public participant IDs.
- `POST /api/v1/reports/{id}/participants` — JWT required.
- `DELETE /api/v1/reports/{id}/participants/me` — JWT required.

Participation status:
- `OPEN`: report is OPEN and capacity is available.
- `FULL`: report is OPEN and all places are occupied.
- `CLOSED`: report is IN_PROGRESS or RESOLVED.

Design choices:
- Use `HelpRequest.volunteersNeeded` as the capacity limit.
- Keep the existing `ReportStatus` values: OPEN, IN_PROGRESS and RESOLVED.
- No participation history or advanced concurrent enrollment handling.
- Participants remain in the database when a help request is completed; deleting the report removes them.

**Test result:** All tests reported passing; 18 tests were added across repository, service and API layers.

Suggested commit: `feat: implement help participation`

### Checkpoint 9 — Report Status Management

**Status:** Implemented and tested

Tasks:
- [x] Reuse the existing authenticated `PUT /api/v1/reports/{id}` endpoint.
- [x] Reuse the existing owner authorization check.
- [x] Create `ReportStatusRules`.
- [x] Validate status transitions in `ReportService.update`.
- [x] Allow OPEN → IN_PROGRESS.
- [x] Allow OPEN → RESOLVED.
- [x] Allow IN_PROGRESS → RESOLVED.
- [x] Reject IN_PROGRESS → OPEN.
- [x] Reject changes away from RESOLVED.
- [x] Allow updates that keep the same status.
- [x] Add six focused status-rule tests.
- [ ] Verify the status update through the running API manually.
- [ ] Verify checkpoint commit history.

**Important scope:** The current status transition rules apply to **all report types**, not only help requests. This was the chosen implementation, but should be checked against the assignment's intended behavior.

**Remaining business-rule gap:** Changing `volunteersNeeded` currently needs a check to prevent reducing capacity below the number of existing participants. Do not mark this resolved until implemented and tested.

Suggested commit: `feat: validate report status transitions`

---

## Phase 4 — Quality & Delivery

### Checkpoint 10 — Testing & Quality Review

**Status:** Pending final review

Tasks:
- [ ] Review unit test coverage for meaningful scenarios, not raw test count.
- [ ] Review repository and HTTP integration coverage.
- [ ] Verify all routes in the running application, not only isolated route tests.
- [ ] Verify authentication and owner/ADMIN authorization.
- [ ] Verify invalid input and error handling.
- [ ] Verify H2 schema initialization and cascade behavior.
- [ ] Add capacity-reduction validation for help requests with existing participants.
- [ ] Confirm whether status transitions should apply to every report type.
- [ ] Review Kotlin null-safety and remove unnecessary code.
- [ ] Run the full test suite and record the exact result.
- [ ] Check Git status and commit reviewed changes.

School requirements:
- At least 3 meaningful unit tests.
- At least 3 meaningful integration tests.
- Check any additional individual portfolio requirements.

Suggested commit: `test: complete API quality review`

### Checkpoint 11 — Documentation & Delivery

**Status:** Pending

Tasks:
- [ ] Complete `README.md`.
- [ ] Document project structure and architecture.
- [ ] Document installation, configuration and startup.
- [ ] Document actual API endpoints and JWT authentication.
- [ ] Add HTTP request examples for reports, auth, voting and participation.
- [ ] Create UML class diagram.
- [ ] Create UML package diagram.
- [ ] Create at least 3 relevant sequence diagrams.
- [ ] Explain OOP and FP usage with examples from the code.
- [ ] Complete the required school report.
- [ ] Prepare the demonstration video.
- [ ] Verify the original school assignment requirements.
- [ ] Run and record final tests.
- [ ] Review staged files for secrets and generated output.
- [ ] Commit and push the final changes.

Suggested commit: `docs: finalize Brul2 documentation`

---

## Development Workflow

1. Identify the requirement and relevant school concepts.
2. Select a small set of meaningful test scenarios.
3. Create or modify only the necessary files.
4. Run `kotlin test`.
5. Manually verify affected API endpoints where relevant.
6. Update the roadmap, README and examples.
7. Review `git status` and changes.
8. Commit and push to GitHub.

## GitHub Workflow

```bash
git status
kotlin test
git add .
git commit -m "type: describe completed checkpoint"
git push
```

Commit prefixes:
- `chore:` Project setup and configuration.
- `feat:` New functionality.
- `fix:` Bug fixes.
- `test:` Tests and test improvements.
- `refactor:` Code improvements without changing behavior.
- `docs:` Documentation updates.

Only commit when the project compiles, relevant tests pass, existing functionality still works, and no secrets or unnecessary generated files are staged.

## Architecture

Brul2 uses a feature-based project structure:

`Routes → Services → Repository Interfaces → Exposed Repositories → H2`

Main packages:
- `config` — Application configuration.
- `common` — Shared technical functionality.
- `database` — Database connection and initialization.
- `security` — JWT and password hashing.
- `features/report` — Report domain, REST API and status rules.
- `features/user` — User management.
- `features/auth` — Registration and login.
- `features/vote` — Report voting.
- `features/help` — Help request participation.

The main routing configuration registers report, vote, help participation and authentication routes under `/api/v1`.

## Current Next Step

**Checkpoint 10 — Testing & Quality Review.**

1. Implement the missing rule that prevents reducing `volunteersNeeded` below the active participant count.
2. Decide whether status transitions should apply to all report types or only help requests.
3. Verify the main application routes and error responses manually.
4. Record the exact test count after the final quality checks.
5. Then continue to Checkpoint 11 — Documentation & Delivery.

Do not add features that are not required by the assignment merely to increase scope or test count.
"""
output = "/mnt/data/ROADMAP_UPDATED.md"
pypandoc.convert_text(content, 'md', format='md', outputfile=output, extra_args=['--standalone'])
print(Path(output).exists(), Path(output).stat().st_size)
