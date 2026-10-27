// src/nl/brul/api/features/report/model/Report.kt
package nl.brul.api.features.report.model

/**
 * Laag: model (het domein).
 *
 * Abstracte ouder van alle meldingen. Bevat wat elke melding heeft:
 * id, titel, beschrijving, categorie, locatie, status, aanmaaktijd, aantal views en eigenaar.
 * In `init` staan de algemene regels: id groter dan 0, titel en beschrijving niet leeg, aanmaaktijd
 * groter dan 0, views niet negatief, eigenaar positief of leeg.
 *
 * Waarom abstract? Een "melding in het algemeen" bestaat niet; het is altijd een SafetyReport,
 * NuisanceReport of HelpRequest.
 * Waarom geen interface? Een interface kan geen gedeelde velden en geen gedeelde `init`-controle
 * hebben.
 * Alle velden zijn `val`: een melding verandert niet, een update maakt een nieuw object.
 */
abstract class Report(
    val id: Long,
    val title: String,
    val description: String,
    val category: ReportCategory,
    val location: Location,
    val status: ReportStatus = ReportStatus.OPEN,
    val createdAt: Long = System.currentTimeMillis(),
    val viewCount: Int = 0,
    val ownerId: Long? = null
) {
    init {
        require(id > 0) {
            "Report ID must be positive."
        }
        require(title.isNotBlank()) {
            "Report title cannot be blank."
        }
        require(description.isNotBlank()) {
            "Report description cannot be blank."
        }
        require(createdAt > 0) {
            "Creation timestamp must be positive."
        }
        require(viewCount >= 0) {
            "View count cannot be negative."
        }
        require(ownerId == null || ownerId > 0) {
            "Owner ID must be positive."
        }
    }

    /**
     * Welk soort melding dit is. Elke subklasse vult dit zelf in met `override`.
     */
    abstract val type: ReportType

    /**
     * Controleert of de categorie bij dit soort melding past.
     * Elke subklasse vult dit zelf in en roept het aan in zijn eigen `init`, niet hier in Report:
     * anders zou het draaien voordat de velden van de subklasse gezet zijn.
     */
    protected abstract fun validateCategory()
}