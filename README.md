# Brul2

Brul2 is a REST API built with Kotlin, Ktor, and JetBrains Amper.

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
│       └── common/
│           └── error/
│               ├── ApiError.kt
│               └── ErrorHandling.kt
└── test/
    └── nl/brul/api/
        ├── HealthRoutesTest.kt
        └── ErrorHandlingTest.kt
```

## Technologies

- Kotlin
- Ktor
- JetBrains Amper

## Project Status

- [x] Checkpoint 1 — Ktor Server & Routing
- [x] Checkpoint 2 — JSON, Configuration & Error Handling
- [ ] Checkpoint 3 — Report Domain Models
