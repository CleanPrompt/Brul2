
# Brul2

Brul2 is a REST API built with Kotlin, Ktor, and JetBrains Amper.

The project focuses on neighborhood reports, including safety issues, nuisance reports, and requests for help.

## Project Structure

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
│       ├── Routing.kt
│       ├── config/
│       │   └── AppConfig.kt
│       ├── common/
│       │   └── error/
│       │       ├── ApiError.kt
│       │       └── ErrorHandling.kt
│       └── features/
│           └── report/
│               └── model/
│                   ├── Report.kt
│                   ├── SafetyReport.kt
│                   ├── NuisanceReport.kt
│                   ├── HelpRequest.kt
│                   ├── Location.kt
│                   ├── ReportStatus.kt
│                   └── ReportType.kt
└── test/
    └── nl/brul/api/
        ├── HealthRoutesTest.kt
        ├── ErrorHandlingTest.kt
        └── features/
            └── report/
                └── model/
                    └── ReportModelTest.kt
```

## Technologies

- Kotlin
- Ktor
- JetBrains Amper
- kotlinx.serialization
- Ktor ContentNegotiation
- Ktor StatusPages
- kotlin.test

## Architecture

Brul2 follows a feature-based architecture.

Each feature will contain its own models, routes, services, and repositories where needed.

The planned application structure is:

`Routes → Services → Repository Interfaces → Database Repositories`

The current implementation contains shared configuration, error handling, and report domain models.

## Report Domain Models

The report domain uses an abstract `Report` class with three subclasses:

- **SafetyReport:** reports related to neighborhood safety.
- **NuisanceReport:** reports related to nuisance or disturbances.
- **HelpRequest:** requests for assistance from neighborhood residents.

The domain models demonstrate object-oriented programming concepts such as abstraction, inheritance, polymorphism, and encapsulation.

## Testing

Brul2 uses `kotlin.test` and Ktor's testing utilities.

Run the complete test suite with:

```bash
kotlin test
```

The test suite covers HTTP endpoints, error handling, and report domain behavior.

## Project Status

- [x] Checkpoint 1 — Ktor Server & Routing
- [x] Checkpoint 2 — JSON, Configuration & Error Handling
- [x] Checkpoint 3 — Report Domain Models
- [x] Checkpoint 4 — Database & Repositories
- [ ] Checkpoint 5 — Reports REST API
- [ ] Checkpoint 6 — Users & Authentication
- [ ] Checkpoint 7 — Voting
- [ ] Checkpoint 8 — Help Participation
- [ ] Checkpoint 9 — Testing & Quality
- [ ] Checkpoint 10 — Documentation & Delivery

## Documentation

See [ROADMAP.md](ROADMAP.md) for the complete development plan.
