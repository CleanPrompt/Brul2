# 🎬 DRAAIBOEK & SCRIPT: MONDELING ASSESSMENT BRUL2
**Module:** INFD-AD-B AI-Powered Software Development (Periode 1, 2026-2027)  
**Onderdeel:** Eerste 15 Minuten (Live Demonstratie + UML & Architectuur)  
**Totaalduur Presentatie:** Maximaal 15:00 minuten (daarna 30 minuten individuele vragen)  
**Locatie:** Avans Hogeschool Breda / 's-Hertogenbosch  

---

## 📋 1. Checklist Vooraf (10 Minuten voor de Deur)

Zorg dat deze 6 punten geregeld zijn **voordat** jullie naar binnen lopen:

1. **Welke laptop sluit aan?**  
   Kies **één** laptop die aangesloten wordt op het Smartboard via HDMI/USB-C (bijvoorbeeld de snelste laptop met werkende Ktor en IntelliJ setup).
2. **Oplader & Scherminstellingen:**  
   - Laptop aan de netstroom hangen (voorkomt batterijbesparing / throttling).
   - Schermresolutie instellen op **1920x1080 (100% schaling)** zodat IntelliJ en de slides scherp en groot leesbaar zijn voor de assessoren.
   - Windows meldingen op "Niet storen / Focus" zetten.
3. **De Applicatie alvast starten:**  
   - Open een terminal in `c:\Users\k.schouten\Documents\GitHub\Brul2`
   - Voer uit: `.\kotlin.bat run`
   - Controleer in de browser of `http://localhost:8080/api/v1/health` een `{"status":"ok"}` geeft.
4. **Presentatie openzetten:**  
   - Open [`docs/PRESENTATIE_SLIDES.md`](file:///c:/Users/k.schouten/Documents/GitHub/Brul2/docs/PRESENTATIE_SLIDES.md) of browserversie.
   - Druk op **`F11`** voor volledig scherm (Full-Screen).
5. **IntelliJ klaarzetten op het tweede scherm/tab:**  
   - Open het bestand [`demo_assessment.http`](file:///c:/Users/k.schouten/Documents/GitHub/Brul2/demo_assessment.http).
   - Zorg dat het projectvenster met de map `test/` zichtbaar is.
6. **Rollen & Beurtverdeling:**
   - **De Piloot:** Degene achter de laptop. Deze bedient muis/toetsenbord, schakelt tussen slides en klikt in IntelliJ. *De piloot klikt rustig terwijl de betreffende teamgenoot spreekt.*
   - **De Sprekers:** Phuc, Maikel, Kelvin, Joselyn en Natasja verdelen de slides en demo-stappen gelijkwaardig conform hun feature-pakketten.

---

## ⏱️ 2. Het Minuut-voor-Minuut Draaiboek (15:00 Min)

```mermaid
flowchart TD
    M1["Min 0:00 - 3:00<br/>Slide 1-3: Intro, Context & C4 Architectuur"] --> M2["Min 3:00 - 3:30<br/>Alt+Tab naar IntelliJ (Piloot)"]
    M2 --> M3["Min 3:30 - 4:15<br/>Run All Tests (296 groene tests)"]
    M3 --> M4["Min 4:15 - 9:00<br/>demo_assessment.http (Stappen 0 t/m 7)"]
    M4 --> M5["Min 9:00 - 9:15<br/>Alt+Tab terug naar Slides"]
    M5 --> M6["Min 9:15 - 13:00<br/>Slide 5-7: UML, State Machine, Error Handling"]
    M6 --> M7["Min 13:00 - 15:00<br/>Slide 8-9: AI Reflectie & Conclusie"]
```

---

### DEEL 1: INTRODUCTIE & ARCHITECTUUR (Min 0:00 – 3:00)

#### Minuut 0:00 – 1:00 | Slide 1: Titel & Teamverdeling
* **Actie Piloot:** Slide 1 staat in beeld.
* **Spreker: Phuc**
  > *"Goedemiddag assessoren. Welkom bij het assessment van onze projectgroep BRUL. Voor Periode 1 hebben wij met ons team van vijf studenten een modulaire, schaalbare Web API ontwikkeld in Kotlin met het Ktor Framework.*  
  > *Op deze eerste slide ziet u onze taak- en featureverdeling: om Separation of Concerns te waarborgen, heeft ieder van ons de volledige eigenaarschap genomen over een eigen feature-package: ikzelf deed Identity en Authenticatie, Maikel Incidenten en Meldingen, Kelvin Hulpvragen en Vrijwilligersparticipatie, Joselyn het Community Voting-systeem en Natasja Gebruikersbeheer en RBAC. We lichten nu kort de maatschappelijke context en onze architectuur toe."*

#### Minuut 1:00 – 2:00 | Slide 2: Probleem & Context (Klik $\rightarrow$)
* **Actie Piloot:** Druk op pijltje rechts ($\rightarrow$) naar Slide 2.
* **Spreker: Maikel**
  > *"Waarom BRUL? Veel traditionele buurt-apps fungeren helaas als online klaagmuren waar mensen alleen klagen over zwerfafval of geluidsoverlast. Met BRUL wilden we dat omdraaien: burgers kunnen niet alleen incidenten of veiligheidssituaties melden op een interactieve kaart, maar ook positieve actie ontketenen via concrete hulpvragen. Denk aan een oudere buurtgenoot die hulp nodig heeft met zware boodschappen of een buurttuin-opruimactie. Wijkbewoners kunnen elkaar direct vinden en helpen via een veilige, laagdrempelige API."*

#### Minuut 2:00 – 3:00 | Slide 3: Systeemarchitectuur & Lokale Persistentie (Klik $\rightarrow$)
* **Actie Piloot:** Druk op pijltje rechts ($\rightarrow$) naar Slide 3.
* **Spreker: Joselyn**
  > *"Om dit te realiseren hebben we gekozen voor Clean Architecture. Onze applicatie is strikt opgedeeld in lagen:*  
  > *1. De Transportlaag met Ktor 3 en Netty, voorzien van plugins voor ContentNegotiation, JWT Authenticatie en StatusPages.*  
  > *2. De Servicelaag waarin alle pure bedrijfsregels en validaties leven, volledig ontkoppeld van HTTP-statussen.*  
  > *3. De Persistentielaag waarin we JetBrains Exposed gebruiken als type-veilige SQL DSL. Onze services communiceren via abstracte Repository interfaces conform het Dependency Inversion Principe.*  
  > *4. En tot slot de database: we gebruiken een ingebedde H2 SQL database met lokale persistente opslag in `data/brul.mv.db`. Voor onze tests schakelt deze automatisch over naar een geïsoleerde in-memory instantie.*  
  > *Laten we direct naar IntelliJ overschakelen om te laten zien hoe dit in de praktijk werkt."*

---

### DEEL 2: LIVE TESTSUITE & USER FLOW (Min 3:00 – 9:00)
*Visueel: Piloot drukt op `Alt + Tab` naar IntelliJ IDEA*

#### Minuut 3:00 – 4:15 | De Geautomatiseerde Testsuite
* **Actie Piloot:**
  1. Klik met de **rechtermuisknop** op de hoofdmap `test/` in het projectoverzicht.
  2. Klik op **"Run 'All Tests'"**.
  3. Wacht enkele seconden terwijl de balk groen volloopt met **296 geslaagde tests**.
* **Spreker: Natasja**
  > *"Voordat we de API handmatig demonstreren, tonen we onze geautomatiseerde testsuite. U ziet hier maar liefst 296 unit- en integratietests over al onze packages. De tests draaien tegen een geïsoleerde H2 in-memory database en dekken zowel alle happy flows, autorisatieregels, grenswaarden als concurrency af. Alles is 100% groen."*

#### Minuut 4:15 – 9:00 | De End-to-End User Flow (`demo_assessment.http`)
* **Actie Piloot:** Open het bestand `demo_assessment.http`. Loop stapsgewijs langs de genummerde play-knoppen.

##### Stap 0 & 1: Health Check & Authenticatie (Min 4:15 – 5:00)
* **Actie Piloot:** Klik op het groene pijltje bij **Stap 0** (`GET /health`), daarna **Stap 1a** (Login Anastasia) en **Stap 1b** (Login Bob).
* **Spreker: Phuc**
  > *"Stap 0 toont onze health check: status 'ok'.*  
  > *Bij Stap 1 loggen we in met twee wijkbewoners: Anastasia en Bob. Wachtwoorden worden geverifieerd tegen gehashte BCrypt-records met veilige salt. Ktor genereert direct een cryptografisch ondertekend JWT Bearer-token met claims voor hun rol en userId. Ons script slaat deze tokens automatisch op als globale variabelen voor de vervolgstappen."*

##### Stap 2: Incident & Hulpvraag plaatsen (Min 5:00 – 5:45)
* **Actie Piloot:** Klik op het groene pijltje bij **Stap 2** (`POST /reports` als Anastasia).
* **Spreker: Maikel**
  > *"Anastasia plaatst nu een nieuwe Hulpvraag: 'Boodschappenhulp gezocht'. Omdat ze slecht ter been is vraagt ze om twee vrijwilligers. Het endpoint accepteert geografische coördinaten en valideert dat de categorie 'GROCERIES' is toegestaan voor een HelpRequest. De server retourneert HTTP 201 Created met het gegenereerde ID."*

##### Stap 3: Vrijwilligersparticipatie & Capaciteit (Min 5:45 – 7:00)
* **Actie Piloot:** 
  1. Klik op **Stap 3a** (`GET /reports/{id}/participants`).
  2. Klik op **Stap 3b** (`POST /reports/{id}/participants` met `bobToken`).
  3. Klik op **Stap 3c** (`GET /reports/{id}/participants`).
  4. Klik op **Stap 3d** (`GET /reports/{id}/participants/list`).
* **Spreker: Kelvin**
  > *"In features/help zien we nu de vrijwilligersparticipatie in actie.*  
  > *Bij 3a roept Bob de publieke deelnamestatus op: volunteersNeeded is 2, participantCount is 0, en status is OPEN.*  
  > *Bij 3b meldt Bob zich aan met zijn JWT-token. De service controleert of hij niet al meedoet en of er capaciteit is, en Exposed schrijft een koppeling in 'help_participations'.*  
  > *Als we bij 3c de status opnieuw opvragen, zien we direct dynamische toestandswijziging: participantCount is 1, availablePlaces is verlaagd naar 1, en de status blijft OPEN.*  
  > *Bij 3d zien we in de deelnemerslijst dat Bob netjes als vrijwilliger geregistreerd staat."*

##### Stap 4: Negatieve Test & Domeinvalidatie (Min 7:00 – 7:45)
* **Actie Piloot:** Klik op het groene pijltje bij **Stap 4** (`POST /reports/{id}/participants` met `anastasiaToken`).
* **Spreker: Kelvin**
  > *"We demonstreren onze defensieve validatie met een negatieve test. Anastasia is de eigenaar van deze hulpvraag. Als zij zichzelf probeert in te schrijven als vrijwilliger, weigert onze service dit en gooit een HelpParticipationForbiddenException.*  
  > *Zoals u ziet in de response: Ktor StatusPages vangt dit centraal op en retourneert keurig HTTP 403 Forbidden met de JSON error 'Owner cannot join their own help request'. Geen servercrashes, maar nette domeinvalidatie."*

##### Stap 5: Community Voting & Prioritering (Min 7:45 – 8:15)
* **Actie Piloot:** Klik op **Stap 5a** (Bob brengt stem LIKE uit) en daarna **Stap 5b** (`GET /reports/{id}/votes`).
* **Spreker: Joselyn**
  > *"Bij stap 5 brengt Bob een positieve stem uit via PUT /reports/{id}/vote met type LIKE. Onze vote-engine herrekent direct de score en totalen. Bij 5b zien we in de vote-summary dat de score 1 is geworden, waardoor deze melding automatisch hoger scoort bij sortering op populariteit."*

##### Stap 6: Admin Beheer & RBAC Autorisatie (Min 8:15 – 8:45)
* **Actie Piloot:** Klik op **Stap 6a** (Admin login) en daarna **Stap 6b** (`GET /users` met `adminToken`).
* **Spreker: Natasja**
  > *"Bij stap 6 logt de beheerder in met rol ADMIN. Via RBAC-autorisatie heeft alleen deze rol toegang tot het /users endpoint. Gewone gebruikers krijgen hier conform onze beveiligingsregels direct een 403 Forbidden."*

##### Stap 7: Volledige Database Testdata Tonen (Min 8:45 – 9:00)
* **Actie Piloot:** Klik op **Stap 7a** (`GET /reports`) en **Stap 7b** (`GET /users`).
* **Spreker: Phuc & Maikel**
  > *Phuc: "Tot slot tonen we bij stap 7 de volledige breedte van onze database: bij 7a halen we alle meldingen op. U ziet in de console en in de response alle drie de domeintypes terug: Safety, Nuisance en HelpRequests met gevarieerde categorieën en statussen.*  
  > *Maikel: "En bij 7b ziet u alle geregistreerde bewoners en beheerders. Hiermee is onze hele dataset direct controleerbaar."*

---

### DEEL 3: UML & ARCHITECTUURDIEPGANG (Min 9:00 – 13:00)
*Visueel: Piloot drukt op `Alt + Tab` terug naar de presentatie*

#### Minuut 9:00 – 10:30 | Slide 5: Klassendiagram & Klassenrelaties (Klik $\rightarrow$)
* **Actie Piloot:** Navigeer naar Slide 5 ("Domeinontwerp & Klassenrelaties").
* **Sprekers: Maikel & Kelvin**
  > *Maikel: "Terug naar onze architectuur. In ons UML-klassendiagram ziet u de drie klassenrelaties duidelijk terug. Allereerst Overerving: HelpRequest, SafetyReport en NuisanceReport erven over van de abstracte klasse Report. Dit voorkomt codeduplicatie en maakt polymorfe afhandeling mogelijk."*  
  > *Kelvin: "Daarnaast Compositie: Location is een Value Object dat onlosmakelijk verbonden is met een Report. En tot slot Associatie: HelpParticipation fungeert als de associatieve entiteit die een User en een HelpRequest aan elkaar koppelt."*

#### Minuut 10:30 – 12:00 | Slide 6: State Machine & Exposed Integriteit (Klik $\rightarrow$)
* **Actie Piloot:** Navigeer naar Slide 6 ("Toestandsmachine & Database-Integriteit").
* **Sprekers: Kelvin & Joselyn**
  > *Kelvin: "Op serviceniveau implementeren we een duidelijke State Machine: een hulpvraag start als OPEN. Zodra de capaciteit door aanmeldingen vol raakt, berekent een Kotlin when-expressie direct de toestand FULL. Wordt de taak afgerond of gesloten, dan wordt hij CLOSED."*  
  > *Joselyn: "Op databaseniveau in Exposed waarborgen we integriteit met twee garanties: ReferenceOption.CASCADE zorgt dat bij het verwijderen van een melding of gebruiker alle bijbehorende stemmen en participaties automatisch worden opgeruimd. Bovendien voorkomt een samengestelde uniqueIndex op (helpRequestId, userId) dat iemand zich dubbel inschrijft."*

#### Minuut 12:00 – 13:00 | Slide 7: Centrale Error Handling (Klik $\rightarrow$)
* **Actie Piloot:** Navigeer naar Slide 7 ("Centrale Foutafhandeling").
* **Spreker: Natasja**
  > *"Hier ziet u onze filosofie rondom error handling. In plaats van vervuilende try-catches in elke route handler, gebruiken we de Ktor StatusPages plugin. De services gooien puur semantische Kotlin runtime exceptions. StatusPages mapt deze centraal naar een gestandaardiseerd ApiError DTO met passende HTTP-statuscodes. Daardoor blijven onze routes extreem beknopt en leesbaar."*

---

### DEEL 4: AI VERANTWOORDING & OVERDRACHT (Min 13:00 – 15:00)

#### Minuut 13:00 – 14:15 | Slide 8: AI Verantwoording (Klik $\rightarrow$)
* **Actie Piloot:** Navigeer naar Slide 8 ("Verantwoording Inzet AI-Tooling").
* **Sprekers: Joselyn & Phuc**
  > *Joselyn: "Conform de Avans leeruitkomsten hebben we gereflecteerd op ons AI-gebruik. Wij hanteerden een '2-sporen model': AI werd gebruikt als interactieve sparringpartner en examinator om randvoorwaarden en theoretische ontwerpkeuzes te toetsen, nooit als blind kopieerapparaat.*  
  > *Phuc: "We merkten al snel dat AI regelmatig verouderde Java-patronen voorstelde, zoals omslachtige formatters in plaats van idiomatische Kotlin templates. Door elke suggestie handmatig te reviewen en te borgen met geautomatiseerde tests, heeft ieder teamlid zijn of haar onderdeel volledig zelfstandig in de vingers gekregen."*

#### Minuut 14:15 – 15:00 | Slide 9: Conclusie & Vragenronde (Klik $\rightarrow$)
* **Actie Piloot:** Navigeer naar Slide 9 ("Klaar voor de Dieptevragen"). Stop de timer.
* **Spreker: Natasja / Gezamenlijk**
  > *"Samenvattend: BRUL beschikt over 296 geslaagde tests, een schaalbare Clean Architecture met Ktor en Exposed, en lokale persistente opslag. De backend is volledig gereed om in Periode 2 verbonden te worden met onze native Android Jetpack Compose app.*  
  > *Wij danken u voor uw aandacht en staan klaar voor uw individuele vragen!"*

---

## 🚨 3. Noodplan (Wat als er iets tegenzit?)

* **Situatie A: Poort 8080 is al bezet (Port already in use error)**  
  *Oplossing:* Open PowerShell en typ:  
  `Get-Process -Id (Get-NetTCPConnection -LocalPort 8080).OwningProcess | Stop-Process -Force`  
  Start daarna direct opnieuw met `.\kotlin.bat run`.
* **Situatie B: H2 database is gelocked (bestand in gebruik)**  
  *Oplossing:* Sluit eventuele andere openstaande terminals of database viewers. De database `data/brul.mv.db` mag maar door één proces tegelijk geopend worden.
* **Situatie C: Smartboard toont geen beeld via HDMI**  
  *Oplossing:* Druk op **`Windows-toets + P`** en selecteer **"Dupliceren"** (Duplicate).
* **Situatie D: Internet valt weg**  
  *Geen probleem!* Zeg gerust met een glimlach: *"Geen zorgen, onze hele stack draait 100% lokaal op Netty en embedded H2, dus we hebben geen actieve internetverbinding nodig."* (Scoort direct punten voor robuustheid!).
