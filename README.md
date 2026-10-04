# Brul

Brul is een buurtapplicatie waarin bewoners gebeurtenissen in hun buurt
kunnen melden en bekijken. Het doel is om bewoners beter te informeren
over wat er in hun omgeving gebeurt.

Deze repository bevat de backend van Brul, ontwikkeld in **Kotlin**. Het
project bevindt zich momenteel in een vroege ontwikkelfase. Op dit
moment zijn het **domein**, de **repository/persistence-laag** en de
**service-laag** geïmplementeerd.

## Huidige projectstructuur

``` text
nl.brul
├── domain
├── persistence
└── service
```

### Domain

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
-   Een melding kan worden afgerond.
-   Alleen een open `HelpRequest` kan worden geclaimd.
-   Het claimen van een `HelpRequest` verandert de status naar
    `CLAIMED`.

### Persistence

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

### Service

De package `service` bevat de applicatielogica en vormt de verbinding
tussen het domein en de persistence-laag.

`ReportService` bevat momenteel functionaliteit voor:

-   het ophalen van meldingen;
-   het ophalen van één specifieke melding;
-   het aanmaken van een melding;
-   het wijzigen van een melding;
-   het verwijderen van een melding;
-   het claimen van een `HelpRequest`;
-   het afronden van een melding.

De service-laag is daarnaast verantwoordelijk voor bedrijfsregels die
afhankelijk zijn van de huidige gebruiker.

Zo kan een gewone gebruiker zijn eigen meldingen beheren, kan een
administrator iedere melding beheren en kan een gebruiker zijn eigen
`HelpRequest` niet claimen.

Hiermee blijven de verantwoordelijkheden gescheiden: het domein bewaakt
een geldige toestand en het gedrag van objecten, terwijl de service
bepaalt of een gebruiker een bepaalde handeling mag uitvoeren.

## Architectuur

De huidige backend gebruikt een gelaagde structuur:

``` text
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

De service benadert de persistence-laag via de interface
`ReportRepository`. Hierdoor is `ReportService` niet direct afhankelijk
van de concrete `InMemoryReportRepository`.

## Objectgeoriënteerd ontwerp

De huidige implementatie gebruikt verschillende objectgeoriënteerde
concepten:

-   **Abstractie** --- `Report` definieert gedeelde eigenschappen en
    gedrag van meldingen.
-   **Overerving** --- `SafetyReport`, `NuisanceReport` en `HelpRequest`
    erven van `Report`.
-   **Encapsulatie** --- de toestand van meldingen wordt via
    gecontroleerd domeingedrag gewijzigd.
-   **Interfaces** --- `Claimable` beschrijft gedrag dat door
    `HelpRequest` wordt geïmplementeerd.
-   **Polymorfisme** --- de repository en service kunnen werken met het
    gemeenschappelijke type `Report`.
-   **Data classes** --- worden gebruikt voor objecten zoals `User` en
    `Location`.
-   **Enums** --- worden gebruikt voor vaste domeinwaarden zoals rollen,
    statussen, urgentieniveaus en typen overlast.

## Testen

Unit tests worden gebruikt om het geïmplementeerde gedrag van het domein
en de service-laag te controleren.

De huidige tests controleren onder andere domeinvalidatie,
statuswijzigingen van meldingen, het claimen van hulpverzoeken en
permissies binnen de service-laag.

De tests kunnen vanuit de hoofdmap van het project worden uitgevoerd
met:

``` powershell
.\kotlin.bat test
```

## Technologie

Momenteel gebruikt het project:

-   Kotlin
-   Ktor-projectstructuur
-   Kotlin CLI / Amper
-   In-memory persistence
-   Git en GitHub

De Ktor API-laag is nog niet geïmplementeerd.

## Volgende stappen

De geplande volgende stappen zijn:

1.  DTO's voor API-requests en responses.
2.  Ktor-routing en HTTP-endpoints.
3.  Zoeken en filteren.
4.  API-validatie en foutafhandeling.
5.  Integratie van authenticatie en autorisatie.
6.  Integratietests.
7.  Persistente databaseopslag.

## Status

Het project is in ontwikkeling.

Huidige status:

-   Domain: geïmplementeerd
-   Repository en in-memory persistence: geïmplementeerd
-   Service-laag: geïmplementeerd
-   Unit tests: geïmplementeerd
-   DTO/API-laag: nog niet geïmplementeerd
