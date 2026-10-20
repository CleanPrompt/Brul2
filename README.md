
# Brul2

Brul2 is a REST API built with **Kotlin, Ktor, JetBrains Amper, JetBrains Exposed, and H2**.

The project allows residents to report neighborhood safety issues, report nuisance, and request help from other residents.

We develop Brul2 in small, testable checkpoints. We use object-oriented programming (OOP), functional programming (FP), database persistence, authentication, and automated testing to build a maintainable application that meets the school assignment requirements.

**Current progress:** Checkpoint 6.4B completed — **156 tests passed**.

**Next checkpoint:** 6.5 — Authorization and Report Ownership.

---

## Project Structure

Brul2 follows a feature-based architecture. Application functionality is organized by feature, while shared technical components are placed in separate packages.

```text
Brul2/
├── .env
├── .env.example
├── .gitignore
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
│       │   ├── Security.kt
│       │   └── Serialization.kt
│       ├── security/
│       │   ├── PasswordHasher.kt
│       │   ├── BCryptPasswordHasher.kt
│       │   ├── JwtConfig.kt
│       │   └── JwtService.kt
│       └── features/
│           ├── auth/
│           │   ├── dto/
│           │   ├── routes/
│           │   └── service/
│           ├── report/
│           │   ├── dto/
│           │   ├── model/
│           │   ├── repository/
│           │   ├── routes/
│           │   └── service/
│           └── user/
│               ├── model/
│               │   ├── User.kt
│               │   └── NewUser.kt
│               └── repository/
│                   ├── UserRepository.kt
│                   ├── ExposedUserRepository.kt
│                   └── UsersTable.kt
├── test/
│   └── nl/brul/api/
│       ├── security/
│       └── features/
│           ├── auth/
│           ├── report/
│           └── user/
├── module.yaml
├── README.md
└── ROADMAP.md
```

### Package Responsibilities

| Package | Responsibility |
|---|---|
| `plugins/` | Configure Ktor routing, JSON serialization, error handling, and authentication |
| `security/` | Implement password hashing, JWT configuration, and token generation and verification |
| `features/auth/` | Handle user registration, login, authentication DTOs, and authenticated profile requests |
| `features/report/` | Handle report models, persistence, business logic, and HTTP endpoints |
| `features/user/` | Handle user models and database persistence |
| `database/` | Initialize the H2 database and its tables |
| `config/` | Load application configuration |
| `common/` | Provide shared application components |

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
| Ktor Authentication and JWT | Authenticate requests using bearer tokens |
| H2 Database | Store application data |
| JetBrains Exposed SQL DSL | Perform database operations |
| jBCrypt | Hash and verify user passwords |
| Auth0 java-jwt | Generate and verify JWT access tokens |
| dotenv-kotlin | Load local environment configuration from `.env` |
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

### Environment Configuration

Brul2 uses `dotenv-kotlin` to load local environment configuration.

Create a `.env` file in the project root:

```dotenv
JWT_SECRET=replace-with-a-random-secret-of-at-least-32-bytes
JWT_ISSUER=brul-api
JWT_AUDIENCE=brul-client
JWT_VALIDITY_MILLIS=3600000
```

Configuration variables:

| Variable | Description |
|---|---|
| `JWT_SECRET` | Secret used to sign JWT tokens; must contain at least 32 UTF-8 bytes |
| `JWT_ISSUER` | Expected JWT issuer |
| `JWT_AUDIENCE` | Expected JWT audience |
| `JWT_VALIDITY_MILLIS` | JWT validity duration in milliseconds |

The configured validity of `3600000` milliseconds equals one hour.

The application uses `dotenv-kotlin` to read the `.env` file. Existing system environment variables take precedence over local `.env` values.

The `.env` file contains sensitive configuration and must not be committed to Git.

Add the following rules to `.gitignore`:

```gitignore
.env
.env.*
!.env.example
```

A `.env.example` file may be committed to document the required configuration variables without exposing actual secrets.

### Run the Application

Open a terminal in the project root and run:

```bash
kotlin run
```

The JWT environment variables are loaded from `.env`, so manually executing `set JWT_SECRET=...` is not required for local development.

Ktor starts the API using the application configuration in:

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
Tests: 156 passed
```

---

## Current API Endpoints

Brul2 currently exposes nine REST API routes.

| Method | Endpoint | Description | Authentication |
|---|---|---|---|
| GET | `/api/v1/health` | Check whether the API is running | Public |
| GET | `/api/v1/reports` | Retrieve reports with optional search, filtering, and sorting | Public |
| GET | `/api/v1/reports/{id}` | Retrieve a report by ID | Public |
| POST | `/api/v1/reports` | Create a report | Public |
| PUT | `/api/v1/reports/{id}` | Update a report | Public |
| DELETE | `/api/v1/reports/{id}` | Delete a report | Public |
| POST | `/api/v1/auth/register` | Register a new user | Public |
| POST | `/api/v1/auth/login` | Log in and receive a JWT access token | Public |
| GET | `/api/v1/auth/me` | Retrieve the authenticated user's profile | Bearer JWT |

All endpoints use the `/api/v1` prefix.

Report authorization and ownership restrictions are planned for Checkpoint 6.5. Report endpoints are not yet protected by JWT authentication.

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

The endpoint returns stored reports as JSON.

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

### User Registration

Register a new user:

```http
POST http://localhost:8080/api/v1/auth/register
Content-Type: application/json

{
  "username": "resident1",
  "email": "resident@example.com",
  "password": "SecurePassword123!"
}
```

A successful registration returns HTTP 201 and the user's public account information.

The response does not include the user's password or password hash.

Registration includes:

- Input validation.
- Email normalization.
- Duplicate email detection.
- BCrypt password hashing.
- Database persistence.

### User Login

Log in with an existing account:

```http
POST http://localhost:8080/api/v1/auth/login
Content-Type: application/json

{
  "email": "resident@example.com",
  "password": "SecurePassword123!"
}
```

A successful login returns HTTP 200 and a JWT access token.

Example response:

```json
{
  "id": 1,
  "username": "resident1",
  "email": "resident@example.com",
  "createdAt": 1760000000000,
  "accessToken": "<JWT_ACCESS_TOKEN>",
  "tokenType": "Bearer"
}
```

The `accessToken` is signed using the configured JWT secret.

The response does not expose the password hash.

### Authenticated User Profile

Retrieve the currently authenticated user's profile:

```http
GET http://localhost:8080/api/v1/auth/me
Authorization: Bearer <JWT_ACCESS_TOKEN>
```

A valid JWT token identifies the user.

The endpoint retrieves the user from the database and returns public profile information.

The endpoint returns HTTP 401 when:

- The bearer token is missing.
- The bearer token is invalid.
- The token is expired.
- The token references a user who no longer exists.

The profile response does not expose the password hash.

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

Authentication follows a similar separation of responsibilities:

```text
HTTP Client
    |
    v
AuthRoutes
    |
    v
AuthService
    |
    +--> PasswordHasher
    |
    v
UserRepository (interface)
    |
    v
ExposedUserRepository
    |
    v
H2 Database
```

During login, `JwtService` generates a signed access token after successful credential verification.

For protected endpoints, Ktor Authentication validates the bearer token before allowing access.

### Routes

Routes receive HTTP requests, read request parameters, and return HTTP responses.

Feature-specific endpoints are placed inside their feature packages.

Examples:

```text
features/report/routes/ReportRoutes.kt
features/auth/routes/AuthRoutes.kt
```

### Services

Services coordinate application operations and enforce business rules.

Examples:

```text
features/report/service/ReportService.kt
features/auth/service/AuthService.kt
```

`ReportService` depends on the `ReportRepository` interface instead of directly depending on Exposed.

`AuthService` depends on `UserRepository` and `PasswordHasher`.

This allows business logic to be tested independently from database operations and concrete hashing implementations.

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
- `NewUser` — Input data for creating a user, located in `features/user/model/`.

### Dependency Injection

We use constructor injection to provide dependencies.

For example:

```kotlin
class ReportService(
    private val repository: ReportRepository
)
```

This keeps services independent of specific persistence implementations.

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

### Security

`Security.kt` configures Ktor Authentication with the JWT provider named `auth-jwt`.

JWT authentication validates:

- The token signature.
- The configured issuer.
- The configured audience.
- The token's expiration.
- The token's subject as a positive user ID.
- The token's issue time.

Protected routes use Ktor's `authenticate("auth-jwt")` block.

### Routing

`Routing.kt` registers:

- The health endpoint.
- Report routes.
- Authentication routes.

It also connects repositories, services, password hashing, and JWT functionality.

### Application Startup

`Application.kt` coordinates startup by:

1. Loading application configuration.
2. Loading JWT settings through `dotenv-kotlin`.
3. Creating `JwtConfig` and `JwtService`.
4. Installing JSON serialization.
5. Installing centralized error handling.
6. Installing JWT authentication.
7. Initializing the database.
8. Registering API routes.

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

- ID.
- Title.
- Description.
- Report type.
- Category.
- Location.
- Status.
- Creation timestamp (`createdAt`).
- View count (`viewCount`).

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

The implementation uses Kotlin features such as:

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

Report ownership is planned for Checkpoint 6.5.

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

`NewUser` represents the data required to create a user and is located in `features/user/model/`.

The user repository provides operations to:

- Create a user.
- Find a user by ID.
- Find a user by email.

### Password Hashing

Checkpoint 6.2 introduced password hashing through a dedicated `security/` package.

We use two components.

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

### Registration and Login

Checkpoint 6.3 introduced `AuthService`, authentication DTOs, HTTP routes, and integration tests.

`AuthService` handles:

- Registering users.
- Normalizing registration and login input.
- Detecting duplicate email addresses.
- Hashing passwords before persistence.
- Verifying credentials during login.

Registration and login are available through the public authentication endpoints.

---

## JWT Authentication

Checkpoint 6.4 introduced JWT-based authentication.

### JwtConfig

`JwtConfig` stores:

- Secret.
- Issuer.
- Audience.
- Token validity duration.

It validates configuration values, including a minimum secret length of 32 UTF-8 bytes.

### JwtService

`JwtService` generates and verifies signed JWT access tokens using HMAC-SHA256.

Generated tokens contain:

- Issuer (`iss`).
- Audience (`aud`).
- User ID as subject (`sub`).
- Issue time (`iat`).
- Expiration time (`exp`).

Token verification rejects invalid signatures, invalid claims, expired tokens, and invalid user IDs.

### Ktor JWT Authentication

Ktor Authentication protects the `/api/v1/auth/me` endpoint.

The route uses the JWT subject to identify the authenticated user.

The database is queried to ensure that the user still exists.

### JWT Integration Tests

Checkpoint 6.4B added integration tests for:

- Successful login returning a bearer token.
- Accessing `/auth/me` with a valid token.
- Rejecting requests without a token.
- Rejecting invalid tokens.
- Rejecting valid tokens referencing nonexistent users.
- Keeping registration publicly accessible.

JWT authentication is implemented and tested.

Authorization rules for report ownership are not implemented yet.

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
- JWT unit tests.
- Authentication integration tests.

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
| 6.2 — Password Hashing | 105 |
| 6.3 — Registration and Login | Completed |
| 6.4A — JWT Configuration and Service | 150 |
| 6.4B — JWT Authentication and Integration Tests | **156** |

The latest verified result is:

```text
Tests: 156 passed
```

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
- Registration and login logic.
- Authentication HTTP endpoints.
- JWT generation and verification.
- Bearer token authentication.
- Protected user profile access.
- Rejection of missing or invalid tokens.
- Rejection of tokens referencing nonexistent users.

### Test Strategy

We use fake repositories in service unit tests to isolate business logic.

We use in-memory H2 databases to verify repository persistence.

We use Ktor Test Host to verify HTTP behavior without launching a separate server.

Run all tests with:

```bash
kotlin test
```

We only mark checkpoints as complete after the relevant implementation compiles and tests pass.

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
- [x] Checkpoint 6.3A — AuthService: Registration and Login Logic
- [x] Checkpoint 6.3B — Authentication DTOs and HTTP Routes
- [x] Checkpoint 6.3C — Authentication Integration Tests
- [x] Checkpoint 6.4A — JWT Configuration and Token Service
- [x] Checkpoint 6.4B — Ktor JWT Authentication and Integration Tests
- [x] Architecture Refactor — Ktor Plugins and Security Packages
- [x] Local Environment Configuration — dotenv-kotlin

### Next

- [ ] Checkpoint 6.5 — Authorization and Report Ownership
- [ ] Checkpoint 6.6 — Authentication and Authorization Integration Tests

### Planned

- [ ] Checkpoint 7 — Report Voting
- [ ] Checkpoint 8 — Help Participation
- [ ] Checkpoint 9 — Testing and Quality Review
- [ ] Checkpoint 10 — Documentation and Delivery

---

## Planned Features

We will implement the remaining functionality incrementally.

### Authorization and Report Ownership

Checkpoint 6.5 will focus on:

- Associating reports with their creators.
- Protecting report creation, update, and deletion endpoints.
- Extracting authenticated user IDs from JWT tokens.
- Allowing report owners to modify their own reports.
- Preventing users from modifying or deleting reports owned by others.
- Returning appropriate HTTP responses for unauthorized and forbidden operations.

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

**Checkpoint 6.4B completed — 156 tests passed.**

We have implemented the report REST API, user persistence, BCrypt password hashing, registration and login, JWT token generation and verification, Ktor JWT authentication, and a protected user profile endpoint.

Local JWT configuration is loaded through `dotenv-kotlin`.

**Next step: Checkpoint 6.5 — Implement authorization and report ownership.**
