# Brul2 — UML Diagram Guidelines

## 1. General Principles

All Brul2 diagrams follow the same visual and technical conventions.

The goals are:

- Consistency across all diagrams.
- Clear and professional presentation.
- Readability in the school report.
- Correct UML notation.
- Accurate representation of the Kotlin implementation.
- Simplicity according to the KISS principle.

The actual Kotlin source code and database schema are always leading.

Do not invent classes, methods, relationships, database fields or interactions that do not exist in the implementation.

## 2. Diagram Overview

The project contains eight planned diagrams.

| Number | Diagram | Filename |
|---|---|---|
| 01 | Report Domain Class Diagram | `01-class-diagram.puml` |
| 02 | Database ERD | `02-database-erd.puml` |
| 03 | Package Diagram | `03-package-diagram.puml` |
| 04 | Authentication Sequence Diagram | `04-sequence-authentication.puml` |
| 05 | Create Report Sequence Diagram | `05-sequence-create-report.puml` |
| 06 | Join Help Request Sequence Diagram | `06-sequence-join-help-request.puml` |
| 07 | Update Report Sequence Diagram | `07-sequence-update-report.puml` |
| 08 | Voting Sequence Diagram | `08-sequence-vote.puml` |

## 3. File Organization

All diagrams are stored in:

```text
docs/
└── uml/
    ├── DIAGRAM-GUIDELINES.md
    ├── 01-class-diagram.puml
    ├── 02-database-erd.puml
    ├── 03-package-diagram.puml
    ├── 04-sequence-authentication.puml
    ├── 05-sequence-create-report.puml
    ├── 06-sequence-join-help-request.puml
    ├── 07-sequence-update-report.puml
    └── 08-sequence-vote.puml
```

Rules:

- Use two-digit numbering: `01` through `08`.
- Use English filenames.
- Use lowercase kebab-case.
- Keep the same numbering in the school report.
- Each diagram must work independently.
- Do not use `style.puml`.
- Do not use `!include` for shared styling.
- Include all required style settings directly in each `.puml` file.

## 4. Language and Naming

- All diagram content must be in English.
- Use the exact Kotlin class and interface names.
- Use the actual method names from the source code.
- Use the actual database table and column names.
- Use consistent terminology across diagrams.
- Keep descriptions short and clear.

Diagram titles follow this format:

```text
Brul2 - 01. Report Domain Class Diagram
Brul2 - 02. Database ERD
Brul2 - 03. Package Diagram
Brul2 - 04. Authentication Sequence Diagram
Brul2 - 05. Create Report Sequence Diagram
Brul2 - 06. Join Help Request Sequence Diagram
Brul2 - 07. Update Report Sequence Diagram
Brul2 - 08. Voting Sequence Diagram
```

## 5. Color Palette

Use a white background and soft pastel colors.

| Element | Color | Hex |
|---|---|---|
| Background | White | `#FFFFFF` |
| Text and arrows | Dark blue-gray | `#243247` |
| Routes | Light blue | `#DBEAFE` |
| Services | Light green | `#D1FAE5` |
| Repositories | Light yellow | `#FEF3C7` |
| Database | Light purple | `#F3E8FF` |
| Domain models and DTOs | Light gray | `#F1F5F9` |
| Package background | Very light gray | `#F8FAFC` |
| Secondary borders | Gray-blue | `#64748B` |
| Notes | Light orange | `#FFF7ED` |
| Note borders | Orange | `#D97706` |

Color rules:

- Use the same colors consistently.
- Prefer pastel colors over saturated colors.
- Do not use gradients.
- Do not use shadows.
- Do not use decorative icons.
- Colors must support readability, not replace UML notation.
- Diagrams must remain understandable when printed in grayscale.

For the class diagram, the abstract `Report` class may use light blue and its subclasses light green to emphasize inheritance.

For the ERD, database tables use light purple.

## 6. Typography

Use:

- Font family: Arial.
- Default font size: 13.
- Attribute font size: 12.
- Font color: `#243247`.

Rules:

- Keep text readable when exported.
- Avoid unnecessarily long labels.
- Use consistent capitalization.
- Avoid excessive bold text.
- Do not use decorative fonts.

## 7. General PlantUML Style

Each diagram contains its own style configuration.

Recommended base configuration:

```plantuml
skinparam backgroundColor #FFFFFF
skinparam shadowing false
skinparam roundCorner 0

skinparam defaultFontName Arial
skinparam defaultFontSize 13
skinparam defaultFontColor #243247

skinparam ArrowColor #243247
skinparam ArrowThickness 1
```

Additional styling can be added depending on the diagram type.

Do not create a shared style file.

## 8. Class Diagram Rules

The class diagram focuses on the report domain.

Include:

- `Report`
- `SafetyReport`
- `NuisanceReport`
- `HelpRequest`
- `Location`
- `ReportType`
- `ReportStatus`
- `ReportCategory`

Rules:

- Represent `Report` as an abstract class.
- Show inheritance using UML generalization arrows.
- Show important attributes and their types.
- Show relevant methods.
- Use UML visibility symbols.
- Represent enums as enumerations.
- Represent `Location` as a data class.
- Show relationships that exist in the implementation.
- Do not invent composition or aggregation relationships.
- Do not include unnecessary constructors, getters or setters.
- Do not include every DTO, route, service or repository.

Visibility notation:

| Symbol | Meaning |
|---|---|
| `+` | Public |
| `-` | Private |
| `#` | Protected |
| `~` | Package |

Use notes only for important domain rules, such as category validation or volunteer requirements.

## 9. ERD Rules

The ERD represents the actual H2 database schema.

Include:

- `users`
- `reports`
- `votes`
- `help_participations`

Show:

- Primary keys.
- Foreign keys.
- Important columns and data types.
- Table relationships.
- Cardinalities.
- Relevant unique constraints.
- Cascade deletion where implemented.

Rules:

- Use actual Exposed table definitions as the source.
- Do not assume relationships without checking the schema.
- Use database table names, not Kotlin model names.
- Keep the diagram focused on persistence.
- Do not include service methods or HTTP endpoints.
- Use consistent notation for primary and foreign keys.

The ERD should clearly explain how users, reports, votes and volunteer participations are stored and related.

## 10. Package Diagram Rules

The package diagram explains the feature-based architecture.

Include the main packages:

- `common`
- `config`
- `database`
- `features.auth`
- `features.help`
- `features.report`
- `features.user`
- `features.vote`
- `plugins`
- `security`

Rules:

- Show meaningful package dependencies.
- Use actual package names.
- Avoid showing every Kotlin file.
- Avoid unnecessary arrows.
- Keep the layout readable.
- Use dashed arrows for dependencies.
- Group related packages logically.

The diagram should make the separation between routes, services, repositories and shared infrastructure understandable.

## 11. Sequence Diagram Rules

Sequence diagrams explain how a specific operation works.

General participant order:

```text
Client -> Route -> Service -> Repository -> Database
```

Include additional participants only when relevant, such as:

- `JwtService`
- `BCryptPasswordHasher`
- `ReportService`
- `HelpParticipationRepository`
- `VoteRepository`

Rules:

- Arrange participants from left to right.
- Use actual Kotlin class names where applicable.
- Show meaningful method calls.
- Use solid arrows for calls.
- Use dashed arrows for responses when useful.
- Use activation bars where they improve clarity.
- Show important validation steps.
- Use `alt` for alternative outcomes.
- Use `opt` for optional behavior.
- Avoid unnecessary implementation details.
- Do not invent database queries or service calls.
- Keep HTTP status codes consistent with the actual API.

Recommended scenarios:

### 04. Authentication

Show registration/login behavior where relevant, including:

- User input.
- Authentication route.
- Authentication service.
- User repository.
- BCrypt password verification.
- JWT generation.
- Successful and failed authentication.

### 05. Create Report

Show:

- Authenticated HTTP request.
- Request validation.
- Report service.
- Report repository.
- Database insertion.
- HTTP response.

### 06. Join Help Request

Show:

- JWT authentication.
- Help request lookup.
- Ownership validation.
- Report status validation.
- Duplicate participation check.
- Capacity validation.
- Participation insertion.
- Success and rejection scenarios.

### 07. Update Report

Show:

- JWT authentication.
- Report ownership validation.
- Existing report lookup.
- Status transition validation.
- Volunteer capacity validation for help requests.
- Database update.
- Success and rejection scenarios.

### 08. Voting

Show:

- JWT authentication.
- Report lookup.
- Existing vote lookup.
- Vote creation or modification.
- Database persistence.
- HTTP response.

## 12. Notes and Business Rules

Use notes sparingly.

Good examples:

- `volunteersNeeded > 0`
- `volunteersNeeded >= currentParticipantCount`
- `RESOLVED reports cannot be reopened`
- `A user can vote once per report`
- `A user cannot join their own help request`

Rules:

- Notes must explain meaningful constraints.
- Keep notes short.
- Avoid repeating information already visible.
- Use the agreed note colors.
- Do not place large paragraphs inside diagrams.

## 13. Layout and Readability

- One diagram should explain one main subject.
- Avoid crossing arrows where possible.
- Prefer balanced layouts.
- Keep related elements close together.
- Do not overload diagrams with technical details.
- Use whitespace to separate logical sections.
- Prefer readability over showing every possible interaction.

If a diagram becomes too large, simplify the content rather than reducing the font size excessively.

## 14. Source Code Accuracy

Before creating or updating a diagram:

1. Check the relevant Kotlin files.
2. Identify the actual classes and methods.
3. Verify inheritance and interface relationships.
4. Verify repository dependencies.
5. Verify business rules.
6. Verify HTTP routes and responses where relevant.
7. Verify database constraints for the ERD.

If required information is missing, request the relevant source files instead of guessing.

The diagrams must describe the implementation, not an imagined architecture.

## 15. Export and Quality Checks

All diagrams must remain editable as `.puml` files.

For documentation:

- Prefer SVG for scalable diagrams.
- Use PNG when required by the document format.
- Keep the original `.puml` source.
- Verify exported diagrams visually.
- Check spelling and naming.
- Check readability at normal document size.
- Ensure no elements are cut off.
- Verify that arrows and labels are understandable.

## 16. Definition of Done

A diagram is complete when:

- [ ] It renders without PlantUML errors.
- [ ] It follows the agreed colors and typography.
- [ ] It uses the correct filename and numbering.
- [ ] It contains only verified classes, methods or relationships.
- [ ] It uses correct UML notation.
- [ ] It is readable and not overcrowded.
- [ ] It is consistent with the other Brul2 diagrams.
- [ ] It has been visually checked.
- [ ] It is ready for use in the school report.

## 17. Final Agreement

All eight Brul2 diagrams follow these guidelines.

The priorities are:

1. Correctness.
2. Readability.
3. Consistency.
4. Simplicity.
5. Visual presentation.

The Kotlin implementation remains the source of truth.

Each diagram is self-contained and does not depend on `style.puml` or another shared PlantUML file.