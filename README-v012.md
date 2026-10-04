# Brul

**Versie: v0.12**

Brul is een buurtapplicatie waarin bewoners gebeurtenissen in hun buurt
kunnen melden en bekijken. Het doel is om bewoners beter te informeren
over wat er in hun omgeving gebeurt.

Deze repository bevat de backend van Brul, ontwikkeld in **Kotlin met
Ktor**. Het project bevindt zich in actieve ontwikkeling. Het domein, de
persistence-laag, service-laag en DTO-laag zijn geïmplementeerd.
Daarnaast is gestart met de Ktor Web API.

## Huidige projectstructuur

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

## Domain

De package `domain` bevat het domeinmodel en het centrale gedrag van
Brul.

De belangrijkste abstractie is `Report`. Brul ondersteunt momenteel drie
soorten meldingen:

-   `SafetyReport` --- een veiligheidsmelding met een urgentieniveau.
-   `NuisanceReport` --- een overlastmelding met een type overlast.
-   `HelpRequest` --- een hulpverzoek dat door een andere gebruiker kan
    worden geclaimd.

Gedeelde gegevens en gedrag van meldingen zijn ondergebracht in de
abstracte klasse `Report`. Het domein bevat daarnaast ondersteunende
typen zoals `User`, `Location`, rollen, statussen, urgentieniveaus en
typen overlast.

`HelpRequest` implementeert de interface `Claimable`, omdat alleen
hulpverzoeken geclaimd kunnen worden.

Belangrijke domeinregels die momenteel zijn geïmplementeerd:

-   Iedere melding krijgt automatisch een ID.
-   Iedere melding krijgt automatisch een aanmaaktijdstip.
-   Een nieuwe melding begint met de status `OPEN`.
-   De titel van een melding mag niet leeg zijn.
-   De latitude moet tussen `-90` en `90` liggen.
-   De longitude moet tussen `-180` en `180` liggen.
-   Bewerkbare gegevens van een melding worden gecontroleerd gewijzigd.
-   Een melding kan worden afgerond.
-   Alleen een open `HelpRequest` kan worden geclaimd.
-   Het claimen van een `HelpRequest` verandert de status naar
    `CLAIMED`.

## DTO

De package `dto` bevat de Data Transfer Objects die worden gebruikt voor
communicatie via de Web API.

Request- en response-objecten zijn van elkaar gescheiden:

``` text
dto
├── request
│   ├── CreateSafetyReportRequest
│   ├── CreateNuisanceReportRequest
│   ├── CreateHelpRequestRequest
│   └── UpdateReportRequest
└── response
    └── ReportResponse
```

De drie soorten meldingen hebben ieder een eigen create-request, omdat
ze verschillende gegevens nodig hebben. `ReportResponse` vormt het
algemene responsemodel voor meldingen.

De DTO's gebruiken `kotlinx.serialization` zodat Ktor Kotlin-objecten en
JSON kan serialiseren en deserialiseren.

`ReportMapper` zet domeinobjecten van het type `Report` om naar
`ReportResponse`. Hierdoor worden de domeinobjecten niet rechtstreeks
als API-response gebruikt.

## Persistence

De package `persistence` bepaalt hoe meldingen worden opgeslagen en
opgehaald.

`ReportRepository` vormt de repository-abstractie voor het ophalen van
alle meldingen, het zoeken van een melding op ID, het opslaan, bijwerken
en verwijderen van meldingen.

De huidige implementatie is `InMemoryReportRepository`. Meldingen worden
voorlopig in het geheugen opgeslagen met behulp van een
Kotlin-collectie.

Hierdoor blijft de huidige implementatie eenvoudig, terwijl de
persistence-implementatie later vervangen kan worden zonder dat het
domein of de service-laag daarvoor volledig aangepast hoeft te worden.

## Service

De package `service` bevat de applicatielogica en vormt de verbinding
tussen het domein en de persistence-laag.

`ReportService.kt` bevat momenteel functionaliteit voor het ophalen,
aanmaken, wijzigen en verwijderen van meldingen, het claimen van een
`HelpRequest` en het afronden van een melding.

De service-laag is daarnaast verantwoordelijk voor bedrijfsregels die
afhankelijk zijn van de huidige gebruiker. Zo kan een gewone gebruiker
zijn eigen meldingen beheren, kan een administrator iedere melding
beheren en kan een gebruiker zijn eigen `HelpRequest` niet claimen.

Hiermee blijven de verantwoordelijkheden gescheiden: het domein bewaakt
een geldige toestand en het gedrag van objecten, terwijl de service
bepaalt of een gebruiker een bepaalde handeling mag uitvoeren.

## Config

De package `config` bevat configuratie voor de Ktor-applicatie.

Momenteel is `ContentNegotiation` geconfigureerd met
`kotlinx.serialization`. Hierdoor kan de Web API JSON-requests verwerken
en responses als JSON terugsturen.

## Routing

De package `routing` bevat de HTTP-routes van de Ktor Web API.

De eerste report-route is geïmplementeerd:

``` http
GET /reports
```

Deze route haalt meldingen op via `ReportService.kt` en zet de gevonden
domeinobjecten via `ReportMapper` om naar `ReportResponse`-objecten.

## Architectuur

De backend gebruikt een gelaagde structuur:

``` text
HTTP request
     ↓
Ktor Routing
     ↓
DTO
     ↓
Service
     ↓
Repository
     ↓
Domain
```

Laag          Verantwoordelijkheid
  ------------- ----------------------------------------------------
Domain        Domeinmodel, toestand en domeingedrag
Persistence   Opslaan en ophalen van meldingen
Service       Use-cases en gebruikersafhankelijke bedrijfsregels
DTO           Gegevensoverdracht van en naar de API
Routing       HTTP-endpoints en koppeling met de service
Config        Configuratie van Ktor-functionaliteit

De service benadert de persistence-laag via de interface
`ReportRepository`. Hierdoor is `ReportService.kt` niet direct afhankelijk
van de concrete `InMemoryReportRepository`.

De huidige Ktor-configuratie maakt de concrete dependencies handmatig
aan. Hiervoor wordt op dit moment geen dependency-injectionframework
gebruikt.

## Objectgeoriënteerd ontwerp

De huidige implementatie gebruikt onder andere abstractie, overerving,
encapsulatie, interfaces en polymorfisme. `Report` vormt de abstracte
basis voor de drie reporttypen, terwijl `Claimable` specifiek gedrag
voor `HelpRequest` beschrijft.

Data classes worden gebruikt voor onder andere `User`, `Location` en de
DTO's. Enums worden gebruikt voor vaste domeinwaarden zoals rollen,
statussen, urgentieniveaus en typen overlast.

## Web API

De Ktor Web API is in ontwikkeling.

Momenteel geïmplementeerd:

``` http
GET /reports
```

Gepland API-contract:

``` text
GET    /reports
GET    /reports/{id}

POST   /reports/safety
POST   /reports/nuisance
POST   /reports/help

PUT    /reports/{id}
DELETE /reports/{id}

POST   /reports/{id}/claim
POST   /reports/{id}/resolve
```

Zoeken en filteren worden later toegevoegd via queryparameters op
`GET /reports`.

## Testen

Unit tests worden gebruikt om het gedrag van het domein en de
service-laag te controleren. De tests controleren onder andere
domeinvalidatie, statuswijzigingen, het claimen van hulpverzoeken en
permissies binnen de service-laag.

Daarnaast is de eerste **Ktor integration test** toegevoegd. Deze
verstuurt een HTTP-request naar `GET /reports` en controleert of de API
`200 OK` retourneert.

De tests kunnen vanuit de hoofdmap van het project worden uitgevoerd
met:

``` powershell
.\kotlin.bat test
```

Huidige teststatus:

``` text
Tests: 15 passed
```

## Technologie

Momenteel gebruikt het project:

-   Kotlin
-   Ktor
-   Kotlin CLI / Amper
-   kotlinx.serialization
-   Ktor ContentNegotiation
-   In-memory persistence
-   Kotlin Test
-   Ktor Test Host
-   Git en GitHub

## Volgende stappen

1.  De overige `GET`-, `POST`, `PUT` en `DELETE`-endpoints
    implementeren.
2.  Zoeken en filteren implementeren.
3.  API-validatie en foutafhandeling toevoegen.
4.  Authenticatie en autorisatie integreren.
5.  Aanvullende integration tests schrijven.
6.  Persistente databaseopslag toevoegen.
7.  API-documentatie en API-testrapport verder uitwerken.

## Status

Het project is in ontwikkeling.

Huidige status voor **v0.12**:

-   Domain: **geïmplementeerd**
-   Repository en in-memory persistence: **geïmplementeerd**
-   Service-laag: **geïmplementeerd**
-   Unit tests: **geïmplementeerd**
-   DTO request/response-laag: **geïmplementeerd**
-   JSON-serialisatie: **geïmplementeerd**
-   Ktor ContentNegotiation: **geïmplementeerd**
-   Report mapping: **geïmplementeerd**
-   Ktor API: **in ontwikkeling**
-   `GET /reports`: **geïmplementeerd**
-   Integration tests: **gestart**
-   Tests: **15 passed**
-   Database persistence: **nog niet geïmplementeerd**
-   Authenticatie/autorisatie: **nog niet geïmplementeerd**
