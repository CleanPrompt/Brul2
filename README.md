# Brul

Brul is een buurtapplicatie waarmee bewoners meldingen en hulpvragen in
hun buurt kunnen aanmaken en bekijken. Deze repository bevat de backend
voor Deel 1 van het project: een Web API gebouwd met Kotlin en Ktor.

## Huidige versie

**v0.17 - Error Handling & Validation**

De backend ondersteunt momenteel het aanmaken, ophalen, zoeken,
filteren, wijzigen, verwijderen, claimen en oplossen van reports.
Centrale foutafhandeling en validatie zijn toegevoegd en de volledige
testsuite bestaat momenteel uit **44 geslaagde tests**.

## Functionaliteit

Brul kent drie soorten reports:

-   `SafetyReport` - veiligheidsmeldingen met een `Urgency`.
-   `NuisanceReport` - overlastmeldingen met een `NuisanceType`.
-   `HelpRequest` - hulpvragen die door een andere gebruiker geclaimd
    kunnen worden.

Reports hebben gedeelde eigenschappen zoals een titel, beschrijving,
locatie, maker, aanmaakmoment en status.

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

-   `domain` bevat het domeinmodel en bewaakt geldige objecttoestand en
    statusovergangen.
-   `dto` bevat de request- en responsemodellen van de API.
-   `routing` verwerkt HTTP-requests, queryparameters en responses.
-   `service` bevat use-cases, businessregels en autorisatieregels.
-   `persistence` abstraheert de opslag van reports.
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
foutafhandeling. API-fouten worden teruggestuurd als een uniforme
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

De huidige teststatus van v0.17 is:

**44 tests passed.**

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
-   in-memory persistence via `ReportRepository`.

## Huidige beperkingen

De backend is nog in ontwikkeling. In v0.17 gelden onder andere de
volgende beperkingen:

-   authentication is nog niet geïmplementeerd;
-   routes gebruiken tijdelijk een vaste testgebruiker;
-   persistence is nog in-memory;
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
