# Brul2

Brul2 is a REST API built with **Kotlin, Ktor, JetBrains Amper, JetBrains Exposed, and H2**.

The project allows residents to report neighborhood safety issues, report nuisance, and request help from other residents.

We develop Brul2 in small, testable checkpoints. We use object-oriented programming (OOP), functional programming (FP), database persistence, and automated testing to build a maintainable application that meets the school assignment requirements.

**Current progress:** Checkpoint 6.2 completed — **105 tests passed**.

**Next checkpoint:** 6.3 — User Registration and Login.

---

## Project Structure

Brul2 follows a feature-based architecture. We organize application functionality by feature and keep shared technical components in separate packages.

```text
Brul2/
├── docs/
│   └── uml/
│       └── report-domain.puml
├── resources/
│   ├── application.yaml
│   └── logback.xml
├── src/
│   └── nl/brul/api/
│       ├── Application.kt
│       ├── common/
│       │   └── error/
│       │       └── ApiError.kt
│       ├── config/
│       │   └── AppConfig.kt
│       ├── database/
│       │   └── DatabaseFactory.kt
│       ├── plugins/
│       │   ├── ErrorHandling.kt
│       │   ├── Routing.kt
│       │   └── Serialization.kt
│       ├── security/
│       │   ├── PasswordHasher.kt
│       │   └── BCryptPasswordHasher.kt
│       └── features/
│           ├── report/
│           │   ├── dto/
│           │   ├── model/
│           │   ├── repository/
│           │   ├── routes/
│           │   └── service/
│           └── user/
│               ├── model/
│               └── repository/
├── test/
│   └── nl/brul/api/
│       ├── ErrorHandlingTest.kt
│       ├── HealthRoutesTest.kt
│       ├── security/
│       │   └── BCryptPasswordHasherTest.kt
│       └── features/
│           ├── report/
│           └── user/
├── module.yaml
├── README.md
└── ROADMAP.md
```

### Package Responsibilities

| Package | Responsibility |
|---|---|
| `plugins/` | Configure Ktor routing, JSON serialization, and error handling |
| `security/` | Implement password hashing and future JWT security components |
| `features/report/` | Handle report models, persistence, business logic, and HTTP endpoints |
| `features/user/` | Handle user models and database persistence |
| `database/` | Initialize the H2 database and its tables |
| `config/` | Load application configuration |
| `common/` | Provide shared application components |

We will add `features/auth/` when we implement registration and login.

---

## Technologies

| Technology | Purpose |
|---|---|
| Kotlin | Main programming language |
| Ktor | REST API framework |
| JetBrains Amper | Project configuration and build system |
| kotlinx.serialization | JSON serialization and deserialization |
| Ktor ContentNegotiation | Handle JSON requests and responses |
| Ktor StatusPages | Handle HTTP errors centrally |
| H2 Database | Store application data |
| JetBrains Exposed SQL DSL | Perform database operations |
| jBCrypt | Hash and verify user passwords |
| kotlin.test | Write unit and integration tests |
| Ktor Test Host | Test HTTP endpoints without starting a separate server |

We use established libraries for common technical tasks and implement Brul2-specific business rules ourselves.

---

## Getting Started

### Requirements

To run Brul2, you need:

- A compatible Java Development Kit (JDK).
- The Kotlin CLI with JetBrains Amper support.
- IntelliJ IDEA (recommended).

### Run the Application

Open a terminal in the project root and run:

```bash
kotlin run
```

Ktor starts the API using the configuration in:

```text
resources/application.yaml
```

The default local API address is:

```text
http://localhost:8080
```

### Run the Tests

Run the complete test suite:

```bash
kotlin test
```

Latest verified result:

```text
✓ Compilation successful for Brul2 [jvm]
✓ Compilation successful for Brul2 [jvm tests]
Tests: 105 passed
```

---

## Current API Endpoints

Brul2 currently exposes six REST API routes.

| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/v1/health` | Check whether the API is running |
| GET | `/api/v1/reports` | Retrieve reports with optional search, filtering, and sorting |
| GET | `/api/v1/reports/{id}` | Retrieve a report by ID |
| POST | `/api/v1/reports` | Create a report |
| PUT | `/api/v1/reports/{id}` | Update a report |
| DELETE | `/api/v1/reports/{id}` | Delete a report |

All endpoints use the `/api/v1` prefix.

### Health Check

Request:

```http
GET http://localhost:8080/api/v1/health
```

The endpoint returns HTTP 200 and a JSON response containing the API status and configured service name.

Example:

```json
{
  "status": "ok",
  "service": "brul-api"
}
```

The service name comes from `resources/application.yaml`.

### Retrieve Reports

Request:

```http
GET http://localhost:8080/api/v1/reports
```

The endpoint returns the stored reports as JSON.

Retrieve a specific report:

```http
GET http://localhost:8080/api/v1/reports/1
```

The API returns HTTP 404 when the requested report does not exist.

### Search, Filtering, and Sorting

The reports endpoint supports searching, filtering, and sorting.

Implemented functionality includes:

- Search by report title or description.
- Filter by report type.
- Filter by report category.
- Filter by location and distance.
- Combine search and filter criteria.
- Sort reports by ID, creation time, view count, or distance.

Supported sorting options include:

| Sort Order | Description |
|---|---|
| `ID_ASC` | Sort by ID in ascending order |
| `ID_DESC` | Sort by ID in descending order |
| `NEWEST` | Show the newest reports first |
| `MOST_VIEWED` | Show reports with the highest view count first |
| `NEAREST` | Sort reports by distance from a supplied location |

Example requests:

```http
GET http://localhost:8080/api/v1/reports?sort=NEWEST
```

```http
GET http://localhost:8080/api/v1/reports?sort=MOST_VIEWED
```

```http
GET http://localhost:8080/api/v1/reports?sort=ID_DESC
```

Distance-based sorting requires valid location coordinates.

### Create, Update, and Delete Reports

The API supports the complete report CRUD lifecycle.

Create a report:

```http
POST http://localhost:8080/api/v1/reports
Content-Type: application/json
```

Update a report:

```http
PUT http://localhost:8080/api/v1/reports/1
Content-Type: application/json
```

Delete a report:

```http
DELETE http://localhost:8080/api/v1/reports/1
```

We use request and response DTOs to separate the public HTTP API from internal domain models.

---

## Architecture

Brul2 uses a feature-based architecture with clear responsibilities.

A typical report request follows this flow:

```text
HTTP Client
    |
    v
Ktor Plugins
    |
    v
ReportRoutes
    |
    v
Request DTO / Mapper
    |
    v
ReportService
    |
    v
ReportRepository (interface)
    |
    v
ExposedReportRepository
    |
    v
H2 Database
```

The response travels back through the application and is converted into JSON.

### Routes

Routes receive HTTP requests, read request parameters, and return HTTP responses.

We keep feature-specific endpoints inside their feature packages.

For example:

```text
features/report/routes/ReportRoutes.kt
```

### Services

Services coordinate application operations and enforce business rules.

For example:

```text
features/report/service/ReportService.kt
```

`ReportService` depends on the `ReportRepository` interface instead of directly depending on Exposed.

This allows us to test business logic independently from database operations.

### Repositories

Repository interfaces define the available persistence operations.

Exposed repository implementations execute those operations against H2.

For reports, we use:

- `ReportRepository` — Repository interface.
- `ExposedReportRepository` — Database implementation.
- `ReportsTable` — Exposed table definition.
- `NewReport` — Input data for creating a report.

For users, we use:

- `UserRepository` — Repository interface.
- `ExposedUserRepository` — Database implementation.
- `UsersTable` — Exposed table definition.
- `NewUser` — Input data for creating a user.

### Dependency Injection

We use constructor injection to provide dependencies.

For example:

```kotlin
class ReportService(
    private val repository: ReportRepository
)
```

This keeps the service independent of a specific database implementation.

We do not currently use a dependency injection framework.

---

## Ktor Plugin Configuration

We organize Ktor configuration inside the `plugins/` package.

### Serialization

`Serialization.kt` configures `ContentNegotiation` and `kotlinx.serialization`.

The JSON configuration rejects unknown request fields:

```kotlin
Json {
    ignoreUnknownKeys = false
}
```

### Error Handling

`ErrorHandling.kt` configures Ktor's `StatusPages` plugin.

The API uses centralized error handling for invalid requests, missing resources, unsupported HTTP methods, and unexpected server errors.

The application returns JSON error responses through `ApiError`.

### Routing

`Routing.kt` registers the health endpoint and report routes.

It also connects the report repository to `ReportService`.

### Application Startup

`Application.kt` coordinates startup by:

1. Loading application configuration.
2. Installing JSON serialization.
3. Installing centralized error handling.
4. Initializing the database.
5. Registering API routes.

This keeps application startup separate from plugin implementation details.

---

## Report Domain Models

The report domain uses an abstract `Report` class and three subclasses.

| Class | Purpose | Specific Property |
|---|---|---|
| `SafetyReport` | Report neighborhood safety concerns | `urgent` |
| `NuisanceReport` | Report neighborhood nuisance | — |
| `HelpRequest` | Request help from other residents | `volunteersNeeded` |

All reports share common properties, including:

- ID
- Title
- Description
- Report type
- Category
- Location
- Status
- Creation timestamp (`createdAt`)
- View count (`viewCount`)

### Report Categories

Every report has a `ReportCategory`.

Each report type accepts specific categories.

| Report Type | Allowed Categories |
|---|---|
| SafetyReport | THEFT, SUSPICIOUS_SITUATION, TRAFFIC_SAFETY |
| NuisanceReport | NOISE, WASTE, VANDALISM, PARKING |
| HelpRequest | GROCERIES, TRANSPORT, MOVING, CHORES, OTHER |

The domain classes reject invalid type-category combinations.

### Object-Oriented Programming

The report domain demonstrates:

- **Abstraction:** `Report` defines shared properties and behavior.
- **Inheritance:** Three concrete report classes extend `Report`.
- **Polymorphism:** Subclasses implement their own category validation.
- **Encapsulation:** Domain models enforce their own validation rules.

### Functional Programming

The implementation also uses Kotlin features such as:

- Lambdas for filtering and sorting.
- Collection operations.
- Extension functions for distance calculations and DTO mapping.
- Function types for configurable dependencies.
- Null-safe operations.

For example, `Location.distanceTo()` calculates the distance between two geographic coordinates using the Haversine formula.

---

## Database

Brul2 uses **H2** and **JetBrains Exposed SQL DSL**.

`DatabaseFactory` initializes the database and creates the required tables.

The application currently uses two main tables.

### Reports Table

`ReportsTable` stores the three report types in one table.

Its data includes:

- Report ID and type.
- Title and description.
- Category and status.
- Latitude and longitude.
- Report-specific fields such as urgency and required volunteers.
- Creation timestamp.
- View count.

The repository converts database rows into the correct report subclasses.

### Users Table

`UsersTable` stores user accounts.

The table contains:

- `id`
- `username`
- `email`
- `passwordHash`
- `createdAt`

The email column has a unique constraint.

`ExposedUserRepository` normalizes email addresses to lowercase and supports creating users and retrieving them by ID or email.

### Database Testing

Repository integration tests use separate in-memory H2 databases.

This allows tests to verify real SQL behavior without modifying the application's persistent development database.

---

## User Domain and Password Security

Checkpoint 6.1 introduced the user domain and database persistence.

The `User` model stores:

- User ID.
- Username.
- Email.
- Password hash.
- Creation timestamp.

`NewUser` represents the data required to create a user.

The user repository provides operations to:

- Create a user.
- Find a user by ID.
- Find a user by email.

### Password Hashing

Checkpoint 6.2 introduced password hashing through a dedicated `security/` package.

We use two components:

**`PasswordHasher`**

Defines the password hashing contract:

```kotlin
interface PasswordHasher {
    fun hash(password: String): String
    fun verify(password: String, passwordHash: String): Boolean
}
```

**`BCryptPasswordHasher`**

Implements the contract with jBCrypt.

The implementation:

- Generates salted BCrypt password hashes.
- Verifies passwords against stored hashes.
- Rejects blank passwords during hashing.
- Rejects passwords exceeding 72 UTF-8 bytes.
- Rejects unsupported BCrypt work factors.
- Returns `false` for invalid verification inputs covered by the implementation.

We never store plaintext passwords.

Registration and login endpoints are not implemented yet. We will connect these security components to `AuthService` in Checkpoint 6.3.

---

## Testing

We use automated tests throughout development.

Our test suite includes:

- Domain unit tests.
- Service unit tests.
- Repository integration tests.
- Ktor route tests.
- HTTP integration tests.
- Password hashing tests.

### Verified Test Progress

| Checkpoint | Total Passing Tests |
|---|---:|
| 4.1 — Report Categories | 19 |
| 5.1 — ReportService CRUD | 28 |
| 5.2A — Search and Distance | 44 |
| 5.2B — Metadata and Sorting | 54 |
| 5.3 — Report DTOs | 66 |
| 5.4 — Report Routes | 76 |
| 5.5 — Error Handling and Integration Tests | 84 |
| 6.1 — User Domain and Database | 95 |
| 6.2 — Password Hashing | **105** |

The final 105-test result also verifies the Ktor plugins and security package refactor.

### Current Test Coverage

The tests cover:

- Health endpoint responses.
- JSON serialization and HTTP error handling.
- Report model validation and category restrictions.
- Report CRUD operations.
- Report search, filtering, and sorting.
- Haversine distance calculations.
- Report metadata persistence.
- DTO mapping and validation.
- Report HTTP endpoints.
- Real H2 persistence through integration tests.
- User model validation.
- User creation and retrieval.
- Email normalization and duplicate detection.
- BCrypt password hashing and verification.
- Invalid password inputs and BCrypt configuration.

### Test Strategy

We use fake repositories in service unit tests to isolate business logic.

We use in-memory H2 databases to verify repository persistence.

We use Ktor Test Host to verify HTTP behavior without launching a separate server.

Run all tests with:

```bash
kotlin test
```

---

## Project Status

### Completed

- [x] Checkpoint 1 — Ktor Server and Routing
- [x] Checkpoint 2 — JSON, Configuration, and Error Handling
- [x] Checkpoint 3 — Report Domain Models
- [x] Checkpoint 4 — Database and Repositories
- [x] Checkpoint 4.1 — Report Categories and Database Refactoring
- [x] Checkpoint 5.1 — ReportService CRUD
- [x] Checkpoint 5.2A — Search, Filtering, and Distance
- [x] Checkpoint 5.2B — Report Metadata and Sorting
- [x] Checkpoint 5.3 — Request and Response DTOs
- [x] Checkpoint 5.4 — Ktor Report Routes
- [x] Checkpoint 5.5 — Error Handling and HTTP Integration Tests
- [x] Checkpoint 6.1 — User Domain and Database
- [x] Checkpoint 6.2 — Password Hashing
- [x] Architecture Refactor — Ktor Plugins and Security Packages

### Next

- [ ] Checkpoint 6.3A — AuthService: Registration and Login Logic
- [ ] Checkpoint 6.3B — Authentication DTOs and HTTP Routes
- [ ] Checkpoint 6.3C — Authentication Integration Tests
- [ ] Checkpoint 6.4 — JWT Authentication
- [ ] Checkpoint 6.5 — Authorization
- [ ] Checkpoint 6.6 — Authentication and Authorization Integration Tests

### Planned

- [ ] Checkpoint 7 — Report Voting
- [ ] Checkpoint 8 — Help Participation
- [ ] Checkpoint 9 — Testing and Quality Review
- [ ] Checkpoint 10 — Documentation and Delivery

---

## Planned Features

We will implement the remaining functionality incrementally.

### Authentication

- Register new users.
- Log in with email and password.
- Return JWT access tokens.
- Authenticate protected requests.
- Apply authorization rules.
- Associate reports with their owners.

Planned endpoints include:

```http
POST /api/v1/auth/register
POST /api/v1/auth/login
GET /api/v1/users/me
```

These endpoints are not available yet.

### Voting

We plan to support:

- Liking and disliking reports.
- Changing or removing votes.
- Preventing duplicate active votes.
- Preventing users from voting on their own reports.
- Calculating vote totals.

### Help Participation

We plan to support:

- Joining help requests.
- Leaving help requests.
- Enforcing participant capacity.
- Preventing duplicate participation.
- Preventing owners from joining their own requests.
- Closing help requests.
- Managing open, full, and closed requests.

### Quality and Delivery

Before delivery, we will:

- Review unit and integration test coverage.
- Verify all API endpoints.
- Review authentication and authorization.
- Add development seed data where useful.
- Complete API documentation.
- Update UML diagrams.
- Document OOP and FP concepts.
- Prepare the required project report and demonstration.
- Verify all school assignment requirements.

---

## Documentation

See [ROADMAP.md](ROADMAP.md) for the detailed development plan.

The `docs/uml/` directory contains the report domain diagram.

We will expand the documentation as we implement additional features.

---

## Development Workflow

We complete each checkpoint using the following process:

1. Define the functionality and expected behavior.
2. Identify relevant Kotlin, Ktor, OOP, and FP concepts.
3. Define test scenarios.
4. Implement the required components.
5. Write unit or integration tests.
6. Run `kotlin test`.
7. Fix compilation errors and failing tests.
8. Verify HTTP endpoints when applicable.
9. Update documentation.
10. Review Git changes, commit, and push.

We only mark a checkpoint as complete after the implementation compiles and the relevant tests pass.

---

## Current Progress

**Checkpoint 6.2 completed — 105 tests passed.**

We have implemented the report REST API, user persistence, BCrypt password hashing, and the improved Ktor plugin architecture.

**Next step: Checkpoint 6.3A — Implement `AuthService` for user registration and login.**