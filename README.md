# Brul2

Brul2 is a REST API built with **Kotlin, Ktor, JetBrains Amper, Exposed and H2**.

The application allows users to create and manage reports about safety issues, nuisance and requests for help. Users can vote on reports and volunteer to participate in help requests.

Brul2 is developed as a second-year HBO Informatica school project. The project focuses on clean code, object-oriented programming (OOP), functional programming (FP), testability and the **Keep It Simple (KISS)** principle.

## Project Status

The core API functionality is implemented and tested.

**Latest confirmed test result: 268 tests passed, 0 failures.**

| Checkpoint | Description | Status |
|---|---|---|
| 1–5 | Foundation, domain, database and Reports API | Completed |
| 6 | Users and JWT authentication | Completed |
| 7 | Voting and sorting | Completed |
| 8 | Help participation | Completed |
| 9 | Report status management | Completed |
| 10.1 | Volunteer capacity validation | Completed |
| 10.2 | Volunteer capacity API integration tests | Completed |
| 10.3 | Final quality review | Pending |
| 11 | Documentation and delivery | In progress |

See [ROADMAP.md](ROADMAP.md) for the complete development history and remaining tasks.

---

## Features

### 1. Report Management

Users can create three types of reports:

- **Safety reports** — Reports about safety-related situations.
- **Nuisance reports** — Reports about nuisance or disturbances.
- **Help requests** — Requests for assistance from volunteers.

The Reports API supports:

- Creating reports.
- Retrieving individual reports.
- Listing all reports.
- Updating existing reports.
- Deleting reports.
- Searching reports by title and description.
- Filtering by report type and category.
- Filtering reports by geographical distance.
- Sorting reports by ID, creation date, distance, view count or likes.
- Managing report statuses.
- Tracking report ownership.

Creating reports requires authentication.

Only the report owner can update a report. Report owners and administrators can delete reports.

### 2. Authentication and Authorization

Brul2 uses JWT (JSON Web Tokens) for authentication.

Supported functionality:

- User registration.
- User login.
- BCrypt password hashing.
- JWT access tokens.
- USER and ADMIN roles.
- Report ownership validation.
- Protected API endpoints.
- Authorization checks for sensitive operations.

Passwords are stored as BCrypt hashes instead of plain text.

Protected endpoints require a valid JWT in the Authorization header.

### 3. Voting

Authenticated users can vote on reports.

Supported functionality:

- Like a report.
- Dislike a report.
- Change an existing vote.
- Remove a vote.
- Retrieve vote totals.
- Retrieve the authenticated user's vote.
- Sort reports by number of likes.

Business rules:

- A user can have one vote per report.
- Existing votes can be changed.
- Votes can be removed.
- Related votes are automatically deleted when a report is deleted.

### 4. Help Participation

Authenticated users can volunteer for help requests.

Supported functionality:

- Join a help request.
- Leave a help request.
- Retrieve participation summaries.
- View the number of participants.
- View the participant list.
- View available volunteer places.
- Prevent duplicate participation.
- Prevent owners from joining their own help requests.
- Prevent participation when capacity is full.
- Prevent new participation when the help request is no longer open.

#### Participation Availability

Help participation uses the following derived availability statuses:

| Status | Description |
|---|---|
| `OPEN` | The help request is open and places are available. |
| `FULL` | All volunteer places are occupied. |
| `CLOSED` | The help request is no longer open for new participants. |

These statuses describe participation availability and are separate from the report's stored status.

### 5. Volunteer Capacity Validation

Help requests contain a `volunteersNeeded` property that defines how many volunteers are required.

The API prevents the owner from reducing this number below the current participant count.

Business rule:

```text
volunteersNeeded >= currentParticipantCount
```

Example:

```text
Original capacity: 5
Registered participants: 3
Requested capacity: 2

Result: Rejected
HTTP status: 400 Bad Request
Stored capacity: 5
```

A valid update is allowed:

```text
Original capacity: 5
Registered participants: 3
Requested capacity: 3

Result: Accepted
HTTP status: 200 OK
Stored capacity: 3
```

This validation is implemented in `ReportService.update()`.

The service uses `HelpParticipationRepository.countByHelpRequest()` to retrieve the current participant count.

The participation repository is connected to `ReportService` in `Routing.kt`.

Both service-level and HTTP integration tests verify this behavior.

### 6. Report Status Management

Reports support three statuses:

- `OPEN`
- `IN_PROGRESS`
- `RESOLVED`

Allowed transitions:

| Current Status | Allowed Next Status |
|---|---|
| `OPEN` | `OPEN`, `IN_PROGRESS`, `RESOLVED` |
| `IN_PROGRESS` | `IN_PROGRESS`, `RESOLVED` |
| `RESOLVED` | `RESOLVED` |

Resolved reports cannot be reopened.

Status transition validation is implemented in:

```text
ReportStatusRules.kt
```

The rules are checked by `ReportService.update()` before updating a report.

These rules currently apply to all report types.

---

## Technologies

| Technology | Purpose |
|---|---|
| Kotlin | Programming language |
| Ktor | REST API framework |
| JetBrains Amper | Project configuration and build tooling |
| kotlinx.serialization | JSON serialization and deserialization |
| Exposed DSL | Database access |
| H2 | Relational database |
| BCrypt | Password hashing |
| JWT | Authentication |
| Kotlin Test | Unit and integration testing |
| PlantUML | UML diagrams |

---

## Project Structure

Brul2 follows a feature-based architecture.

The structure below reflects the latest project tree.

```text
Brul2/
├── docs/
│   └── uml/
│       └── report-domain.puml
│
├── resources/
│   ├── application.yaml
│   └── logback.xml
│
├── src/
│   └── nl/brul/api/
│       ├── Application.kt
│       │
│       ├── common/
│       │   └── error/
│       │       └── ApiError.kt
│       │
│       ├── config/
│       │   └── AppConfig.kt
│       │
│       ├── database/
│       │   └── DatabaseFactory.kt
│       │
│       ├── features/
│       │   ├── auth/
│       │   │   ├── dto/
│       │   │   │   ├── AuthResponse.kt
│       │   │   │   ├── CurrentUserResponse.kt
│       │   │   │   ├── LoginRequest.kt
│       │   │   │   ├── LoginResponse.kt
│       │   │   │   └── RegisterRequest.kt
│       │   │   ├── routes/
│       │   │   │   └── AuthRoutes.kt
│       │   │   └── service/
│       │   │       ├── AuthService.kt
│       │   │       ├── EmailAlreadyRegisteredException.kt
│       │   │       └── InvalidCredentialsException.kt
│       │   │
│       │   ├── help/
│       │   │   ├── dto/
│       │   │   │   └── HelpParticipationResponse.kt
│       │   │   ├── model/
│       │   │   │   ├── HelpParticipation.kt
│       │   │   │   └── HelpParticipationStatus.kt
│       │   │   ├── repository/
│       │   │   │   ├── ExposedHelpParticipationRepository.kt
│       │   │   │   ├── HelpParticipationRepository.kt
│       │   │   │   └── HelpParticipationsTable.kt
│       │   │   ├── routes/
│       │   │   │   └── HelpParticipationRoutes.kt
│       │   │   └── service/
│       │   │       ├── HelpParticipationExceptions.kt
│       │   │       └── HelpParticipationService.kt
│       │   │
│       │   ├── report/
│       │   │   ├── dto/
│       │   │   │   ├── CreateReportRequest.kt
│       │   │   │   ├── ReportMapper.kt
│       │   │   │   ├── ReportResponse.kt
│       │   │   │   └── UpdateReportRequest.kt
│       │   │   ├── model/
│       │   │   │   ├── HelpRequest.kt
│       │   │   │   ├── Location.kt
│       │   │   │   ├── NewReport.kt
│       │   │   │   ├── NuisanceReport.kt
│       │   │   │   ├── Report.kt
│       │   │   │   ├── ReportCategory.kt
│       │   │   │   ├── ReportStatus.kt
│       │   │   │   ├── ReportType.kt
│       │   │   │   └── SafetyReport.kt
│       │   │   ├── repository/
│       │   │   │   ├── ExposedReportRepository.kt
│       │   │   │   ├── ReportRepository.kt
│       │   │   │   └── ReportsTable.kt
│       │   │   ├── routes/
│       │   │   │   └── ReportRoutes.kt
│       │   │   └── service/
│       │   │       ├── ReportSearch.kt
│       │   │       ├── ReportService.kt
│       │   │       └── ReportStatusRules.kt
│       │   │
│       │   ├── user/
│       │   │   ├── model/
│       │   │   │   ├── NewUser.kt
│       │   │   │   ├── User.kt
│       │   │   │   └── UserRole.kt
│       │   │   └── repository/
│       │   │       ├── ExposedUserRepository.kt
│       │   │       ├── UserRepository.kt
│       │   │       └── UsersTable.kt
│       │   │
│       │   └── vote/
│       │       ├── dto/
│       │       │   ├── VoteRequest.kt
│       │       │   ├── VoteResponse.kt
│       │       │   └── VoteSummaryResponse.kt
│       │       ├── model/
│       │       │   ├── Vote.kt
│       │       │   ├── VoteSummary.kt
│       │       │   └── VoteType.kt
│       │       ├── repository/
│       │       │   ├── ExposedVoteRepository.kt
│       │       │   ├── VoteRepository.kt
│       │       │   └── VotesTable.kt
│       │       ├── routes/
│       │       │   └── VoteRoutes.kt
│       │       └── service/
│       │           └── VoteService.kt
│       │
│       ├── plugins/
│       │   ├── ErrorHandling.kt
│       │   ├── Routing.kt
│       │   ├── Security.kt
│       │   └── Serialization.kt
│       │
│       └── security/
│           ├── BCryptPasswordHasher.kt
│           ├── JwtConfig.kt
│           ├── JwtService.kt
│           └── PasswordHasher.kt
│
├── test/
│   └── nl/brul/api/
│       ├── features/
│       │   ├── auth/
│       │   │   ├── routes/
│       │   │   └── service/
│       │   ├── help/
│       │   │   ├── repository/
│       │   │   ├── routes/
│       │   │   └── service/
│       │   ├── report/
│       │   │   ├── dto/
│       │   │   ├── model/
│       │   │   ├── repository/
│       │   │   ├── routes/
│       │   │   └── service/
│       │   ├── user/
│       │   │   ├── model/
│       │   │   └── repository/
│       │   └── vote/
│       │       ├── model/
│       │       ├── repository/
│       │       ├── routes/
│       │       └── service/
│       ├── security/
│       ├── ErrorHandlingTest.kt
│       └── HealthRoutesTest.kt
│
├── .env
├── .env.example
├── .gitattributes
├── .gitignore
├── kotlin
├── kotlin.bat
├── LICENSE
├── module.yaml
├── README.md
└── ROADMAP.md
```

The project structure is organized around application features.

Each feature contains only the layers it needs.

### Important Source Files

| File | Responsibility |
|---|---|
| `Application.kt` | Application entry point |
| `Routing.kt` | Registers API routes and connects services |
| `Security.kt` | Ktor authentication configuration |
| `Serialization.kt` | JSON serialization configuration |
| `ErrorHandling.kt` | Centralized HTTP error handling |
| `AppConfig.kt` | Application configuration |
| `DatabaseFactory.kt` | Database initialization |
| `ReportService.kt` | Report business logic |
| `ReportStatusRules.kt` | Status transition validation |
| `HelpParticipationService.kt` | Help participation business logic |
| `VoteService.kt` | Voting business logic |
| `AuthService.kt` | Registration and login |
| `JwtService.kt` | JWT operations |
| `BCryptPasswordHasher.kt` | Password hashing |

---

## Architecture

Brul2 separates HTTP handling, business logic and database operations.

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

### Routes

Routes handle:

- HTTP methods and paths.
- Request deserialization.
- Response serialization.
- Authentication requirements.
- Authorization checks.
- HTTP status codes.

### Services

Services contain business rules.

Examples:

- Report status transition validation.
- Volunteer capacity validation.
- Help participation rules.
- Voting rules.
- Authentication logic.

### Repository Interfaces

Repository interfaces define persistence operations without depending directly on database implementation details.

This makes services easier to test using fake repositories.

### Exposed Repositories

Exposed repository implementations use Exposed DSL to communicate with the H2 database.

### Domain Models

Domain models represent application concepts and validate their properties.

Examples:

- `Report`
- `SafetyReport`
- `NuisanceReport`
- `HelpRequest`
- `User`
- `Vote`
- `HelpParticipation`

---

## API Endpoints

All endpoints use the prefix:

```text
/api/v1
```

### Health

| Method | Endpoint | Authentication |
|---|---|---|
| GET | `/health` | Public |

Example:

```http
GET /api/v1/health
```

Example response:

```json
{
  "status": "ok",
  "service": "Brul2"
}
```

The `service` value is read from the application configuration.

### Reports

| Method | Endpoint | Authentication |
|---|---|---|
| GET | `/reports` | Public |
| GET | `/reports/{id}` | Public |
| POST | `/reports` | JWT |
| PUT | `/reports/{id}` | Owner |
| DELETE | `/reports/{id}` | Owner or ADMIN |

#### Search Parameters

The `GET /reports` endpoint supports:

| Parameter | Description |
|---|---|
| `query` | Search title and description |
| `type` | Filter by report type |
| `category` | Filter by report category |
| `latitude` | Search center latitude |
| `longitude` | Search center longitude |
| `radiusKm` | Search radius in kilometers |
| `sort` | Sorting option |

Available sorting options:

- `ID_ASC`
- `ID_DESC`
- `NEAREST`
- `NEWEST`
- `MOST_VIEWED`
- `MOST_LIKED`

Example:

```http
GET /api/v1/reports?type=HELP_REQUEST&sort=NEWEST
```

Location-based searches use latitude and longitude together.

### Authentication

| Method | Endpoint | Authentication |
|---|---|---|
| POST | `/auth/register` | Public |
| POST | `/auth/login` | Public |

Protected endpoints require:

```http
Authorization: Bearer YOUR_JWT_TOKEN
```

### Voting

| Method | Endpoint | Authentication |
|---|---|---|
| GET | `/reports/{id}/votes` | Public |
| PUT | `/reports/{id}/vote` | JWT |
| GET | `/reports/{id}/vote/me` | JWT |
| DELETE | `/reports/{id}/vote` | JWT |

### Help Participation

| Method | Endpoint | Authentication |
|---|---|---|
| GET | `/reports/{id}/participants` | Public |
| GET | `/reports/{id}/participants/list` | Public |
| POST | `/reports/{id}/participants` | JWT |
| DELETE | `/reports/{id}/participants/me` | JWT |

The participation summary provides:

- Help request ID.
- Required number of volunteers.
- Current participant count.
- Available volunteer places.
- Participation status.

---

## Example Requests

The examples below use the API paths without a fixed host.

When testing locally, prepend the configured server address.

### Create a Help Request

```http
POST /api/v1/reports
Authorization: Bearer YOUR_JWT_TOKEN
Content-Type: application/json

{
  "type": "HELP_REQUEST",
  "title": "Help with groceries",
  "description": "I need help carrying groceries.",
  "category": "GROCERIES",
  "latitude": 52.0705,
  "longitude": 4.3007,
  "volunteersNeeded": 5
}
```

### Retrieve Reports

```http
GET /api/v1/reports
```

### Search Reports

```http
GET /api/v1/reports?query=groceries
```

### Filter by Location

```http
GET /api/v1/reports?latitude=52.0705&longitude=4.3007&radiusKm=5&sort=NEAREST
```

### Update a Help Request

```http
PUT /api/v1/reports/1
Authorization: Bearer YOUR_JWT_TOKEN
Content-Type: application/json

{
  "type": "HELP_REQUEST",
  "title": "Help with groceries",
  "description": "I need help carrying groceries.",
  "category": "GROCERIES",
  "latitude": 52.0705,
  "longitude": 4.3007,
  "status": "IN_PROGRESS",
  "volunteersNeeded": 5
}
```

Only the report owner can update the report.

### Invalid Volunteer Capacity Update

Assume three volunteers are already registered.

The owner attempts to reduce the capacity to two:

```http
PUT /api/v1/reports/1
Authorization: Bearer YOUR_JWT_TOKEN
Content-Type: application/json

{
  "type": "HELP_REQUEST",
  "title": "Help with groceries",
  "description": "I need help carrying groceries.",
  "category": "GROCERIES",
  "latitude": 52.0705,
  "longitude": 4.3007,
  "status": "OPEN",
  "volunteersNeeded": 2
}
```

Expected response:

```http
400 Bad Request
```

The stored volunteer capacity remains unchanged.

### Retrieve Vote Totals

```http
GET /api/v1/reports/1/votes
```

### Join a Help Request

```http
POST /api/v1/reports/1/participants
Authorization: Bearer YOUR_JWT_TOKEN
```

### Leave a Help Request

```http
DELETE /api/v1/reports/1/participants/me
Authorization: Bearer YOUR_JWT_TOKEN
```

### Retrieve Participation Summary

```http
GET /api/v1/reports/1/participants
```

### Retrieve Participant List

```http
GET /api/v1/reports/1/participants/list
```

---

## Installation and Running

### Requirements

- IntelliJ IDEA.
- A compatible JDK.
- Kotlin and JetBrains Amper tooling.
- Git for version control.

Use the JDK version configured for the project.

### Clone the Repository

```bash
git clone YOUR_REPOSITORY_URL
cd Brul2
```

Replace `YOUR_REPOSITORY_URL` with the actual GitHub repository URL.

### Open the Project

1. Open IntelliJ IDEA.
2. Open the Brul2 project directory.
3. Allow JetBrains Amper to load the project.
4. Check `module.yaml`.
5. Check `resources/application.yaml`.
6. Configure the required environment variables.
7. Start the application using the configured Amper run task or the application's main function.

### Configuration

The project contains:

```text
.env
.env.example
resources/application.yaml
module.yaml
```

Use `.env.example` as a reference for required environment variables.

Do not commit real passwords, JWT secrets or other credentials.

Ensure `.env` is excluded from Git tracking.

### Health Check

After starting the application, send a request to:

```http
GET http://localhost:8080/api/v1/health
```

Port `8080` is an example. Use the actual port configured for the application.

---

## Database

Brul2 uses **H2** as its relational database and **Exposed DSL** for database operations.

### Main Tables

| Table | Purpose |
|---|---|
| `reports` | Stores reports and help requests |
| `users` | Stores user accounts and password hashes |
| `votes` | Stores report votes |
| `help_participations` | Stores volunteer registrations |

The database is initialized through `DatabaseFactory.kt`.

### Relationships

- Reports can belong to users.
- Votes belong to users and reports.
- Help participations belong to users and help requests.
- A user can participate only once in the same help request.

### Cascade Deletion

When a report is deleted:

- Related votes are deleted.
- Related help participations are deleted.

This behavior is configured using database foreign keys.

---

## Testing

Brul2 contains automated unit and integration tests.

### Current Test Result

**268 tests passed — 0 failures.**

This result was confirmed after implementing volunteer capacity validation and its HTTP integration tests.

### Test Coverage

The test suite covers:

- Domain model validation.
- Report creation and persistence.
- Report searching and sorting.
- API requests and responses.
- Registration and login.
- JWT authentication.
- Role-based authorization.
- Report ownership.
- Voting rules and vote totals.
- Help participation.
- Volunteer capacity.
- Report status transitions.
- Database cascade deletion.
- Error handling.

### Volunteer Capacity Tests

Three service tests verify:

1. Capacity cannot be reduced below the participant count.
2. Capacity can be reduced to exactly the participant count.
3. Capacity can be increased above the participant count.

Two HTTP integration tests verify:

1. An invalid capacity reduction returns HTTP 400 and does not modify the report.
2. A valid capacity update returns HTTP 200 and stores the new capacity.

### Run All Tests

From the project root:

```bash
kotlin test
```

The project includes the Amper launcher files:

```text
kotlin
kotlin.bat
```

### Test Structure

Tests are organized by feature and layer.

```text
test/nl/brul/api/
├── features/
│   ├── auth/
│   │   ├── routes/
│   │   └── service/
│   ├── help/
│   │   ├── repository/
│   │   ├── routes/
│   │   └── service/
│   ├── report/
│   │   ├── dto/
│   │   ├── model/
│   │   ├── repository/
│   │   ├── routes/
│   │   └── service/
│   ├── user/
│   │   ├── model/
│   │   └── repository/
│   └── vote/
│       ├── model/
│       ├── repository/
│       ├── routes/
│       └── service/
├── security/
├── ErrorHandlingTest.kt
└── HealthRoutesTest.kt
```

Examples of important test files:

- `ReportRoutesTest.kt`
- `ReportApiIntegrationTest.kt`
- `ReportServiceTest.kt`
- `ReportStatusRulesTest.kt`
- `ReportVolunteerCapacityTest.kt`
- `HelpParticipationServiceTest.kt`
- `HelpParticipationApiTest.kt`
- `VoteApiIntegrationTest.kt`
- `VoteCascadeDeleteTest.kt`
- `AuthApiIntegrationTest.kt`

---

## Object-Oriented Programming

Brul2 demonstrates several OOP concepts.

### Abstraction

The abstract `Report` class defines shared properties and behavior for all report types.

### Inheritance

The following classes extend `Report`:

- `SafetyReport`
- `NuisanceReport`
- `HelpRequest`

### Polymorphism

Different report types can be handled through the shared `Report` type.

For example, `ReportRepository` returns `Report` objects while the actual objects may be different report subclasses.

### Encapsulation

Validation and business rules are kept inside appropriate models and services.

Examples:

- `Report` validates common properties.
- `HelpRequest` validates volunteer requirements.
- `ReportStatusRules` validates status transitions.
- `ReportService` validates volunteer capacity.

### Interfaces

Repository interfaces separate business logic from database implementations.

Examples:

- `ReportRepository`
- `UserRepository`
- `VoteRepository`
- `HelpParticipationRepository`

This allows services to be tested using fake repositories instead of requiring a database for every test.

---

## Functional Programming

Brul2 uses Kotlin collection operations and functional programming concepts.

Examples include:

| Function | Usage |
|---|---|
| `filter` | Searching and filtering reports |
| `map` | Converting database results or models |
| `count` | Counting votes |
| `associate` | Creating lookup maps for vote totals |
| `sortedWith` | Sorting reports |
| `compareBy` | Defining sorting rules |
| `thenBy` | Applying secondary sorting rules |

These functions help keep data processing concise and readable.

### Example: Filtering Reports

```kotlin
val matchingReports = reports.filter { report ->
    report.title.contains(query, ignoreCase = true) ||
        report.description.contains(query, ignoreCase = true)
}
```

### Example: Counting Likes

```kotlin
val likeCount = votes.count { vote ->
    vote.type == VoteType.LIKE
}
```

---

## Error Handling

The API uses Ktor StatusPages for centralized error handling.

### Common HTTP Responses

| Status | Meaning |
|---|---|
| `200 OK` | Request completed successfully |
| `201 Created` | Resource created |
| `204 No Content` | Resource deleted or participation removed |
| `400 Bad Request` | Invalid request data or business-rule validation |
| `401 Unauthorized` | Missing or invalid authentication |
| `403 Forbidden` | Insufficient permissions |
| `404 Not Found` | Requested resource does not exist |
| `409 Conflict` | Conflicting operation |
| `500 Internal Server Error` | Unexpected server error |

### Examples

Invalid volunteer capacity:

```http
400 Bad Request
```

Duplicate participation:

```http
409 Conflict
```

Missing authentication:

```http
401 Unauthorized
```

Unexpected server errors are logged without exposing internal implementation details to API clients.

---

## UML Documentation

The project contains a UML documentation directory:

```text
docs/
└── uml/
    └── report-domain.puml
```

The current PlantUML file is:

```text
docs/uml/report-domain.puml
```

Further UML documentation is planned as part of Checkpoint 11.

Planned deliverables include:

- UML class diagram.
- UML package diagram.
- At least three sequence diagrams.

Suggested sequence diagrams:

1. User login.
2. Report creation.
3. Joining a help request.

The diagrams should be verified against the final implementation.

---

## Development Roadmap

Development is organized into checkpoints.

| Checkpoint | Description | Status |
|---|---|---|
| 1 | Ktor Server & Routing | Completed |
| 2 | JSON, Configuration & Error Handling | Completed |
| 3 | Report Domain Models | Completed |
| 4 | Database & Repositories | Completed |
| 5 | Reports REST API | Completed |
| 6 | Users & Authentication | Completed |
| 7 | Voting | Completed |
| 8 | Help Participation | Completed |
| 9 | Report Status Management | Completed |
| 10.1 | Volunteer Capacity Validation | Completed |
| 10.2 | Volunteer Capacity API Integration Tests | Completed |
| 10.3 | Final Quality Review | Pending |
| 11 | Documentation & Delivery | In progress |

See [ROADMAP.md](ROADMAP.md) for the detailed development history.

---

## Remaining Work

Before final delivery:

- Manually verify the main API endpoints.
- Confirm whether status transition rules should apply to all report types.
- Review authentication and authorization.
- Verify database initialization and cascade deletion.
- Review configuration and code quality.
- Verify installation instructions.
- Complete UML diagrams.
- Complete at least three sequence diagrams.
- Explain OOP and FP usage in the school report.
- Complete the school report.
- Record the demonstration video.
- Verify the official assignment requirements.
- Run the final automated test suite.
- Check Git status and push final changes.

---

## Project Goal

The goal of Brul2 is to demonstrate the development of a maintainable REST API using Kotlin and Ktor.

The project combines:

- Domain modeling.
- Object-oriented programming.
- Functional programming.
- Database persistence.
- Authentication.
- Authorization.
- REST API design.
- Business-rule validation.
- Automated testing.
- Technical documentation.

The implementation intentionally avoids unnecessary complexity and follows the KISS principle.