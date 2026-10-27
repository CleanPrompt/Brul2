---
marp: true
theme: default
paginate: true
header: 'BRUL — Digitale Buurtapp | Assessment Periode 1'
footer: 'Avans Hogeschool — HBO-ICT Jaar 2'
style: |
  section {
    background-color: #0f172a;
    color: #f8fafc;
    font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
  }
  h1, h2, h3 {
    color: #ef4444;
  }
  a {
    color: #38bdf8;
  }
  code {
    background-color: #1e293b;
    color: #f59e0b;
  }
---

# BRUL — Digitale Buurtapp
### Mondeling Assessment Ktor Web API (Periode 1)

**Teamleden & Pakketten:**
- **Phuc:** `features/auth` (Identity & JWT Authenticatie)
- **Maikel:** `features/report` (Incidents & Geo-reports)
- **Kelvin:** `features/help` (Hulpvragen & Vrijwilligersparticipatie)
- **Joselyn:** `features/vote` (Community Voting & Score Engine)
- **Natasja:** `features/user` (User Management & RBAC)

---

## Waarom BRUL?
### Het Buurtplatform van Morgen

- **🚨 Veiligheid & Overlast:** Direct verdachte situaties of overlast melden en geografisch ontsluiten op de kaart (`SafetyReport`, `NuisanceReport`).
- **🤝 Sociale Cohesie (Hulpvragen):** Buurtbewoners helpen elkaar. Concrete hulpvragen plaatsen met dynamische vrijwilligerscapaciteit (`HelpRequest`).
- **👍 Democratisch Beheer:** Upvotes en downvotes bepalen de wijkprioriteiten (`VoteScore`).

---

## Gelaagde Architectuur & Clean Design

1. **Transport & Routing:** Ktor 3.x (Netty Engine)
   - Routing DSL, StatusPages, ContentNegotiation, JWT Authenticate.
2. **Domein & Bedrijfslogica:** Services
   - Pure Kotlin bedrijfsregels (`HelpParticipationService`, `ReportService`).
3. **Persistentielaag:** JetBrains Exposed (SQL DSL)
   - Interface contracts (`HelpParticipationRepository`) conform DIP.
4. **Database Engine:** Embedded H2 SQL Database
   - Lokale persistente opslag in `../data/brul.mv.db` (en `jdbc:h2:mem` voor tests).

---

## ⚡ Live Demonstratie in IntelliJ

### Schakel over naar IntelliJ IDEA:
1. **Run All Tests:**
   - Demonstratie van ~35 geautomatiseerde unittests en integratietests.
2. **End-to-End User Flow (`demo_assessment.http`):**
   - Inloggen als burgers (JWT Token creatie).
   - Melding aanmaken (Hulpvraag voor 2 vrijwilligers).
   - Vrijwilliger aanmelden & dynamische capaciteitsafname (`201 Created`).
   - Negatieve flow: Eigenaar claimt eigen taak (`403 Forbidden`).
   - Voting & Admin beheer (`RBAC`).

---

## Domeinontwerp & Klassenrelaties (UML)

- **1. Overerving (IS-A):**
  - `HelpRequest`, `SafetyReport` en `NuisanceReport` erven over van de `abstract class Report`.
  - Hergebruik van velden, polymorfe afhandeling en dynamische categorievalidatie.
- **2. Compositie (PART-OF):**
  - `Location(latitude, longitude)` is een onlosmakelijk Value Object binnen `Report`.
- **3. Associatie:**
  - `HelpParticipation` verbindt `User` en `HelpRequest` als associatieve entiteit via unieke sleutels.

---

## State Machine & Database-Integriteit

### 1. Capaciteit State Machine in Service:
- `OPEN` $\rightarrow$ Beschikbare plekken $> 0$
- `FULL` $\rightarrow$ Beschikbare plekken $== 0$
- `CLOSED` $\rightarrow$ Rapport gesloten door eigenaar/moderator
- Berekend met `coerceAtLeast(0)` en pure Kotlin `when` expressie.

### 2. Exposed Database Garanties:
- `ReferenceOption.CASCADE` op foreign keys (geen zwevende wees-records).
- Samengestelde `uniqueIndex(helpRequestId, userId)` tegen dubbele inschrijvingen.

---

## Centrale Error Handling met StatusPages

- **Geen vervuilende `try-catch` blokken** in routing handlers.
- **Servicelaag gooit getypeerde domeinexceptions:**
  - `HelpParticipationForbiddenException` $\rightarrow$ `403 Forbidden`
  - `HelpParticipationConflictException` $\rightarrow$ `409 Conflict`
  - `HelpRequestNotFoundException` $\rightarrow$ `404 Not Found`
- **Eenduidig JSON Error Contract:**
  - Clients ontvangen altijd gestandaardiseerde `{"error": "boodschap"}` payloads.

---

## Verantwoording Inzet AI-Tooling

- **Het 2-Sporen Model:**
  - AI (Gemini / Antigravity) ingezet als sparringpartner voor theorie en het formuleren van scherpe examenvragen.
- **Kritische Verificatie:**
  - Verouderde Java-patronen (zoals `String.format`) direct herkend en herschreven naar natuurlijke Kotlin syntax.
- **Geleerde Les:**
  - *"AI is een kennisversneller, geen kennisvervanger."* Zonder grondige unittests en geïsoleerde minitestjes creëer je direct technische schuld.

---

## Conclusie & Vragenronde

- ✅ **100% Groene Testsuite** (~35 unit- & integratietests)
- ✅ **Clean Architecture** met Ktor & Exposed
- ✅ **Lokale Persistente Opslag** via embedded H2
- 🚀 **Gereed voor Periode 2:** Android Native Client met Jetpack Compose!

*Hartelijk dank voor uw aandacht. Wij staan klaar voor uw individuele vragen!*
