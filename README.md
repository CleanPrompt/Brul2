# Brul2

Brul2 is a REST API built with **Kotlin, Ktor, JetBrains Amper, Exposed and H2**.

The application allows users to create and manage reports about safety issues, nuisance and requests for help. Users can also vote on reports and volunteer to participate in help requests.

Brul2 is developed as a second-year HBO Informatica school project. The project focuses on clean code, object-oriented programming (OOP), functional programming (FP), testability and the **Keep It Simple (KISS)** principle.

## Features

### Report Management

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

Creating and updating reports requires authentication. Only the report owner can update a report. Report owners and administrators can delete reports.

### Authentication and Authorization

Brul2 uses JWT (JSON Web Tokens) for authentication.

Supported functionality:

- User registration.
- User login.
- BCrypt password hashing.
- JWT authentication.
- USER and ADMIN roles.
- Report ownership validation.
- Protected API endpoints.

Passwords are stored as BCrypt hashes instead of plain text.

### Voting

Authenticated users can vote on reports.

Supported functionality:

- Like a report.
- Dislike a report.
- Change an existing vote.
- Remove a vote.
- Retrieve vote totals.
- Retrieve the authenticated user's vote.
- Sort reports by number of likes.

Users cannot create multiple votes for the same report. Related votes are deleted when a report is deleted.

### Help Participation

Authenticated users can volunteer for help requests.

Supported functionality:

- Join a help request.
- Leave a help request.
- View the number of participants.
- View the list of participants.
- View available volunteer places.
- Prevent duplicate participation.
- Prevent users from joining their own help requests.
- Prevent participation when all places are occupied.
- Prevent new participation when a help request is no longer open.

Help participation uses the following availability statuses:

| Status | Description |
|---|---|
| `OPEN` | The help request is open and places are available. |
| `FULL` | All volunteer places are occupied. |
| `CLOSED` | The help request is no longer open for new participants. |

### Report Status Management

Reports support three statuses:

- `OPEN`
- `IN_PROGRESS`
- `RESOLVED`

Allowed transitions:

| Current status | Allowed next status |
|---|---|
| `OPEN` | `OPEN`, `IN_PROGRESS`, `RESOLVED` |
| `IN_PROGRESS` | `IN_PROGRESS`, `RESOLVED` |
| `RESOLVED` | `RESOLVED` |

Resolved reports cannot be reopened.

These rules currently apply to all report types.

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

## Project Structure

Brul2 follows a feature-based architecture.

The main application packages are organized as follows:

```text
Brul2/
├── README.md
├── ROADMAP.md
├── module.yaml
├── resources/
│   └── application.yaml
├── src/
│   └── nl/brul/api/
│       ├── Application.kt
│       ├── config/
│       │   └── AppConfig.kt
│       ├── database/
│       │   └── DatabaseFactory.kt
│       ├── plugins/
│       │   ├── Routing.kt
│       │   └── ErrorHandling.kt
│       ├── security/
│       │   ├── BCryptPasswordHasher.kt
│       │   └── JwtService.kt
│       └── features/
│           ├── report/
│           │   ├── model/
│           │   ├── dto/
│           │   ├── repository/
│           │   ├── service/
│           │   └── routes/
│           ├── user/
│           │   ├── model/
│           │   └── repository/
│           ├── auth/
│           │   ├── dto/
│           │   ├── service/
│           │   └── routes/
│           ├── vote/
│           │   ├── model/
│           │   ├── dto/
│           │   ├── repository/
│           │   ├── service/
│           │   └── routes/
│           └── help/
│               ├── model/
│               ├── dto/
│               ├── repository/
│               ├── service/
│               └── routes/
└── test/
    └── nl/brul/api/
        └── ...
```

This structure shows the main packages. Individual source and test files may differ.

### Architecture

Each feature separates HTTP handling, business logic and database operations.

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

**Routes** handle HTTP requests, responses and authentication.

**Services** contain business logic and validation.

**Repository interfaces** define database operations and make services easier to test.

**Exposed repositories** implement database operations using Exposed DSL.

**Domain models** represent reports, users, votes and help participations.

## API Endpoints

All endpoints use the prefix:

`/api/v1`

### Health

| Method | Endpoint | Authentication |
|---|---|---|
| GET | `/health` | Public |

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

#### Report Search Parameters

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

Protected endpoints require a JWT access token.

Example:

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

## Example Requests

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
  "volunteersNeeded": 3
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
  "volunteersNeeded": 3
}
```

Only the report owner can update the report.

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

## Installation and Running

### Requirements

- IntelliJ IDEA.
- A compatible JDK.
- Kotlin and JetBrains Amper tooling.
- Git for version control.

Use the JDK version configured for the project.

### Start the Application

1. Clone the repository.
2. Open the project in IntelliJ IDEA.
3. Allow JetBrains Amper to load the project.
4. Check `module.yaml` and `resources/application.yaml`.
5. Start the application using the configured Amper run task or the application's main function.

The API runs on the host and port configured for the Ktor server.

Example health check:

```http
GET http://localhost:8080/api/v1/health
```

Port `8080` is an example. Use the actual port from the application configuration.

### Configuration

Application settings are managed through the project's configuration files and environment where applicable.

The project uses JWT authentication, so the signing secret must be configured securely.

**Do not commit real JWT secrets, passwords or other credentials to GitHub.**

## Database

Brul2 uses **H2** as its relational database and **Exposed DSL** for database operations.

Main database tables:

| Table | Purpose |
|---|---|
| `reports` | Stores reports and help requests |
| `users` | Stores user accounts and password hashes |
| `votes` | Stores report votes |
| `help_participations` | Stores volunteer registrations |

The database is initialized by `DatabaseFactory`.

Related votes and help participations use foreign key cascade deletion when their report is deleted.

## Testing

Brul2 includes automated unit and integration tests.

The tests cover:

- Domain model validation.
- Report creation and persistence.
- Report searching and sorting.
- API requests and responses.
- Registration and login.
- JWT authentication.
- Role-based authorization.
- Report ownership.
- Voting rules and vote totals.
- Help participation and volunteer capacity.
- Report status transitions.
- Database cascade deletion.
- Error handling.

### Run All Tests

From the project root:

```bash
kotlin test
```

All tests were reported as passing after Checkpoint 9.

The expected total is approximately **263 tests**, based on the previously recorded results and added tests. The exact final count should be confirmed from the test runner.

## Object-Oriented Programming

Brul2 demonstrates several OOP concepts.

**Abstraction:** `Report` defines shared behavior for different report types.

**Inheritance:** `SafetyReport`, `NuisanceReport` and `HelpRequest` extend `Report`.

**Polymorphism:** Different report types can be handled through the shared `Report` type.

**Encapsulation:** Domain validation and business rules are kept inside appropriate models and services.

**Interfaces:** Repository interfaces separate business logic from database implementations.

## Functional Programming

Brul2 also uses Kotlin's functional programming features.

Examples include:

- `filter` for report searching.
- `map` for converting models to response DTOs.
- `count` for counting votes.
- `associate` for grouping vote totals by report.
- `sortedWith`, `compareBy` and `thenBy` for sorting results.
- Nullable types and safe calls for handling optional values.

These functions help keep data processing readable and concise.

## Error Handling

The API uses HTTP status codes to communicate request results.

Common responses include:

| Status | Meaning |
|---|---|
| `200 OK` | Request completed successfully |
| `201 Created` | Resource created |
| `204 No Content` | Resource deleted or participation removed |
| `400 Bad Request` | Invalid request data |
| `401 Unauthorized` | Missing or invalid authentication |
| `403 Forbidden` | Insufficient permissions |
| `404 Not Found` | Requested resource does not exist |
| `409 Conflict` | Conflicting operation, such as duplicate participation |
| `500 Internal Server Error` | Unexpected server error |

Ktor StatusPages provides centralized error handling.

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
| 10 | Testing & Quality Review | In progress |
| 11 | Documentation & Delivery | In progress |

See [ROADMAP.md](ROADMAP.md) for the development history, tasks and remaining work.

## Remaining Work

Before final delivery:

- Verify all API endpoints manually.
- Confirm the exact automated test count.
- Validate that the number of required volunteers cannot be reduced below the existing participant count.
- Review whether report status rules should apply to all report types.
- Complete UML diagrams and sequence diagrams.
- Finish the required school report and demonstration video.
- Verify all school assignment requirements.
- Review configuration and Git changes before final submission.

## Project Goal

The goal of Brul2 is to demonstrate the development of a maintainable REST API using Kotlin and Ktor.

The project combines domain modeling, OOP, FP, database persistence, authentication, authorization, REST API design and automated testing.

The implementation intentionally avoids unnecessary complexity and follows the KISS principle.