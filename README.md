# Brul2

Brul2 is a REST API built with Kotlin, Ktor, and JetBrains Amper.

The project focuses on neighborhood reports, including safety issues, nuisance reports, and requests for help.

The application is developed step by step, with automated tests for each checkpoint.

## Project Structure

```text
Brul2
├── docs
│   └── uml
│       └── report-domain.puml
├── resources
│   ├── application.yaml
│   └── logback.xml
├── src
│   ├── nl
│   │   └── brul
│   │       └── api
│   │           ├── common
│   │           │   └── error
│   │           │       ├── ApiError.kt
│   │           │       └── ErrorHandling.kt
│   │           ├── config
│   │           │   └── AppConfig.kt
│   │           ├── database
│   │           │   └── DatabaseFactory.kt
│   │           ├── features
│   │           │   └── report
│   │           │       ├── dto
│   │           │       │   ├── CreateReportRequest.kt
│   │           │       │   ├── ReportMapper.kt
│   │           │       │   ├── ReportResponse.kt
│   │           │       │   └── UpdateReportRequest.kt
│   │           │       ├── model
│   │           │       │   ├── HelpRequest.kt
│   │           │       │   ├── Location.kt
│   │           │       │   ├── NuisanceReport.kt
│   │           │       │   ├── Report.kt
│   │           │       │   ├── ReportCategory.kt
│   │           │       │   ├── ReportStatus.kt
│   │           │       │   ├── ReportType.kt
│   │           │       │   └── SafetyReport.kt
│   │           │       ├── repository
│   │           │       │   ├── ExposedReportRepository.kt
│   │           │       │   ├── NewReport.kt
│   │           │       │   ├── ReportRepository.kt
│   │           │       │   └── ReportsTable.kt
│   │           │       ├── routes
│   │           │       │   └── ReportRoutes.kt
│   │           │       └── service
│   │           │           ├── ReportSearch.kt
│   │           │           └── ReportService.kt
│   │           ├── Application.kt
│   │           └── Routing.kt
│   └── .txt
├── test
│   └── nl
│       └── brul
│           └── api
│               ├── features
│               │   └── report
│               │       ├── dto
│               │       │   └── ReportDtoTest.kt
│               │       ├── model
│               │       │   └── ReportModelTest.kt
│               │       ├── repository
│               │       │   ├── ExposedReportRepositoryTest.kt
│               │       │   └── ReportMetadataTest.kt
│               │       ├── routes
│               │       │   ├── ReportApiIntegrationTest.kt
│               │       │   └── ReportRoutesTest.kt
│               │       └── service
│               │           ├── ReportSearchTest.kt
│               │           ├── ReportServiceTest.kt
│               │           └── ReportSortingTest.kt
│               ├── ErrorHandlingTest.kt
│               └── HealthRoutesTest.kt
├── .gitattributes
├── .gitignore
├── kotlin
├── kotlin.bat
├── LICENSE
├── module.yaml
├── projectstructuur.txt
├── README.md
└── ROADMAP.md
PS C:\Users\phamb\IdeaProjects\Brul2> Show-Tree
├── docs
│   └── uml
│       └── report-domain.puml
├── resources
│   ├── application.yaml
│   └── logback.xml
├── src
│   └── nl
│       └── brul
│           └── api
│               ├── common
│               │   └── error
│               │       ├── ApiError.kt
│               │       └── ErrorHandling.kt
│               ├── config
│               │   └── AppConfig.kt
│               ├── database
│               │   └── DatabaseFactory.kt
│               ├── features
│               │   └── report
│               │       ├── dto
│               │       │   ├── CreateReportRequest.kt
│               │       │   ├── ReportMapper.kt
│               │       │   ├── ReportResponse.kt
│               │       │   └── UpdateReportRequest.kt
│               │       ├── model
│               │       │   ├── HelpRequest.kt
│               │       │   ├── Location.kt
│               │       │   ├── NuisanceReport.kt
│               │       │   ├── Report.kt
│               │       │   ├── ReportCategory.kt
│               │       │   ├── ReportStatus.kt
│               │       │   ├── ReportType.kt
│               │       │   └── SafetyReport.kt
│               │       ├── repository
│               │       │   ├── ExposedReportRepository.kt
│               │       │   ├── NewReport.kt
│               │       │   ├── ReportRepository.kt
│               │       │   └── ReportsTable.kt
│               │       ├── routes
│               │       │   └── ReportRoutes.kt
│               │       └── service
│               │           ├── ReportSearch.kt
│               │           └── ReportService.kt
│               ├── Application.kt
│               └── Routing.kt
├── test
│   └── nl
│       └── brul
│           └── api
│               ├── features
│               │   └── report
│               │       ├── dto
│               │       │   └── ReportDtoTest.kt
│               │       ├── model
│               │       │   └── ReportModelTest.kt
│               │       ├── repository
│               │       │   ├── ExposedReportRepositoryTest.kt
│               │       │   └── ReportMetadataTest.kt
│               │       ├── routes
│               │       │   ├── ReportApiIntegrationTest.kt
│               │       │   └── ReportRoutesTest.kt
│               │       └── service
│               │           ├── ReportSearchTest.kt
│               │           ├── ReportServiceTest.kt
│               │           └── ReportSortingTest.kt
│               ├── ErrorHandlingTest.kt
│               └── HealthRoutesTest.kt
├── .gitattributes
├── .gitignore
├── hete_saus.txt
├── kotlin
├── kotlin.bat
├── LICENSE
├── module.yaml
├── README.md
└── ROADMAP.md
```

## Technologies

- **Kotlin** — Main programming language.
- **Ktor** — REST API framework.
- **JetBrains Amper** — Project configuration and build system.
- **kotlinx.serialization** — JSON serialization.
- **Ktor ContentNegotiation** — JSON request and response handling.
- **Ktor StatusPages** — Centralized error handling.
- **H2 Database** — Database persistence.
- **JetBrains Exposed** — Kotlin SQL library for database operations.
- **kotlin.test** — Unit and integration testing.
- **Ktor Test Host** — HTTP endpoint testing.

## Getting Started

### Requirements

- A compatible JDK.
- Kotlin CLI with Amper support.
- IntelliJ IDEA (recommended).

### Run the Application

From the project root, run:

```bash
kotlin run
```

Application settings are defined in:

```text
resources/application.yaml
```

### Health Endpoint

The currently implemented HTTP endpoint is:

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

The service name comes from the application configuration.

Report REST endpoints will be implemented later in Checkpoint 5.

## Architecture

Brul2 follows a feature-based architecture.

The application separates HTTP routing, business logic, and database access.

```text
Ktor Routes
     ↓
Services
     ↓
Repository Interfaces
     ↓
Exposed Repositories
     ↓
H2 Database
```

### Current Implementation

The following components are implemented:

- Application configuration.
- JSON serialization.
- Centralized error handling.
- Database initialization.
- Report domain models.
- Report repository interface.
- Exposed repository implementation.
- Report service with CRUD operations.

The `ReportService` depends on the `ReportRepository` interface instead of directly depending on Exposed.

This allows the service to be tested independently from the database.

## Report Domain Models

The report domain uses an abstract `Report` class with three subclasses:

- **SafetyReport:** Reports related to neighborhood safety, including an `urgent` property.
- **NuisanceReport:** Reports related to nuisance or disturbances.
- **HelpRequest:** Requests for assistance from neighborhood residents, including a `volunteersNeeded` property.

All reports share the following properties:

- ID
- Title
- Description
- Category
- Location
- Status
- Report type

### Report Categories

Every report has a category represented by the `ReportCategory` enum.

Each report type only accepts specific categories.

| Report Type | Allowed Categories |
|---|---|
| SafetyReport | THEFT, SUSPICIOUS_SITUATION, TRAFFIC_SAFETY |
| NuisanceReport | NOISE, WASTE, VANDALISM, PARKING |
| HelpRequest | GROCERIES, TRANSPORT, MOVING, CHORES, OTHER |

Each subclass validates its own allowed categories.

Invalid combinations are rejected using Kotlin's `require()` function.

### Object-Oriented Programming

The domain models demonstrate:

- **Abstraction:** Shared properties and behavior in the abstract `Report` class.
- **Inheritance:** Three report subclasses inherit from `Report`.
- **Polymorphism:** Subclasses override category validation.
- **Encapsulation:** Domain rules are implemented inside the model classes.

## Database and Repositories

Brul2 uses H2 for database persistence and JetBrains Exposed for SQL operations.

Database initialization is handled by `DatabaseFactory`.

### Reports Table

All three report types are stored in one `reports` table.

The table contains:

- `id`
- `type`
- `title`
- `description`
- `category`
- `latitude`
- `longitude`
- `status`
- `urgent`
- `volunteers_needed`

The `category` column is required for all reports.

The `urgent` and `volunteers_needed` columns are nullable because they only apply to specific report types.

### Repository Pattern

The `ReportRepository` interface defines the CRUD operations:

```kotlin
fun create(report: NewReport): Report

fun findAll(): List<Report>

fun findById(id: Long): Report?

fun update(report: Report): Boolean

fun delete(id: Long): Boolean
```

`ExposedReportRepository` implements these operations using Exposed and H2.

The repository prevents an existing report from being updated into a different report type.

## Report Service

Checkpoint 5.1 introduced `ReportService`.

The service provides CRUD operations:

```kotlin
fun create(report: NewReport): Report

fun findAll(): List<Report>

fun findById(id: Long): Report?

fun update(report: Report): Boolean

fun delete(id: Long): Boolean
```

The service receives a `ReportRepository` through its constructor:

```kotlin
class ReportService(
    private val repository: ReportRepository
)
```

This demonstrates constructor dependency injection.

The service coordinates report operations and performs service-level validation.

Currently, the service validates positive IDs for retrieval and deletion.

Other validation rules are handled by `NewReport` and the report domain models.

Search, filtering, and sorting will be implemented in Checkpoint 5.2.

## Testing

Brul2 uses `kotlin.test`, Ktor's testing utilities, and H2 in-memory databases.

Run the complete test suite with:

```bash
kotlin test
```

### Current Test Results

**28 tests passed.**

| Test Category | Tests Passed |
|---|---:|
| Ktor integration tests | 3 |
| Report domain unit tests | 10 |
| Repository integration tests | 6 |
| Report service unit tests | 9 |
| **Total** | **28** |

### Test Coverage

The current test suite covers:

- Health endpoint responses.
- Centralized HTTP error handling.
- Report model validation.
- Report categories and report types.
- Location coordinate validation.
- Help-request volunteer validation.
- Creating reports in the database.
- Retrieving reports from the database.
- Updating and deleting reports.
- Preventing changes to existing report types.
- Report service CRUD operations.
- Invalid report IDs.

### Service Unit Tests

The service tests use a fake implementation of `ReportRepository`.

This allows `ReportService` to be tested without starting Ktor or connecting to H2.

The actual database behavior is tested separately through repository integration tests.

## Project Status

- [x] Checkpoint 1 — Ktor Server & Routing
- [x] Checkpoint 2 — JSON, Configuration & Error Handling
- [x] Checkpoint 3 — Report Domain Models
- [x] Checkpoint 4 — Database & Repositories
- [x] Checkpoint 4.1 — Report Categories & Database Refactoring
- [ ] Checkpoint 5 — Reports REST API
    - [x] 5.1 — ReportService CRUD
    - [ ] 5.2 — Search, Filtering & Sorting
    - [ ] 5.3 — Request & Response DTOs
    - [ ] 5.4 — Ktor Report Routes
    - [ ] 5.5 — Error Handling & API Tests
- [ ] Checkpoint 6 — Users & Authentication
- [ ] Checkpoint 7 — Voting
- [ ] Checkpoint 8 — Help Participation
- [ ] Checkpoint 9 — Testing & Quality
- [ ] Checkpoint 10 — Documentation & Delivery

## Planned Features

The following functionality is planned for future checkpoints:

- REST endpoints for creating, retrieving, updating, and deleting reports.
- Searching and filtering reports by text, type, category, and location.
- Sorting reports by different criteria.
- User registration and authentication with JWT.
- Report ownership and authorization.
- Voting on reports.
- Help-request participation.
- Joining and leaving help requests.
- Help-request capacity and status management.
- Seed data for development and demonstrations.
- Additional HTTP integration tests.
- API documentation and UML diagrams.

These features will be implemented incrementally while preserving the important functionality of the original Brul project.

## Documentation

See [ROADMAP.md](ROADMAP.md) for the complete development plan.

The project documentation also includes checkpoint descriptions, implementation decisions, testing results, and PlantUML diagrams.

## Development Approach

Brul2 is developed in small, testable checkpoints.

Each checkpoint follows the same approach:

1. Implement a focused part of the application.
2. Use Kotlin and Ktor functionality covered in the lessons.
3. Write or update automated tests.
4. Run the complete test suite.
5. Fix compilation errors and failing tests.
6. Commit the changes after the tests pass.

The goal is to create a maintainable REST API while demonstrating Kotlin, object-oriented programming, functional programming, database persistence, and software testing.

**Current progress: Checkpoint 5.1 completed — 28 tests passed.**