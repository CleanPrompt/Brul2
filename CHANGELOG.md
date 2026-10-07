# Changelog

Alle relevante wijzigingen aan Brul vanaf versie 0.14 worden in dit
bestand bijgehouden.

## 0.18.2 - API-demo

### Toegevoegd

-   User flows voor de IntelliJ HTTP Client in `test/http-requests/`:
    -   `1-search-and-filter-flow.http`: ophalen, zoeken en filteren,
        plus een ongeldige filterwaarde (400) en een onbekend ID (404);
    -   `2-report-lifecycle-flow.http`: de drie reporttypes aanmaken,
        validatie (400), wijzigen, oplossen, opnieuw oplossen of
        wijzigen na oplossen (409) en verwijderen (204);
    -   `3-business-rules-flow.http`: de domeinregels op de demodata,
        zoals rechten (403), claimen (409) en het oplossen van een
        hulpvraag door de helper.
-   Elke request controleert met `client.test` de verwachte statuscode
    en melding.
-   README beschrijft hoe je de server start en de flows uitvoert.

## 0.18.1 - Review

### Gewijzigd

-   De tijdelijke gebruiker in de routes is nu demogebruiker `anna`
    (`user-1`) uit `ReportSeedData`. Reports die via de API worden
    aangemaakt en de demodata tonen daardoor dezelfde gebruiker.
-   `configureRouting()` heeft geen eigen standaardwaarde meer voor
    `seedData`; alleen `module()` bepaalt die.
-   De tabellen in `README.md` zijn omgezet naar GitHub-tabellen, zodat
    ze op GitHub correct worden weergegeven.

### Teststatus

-   **69 tests passed.**

## 0.18 - Domeinlogica

### Toegevoegd

-   `Report.type` als abstracte property: elk subtype geeft zelf zijn
    `ReportType` op (polymorfisme in plaats van `is`-checks).
-   `Report.canBeResolvedBy()` als open methode. `HelpRequest`
    overschrijft deze zodat ook de helper die de hulpvraag claimde hem
    mag oplossen.
-   `Report.canBeManagedBy()` en `checkCanBeManagedBy()`: alleen de
    maker of een admin mag een report wijzigen of verwijderen.
-   `NotAllowedException` in het domein, vertaald naar `403 Forbidden`.
-   `ReportFilter` in het domein: zoek- en filtercriteria als lijst van
    predicates `(Report) -> Boolean`, met extension function
    `filterBy()`.
-   Generieke `CrudRepository<T, ID>` en `InMemoryRepository<T, ID>`;
    `ReportRepository` en `InMemoryReportRepository` bouwen hierop.
-   `ReportSeedData`: realistische demodata bij het opstarten van de
    server (`module(seedData = true)` is de standaard).
-   Logo toegevoegd aan de frontend.
-   Domeintests voor wijzigen, oplossen, claimen, rechten en filteren.

### Gewijzigd

-   De regel "je kunt je eigen HelpRequest niet claimen" staat nu in
    `HelpRequest.claim()` in plaats van in de service.
-   Rechten voor wijzigen, verwijderen en oplossen worden door het
    domein bewaakt; `ReportService` coördineert alleen nog.
-   Een opgelost report kan niet meer gewijzigd worden
    (`409 Conflict`).
-   `java.time.LocalDateTime` en `java.util.UUID` vervangen door
    `kotlin.time.Instant` en `kotlin.uuid.Uuid`. `createdAt` kan bij
    het aanmaken worden meegegeven, wat tijdsafhankelijke tests
    mogelijk maakt.
-   Integration tests starten met `module(seedData = false)`.
-   `claimHelpRequest()` werkt via de `Claimable`-interface en
    retourneert een `Report`.

### Verwijderd

-   `ForbiddenException`, vervangen door `NotAllowedException` in het
    domein.

### Teststatus

-   Domeintests toegevoegd voor wijzigen, oplossen, claimen, rechten en
    filteren; servicetests en integration tests uitgebreid.
-   **69 tests passed.**

## 0.17

### Toegevoegd

-   Centrale error handling toegevoegd met Ktor `StatusPages`.
-   `ErrorResponse` toegevoegd voor een uniforme JSON-structuur bij
    API-fouten.
-   `ReportNotFoundException` toegevoegd voor niet-bestaande reports.
-   `ForbiddenException` toegevoegd voor acties waarvoor een gebruiker
    niet bevoegd is.
-   `ConflictException` toegevoegd voor conflicten met de huidige
    toestand of businessregels.
-   Validatiefouten vanuit het domein worden vertaald naar
    `400 Bad Request`.
-   Niet-bestaande reports worden vertaald naar `404 Not Found`.
-   Niet-toegestane acties kunnen worden vertaald naar `403 Forbidden`.
-   Conflicten met businessregels of de huidige status worden vertaald
    naar `409 Conflict`.
-   Ongeldige waarden voor enum-queryparameters worden afgewezen met
    `400 Bad Request`.
-   Integration tests toegevoegd voor ongeldige waarden van `type`,
    `status`, `urgency` en `nuisanceType`.
-   Integration test toegevoegd voor het twee keer oplossen van
    hetzelfde report.
-   Integration test toegevoegd voor het claimen van een `SafetyReport`.
-   Integration test toegevoegd voor de inhoud van de uniforme
    `ErrorResponse`.

### Gewijzigd

-   `ReportService` gebruikt expliciete exceptions in plaats van `null`-
    en `Boolean`-resultaten om foutgevallen aan te geven.
-   `getReport()` retourneert een `Report` of gooit een
    `ReportNotFoundException`.
-   `updateReport()` retourneert het gewijzigde `Report` en gebruikt
    exceptions voor foutgevallen.
-   `deleteReport()` retourneert geen `Boolean` meer; een succesvolle
    uitvoering heeft geen resultaat en foutgevallen worden via
    exceptions afgehandeld.
-   `claimHelpRequest()` retourneert de gewijzigde `HelpRequest` en
    gebruikt `ConflictException` bij een ongeldige claim.
-   `resolveReport()` retourneert het gewijzigde `Report` en gebruikt
    `ConflictException` wanneer oplossen niet is toegestaan.
-   Gedeelde controles voor het vinden en beheren van reports zijn
    gecentraliseerd in `ReportService`.
-   `ReportRoutes` vereenvoudigd doordat foutafhandeling niet langer per
    route handmatig wordt uitgevoerd.
-   Verwerking van report-ID's en enum-queryparameters in de routing
    consistenter gemaakt.
-   `Application.kt` configureert nu ook de centrale error handling.
-   `module.yaml` uitgebreid met de Ktor `StatusPages`-dependency.
-   Bestaande service- en routingtests aangepast aan de nieuwe
    servicecontracten en HTTP-statuscodes.

### Teststatus

-   Volledige testsuite na de error-handling- en validatiefase succesvol
    uitgevoerd.
-   Validatie van een lege titel retourneert nu `400 Bad Request` in
    plaats van `500 Internal Server Error`.
-   Validatie van ongeldige locatiecoördinaten retourneert nu
    `400 Bad Request` in plaats van `500 Internal Server Error`.
-   Ongeldige enum-queryparameters worden getest als `400 Bad Request`.
-   Conflicterende acties, zoals een report twee keer oplossen of een
    `SafetyReport` claimen, worden getest als `409 Conflict`.
-   De JSON-structuur van `ErrorResponse` wordt gecontroleerd.
-   **44 tests passed.**

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
