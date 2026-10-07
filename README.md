# Brul

Brul is een buurtapplicatie waarmee bewoners meldingen en hulpvragen in
hun buurt kunnen aanmaken en bekijken. Deze repository bevat de backend
voor Deel 1 van het project: een Web API gebouwd met Kotlin en Ktor.

## Huidige versie

**v0.18 - Domeinlogica**

De backend ondersteunt momenteel het aanmaken, ophalen, zoeken,
filteren, wijzigen, verwijderen, claimen en oplossen van reports.
In v0.18 zijn de businessregels naar het domeinmodel verplaatst, start
de server met realistische demodata en bestaat de volledige testsuite
uit **69 geslaagde tests**.

## Functionaliteit

Brul kent drie soorten reports:

-   `SafetyReport` - veiligheidsmeldingen met een `Urgency`.
-   `NuisanceReport` - overlastmeldingen met een `NuisanceType`.
-   `HelpRequest` - hulpvragen die door een andere gebruiker geclaimd
    kunnen worden.

Reports hebben gedeelde eigenschappen zoals een titel, beschrijving,
locatie, maker, aanmaakmoment en status.

### Domeinregels

De regels worden bewaakt door de domeinobjecten zelf:

-   Een titel mag niet leeg zijn; een locatie moet geldige coördinaten
    hebben.
-   Alleen de maker van een report of een admin mag het report wijzigen
    of verwijderen (`Report.canBeManagedBy`).
-   Een opgelost report kan niet opnieuw opgelost of gewijzigd worden.
-   Een `HelpRequest` kan alleen geclaimd worden als hij `OPEN` is, en
    niet door de maker zelf.
-   Wie een report mag oplossen is polymorf: standaard de maker of een
    admin; bij een `HelpRequest` ook de helper die hem claimde
    (`HelpRequest` overschrijft `canBeResolvedBy`).

Zoeken en filteren gebeurt met `ReportFilter`: elk ingevuld criterium
wordt een predicate `(Report) -> Boolean` en een report voldoet als het
aan alle predicates voldoet.

De API ondersteunt daarnaast:

-   ophalen van een collectie reports;
-   ophalen van een report op ID;
-   aanmaken van de drie reporttypes;
-   wijzigen en verwijderen van reports;
-   oplossen van reports;
-   claimen van `HelpRequest`;
-   zoeken op titel en beschrijving;
-   filteren op type, status, urgentie en overlasttype;
-   combineren van zoek- en filtercriteria;
-   uniforme foutresponses.

## Architectuur

De backend gebruikt een eenvoudige gelaagde architectuur:

``` text
HTTP request
    |
    v
Routing
    |
    v
Service
    |
    +----> Domain
    |
    v
Repository
    |
    v
In-memory persistence
```

De packages zijn als volgt verdeeld:

``` text
nl.brul
├── config
├── domain
├── dto
│   ├── request
│   └── response
├── persistence
├── routing
└── service
```

### Verantwoordelijkheden

-   `domain` bevat het domeinmodel en bewaakt geldige objecttoestand,
    statusovergangen en de regels over wie welke actie mag uitvoeren.
-   `dto` bevat de request- en responsemodellen van de API.
-   `routing` verwerkt HTTP-requests, queryparameters en responses.
-   `service` bevat de use-cases: een report ophalen, het domein de
    actie laten uitvoeren en het resultaat opslaan.
-   `persistence` abstraheert de opslag van reports via een generieke
    `CrudRepository<T, ID>` en bevat de demodata (`ReportSeedData`).
-   `config` bevat Ktor-configuratie, waaronder serialisatie, static
    content en centrale error handling.

## API

De huidige report-endpoints zijn:

Methode    Endpoint                  Functie
  ---------- ------------------------- -------------------------------------
`GET`      `/reports`                Reports ophalen, zoeken en filteren
`GET`      `/reports/{id}`           Eén report ophalen
`POST`     `/reports/safety`         SafetyReport aanmaken
`POST`     `/reports/nuisance`       NuisanceReport aanmaken
`POST`     `/reports/help`           HelpRequest aanmaken
`PUT`      `/reports/{id}`           Report wijzigen
`DELETE`   `/reports/{id}`           Report verwijderen
`POST`     `/reports/{id}/claim`     HelpRequest claimen
`POST`     `/reports/{id}/resolve`   Report oplossen

### Search & Filter

`GET /reports` ondersteunt de volgende optionele queryparameters:

-   `search`
-   `type`
-   `status`
-   `urgency`
-   `nuisanceType`

Voorbeelden:

``` text
GET /reports?search=straat
GET /reports?type=SAFETY
GET /reports?status=OPEN
GET /reports?urgency=HIGH
GET /reports?nuisanceType=NOISE
GET /reports?type=SAFETY&urgency=HIGH&search=street
```

Zoeken op `title` en `description` is hoofdletterongevoelig.
Queryparameters kunnen worden gecombineerd.

## Error handling en validatie

Vanaf v0.17 gebruikt Brul Ktor `StatusPages` voor centrale
foutafhandeling. Vanaf v0.18 komen `403 Forbidden` en de meeste
`409 Conflict`-fouten rechtstreeks uit het domein
(`NotAllowedException` en `IllegalStateException`). API-fouten worden teruggestuurd als een uniforme
`ErrorResponse`.

Voorbeeld:

``` json
{
  "status": 400,
  "error": "Bad Request",
  "message": "Invalid value 'BANANA' for query parameter 'type'."
}
```

De belangrijkste HTTP-statuscodes zijn:

  -----------------------------------------------------------------------
Status                              Betekenis
  ----------------------------------- -----------------------------------
`200 OK`                            Request succesvol

`201 Created`                       Report succesvol aangemaakt

`204 No Content`                    Report succesvol verwijderd

`400 Bad Request`                   Ongeldige invoer of queryparameter

`403 Forbidden`                     Gebruiker mag de actie niet
uitvoeren

`404 Not Found`                     Report bestaat niet

`409 Conflict`                      Actie conflicteert met een
businessregel of huidige status
  -----------------------------------------------------------------------

`401 Unauthorized` wordt relevant zodra echte authentication is
geïmplementeerd.

## Testen

De backend wordt getest met Kotlin Test en Ktor Test Host. Er zijn unit
tests voor domein- en servicelogica en integration tests voor de
HTTP-routes.

De huidige teststatus van v0.18 is:

**69 tests passed.**

De integration tests starten de applicatie met
`module(seedData = false)`, zodat elke test met een lege repository
begint.

Tests uitvoeren:

``` powershell
.\kotlin.bat test
```

Een specifieke testklasse uitvoeren kan bijvoorbeeld met:

``` powershell
.\kotlin.bat test --include-classes="nl.brul.routing.ReportRoutesTest"
```

## Techniek

Brul gebruikt momenteel:

-   Kotlin/JVM;
-   Ktor;
-   Amper / Kotlin CLI;
-   `kotlinx.serialization`;
-   Ktor `ContentNegotiation`;
-   Ktor `StatusPages`;
-   Kotlin Test;
-   Ktor Test Host;
-   in-memory persistence via `ReportRepository`, met demodata bij het
    opstarten;
-   `kotlin.time` en `kotlin.uuid` uit de Kotlin standard library.

## Huidige beperkingen

De backend is nog in ontwikkeling. In v0.18 gelden onder andere de
volgende beperkingen:

-   authentication is nog niet geïmplementeerd;
-   routes gebruiken tijdelijk een vaste testgebruiker;
-   persistence is nog in-memory (wel met realistische demodata);
-   MariaDB is gepland voor een latere versie;
-   de AI-component voor de opdracht is nog niet geïmplementeerd.

## Roadmap

De eerstvolgende geplande stappen zijn:

1.  Authentication en koppeling met de bestaande autorisatieregels.
2.  MariaDB-persistence.
3.  AI-component voor Deel 1.
4.  Definitieve API-tests en testdocumentatie.
5.  UML en ontwikkeldocument bijwerken.
6.  Eindcontrole van de requirements voor Deel 1.

## Changelog

Zie [`CHANGELOG.md`](CHANGELOG.md) voor de wijzigingen per versie.
