# Brul2 — Development Roadmap

Brul2 is a REST API built with **Kotlin, Ktor, JetBrains Amper, Exposed and H2**.

The project follows a feature-based architecture and focuses on simplicity, maintainability, object-oriented programming (OOP), functional programming (FP), automated testing and the requirements of the school assignment.

## Project Principles

- Keep It Simple (KISS).
- Use libraries and techniques covered in the school lessons.
- Organize code by feature.
- Separate routes, services and repositories.
- Use repository interfaces for testability.
- Write code, comments and documentation in English.
- Use Kotlin null-safety and avoid `!!`.
- Write meaningful tests throughout development.
- Complete each checkpoint with a Git commit.
- Avoid unnecessary complexity.
- Do not implement an AI agent.

---

## Progress Overview

| Checkpoint | Feature | Status |
|---|---|---|
| 1 | Ktor Server & Routing | Completed |
| 2 | JSON, Configuration & Error Handling | Completed |
| 3 | Report Domain Models | Completed |
| 4 | Database & Repositories | Completed |
| 5 | Reports REST API | Completed |
| 6 | Users & JWT Authentication | Completed |
| 7 | Voting & Sorting | Completed |
| 8 | Help Participation | Completed |
| 9 | Report Status Management | Completed |
| 10.1 | Volunteer Capacity Validation | Completed |
| 10.2 | Volunteer Capacity API Integration Tests | Completed |
| 10.3 | Final Quality Review | Pending |
| 11 | Documentation & Delivery | In progress |

### Current Test Status

**268 tests passed — 0 failures.**

The complete automated test suite passed after Checkpoint 10.2.

The latest changes were committed to Git.

### Current Development Status

The core REST API functionality is implemented and tested.

Remaining work focuses on:

1. Final quality review.
2. Manual verification of API endpoints.
3. Confirming remaining business-rule decisions.
4. UML diagrams.
5. Sequence diagrams.
6. School report and demonstration video.
7. Final delivery checks.

**Development principle:** Prefer meaningful tests and KISS over unnecessary complexity or increasing the test count without a clear reason.

---

# Phase 1 — Project Foundation

## Checkpoint 1 — Ktor Server & Routing

**Status: Completed**

### Tasks

- [x] Create Brul2 using JetBrains Amper.
- [x] Configure `module.yaml`.
- [x] Configure `application.yaml`.
- [x] Create `Application.kt`.
- [x] Create `Routing.kt`.
- [x] Implement `GET /api/v1/health`.
- [x] Start the server successfully.
- [x] Create health endpoint tests.
- [x] Run automated tests.
- [x] Document the project setup.

### Implemented Endpoint

```http
GET /api/v1/health
```

### Tests

- Health endpoint returns HTTP 200.
- Health endpoint returns the expected JSON response.

### Suggested Commit

```text
chore: initialize Brul2 Ktor project
```

---

## Checkpoint 2 — JSON, Configuration & Error Handling

**Status: Completed**

### Tasks

- [x] Configure Ktor ContentNegotiation.
- [x] Configure kotlinx.serialization.
- [x] Create `AppConfig.kt`.
- [x] Create `ApiError.kt`.
- [x] Create `ErrorHandling.kt`.
- [x] Configure Ktor StatusPages.
- [x] Return JSON from API endpoints.
- [x] Handle invalid request data.
- [x] Handle unexpected server errors.
- [x] Add relevant tests.
- [x] Document error handling.

### Error Handling

The API uses centralized error handling through `StatusPages`.

Common HTTP responses:

| Status | Description |
|---|---|
| `200 OK` | Successful request |
| `201 Created` | Resource created |
| `204 No Content` | Successful deletion |
| `400 Bad Request` | Invalid request |
| `401 Unauthorized` | Authentication required or invalid |
| `403 Forbidden` | Insufficient permissions |
| `404 Not Found` | Resource not found |
| `409 Conflict` | Conflicting operation |
| `500 Internal Server Error` | Unexpected server error |

Unexpected exceptions are logged without exposing internal details in the API response.

### Tests

- Valid JSON responses.
- Invalid request handling.
- Appropriate HTTP status codes.
- Unexpected error handling.

### Suggested Commit

```text
feat: configure JSON and error handling
```

---

# Phase 2 — Domain & Database

## Checkpoint 3 — Report Domain Models

**Status: Completed**

### Tasks

- [x] Create abstract `Report` class.
- [x] Create `SafetyReport`.
- [x] Create `NuisanceReport`.
- [x] Create `HelpRequest`.
- [x] Create `Location`.
- [x] Create `ReportType`.
- [x] Create `ReportCategory`.
- [x] Create `ReportStatus`.
- [x] Implement domain validation.
- [x] Write unit tests.
- [x] Document domain models.

### Report Types

| Type | Description |
|---|---|
| `SAFETY` | Safety-related reports |
| `NUISANCE` | Nuisance-related reports |
| `HELP_REQUEST` | Requests for volunteer assistance |

### Domain Validation

The abstract `Report` class validates shared properties:

- Report ID must be positive.
- Title cannot be blank.
- Description cannot be blank.
- Creation timestamp must be positive.
- View count cannot be negative.
- Owner ID must be positive when provided.

Each concrete report type validates its own category.

`HelpRequest` also requires:

```text
volunteersNeeded > 0
```

### School Concepts

- Abstract classes.
- Inheritance.
- Polymorphism.
- Encapsulation.
- Data classes.
- Enums.
- Null safety.
- Constructor validation.

### Tests

- Valid report creation.
- Invalid category validation.
- Invalid location validation.
- Polymorphic behavior.
- Help request validation.

### Suggested Commit

```text
feat: implement report domain models
```

---

## Checkpoint 4 — Database & Repositories

**Status: Completed**

### Tasks

- [x] Add H2 dependency.
- [x] Add Exposed DSL dependencies.
- [x] Create `DatabaseFactory.kt`.
- [x] Create `ReportsTable.kt`.
- [x] Create `ReportRepository` interface.
- [x] Create `ExposedReportRepository`.
- [x] Configure database initialization.
- [x] Implement database persistence.
- [x] Write database integration tests.
- [x] Document database architecture.

### Database Tables

The application currently uses four main tables:

| Table | Purpose |
|---|---|
| `reports` | Report data |
| `users` | User accounts |
| `votes` | Report votes |
| `help_participations` | Volunteer registrations |

### Repository Pattern

Database operations are defined through repository interfaces.

Example:

```kotlin
interface ReportRepository {
    fun create(report: NewReport): Report
    fun findAll(): List<Report>
    fun findById(id: Long): Report?
    fun update(report: Report): Boolean
    fun delete(id: Long): Boolean
}
```

`ExposedReportRepository` implements these operations using Exposed DSL and H2.

### Tests

- Create and retrieve reports.
- Update existing reports.
- Delete reports.
- Verify database persistence.
- Verify report type mapping.

### Suggested Commit

```text
feat: add Exposed database persistence
```

---

# Phase 3 — REST API Features

## Checkpoint 5 — Reports REST API

**Status: Completed**

### Tasks

- [x] Create report request DTOs.
- [x] Create report response DTOs.
- [x] Create `ReportService.kt`.
- [x] Create `ReportRoutes.kt`.
- [x] Implement report creation.
- [x] Implement report retrieval.
- [x] Implement report listing.
- [x] Implement report searching.
- [x] Implement filtering.
- [x] Implement sorting.
- [x] Implement report updates.
- [x] Implement report deletion.
- [x] Implement validation and error responses.
- [x] Write unit tests.
- [x] Write HTTP integration tests.
- [x] Document API endpoints.

### Endpoints

| Method | Endpoint | Authentication |
|---|---|---|
| GET | `/api/v1/reports` | Public |
| GET | `/api/v1/reports/{id}` | Public |
| POST | `/api/v1/reports` | JWT |
| PUT | `/api/v1/reports/{id}` | Owner |
| DELETE | `/api/v1/reports/{id}` | Owner or ADMIN |

### Search Parameters

The report listing endpoint supports:

| Parameter | Description |
|---|---|
| `query` | Search title and description |
| `type` | Filter by report type |
| `category` | Filter by category |
| `latitude` | Search center latitude |
| `longitude` | Search center longitude |
| `radiusKm` | Search radius in kilometers |
| `sort` | Sorting option |

### Sorting Options

- `ID_ASC`
- `ID_DESC`
- `NEAREST`
- `NEWEST`
- `MOST_VIEWED`
- `MOST_LIKED`

### Tests

- Create valid reports.
- Reject invalid report data.
- Retrieve existing reports.
- Return 404 for missing reports.
- Search and filter reports.
- Sort reports.
- Update reports.
- Delete reports.
- Reject invalid query parameters.

### Suggested Commit

```text
feat: implement reports REST API
```

---

## Checkpoint 6 — Users & Authentication

**Status: Completed**

### Tasks

- [x] Create `User` model.
- [x] Create `Role` enum.
- [x] Create `UsersTable.kt`.
- [x] Create user repository.
- [x] Create authentication DTOs.
- [x] Implement user registration.
- [x] Implement login.
- [x] Implement BCrypt password hashing.
- [x] Configure JWT authentication.
- [x] Protect authenticated endpoints.
- [x] Implement USER and ADMIN roles.
- [x] Implement report ownership.
- [x] Implement authorization rules.
- [x] Write unit and integration tests.
- [x] Document authentication.

### Endpoints

```http
POST /api/v1/auth/register
POST /api/v1/auth/login
```

### Authentication

Protected requests use:

```http
Authorization: Bearer YOUR_JWT_TOKEN
```

### Authorization Rules

- Unauthenticated users cannot create reports.
- Only authenticated users can create reports.
- Only the owner can update a report.
- The owner or an ADMIN can delete a report.
- Protected voting and participation operations require authentication.

### Tests

- Valid registration.
- Duplicate registration rejection.
- Valid login.
- Incorrect password rejection.
- JWT validation.
- Missing authentication.
- Ownership validation.
- Role-based authorization.

### Implementation Notes

- Passwords are stored as BCrypt hashes.
- JWT access tokens are used for authentication.
- Refresh tokens are not part of the current implementation.
- Authentication is kept simple to match the school assignment.

### Suggested Commit

```text
feat: implement user authentication
```

---

## Checkpoint 7 — Voting

**Status: Completed**

### Tasks

- [x] Create vote model.
- [x] Create vote DTOs.
- [x] Create `VotesTable.kt`.
- [x] Create vote repository.
- [x] Create `VoteService.kt`.
- [x] Create voting routes.
- [x] Implement liking reports.
- [x] Implement disliking reports.
- [x] Implement changing votes.
- [x] Implement removing votes.
- [x] Prevent duplicate votes.
- [x] Implement vote counting.
- [x] Implement `MOST_LIKED` sorting.
- [x] Require authentication for voting.
- [x] Configure cascade deletion.
- [x] Write unit and integration tests.
- [x] Document voting endpoints.

### Endpoints

| Method | Endpoint | Authentication |
|---|---|---|
| GET | `/api/v1/reports/{id}/votes` | Public |
| PUT | `/api/v1/reports/{id}/vote` | JWT |
| GET | `/api/v1/reports/{id}/vote/me` | JWT |
| DELETE | `/api/v1/reports/{id}/vote` | JWT |

### Business Rules

- A user can have one vote per report.
- Users can change their vote.
- Users can remove their vote.
- Vote totals are calculated per report.
- Related votes are deleted when a report is deleted.

### Tests

- Create votes.
- Prevent duplicate votes.
- Change votes.
- Remove votes.
- Calculate totals.
- Reject unauthorized voting.
- Sort by likes.
- Verify cascade deletion.

### Recorded Milestone

**239 tests passed after Checkpoint 7.**

### Suggested Commit

```text
feat: implement report voting
```

---

## Checkpoint 8 — Help Participation

**Status: Completed**

### Tasks

- [x] Create `HelpParticipation` model.
- [x] Create `HelpParticipationsTable.kt`.
- [x] Create `HelpParticipationRepository`.
- [x] Create `ExposedHelpParticipationRepository`.
- [x] Create `HelpParticipationService`.
- [x] Create help participation routes.
- [x] Register help participation routes in `Routing.kt`.
- [x] Implement joining help requests.
- [x] Implement leaving help requests.
- [x] Prevent duplicate participation.
- [x] Prevent owners from joining their own help requests.
- [x] Validate volunteer capacity.
- [x] Prevent joining closed help requests.
- [x] Implement participation summaries.
- [x] Implement participant listing.
- [x] Require authentication for joining and leaving.
- [x] Configure cascade deletion.
- [x] Write unit and integration tests.
- [x] Document help participation endpoints.

### Endpoints

| Method | Endpoint | Authentication |
|---|---|---|
| GET | `/api/v1/reports/{id}/participants` | Public |
| GET | `/api/v1/reports/{id}/participants/list` | Public |
| POST | `/api/v1/reports/{id}/participants` | JWT |
| DELETE | `/api/v1/reports/{id}/participants/me` | JWT |

### Business Rules

- Only help requests support volunteer participation.
- The report owner cannot join their own help request.
- A user cannot join the same help request twice.
- A help request cannot exceed its volunteer capacity.
- New participants can only join an open help request.
- Participants can leave a help request.
- A user/report combination must be unique in the database.

### Participation Availability

Participation summaries derive an availability status:

| Status | Meaning |
|---|---|
| `OPEN` | New volunteers can join |
| `FULL` | No volunteer places remain |
| `CLOSED` | The report is not open for participation |

This availability status is separate from the stored report status.

### Tests

- Join a help request.
- Reject duplicate participation.
- Reject joining your own request.
- Reject participation when full.
- Leave a help request.
- Reject joining a closed request.
- Verify participation summaries.
- Verify cascade deletion.

### Recorded Milestone

**18 tests added in Checkpoint 8.**

### Suggested Commit

```text
feat: implement help participation
```

---

## Checkpoint 9 — Report Status Management

**Status: Completed**

### Tasks

- [x] Reuse `PUT /api/v1/reports/{id}`.
- [x] Reuse report owner authorization.
- [x] Create `ReportStatusRules.kt`.
- [x] Validate status transitions in `ReportService.update()`.
- [x] Allow unchanged status.
- [x] Allow `OPEN` to `IN_PROGRESS`.
- [x] Allow `OPEN` to `RESOLVED`.
- [x] Allow `IN_PROGRESS` to `RESOLVED`.
- [x] Reject `IN_PROGRESS` to `OPEN`.
- [x] Reject reopening resolved reports.
- [x] Add six status-rule tests.
- [ ] Manually verify all status transitions through the running server.
- [ ] Confirm that these rules match the assignment requirements.

### Report Statuses

- `OPEN`
- `IN_PROGRESS`
- `RESOLVED`

### Allowed Transitions

| Current Status | Next Status | Allowed |
|---|---|---|
| OPEN | OPEN | Yes |
| OPEN | IN_PROGRESS | Yes |
| OPEN | RESOLVED | Yes |
| IN_PROGRESS | IN_PROGRESS | Yes |
| IN_PROGRESS | RESOLVED | Yes |
| IN_PROGRESS | OPEN | No |
| RESOLVED | RESOLVED | Yes |
| RESOLVED | OPEN | No |
| RESOLVED | IN_PROGRESS | No |

### Implementation

Status validation is handled by:

```text
ReportStatusRules.validateTransition()
```

The service checks the existing report status before updating the database.

### Important Design Decision

The current implementation applies status transition rules to **all report types**.

This behavior is implemented and tested. Whether the school assignment requires these rules for every report type still needs to be confirmed.

### Recorded Milestone

**6 status-rule tests added in Checkpoint 9.**

### Suggested Commit

```text
feat: validate report status transitions
```

---

# Phase 4 — Quality & Delivery

## Checkpoint 10 — Testing & Quality

**Status: In progress**

Checkpoint 10 focuses on validating existing functionality rather than introducing unnecessary new features.

### Checkpoint 10.1 — Volunteer Capacity Validation

**Status: Completed**

#### Problem

A help request could previously be updated to require fewer volunteers than the number of people already participating.

Example:

```text
Original capacity: 5
Registered participants: 3
Requested new capacity: 2
```

The update must be rejected because three volunteers are already registered.

#### Business Rule

```text
volunteersNeeded >= currentParticipantCount
```

#### Tasks

- [x] Reuse `HelpParticipationRepository.countByHelpRequest()`.
- [x] Add participation repository dependency to `ReportService`.
- [x] Connect the participation repository in `Routing.kt`.
- [x] Validate volunteer capacity in `ReportService.update()`.
- [x] Reject invalid capacity reductions.
- [x] Allow capacity equal to participant count.
- [x] Allow capacity increases.
- [x] Write three focused service tests.
- [x] Run the complete test suite.

#### Implementation

The service checks the participant count before updating a `HelpRequest`.

An invalid reduction throws an `IllegalArgumentException`.

The existing error handling converts this exception to:

```http
400 Bad Request
```

#### Test Scenarios

| Participants | New Capacity | Expected Result |
|---|---|---|
| 3 | 2 | Rejected |
| 3 | 3 | Accepted |
| 3 | 6 | Accepted |

#### Recorded Milestone

**266 tests passed after Checkpoint 10.1.**

---

### Checkpoint 10.2 — Volunteer Capacity API Integration Tests

**Status: Completed**

#### Goal

Verify that volunteer capacity validation works through the actual REST API, not only when calling the service directly.

#### Tasks

- [x] Review `ReportRoutesTest.kt`.
- [x] Review `ErrorHandling.kt`.
- [x] Add a fake participation repository to the route test setup.
- [x] Configure `ReportService` with the fake participation repository.
- [x] Test invalid capacity reduction through HTTP PUT.
- [x] Verify HTTP 400 for invalid capacity.
- [x] Verify that rejected updates do not modify the stored report.
- [x] Test a valid capacity update through HTTP PUT.
- [x] Verify HTTP 200 for valid capacity.
- [x] Verify that the new capacity is stored.
- [x] Run the complete test suite.
- [x] Commit the completed changes.

#### Test 1 — Invalid Capacity

```text
Initial capacity: 5
Registered participants: 3
Requested capacity: 2

Expected:
HTTP 400 Bad Request
Stored capacity remains 5
```

#### Test 2 — Valid Capacity

```text
Initial capacity: 5
Registered participants: 3
Requested capacity: 3

Expected:
HTTP 200 OK
Stored capacity becomes 3
```

#### Files Involved

- `ReportService.kt`
- `Routing.kt`
- `ReportVolunteerCapacityTest.kt`
- `ReportRoutesTest.kt`

The following files did not require changes:

- `Report.kt`
- `ReportRepository.kt`
- `ExposedReportRepository.kt`
- `HelpParticipationRepository.kt`
- `ErrorHandling.kt`

#### Recorded Milestone

**268 tests passed — 0 failures.**

The changes were committed to Git.

#### Commit

```text
Complete volunteer capacity validation and API tests
```

The exact commit hash and message can be checked in Git history.

---

### Checkpoint 10.3 — Final Quality Review

**Status: Pending**

#### Tasks

- [ ] Review automated test coverage.
- [ ] Manually verify the health endpoint.
- [ ] Manually verify registration and login.
- [ ] Manually verify report creation, retrieval, updating and deletion.
- [ ] Manually verify report searching and sorting.
- [ ] Manually verify voting.
- [ ] Manually verify help participation.
- [ ] Manually verify report status transitions.
- [ ] Manually verify volunteer capacity validation.
- [ ] Verify missing and invalid JWT behavior.
- [ ] Verify owner and ADMIN authorization.
- [ ] Verify invalid request handling.
- [ ] Verify database initialization.
- [ ] Verify database cascade deletion.
- [ ] Confirm the intended scope of status transition rules.
- [ ] Review Kotlin null-safety.
- [ ] Remove unused imports and unnecessary code.
- [ ] Check for hardcoded secrets or credentials.
- [ ] Run the complete automated test suite.
- [ ] Check Git status before proceeding.

#### Important Review Point

`ReportService` currently accepts an optional `HelpParticipationRepository`.

When the repository is not provided, the volunteer count defaults to zero.

The real application supplies this repository through `Routing.kt`, so the capacity check is active there.

During the final review, confirm that all production construction paths provide the required repository.

#### School Requirements

- At least three meaningful unit tests.
- At least three meaningful integration tests.
- Demonstrate OOP concepts.
- Demonstrate FP concepts.
- Meet individual portfolio requirements.

The current test suite contains **268 passing tests**, but the school requirements should be checked against the official assignment.

#### Completion Criteria

Checkpoint 10 is complete when:

- All automated tests pass.
- Relevant endpoints have been manually verified.
- No known blocking defects remain.
- Business-rule decisions are confirmed.
- The project is ready for final documentation.

#### Suggested Commit

```text
test: complete final API quality review
```

---

## Checkpoint 11 — Documentation & Delivery

**Status: In progress**

### 11.1 — README

**Status: Updated; final verification pending**

#### Tasks

- [x] Write a complete project introduction.
- [x] Document the technology stack.
- [x] Document the feature-based architecture.
- [x] Document report management.
- [x] Document authentication and authorization.
- [x] Document voting.
- [x] Document help participation.
- [x] Document status management.
- [x] Document the database structure.
- [x] Document API endpoints.
- [x] Add HTTP request examples.
- [x] Explain OOP and FP concepts.
- [x] Document testing.
- [x] Document remaining work.
- [ ] Verify README against the final source code.
- [ ] Update the recorded test count to 268 where necessary.
- [ ] Verify installation and startup instructions on a clean environment.

### 11.2 — UML Class Diagram

**Status: Pending**

#### Tasks

- [ ] Create a UML class diagram.
- [ ] Include the abstract `Report` class.
- [ ] Include `SafetyReport`, `NuisanceReport` and `HelpRequest`.
- [ ] Show inheritance relationships.
- [ ] Include relevant user and participation models.
- [ ] Include relevant repository interfaces.
- [ ] Show important associations.
- [ ] Verify class names and relationships against the actual code.
- [ ] Add the diagram to the school documentation.

#### Important Concepts

- Abstraction.
- Inheritance.
- Polymorphism.
- Encapsulation.
- Interfaces.
- Associations.

### 11.3 — UML Package Diagram

**Status: Pending**

#### Tasks

- [ ] Create a package diagram.
- [ ] Show the feature-based package structure.
- [ ] Show dependencies between routes, services and repositories.
- [ ] Show shared configuration and security packages.
- [ ] Explain the separation of responsibilities.

### 11.4 — Sequence Diagrams

**Status: Pending**

Create at least three sequence diagrams based on real application behavior.

#### Suggested Diagram 1 — User Login

```text
Client
  -> AuthRoutes
  -> AuthService
  -> UserRepository
  -> BCryptPasswordHasher
  -> JwtService
  -> Client
```

#### Suggested Diagram 2 — Create Report

```text
Client
  -> ReportRoutes
  -> ReportService
  -> ReportRepository
  -> H2 Database
  -> Client
```

#### Suggested Diagram 3 — Join Help Request

```text
Client
  -> HelpParticipationRoutes
  -> HelpParticipationService
  -> ReportRepository
  -> HelpParticipationRepository
  -> H2 Database
  -> Client
```

#### Tasks

- [ ] Create login sequence diagram.
- [ ] Create report creation sequence diagram.
- [ ] Create help participation sequence diagram.
- [ ] Include relevant validation steps.
- [ ] Include relevant alternative/error flows.
- [ ] Verify method names and call order against the actual code.
- [ ] Add diagrams to the school documentation.

### 11.5 — OOP and FP Explanation

**Status: Partially documented**

#### OOP Tasks

- [x] Identify abstraction in the report model.
- [x] Identify inheritance in report subclasses.
- [x] Identify polymorphism in report handling.
- [x] Identify encapsulation in models and services.
- [x] Identify repository interfaces.
- [ ] Add concrete code examples to the school report.
- [ ] Explain why these design choices were made.

#### FP Tasks

- [x] Identify use of `filter`.
- [x] Identify use of `map`.
- [x] Identify use of `count`.
- [x] Identify use of `associate`.
- [x] Identify use of `sortedWith`.
- [x] Identify use of `compareBy` and `thenBy`.
- [ ] Add concrete code examples to the school report.
- [ ] Explain the benefits of functional collection operations.

### 11.6 — School Report

**Status: Pending**

#### Tasks

- [ ] Describe the project goal.
- [ ] Describe the functional requirements.
- [ ] Explain the architecture.
- [ ] Explain the database design.
- [ ] Explain authentication and authorization.
- [ ] Explain OOP concepts.
- [ ] Explain FP concepts.
- [ ] Include UML diagrams.
- [ ] Include sequence diagrams.
- [ ] Explain testing strategy.
- [ ] Include relevant test evidence.
- [ ] Describe development decisions.
- [ ] Reflect on challenges and improvements.
- [ ] Check the official assessment rubric.

### 11.7 — Demonstration Video

**Status: Pending**

#### Suggested Demonstration

1. Introduce Brul2.
2. Explain the technology stack.
3. Show the project structure.
4. Start the API.
5. Demonstrate the health endpoint.
6. Register a user.
7. Log in and obtain a JWT.
8. Create a report.
9. Search and retrieve reports.
10. Update a report.
11. Demonstrate voting.
12. Demonstrate help participation.
13. Demonstrate capacity validation.
14. Show the automated test results.
15. Briefly explain the architecture and UML diagrams.

#### Tasks

- [ ] Prepare a demonstration script.
- [ ] Prepare example requests.
- [ ] Record the demonstration.
- [ ] Verify audio and video quality.
- [ ] Check the required video length.
- [ ] Export the final video.

### 11.8 — Final Delivery

**Status: Pending**

#### Tasks

- [ ] Verify the README.
- [ ] Verify the ROADMAP.
- [ ] Verify UML diagrams.
- [ ] Verify sequence diagrams.
- [ ] Verify the school report.
- [ ] Verify the demonstration video.
- [ ] Run all automated tests.
- [ ] Confirm the final test count.
- [ ] Verify configuration files.
- [ ] Verify no secrets are committed.
- [ ] Check Git status.
- [ ] Commit final documentation changes.
- [ ] Push changes to GitHub.
- [ ] Verify the repository is accessible.
- [ ] Submit all required deliverables.

#### Suggested Commit

```text
docs: finalize Brul2 documentation
```

---

# Implemented API Summary

## Health

```http
GET /api/v1/health
```

## Authentication

```http
POST /api/v1/auth/register
POST /api/v1/auth/login
```

## Reports

```http
GET    /api/v1/reports
GET    /api/v1/reports/{id}
POST   /api/v1/reports
PUT    /api/v1/reports/{id}
DELETE /api/v1/reports/{id}
```

## Voting

```http
GET    /api/v1/reports/{id}/votes
PUT    /api/v1/reports/{id}/vote
GET    /api/v1/reports/{id}/vote/me
DELETE /api/v1/reports/{id}/vote
```

## Help Participation

```http
GET    /api/v1/reports/{id}/participants
GET    /api/v1/reports/{id}/participants/list
POST   /api/v1/reports/{id}/participants
DELETE /api/v1/reports/{id}/participants/me
```

## Authentication Summary

Public endpoints:

- Health check.
- Registration.
- Login.
- Report retrieval and searching.
- Vote summaries.
- Participation summaries and lists.

Authenticated endpoints:

- Report creation.
- Report updates by the owner.
- Report deletion by the owner or ADMIN.
- Voting.
- Joining help requests.
- Leaving help requests.

---

# Architecture

Brul2 uses a feature-based architecture.

## Main Flow

```text
HTTP Request
     |
     v
Ktor Routes
     |
     v
Services
     |
     v
Repository Interfaces
     |
     v
Exposed Repositories
     |
     v
H2 Database
```

## Main Packages

| Package | Responsibility |
|---|---|
| `config` | Application configuration |
| `common` | Shared technical functionality |
| `database` | Database initialization |
| `plugins` | Ktor routing and error handling |
| `security` | Password hashing and JWT |
| `features/report` | Report models, CRUD, search and status rules |
| `features/user` | User model and persistence |
| `features/auth` | Registration and login |
| `features/vote` | Voting and vote summaries |
| `features/help` | Help participation and capacity |

## Design Principles

- Routes handle HTTP requests and responses.
- Services implement business rules.
- Repository interfaces define persistence operations.
- Exposed repositories access the database.
- Domain models validate their own properties.
- Authentication and authorization protect operations.
- Tests verify behavior at multiple layers.

---

# Development Workflow

Follow these steps for each remaining checkpoint:

1. Review the requirement.
2. Identify relevant existing functionality.
3. Define meaningful test scenarios.
4. Make the smallest necessary changes.
5. Run automated tests.
6. Manually verify relevant behavior.
7. Update documentation.
8. Check Git status.
9. Commit completed work.
10. Push to GitHub when ready.

## Useful Commands

Run all tests:

```bash
kotlin test
```

Check Git status:

```bash
git status
```

Stage changes:

```bash
git add .
```

Commit changes:

```bash
git commit -m "type: describe completed work"
```

Push changes:

```bash
git push
```

## Commit Naming Conventions

| Prefix | Purpose |
|---|---|
| `chore:` | Setup and configuration |
| `feat:` | New functionality |
| `fix:` | Bug fixes |
| `test:` | Tests and test improvements |
| `refactor:` | Code improvements without behavior changes |
| `docs:` | Documentation |

Only commit when:

- The project compiles.
- Relevant tests pass.
- Existing functionality still works.
- No secrets or unnecessary generated files are staged.
- Documentation is updated where needed.

---

# Test Milestones

| Milestone | Passing Tests |
|---|---:|
| After Checkpoint 7 | 239 |
| After Checkpoint 8 | 257 estimated |
| After Checkpoint 9 | 263 estimated |
| After Checkpoint 10.1 | 266 |
| After Checkpoint 10.2 | **268 confirmed** |

The intermediate totals for Checkpoints 8 and 9 are based on the number of tests added. The final total of **268 passing tests** was confirmed after Checkpoint 10.2.

---

# Current Next Step

## Checkpoint 10.3 — Final Quality Review

The next step is to perform a final review of the existing application.

### Priorities

1. Verify the main API endpoints manually.
2. Confirm the status transition requirements.
3. Review authentication and authorization.
4. Check database initialization and cascade deletion.
5. Review configuration and code quality.
6. Run all 268 tests again.

After this review, proceed to **Checkpoint 11 — Documentation & Delivery**.

### First Documentation Deliverable

Create the **UML class diagram** showing:

- `Report`
- `SafetyReport`
- `NuisanceReport`
- `HelpRequest`
- Relevant repository interfaces
- Important model relationships

Then create the UML package diagram and three sequence diagrams.

---

# Latest Progress

**Date:** 20 October 2026

**Completed:**

- Checkpoints 1–9.
- Checkpoint 10.1 — Volunteer Capacity Validation.
- Checkpoint 10.2 — Volunteer Capacity API Integration Tests.
- Full automated test suite: **268 passed**.
- Volunteer capacity validation changes committed.
- README updated.
- ROADMAP updated.

**Remaining:**

- Checkpoint 10.3 — Final Quality Review.
- Final README verification.
- UML class diagram.
- UML package diagram.
- Three sequence diagrams.
- OOP and FP explanations for the school report.
- School report.
- Demonstration video.
- Final GitHub and submission checks.

**Project status:** Core implementation completed; final quality review and school deliverables remain.