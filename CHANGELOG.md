# Changelog

Alle relevante wijzigingen aan Brul vanaf versie 0.14 worden in dit
bestand bijgehouden.

## 0.16

### Toegevoegd

-   Search & Filter-functionaliteit toegevoegd aan `GET /reports`.
-   Ondersteuning toegevoegd voor de queryparameters `search`, `type`,
    `status`, `urgency` en `nuisanceType`.
-   Queryparameters kunnen met elkaar worden gecombineerd.
-   Zoeken werkt hoofdletterongevoelig op `title` en `description`.
-   `ReportType` toegevoegd met de waarden `SAFETY`, `NUISANCE` en
    `HELP`.
-   Integration tests toegevoegd voor zoeken en filteren.

### Gewijzigd

-   `ReportService.getReports()` uitgebreid met optionele zoek- en
    filtercriteria.
-   `ReportResponse.type` gewijzigd van `String` naar `ReportType`.
-   `ReportMapper` aangepast om `ReportType` te gebruiken.
-   `Report` gewijzigd van een `abstract class` naar een `sealed class`,
    zodat Kotlin de volledige report-hiërarchie kan controleren.
-   Routing voor `GET /reports` uitgebreid met verwerking van
    queryparameters.
-   Bestaande routingtests aangepast aan het typeveilige `ReportType`.

### Teststatus

-   Volledige testsuite succesvol uitgevoerd.
-   **37 tests passed.**

## 0.15

### Toegevoegd

-   DTO-laag toegevoegd met aparte request- en responsemodellen.
-   Request DTO's toegevoegd voor `SafetyReport`, `NuisanceReport`,
    `HelpRequest` en algemene report-updates.
-   `ReportResponse` en `ReportMapper` toegevoegd.
-   JSON-serialisatie met `kotlinx.serialization` en Ktor Content
    Negotiation geconfigureerd.
-   Ktor-routes toegevoegd voor ophalen, aanmaken, wijzigen,
    verwijderen, claimen en oplossen van reports.
-   Integration tests toegevoegd voor de belangrijkste API-routes.
-   Een eenvoudige webinterface toegevoegd als ontwikkel- en
    demonstratiehulpmiddel; deze is geen onderdeel van de
    opdrachtarchitectuur.

### Gewijzigd

-   `Application.kt` configureert JSON-serialisatie en routing.
-   `Routing.kt` maakt de in-memory repository en `ReportService` aan en
    koppelt de report-routes.
-   Enums die via de API worden geserialiseerd zijn geschikt gemaakt
    voor `kotlinx.serialization`.

### Teststatus

-   Backend en routing succesvol gecompileerd en getest.
-   **26 tests passed.**

## 0.14

### Vastgelegd

-   Basisarchitectuur voor Deel 1 van Brul uitgewerkt.
-   Gelaagde packagestructuur vastgesteld: `domain`, `dto`,
    `persistence`, `routing`, `service` en `config`.
-   Domeinmodel uitgewerkt rond de basisklasse `Report`.
-   Drie reporttypes uitgewerkt: `SafetyReport`, `NuisanceReport` en
    `HelpRequest`.
-   Rollen `USER` en `ADMIN` vastgelegd.
-   Businessregels voor aanmaken, wijzigen, verwijderen, claimen en
    oplossen van reports vastgelegd.
-   Statusovergangen voor reports vastgelegd.
-   `Claimable`-interface voor `HelpRequest` opgenomen.
-   In-memory persistence via `ReportRepository` en
    `InMemoryReportRepository` uitgewerkt.
-   `ReportService` toegevoegd voor use-cases en autorisatieregels op
    serviceniveau.
-   MariaDB vastgelegd als beoogde latere persistencelaag; de eerste
    implementatie gebruikt in-memory persistence.

### Ontwerpkeuzes

-   Routing, service, domain en persistence blijven van elkaar
    gescheiden.
-   Constructor injection wordt gebruikt zonder extra
    dependency-injectionframework.
-   Geen onnodige patterns of frameworks toegevoegd.
-   De eenvoudigste oplossing die de requirements en het domein
    duidelijk ondersteunt blijft het uitgangspunt.
