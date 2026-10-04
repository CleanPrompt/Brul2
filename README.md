# Brul

**Versie: v0.14**

Brul is een buurtapplicatie waarin bewoners gebeurtenissen in hun buurt
kunnen melden en bekijken. Het doel is om bewoners beter te informeren
over wat er in hun omgeving gebeurt.

Deze repository bevat de backend van Brul, ontwikkeld in **Kotlin met
Ktor**. Het project bevindt zich in actieve ontwikkeling. Het domein, de
persistence-laag, service-laag en DTO-laag zijn geïmplementeerd. De
basisrouting van de Ktor Web API is inmiddels volledig uitgewerkt.

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

De belangrijkste abstractie is `Report`. Brul ondersteunt drie soorten
meldingen:

-   `SafetyReport` --- een veiligheidsmelding met een urgentieniveau.
-   `NuisanceReport` --- een overlastmelding met een type overlast.
-   `HelpRequest` --- een hulpverzoek dat door een andere gebruiker kan
    worden geclaimd.

Gedeelde gegevens en gedrag zijn ondergebracht in de abstracte klasse
`Report`. Het domein bevat daarnaast onder andere `User`, `Location`,
rollen, statussen, urgentieniveaus en typen overlast.

`HelpRequest` implementeert de interface `Claimable`, omdat alleen
hulpverzoeken geclaimd kunnen worden.

Belangrijke geïmplementeerde domeinregels:

-   Iedere melding krijgt automatisch een ID.
-   Iedere melding krijgt automatisch een aanmaaktijdstip.
-   Een nieuwe melding begint met de status `OPEN`.
-   De titel mag niet leeg zijn.
-   Latitude moet tussen `-90` en `90` liggen.
-   Longitude moet tussen `-180` en `180` liggen.
-   Bewerkbare gegevens worden via gecontroleerd domeingedrag gewijzigd.
-   `id`, `createdBy` en `createdAt` blijven onveranderbaar.
-   Een melding kan worden afgerond.
-   Alleen een open `HelpRequest` kan worden geclaimd.
-   Het claimen van een `HelpRequest` verandert de status naar
    `CLAIMED`.

## DTO

De package `dto` bevat de Data Transfer Objects voor communicatie via de
Web API.

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

De drie soorten meldingen hebben ieder een eigen create-request omdat ze
verschillende gegevens nodig hebben. `ReportResponse` vormt het algemene
responsemodel.

De DTO's gebruiken `kotlinx.serialization` zodat Ktor Kotlin-objecten en
JSON kan serialiseren en deserialiseren.

`ReportMapper` zet domeinobjecten van het type `Report` om naar
`ReportResponse`. Hierdoor worden domeinobjecten niet rechtstreeks als
API-response gebruikt.

## Persistence

De package `persistence` bepaalt hoe meldingen worden opgeslagen en
opgehaald.

`ReportRepository` vormt de repository-abstractie voor het ophalen,
zoeken, opslaan, bijwerken en verwijderen van meldingen.

De huidige implementatie is `InMemoryReportRepository`. Meldingen worden
voorlopig in het geheugen opgeslagen met behulp van een
Kotlin-collectie.

Hierdoor kan de persistence-implementatie later worden vervangen zonder
dat het domein of de service-laag volledig aangepast hoeft te worden.

## Service

De package `service` bevat de applicatielogica tussen routing, domein en
persistence.

`ReportService` ondersteunt momenteel:

-   alle meldingen ophalen;
-   één melding ophalen;
-   een `SafetyReport` aanmaken;
-   een `NuisanceReport` aanmaken;
-   een `HelpRequest` aanmaken;
-   een melding wijzigen;
-   een melding verwijderen;
-   een `HelpRequest` claimen;
-   een melding afronden.

De service bewaakt daarnaast gebruikersafhankelijke bedrijfsregels. Een
gewone gebruiker kan zijn eigen meldingen beheren, een administrator kan
iedere melding beheren en een gebruiker kan zijn eigen `HelpRequest`
niet claimen.

Het domein bewaakt een geldige toestand en domeingedrag; de service
bepaalt of een gebruiker een bepaalde handeling mag uitvoeren.

## Config

De package `config` bevat configuratie voor Ktor.

`ContentNegotiation` is geconfigureerd met `kotlinx.serialization`.
Hierdoor kan de Web API JSON-requests verwerken en responses als JSON
terugsturen.

## Routing

De package `routing` bevat de HTTP-routes van de Ktor Web API.

De volgende basisroutes zijn geïmplementeerd:

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

De routes gebruiken `ReportService` voor de applicatielogica en
`ReportMapper` voor het omzetten van domeinobjecten naar API-responses.

Authenticatie is nog niet geïmplementeerd. Tijdens de huidige
ontwikkelfase gebruikt de routing daarom tijdelijk een vaste gebruiker.
Deze tijdelijke oplossing wordt vervangen wanneer authenticatie en
autorisatie worden geïntegreerd.

## Architectuur

De backend gebruikt een gelaagde structuur:

``` text
HTTP request
     ↓
Ktor Routing
     ↓
Request DTO
     ↓
Service
     ↓
Repository
     ↓
Domain
```

Responses lopen via de DTO-laag terug naar de client:

``` text
Domain
   ↓
ReportMapper
   ↓
Response DTO
   ↓
JSON response
```

Laag          Verantwoordelijkheid
  ------------- ----------------------------------------------------
Domain        Domeinmodel, toestand en domeingedrag
Persistence   Opslaan en ophalen van meldingen
Service       Use-cases en gebruikersafhankelijke bedrijfsregels
DTO           Gegevensoverdracht van en naar de API
Routing       HTTP-endpoints en koppeling met de service
Config        Configuratie van Ktor-functionaliteit

De service benadert persistence via `ReportRepository`. Hierdoor is
`ReportService` niet direct afhankelijk van `InMemoryReportRepository`.

De concrete dependencies worden momenteel handmatig gekoppeld. Er wordt
geen dependency-injectionframework gebruikt.

## Objectgeoriënteerd ontwerp

De implementatie gebruikt onder andere:

-   **Abstractie** --- `Report` bevat gedeelde eigenschappen en gedrag.
-   **Overerving** --- de drie reporttypen erven van `Report`.
-   **Encapsulatie** --- toestand wordt via gecontroleerde functies
    zoals `update`, `claim` en `resolve` gewijzigd.
-   **Interfaces** --- `Claimable` beschrijft claim-gedrag.
-   **Polymorfisme** --- repository en service werken met het
    gemeenschappelijke type `Report`.
-   **Data classes** --- onder andere voor `User`, `Location` en DTO's.
-   **Enums** --- voor rollen, statussen, urgentieniveaus en typen
    overlast.

## Web API

De basis van het API-contract is nu in routing geïmplementeerd.

Ondersteunde operaties:

``` text
GET    /reports                 Alle meldingen ophalen
GET    /reports/{id}            Eén melding ophalen
POST   /reports/safety          SafetyReport aanmaken
POST   /reports/nuisance        NuisanceReport aanmaken
POST   /reports/help            HelpRequest aanmaken
PUT    /reports/{id}            Melding wijzigen
DELETE /reports/{id}            Melding verwijderen
POST   /reports/{id}/claim      HelpRequest claimen
POST   /reports/{id}/resolve    Melding afronden
```

Zoeken en filteren via queryparameters worden later toegevoegd.

De foutafhandeling is nog niet definitief. De service gebruikt op
sommige plaatsen momenteel `null` of `Boolean` als resultaat. Daardoor
kan de routing nog niet altijd onderscheid maken tussen bijvoorbeeld
`404 Not Found`, `403 Forbidden` en `409 Conflict`. Dit wordt in een
volgende fase verbeterd.

## Testen

Unit tests controleren het gedrag van het domein en de service-laag. De
tests controleren onder andere:

-   domeinvalidatie;
-   statuswijzigingen;
-   het claimen van hulpverzoeken;
-   permissies binnen de service-laag;
-   service- en repositorygedrag.

Daarnaast zijn Ktor integration tests gestart.

Momenteel worden onder andere de volgende API-scenario's getest:

-   `GET /reports` retourneert `200 OK`.
-   `GET /reports/{id}` met een onbekend ID retourneert `404 Not Found`.

Tests uitvoeren:

``` powershell
.\kotlin.bat test
```

Huidige teststatus voor v0.14:

``` text
Tests: 16 passed
```

De overige nieuwe routes zijn geïmplementeerd en compileren, maar moeten
nog uitgebreider via HTTP-integration tests worden gecontroleerd.

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

1.  De nieuwe POST-, PUT-, DELETE-, claim- en resolve-routes via
    integration tests controleren.
2.  Zoeken en filteren implementeren.
3.  Consistente API-validatie en foutafhandeling toevoegen.
4.  Authenticatie en autorisatie integreren.
5.  Aanvullende integration tests schrijven.
6.  Persistente databaseopslag toevoegen.
7.  API-documentatie en het API-testrapport verder uitwerken.

## Status

Huidige status voor **v0.14**:

-   Domain: **geïmplementeerd**
-   Repository en in-memory persistence: **geïmplementeerd**
-   Service-laag: **geïmplementeerd**
-   DTO request/response-laag: **geïmplementeerd**
-   JSON-serialisatie: **geïmplementeerd**
-   Ktor ContentNegotiation: **geïmplementeerd**
-   Report mapping: **geïmplementeerd**
-   Basisrouting Web API: **geïmplementeerd**
-   GET-routes: **geïmplementeerd en gedeeltelijk integration-tested**
-   POST-routes: **geïmplementeerd, integration tests volgen**
-   PUT-route: **geïmplementeerd, integration test volgt**
-   DELETE-route: **geïmplementeerd, integration test volgt**
-   Claim/resolve-routes: **geïmplementeerd, integration tests volgen**
-   Unit- en integration tests: **16 tests geslaagd**
-   Zoeken/filteren: **nog niet geïmplementeerd**
-   Definitieve foutafhandeling: **nog niet geïmplementeerd**
-   Authenticatie/autorisatie: **nog niet geïmplementeerd**
-   Database persistence: **nog niet geïmplementeerd**
