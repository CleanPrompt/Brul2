# 1. Vastgestelde ontwerpkeuzes

**Project:** PocketBattleAPI  
**Document:** Software Design Document (SDD)  
**Hoofdstuk:** 1 — Vastgestelde ontwerpkeuzes  
**Versie:** 1.0  
**Datum:** 22 oktober 2026  
**Status:** Vastgesteld  
**Programmeertaal:** Kotlin  
**Backendframework:** Ktor  
**Buildtool:** JetBrains Amper

---

## 1.1 Inleiding

Dit hoofdstuk beschrijft de vastgestelde ontwerpkeuzes voor PocketBattleAPI versie 1.0.

De ontwerpkeuzes vormen de basis voor:

- De functionele en niet-functionele requirements.
- De business rules.
- Het Use Case Diagram.
- Het Activity Diagram.
- Het Class Diagram.
- De Sequence Diagrams.
- Het Component Diagram.
- Het databaseontwerp.
- De implementatie in Kotlin.
- De automatische tests.
- De technische projectdocumentatie.

Het doel is om vooraf duidelijke afspraken te maken over de functionaliteiten, spelregels, architectuur en technische uitgangspunten van de applicatie.

Hierdoor kunnen ontwerp, implementatie en tests op dezelfde specificaties worden gebaseerd.

### 1.1.1 Projectomschrijving

PocketBattleAPI is een REST API waarmee gebruikers monsters kunnen beheren en volledig automatische, turn-based gevechten tussen twee monsters kunnen simuleren.

De applicatie is geïnspireerd op het principe van elementaire monstergevechten.

Gebruikers kunnen monsters aanmaken, bekijken, wijzigen, verwijderen en filteren op elementtype. Daarnaast kunnen zij twee bestaande monsters selecteren en een gevecht laten simuleren.

De simulator voert het volledige gevecht zelfstandig uit en retourneert een resultaat met de winnaar, het aantal beurten en een gedetailleerde gevechtslog.

### 1.1.2 Projectdoelstellingen

PocketBattleAPI heeft twee hoofddoelstellingen.

**Functionele doelstelling**

Het ontwikkelen van een werkende REST API met:

- Monsterbeheer.
- Automatische gevechtssimulaties.
- Elementaire type-effectiviteit.
- Schadeberekening.
- Gevechtslogging.
- Gegevensopslag.

**Educatieve doelstelling**

Het toepassen en verdiepen van kennis over:

- Kotlin.
- Objectgeoriënteerd programmeren (OOP).
- Functioneel programmeren (FP).
- Encapsulatie.
- Overerving en interfaces.
- Softwarearchitectuur.
- REST API's.
- Ktor.
- Databases.
- Repositorypatronen.
- Dependency Injection.
- Unit tests.
- Integratietests.
- UML en PlantUML.

Het project dient daarnaast als leer- en portfolio-opdracht.

---

## 1.2 Overzicht van vastgestelde ontwerpkeuzes

| Onderdeel | Vastgestelde keuze |
|---|---|
| Projectnaam | PocketBattleAPI |
| Versie | 1.0 |
| Applicatietype | REST API |
| Programmeertaal | Kotlin |
| Backendframework | Ktor |
| Buildtool | JetBrains Amper |
| Buildconfiguratie | `module.yaml` |
| Programmeerparadigma | Objectgeoriënteerd, aangevuld met functionele programmeerconcepten |
| Architectuur | Feature-based architectuur |
| Domein | Turn-based monstergevechten |
| Ondersteunde elementtypes | FIRE, WATER en GRASS |
| Aantal aanvallen per monster | Precies twee |
| Eerste aanval | Normale aanval |
| Tweede aanval | Elementaanval |
| Aanvalsselectie | Automatisch en afwisselend |
| Gevechtsuitvoering | Volledig automatisch |
| Gevechtsstatistieken | HP, Level, Defense en Speed |
| Type-effectiviteit | Fire > Grass > Water > Fire |
| Sterke aanval | 1.5× schade |
| Neutrale aanval | 1.0× schade |
| Zwakke aanval | 0.75× schade |
| Schadeberekening | Deterministische formule |
| Minimale schade | 1 HP |
| Startvolgorde | Monster met hoogste Speed begint |
| Gelijke Speed | Monster 1 begint |
| Beurtvolgorde | Om de beurt aanvallen |
| Einde gevecht | Zodra een monster 0 HP bereikt |
| Winnaar | Het monster dat de tegenstander verslaat |
| Gevechtsresultaat | Winnaar, verliezer, aantal beurten en gevechtslog |
| Gevechts-HP | Tijdelijk, via onafhankelijke gevechtsinstanties |
| Gegevensopslag | Eerst in-memory, daarna H2 |
| Databasebibliotheek | JetBrains Exposed |
| API-dataformaat | JSON |
| Teststrategie | Unit tests, repositorytests en Ktor-integratietests |
| Frontend | Niet inbegrepen |
| Authenticatie | Niet inbegrepen |
| Multiplayer | Niet inbegrepen |
| Permanente gevechtsgeschiedenis | Niet inbegrepen |

---

## 1.3 Monsterontwerp

### 1.3.1 Monster als domeinobject

Een monster is de centrale domeinentiteit binnen PocketBattleAPI.

Ieder monster heeft:

1. Een unieke identiteit.
2. Een naam.
3. Een elementtype.
4. Gevechtsstatistieken.
5. Precies twee beschikbare aanvallen.

De monstergegevens worden beheerd via de REST API.

De gevechtslogica werkt met tijdelijke gevechtsinstanties, zodat de opgeslagen monstergegevens niet veranderen door een simulatie.

### 1.3.2 Identificerende eigenschappen

| Eigenschap | Kotlin-type | Beschrijving |
|---|---|---|
| `id` | `Int` | Unieke identificatie |
| `name` | `String` | Naam van het monster |
| `type` | `ElementType` | Elementtype van het monster |

De toegestane elementtypes zijn:

- `FIRE`
- `WATER`
- `GRASS`

Een monster heeft altijd precies één elementtype.

### 1.3.3 Gevechtsstatistieken

| Eigenschap | Kotlin-type | Beschrijving |
|---|---|---|
| `level` | `Int` | Beïnvloedt de veroorzaakte schade |
| `maxHp` | `Int` | Maximale hoeveelheid levenspunten |
| `currentHp` | `Int` | Resterende levenspunten tijdens een gevecht |
| `defense` | `Int` | Vermindert ontvangen schade |
| `speed` | `Int` | Bepaalt welk monster begint |

De statistieken hebben verschillende verantwoordelijkheden.

**HP**

HP staat voor Health Points.

Een monster met 0 HP is verslagen.

De actuele HP mag nooit negatief zijn en mag de maximale HP niet overschrijden.

**Level**

Level beïnvloedt de hoeveelheid schade die een monster veroorzaakt.

Een hoger level resulteert, bij verder gelijke omstandigheden, in meer schade.

**Defense**

Defense vermindert de hoeveelheid schade die een monster ontvangt.

Een hogere Defense resulteert, bij verder gelijke omstandigheden, in minder of gelijke schade.

**Speed**

Speed bepaalt welk monster het gevecht begint.

Speed heeft geen invloed op de schadeberekening.

Na de eerste beurt vallen beide monsters afwisselend aan.

### 1.3.4 Geldige waarden

Voor PocketBattleAPI v1.0 gelden de volgende regels:

| Eigenschap | Geldige waarde |
|---|---|
| `id` | Positief geheel getal |
| `name` | Niet-lege tekst |
| `type` | FIRE, WATER of GRASS |
| `level` | Geheel getal groter dan 0 |
| `maxHp` | Geheel getal groter dan 0 |
| `currentHp` | Van 0 tot en met `maxHp` |
| `defense` | Geheel getal groter dan of gelijk aan 0 |
| `speed` | Geheel getal groter dan 0 |

De applicatie weigert ongeldige invoer.

### 1.3.5 Voorbeeldmonster

```json
{
  "id": 1,
  "name": "Ember",
  "type": "FIRE",
  "level": 5,
  "maxHp": 40,
  "defense": 8,
  "speed": 12
}
```

Dit voorbeeld toont de conceptuele gegevens van een opgeslagen monster.

De definitieve JSON-DTO's worden tijdens het API-ontwerp gespecificeerd.

### 1.3.6 Tijdelijke gevechts-HP

Een gevecht mag de opgeslagen monstergegevens niet wijzigen.

Daarom worden bij de start van een gevecht onafhankelijke gevechtsinstanties aangemaakt.

Deze instanties:

- Bevatten de benodigde monstergegevens.
- Starten met volledige HP.
- Houden hun eigen actuele HP bij.
- Worden uitsluitend gebruikt tijdens de simulatie.

Wanneer een gevecht eindigt, blijven de opgeslagen monsters ongewijzigd.

Hierdoor kunnen dezelfde monsters meerdere keren tegen elkaar vechten.

---

## 1.4 Elementtypes en type-effectiviteit

### 1.4.1 Ondersteunde elementtypes

PocketBattleAPI ondersteunt drie elementtypes:

1. FIRE
2. WATER
3. GRASS

Andere elementtypes worden in versie 1.0 niet ondersteund.

### 1.4.2 Relaties tussen elementtypes

De elementtypes hebben een cyclische relatie:

- Fire is sterk tegen Grass.
- Grass is sterk tegen Water.
- Water is sterk tegen Fire.

Omgekeerd geldt:

- Fire is zwak tegen Water.
- Water is zwak tegen Grass.
- Grass is zwak tegen Fire.

### 1.4.3 Typevermenigvuldigers

De type-effectiviteit wordt verwerkt met een vermenigvuldigingsfactor.

| Effectiviteit | Vermenigvuldiger | Effect |
|---|---:|---|
| Sterk | 1.5 | 50% extra schade |
| Neutraal | 1.0 | Geen aanpassing |
| Zwak | 0.75 | 25% minder schade |

Deze waarden zijn definitief vastgesteld voor PocketBattleAPI v1.0.

### 1.4.4 Effectiviteitsmatrix

| Aanvalstype | Verdediger FIRE | Verdediger WATER | Verdediger GRASS |
|---|---:|---:|---:|
| FIRE | 1.0 | 0.75 | 1.5 |
| WATER | 1.5 | 1.0 | 0.75 |
| GRASS | 0.75 | 1.5 | 1.0 |
| Normale aanval | 1.0 | 1.0 | 1.0 |

### 1.4.5 Normale aanvallen

Een normale aanval heeft geen elementtype.

Daarom geldt voor iedere normale aanval:

```text
TypeMultiplier = 1.0
```

Normale aanvallen ontvangen nooit een elementair voordeel of nadeel.

### 1.4.6 Ontwerpbeslissing

Type-effectiviteit wordt centraal bepaald binnen de gevechtslogica.

De regels worden niet afzonderlijk gekopieerd naar iedere monsterklasse.

Dit voorkomt dubbele logica en maakt het mogelijk om alle combinaties systematisch te testen.

---

## 1.5 Aanvalssysteem

### 1.5.1 Aantal aanvallen

Ieder monster heeft precies twee aanvallen:

1. Eén normale aanval.
2. Eén elementaanval.

Een monster kan in versie 1.0 geen extra aanvallen leren.

### 1.5.2 Normale aanval

De normale aanval:

- Heeft een naam.
- Heeft een positieve basiskracht.
- Heeft geen elementtype.
- Gebruikt altijd een typevermenigvuldiger van 1.0.

Een voorbeeld is:

```text
Name: Tackle
Type: Normal
BasePower: 15
```

`Normal` betekent hier dat de aanval geen elementtype heeft.

Het is geen vierde monsterelementtype.

### 1.5.3 Elementaanval

De elementaanval:

- Heeft een naam.
- Heeft een positieve basiskracht.
- Heeft hetzelfde elementtype als het monster.
- Kan profiteren van type-effectiviteit.

Voorbeelden:

| Monster | Elementtype | Elementaanval |
|---|---|---|
| Ember | FIRE | Flame Burst |
| Ripple | WATER | Water Splash |
| Sprout | GRASS | Leaf Strike |

### 1.5.4 Eigenschappen van een aanval

| Eigenschap | Kotlin-type | Beschrijving |
|---|---|---|
| `name` | `String` | Naam van de aanval |
| `type` | `ElementType?` | Elementtype, of `null` bij een normale aanval |
| `basePower` | `Int` | Basiskracht van de aanval |

De exacte representatie van normale aanvallen wordt definitief uitgewerkt in het Class Diagram.

### 1.5.5 Automatische aanvalsselectie

De gebruiker kiest tijdens een gevecht geen aanvallen.

De simulator bepaalt zelfstandig welke aanval wordt uitgevoerd.

Voor versie 1.0 geldt een vaste strategie:

1. Tijdens de eerste eigen beurt gebruikt een monster zijn normale aanval.
2. Tijdens de tweede eigen beurt gebruikt het monster zijn elementaanval.
3. Tijdens de derde eigen beurt gebruikt het monster opnieuw zijn normale aanval.
4. Tijdens de vierde eigen beurt gebruikt het monster opnieuw zijn elementaanval.
5. Dit patroon herhaalt zich totdat het gevecht eindigt.

De teller wordt per monster bijgehouden.

Dit betekent dat beide monsters hun eigen aanvalsvolgorde hebben.

### 1.5.6 Waarom een vaste strategie?

Een vaste aanvalsstrategie heeft meerdere voordelen:

- De simulatie is voorspelbaar.
- De resultaten zijn reproduceerbaar.
- Unit tests zijn eenvoudig te schrijven.
- Beide aanvallen worden gebruikt.
- Er is geen willekeurige aanvalsselectie nodig.
- De simulator blijft overzichtelijk.

Willekeurige aanvalsselectie valt buiten de scope van versie 1.0.

---

## 1.6 Exacte schadeformule

### 1.6.1 Definitieve ontwerpbeslissing

PocketBattleAPI gebruikt een deterministische schadeformule.

De schade wordt berekend op basis van:

1. De basiskracht van de gekozen aanval.
2. Het level van de aanvaller.
3. De Defense van de verdediger.
4. De type-effectiviteit.

Speed en HP beïnvloeden de berekende schade niet.

### 1.6.2 Wiskundige formule

De definitief goedgekeurde formule is:

\[
\boxed{
Damage =
\max\left(
1,
\left\lfloor
\frac{BasePower \times (Level + 5)}
{Defense + 10}
\times TypeMultiplier
\right\rfloor
\right)
}
\]

Hierbij geldt:

- `BasePower`: basiskracht van de gebruikte aanval.
- `Level`: level van het aanvallende monster.
- `Defense`: Defense van het verdedigende monster.
- `TypeMultiplier`: factor voor de type-effectiviteit.
- `floor`: afronden naar beneden.
- `max(1, ...)`: minimaal 1 schade.

### 1.6.3 Betekenis van de formule

**BasePower**

Een aanval met een hogere basiskracht veroorzaakt, bij gelijke overige omstandigheden, meer of gelijke schade.

**Level + 5**

Het level verhoogt de aanvalskracht.

De constante 5 zorgt ervoor dat ook monsters met een laag level voldoende schade kunnen veroorzaken.

**Defense + 10**

Defense vermindert de schade.

De constante 10 voorkomt delen door nul en zorgt ervoor dat lage Defense-waarden niet tot extreem hoge schade leiden.

**TypeMultiplier**

De typevermenigvuldiger past de schade aan op basis van de elementaire verhouding.

**Afronding naar beneden**

Schade wordt altijd als een geheel getal weergegeven.

**Minimale schade**

Iedere geldige aanval veroorzaakt minimaal 1 HP schade.

Hierdoor blijft een gevecht voortgang maken.

### 1.6.4 Typevermenigvuldigers

De volgende waarden zijn definitief:

```text
Strong  = 1.5
Neutral = 1.0
Weak    = 0.75
```

### 1.6.5 Berekeningsvolgorde

De berekening vindt plaats in deze volgorde:

1. Lees de `basePower` van de aanval.
2. Lees het `level` van de aanvaller.
3. Lees de `defense` van de verdediger.
4. Bepaal de `typeMultiplier`.
5. Bereken `basePower * (level + 5)`.
6. Deel door `defense + 10`.
7. Vermenigvuldig met de typevermenigvuldiger.
8. Rond het resultaat naar beneden af.
9. Gebruik minimaal 1 als uiteindelijke schade.

De implementatie moet voorkomen dat tussentijdse gehele-getalafronding het resultaat onbedoeld verandert.

### 1.6.6 Voorbeeldmonsters

Voor de voorbeelden gebruiken we:

| Eigenschap | Ember | Ripple |
|---|---|---|
| Type | FIRE | WATER |
| Level | 5 | 5 |
| Max HP | 40 | 40 |
| Defense | 8 | 10 |
| Speed | 12 | 18 |
| Elementaanval | Flame Burst | Water Splash |
| BasePower | 20 | 20 |

### 1.6.7 Voorbeeld A: Water tegen Fire

Ripple gebruikt Water Splash tegen Ember.

Gegevens:

```text
BasePower      = 20
Level          = 5
Defense        = 8
TypeMultiplier = 1.5
```

Berekening:

\[
Damage =
\max\left(
1,
\left\lfloor
\frac{20 \times (5+5)}
{8+10}
\times 1.5
\right\rfloor
\right)
\]

\[
Damage =
\left\lfloor
\frac{200}{18} \times 1.5
\right\rfloor
\]

\[
Damage =
\left\lfloor 16.666\ldots \right\rfloor
\]

\[
\boxed{Damage = 16}
\]

Ember verliest 16 HP.

Bij een beginwaarde van 40 HP blijft over:

```text
40 - 16 = 24 HP
```

### 1.6.8 Voorbeeld B: Fire tegen Water

Ember gebruikt Flame Burst tegen Ripple.

Gegevens:

```text
BasePower      = 20
Level          = 5
Defense        = 10
TypeMultiplier = 0.75
```

Berekening:

\[
Damage =
\max\left(
1,
\left\lfloor
\frac{20 \times (5+5)}
{10+10}
\times 0.75
\right\rfloor
\right)
\]

\[
Damage =
\left\lfloor
\frac{200}{20} \times 0.75
\right\rfloor
\]

\[
Damage =
\left\lfloor 7.5 \right\rfloor
\]

\[
\boxed{Damage = 7}
\]

Ripple verliest 7 HP.

Bij een beginwaarde van 40 HP blijft over:

```text
40 - 7 = 33 HP
```

### 1.6.9 Voorbeeld C: Normale aanval

Ember gebruikt Tackle tegen Ripple.

Gegevens:

```text
BasePower      = 15
Level          = 5
Defense        = 10
TypeMultiplier = 1.0
```

Berekening:

\[
Damage =
\left\lfloor
\frac{15 \times 10}{20} \times 1.0
\right\rfloor
\]

\[
Damage = \lfloor 7.5 \rfloor
\]

\[
\boxed{Damage = 7}
\]

Omdat Tackle een normale aanval is, wordt geen elementvoordeel toegepast.

### 1.6.10 Minimale schade

Wanneer de formule een waarde onder 1 oplevert, wordt de schade vastgesteld op 1.

Voorbeeld:

```text
BasePower      = 1
Level          = 1
Defense        = 100
TypeMultiplier = 0.75
```

De berekende waarde is kleiner dan 1.

Na toepassing van de minimumregel:

```text
Damage = 1
```

### 1.6.11 Verantwoordelijkheid van de DamageCalculator

De schadeberekening wordt ondergebracht in een afzonderlijke `DamageCalculator`.

Deze component:

- Ontvangt de gegevens die nodig zijn voor de berekening.
- Bepaalt de type-effectiviteit.
- Past de definitieve formule toe.
- Retourneert de schade als een geheel getal.

De `DamageCalculator`:

- Wijzigt geen HP.
- Kiest geen aanvallen.
- Bepaalt geen winnaar.
- Schrijft niets naar de database.
- Verstuurt geen HTTP-responses.

### 1.6.12 Functioneel programmeerprincipe

De schadeberekening wordt ontworpen als een pure berekening.

Dat betekent:

- Gelijke invoer levert gelijke uitvoer op.
- De berekening heeft geen bijwerkingen.
- Er wordt geen gedeelde toestand gewijzigd.
- De berekening kan onafhankelijk worden getest.

Dit is een bewuste toepassing van functionele programmeerconcepten binnen een objectgeoriënteerde applicatie.

### 1.6.13 Testbaarheid

Voor de schadeformule worden minimaal de volgende scenario's getest:

1. Een sterke elementaanval.
2. Een zwakke elementaanval.
3. Een neutrale elementaanval.
4. Een normale aanval.
5. Een aanval tegen hoge Defense.
6. De minimale schade van 1.
7. Correcte afronding naar beneden.
8. Invloed van Level.
9. Invloed van BasePower.
10. Alle combinaties van elementtypes.

De voorbeeldberekeningen uit dit hoofdstuk kunnen als uitgangspunt voor unit tests worden gebruikt.

---

## 1.7 Gevechtssysteem

### 1.7.1 Algemene werking

Een gevecht is een volledig automatische simulatie tussen twee bestaande monsters.

De gebruiker hoeft alleen aan te geven welke twee monsters deelnemen.

De simulator bepaalt vervolgens:

- De startvolgorde.
- De aanvallen.
- De schade.
- De resterende HP.
- Het einde van het gevecht.
- De winnaar.

### 1.7.2 Startvoorwaarden

Een gevecht kan alleen starten wanneer:

1. Beide monsters bestaan.
2. De opgegeven monster-ID's verschillend zijn.
3. Beide monsters geldige gegevens hebben.
4. Beide monsters precies twee geldige aanvallen hebben.

Een monster mag niet tegen zichzelf vechten.

### 1.7.3 Beginstatus

Bij het starten van een gevecht:

- Worden onafhankelijke gevechtsinstanties gemaakt.
- Beginnen beide monsters met volledige HP.
- Worden de eigen aanvalstellers op nul gezet.
- Wordt de startvolgorde bepaald.

Opgeslagen monsters worden niet gewijzigd.

### 1.7.4 Startvolgorde op basis van Speed

Speed bepaalt welk monster de eerste aanval uitvoert.

| Situatie | Eerste aanvaller |
|---|---|
| Monster 1 heeft hogere Speed | Monster 1 |
| Monster 2 heeft hogere Speed | Monster 2 |
| Beide monsters hebben gelijke Speed | Monster 1 |

Met monster 1 wordt het eerste monster bedoeld dat in het battle-request is opgegeven.

### 1.7.5 Voorbeeld startvolgorde

```text
Ember:
Speed = 12

Ripple:
Speed = 18
```

Ripple begint, omdat:

```text
18 > 12
```

Daarna vallen beide monsters afwisselend aan.

### 1.7.6 Beurtverloop

Iedere beurt bestaat uit de volgende stappen:

1. Bepaal het aanvallende monster.
2. Bepaal het verdedigende monster.
3. Selecteer automatisch de volgende aanval van de aanvaller.
4. Bereken de schade met de DamageCalculator.
5. Trek de schade af van de actuele HP van de verdediger.
6. Zorg dat de HP niet onder nul komt.
7. Registreer de gebeurtenis in de gevechtslog.
8. Controleer of de verdediger verslagen is.
9. Wissel van aanvaller wanneer beide monsters nog leven.

Dit proces wordt herhaald totdat een monster 0 HP heeft.

### 1.7.7 HP tijdens een gevecht

De HP wordt bijgewerkt volgens:

\[
CurrentHp_{new} =
\max(0, CurrentHp_{old} - Damage)
\]

Een monster kan daardoor nooit negatieve HP krijgen.

### 1.7.8 Verslagen monster

Een monster is verslagen wanneer:

```text
currentHp == 0
```

Een verslagen monster kan niet meer aanvallen.

Zodra een monster is verslagen, stopt de simulatie onmiddellijk.

### 1.7.9 Winnaar

De winnaar is het monster dat na de laatste aanval nog HP over heeft.

De verliezer is het monster waarvan de HP op nul is gekomen.

### 1.7.10 Geen willekeur

PocketBattleAPI v1.0 gebruikt geen willekeur bij:

- De startvolgorde.
- De aanvalsselectie.
- De schadeberekening.

Daardoor levert een gevecht tussen dezelfde twee onveranderde monsters steeds hetzelfde resultaat op, mits de volgorde waarin zij worden opgegeven gelijk blijft.

### 1.7.11 Geen actieve gevechtssessies

Een gevecht wordt volledig uitgevoerd binnen één API-request.

De applicatie ondersteunt geen:

- Handmatige acties per beurt.
- Pauzeren.
- Hervatten.
- Actieve sessies.
- Multiplayerinteractie.

---

## 1.8 Gevechtsresultaat

### 1.8.1 Resultaatgegevens

Na een gevecht retourneert de simulator een resultaat met minimaal:

| Eigenschap | Beschrijving |
|---|---|
| `winnerId` | ID van het winnende monster |
| `winnerName` | Naam van het winnende monster |
| `loserId` | ID van het verliezende monster |
| `totalTurns` | Aantal uitgevoerde aanvallen |
| `battleLog` | Chronologische gebeurtenissenlijst |

In PocketBattleAPI v1.0 telt iedere uitgevoerde aanval als één beurt.

### 1.8.2 Gevechtslog

Iedere gebeurtenis in de gevechtslog bevat:

- Beurtnummer.
- ID of naam van de aanvaller.
- ID of naam van de verdediger.
- Naam van de gebruikte aanval.
- Veroorzaakte schade.
- Resterende HP van de verdediger.

### 1.8.3 Voorbeeld gevechtsgebeurtenis

```json
{
  "turn": 1,
  "attackerId": 2,
  "defenderId": 1,
  "attackName": "Tackle",
  "damage": 8,
  "defenderRemainingHp": 32
}
```

Dit is een illustratie van de beoogde structuur.

De definitieve DTO-namen en JSON-velden worden tijdens het API-ontwerp vastgelegd.

### 1.8.4 Geen permanente gevechtsgeschiedenis

Gevechtsresultaten worden in versie 1.0 niet permanent opgeslagen.

De API retourneert het resultaat direct na afloop van de simulatie.

---

## 1.9 Monsterbeheer

### 1.9.1 CRUD-functionaliteit

PocketBattleAPI ondersteunt de volgende bewerkingen:

- Create.
- Read.
- Update.
- Delete.

### 1.9.2 REST-endpoints

| Functionaliteit | HTTP-methode | Endpoint |
|---|---|---|
| Alle monsters opvragen | GET | `/monsters` |
| Monster op ID opvragen | GET | `/monsters/{id}` |
| Monster aanmaken | POST | `/monsters` |
| Monster wijzigen | PUT | `/monsters/{id}` |
| Monster verwijderen | DELETE | `/monsters/{id}` |
| Monsters filteren op type | GET | `/monsters?type=FIRE` |
| Gevecht simuleren | POST | `/battles` |

### 1.9.3 Filteren

Gebruikers kunnen monsters filteren op elementtype.

Voorbeeld:

```http
GET /monsters?type=WATER
```

De API retourneert uitsluitend monsters van het type WATER.

### 1.9.4 Zoeken en sorteren

Zoeken op naam en sorteren op naam of level zijn mogelijke uitbreidingen.

Deze functionaliteiten zijn geen verplichte onderdelen van de eerste werkende versie.

### 1.9.5 API-communicatie

De REST API gebruikt JSON voor gestructureerde requests en responses.

De applicatie gebruikt passende HTTP-statuscodes, waaronder:

| Statuscode | Betekenis |
|---|---|
| 200 | Request succesvol |
| 201 | Resource aangemaakt |
| 204 | Request succesvol, zonder response body |
| 400 | Ongeldige invoer |
| 404 | Resource niet gevonden |
| 409 | Conflict |
| 500 | Onverwachte serverfout |

De exacte foutresponsstructuur wordt later vastgelegd.

---

## 1.10 Gegevensopslag en persistentie

### 1.10.1 Gefaseerde ontwikkeling

De gegevensopslag wordt in twee stappen ontwikkeld.

**Fase A — In-memory opslag**

De applicatie gebruikt eerst een repository die monsters in het geheugen bewaart.

Voordelen:

- Eenvoudig te implementeren.
- Geen databaseconfiguratie nodig.
- Geschikt voor het testen van businesslogica.
- Maakt de architectuur inzichtelijk.

De gegevens verdwijnen wanneer de applicatie opnieuw wordt gestart.

**Fase B — Persistente H2-opslag**

Later wordt de in-memory repository vervangen door een implementatie met H2.

De gegevens worden opgeslagen in een lokaal databasebestand.

### 1.10.2 Databasekeuzes

| Onderdeel | Keuze |
|---|---|
| Database | H2 |
| Opslagmodus | File-based |
| Databasebibliotheek | JetBrains Exposed |
| Repository-interface | `MonsterRepository` |
| Eerste implementatie | `InMemoryMonsterRepository` |
| Tweede implementatie | `H2MonsterRepository` |
| Databaseconfiguratie | `DatabaseFactory` |

Er wordt bewust geen uitsluitend tijdelijke in-memory H2-database gebruikt als definitieve persistente opslag.

### 1.10.3 Repositorypatroon

Beide opslagimplementaties gebruiken dezelfde interface.

Conceptueel:

```text
MonsterService
      |
      v
MonsterRepository
      |
      +-- InMemoryMonsterRepository
      |
      +-- H2MonsterRepository
```

De service kent de repositoryinterface, maar hoeft niet te weten welke opslagimplementatie wordt gebruikt.

### 1.10.4 Dependency Injection

De repository wordt als afhankelijkheid aan de service geleverd.

Hierdoor kan de opslagimplementatie worden vervangen zonder de businesslogica fundamenteel te wijzigen.

Dit ondersteunt:

- Testbaarheid.
- Onderhoudbaarheid.
- Uitbreidbaarheid.
- Scheiding van verantwoordelijkheden.

---

## 1.11 Softwarearchitectuur

### 1.11.1 Architectuurstijl

PocketBattleAPI gebruikt een feature-based projectstructuur.

De belangrijkste features zijn:

- Monsterbeheer.
- Gevechtssimulatie.

Daarnaast zijn er gedeelde onderdelen voor:

- Databaseconfiguratie.
- Foutafhandeling.
- Ktor-configuratie.

### 1.11.2 Logische lagen

| Laag | Verantwoordelijkheid |
|---|---|
| Routes | HTTP-requests verwerken |
| DTO's | API-invoer en -uitvoer beschrijven |
| Services | Businessprocessen coördineren |
| Models | Domeinobjecten en spelregels representeren |
| Repositories | Gegevens ophalen en opslaan |
| Database | Persistente opslag verzorgen |

### 1.11.3 Voorlopige projectstructuur

```text
PocketBattleAPI/
├── docs/
│   └── diagrams/
├── resources/
├── src/
│   ├── Application.kt
│   ├── Routing.kt
│   └── nl/
│       └── pocketbattle/
│           └── api/
│               ├── common/
│               │   └── error/
│               ├── database/
│               ├── features/
│               │   ├── monster/
│               │   │   ├── dto/
│               │   │   ├── model/
│               │   │   ├── repository/
│               │   │   ├── routes/
│               │   │   └── service/
│               │   └── battle/
│               │       ├── dto/
│               │       ├── model/
│               │       ├── routes/
│               │       └── service/
│               └── plugins/
├── module.yaml
├── kotlin
└── kotlin.bat
```

Dit is de beoogde structuur.

De bestaande Ktor-entrypoint en Amper-configuratie blijven behouden totdat een expliciete refactor wordt uitgevoerd.

### 1.11.4 Ontwerpprincipes

**Encapsulation**

Objecten beschermen hun eigen toestand.

Een monster mag bijvoorbeeld geen negatieve HP krijgen.

**Single Responsibility Principle**

Iedere klasse heeft een duidelijk afgebakende verantwoordelijkheid.

**Separation of Concerns**

HTTP-verwerking, businesslogica en gegevensopslag blijven gescheiden.

**Dependency Inversion**

Services zijn afhankelijk van abstracties, zoals repositoryinterfaces.

**Testability**

Businesslogica kan onafhankelijk van Ktor en de database worden getest.

**Pure Functions**

Berekeningen zoals schadebepaling worden waar mogelijk als pure functies ontworpen.

---

## 1.12 Teststrategie

### 1.12.1 Unit tests

Unit tests controleren individuele onderdelen van de applicatie.

Voorbeelden:

- Monstergegevens valideren.
- HP aanpassen.
- Type-effectiviteit bepalen.
- Schade berekenen.
- Startvolgorde bepalen.
- Aanvallen afwisselen.
- Winnaar bepalen.
- Gevechten beëindigen.

### 1.12.2 Repositorytests

Repositorytests controleren:

- Monsters toevoegen.
- Monsters opvragen.
- Monsters wijzigen.
- Monsters verwijderen.
- Filteren op type.

De in-memory en H2-repositories moeten dezelfde contractuele functionaliteit ondersteunen.

### 1.12.3 Integratietests

Ktor-integratietests controleren:

- REST-endpoints.
- JSON-serialisatie.
- HTTP-statuscodes.
- Requestvalidatie.
- Foutafhandeling.
- Gevechtssimulaties via de API.

### 1.12.4 Reproduceerbaarheid

Omdat de gevechtssimulator deterministisch werkt, moeten tests met dezelfde invoer steeds dezelfde resultaten opleveren.

Er is geen willekeurige schadefactor.

Er is geen willekeurige aanvalsselectie.

Er is geen willekeurige startvolgorde.

---

## 1.13 Scope en afbakening

### 1.13.1 Binnen scope

De volgende functionaliteiten behoren tot PocketBattleAPI v1.0:

- Monsterbeheer via CRUD.
- Drie elementtypes.
- HP, Level, Defense en Speed.
- Twee aanvallen per monster.
- Automatische aanvalsselectie.
- Volledig automatische gevechten.
- Startvolgorde op basis van Speed.
- Deterministische schadeberekening.
- Elementaire type-effectiviteit.
- Winnaar bepalen.
- Gedetailleerde gevechtslog.
- REST API met JSON.
- In-memory opslag.
- Persistente H2-opslag.
- Repositorypatroon.
- Unit tests.
- Repositorytests.
- Integratietests.
- UML-diagrammen.
- Technische documentatie.

### 1.13.2 Buiten scope

De volgende functionaliteiten worden niet ontwikkeld in versie 1.0:

- Frontend.
- Grafische gebruikersinterface.
- Gebruikersaccounts.
- Authenticatie.
- Autorisatie.
- Multiplayer.
- Online matchmaking.
- Handmatige aanvalskeuzes.
- Actieve gevechtssessies.
- Gevechten pauzeren of hervatten.
- Experience points.
- Automatische level-ups.
- Monsterevoluties.
- Inventaris.
- Items.
- Status-effecten.
- Kritieke aanvallen.
- Willekeurige schade.
- Willekeurige aanvalsselectie.
- Permanente gevechtsgeschiedenis.

Deze onderdelen kunnen eventueel in een latere versie worden overwogen.

---

## 1.14 Ontwikkelfasering

De implementatie wordt gefaseerd uitgevoerd.

### Fase 1 — Kotlin en domeinlogica

Onderwerpen:

- Kotlin-fundamentals.
- Klassen en objecten.
- Overerving.
- Abstracte klassen.
- Interfaces.
- Data classes.
- Enums.
- Encapsulatie.
- Schadeberekening.
- BattleService.
- Unit tests.

**Mijlpaal 1:**

Een werkende, onafhankelijk testbare gevechtssimulator zonder afhankelijkheid van HTTP of een database.

### Fase 2 — Monsterbeheer en repositories

Onderwerpen:

- Collections.
- Generics.
- Repositoryinterfaces.
- In-memory repositories.
- MonsterService.
- CRUD.
- Null safety.
- Lambdas.
- Higher-order functions.
- Functionele collectieoperaties.
- Repository- en servicetests.

### Fase 3 — Ktor REST API

Onderwerpen:

- HTTP.
- REST.
- Ktor-routing.
- DTO's.
- JSON-serialisatie.
- Monster-endpoints.
- Battle