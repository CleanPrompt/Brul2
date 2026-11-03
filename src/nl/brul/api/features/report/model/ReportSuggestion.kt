// src/nl/brul/api/features/report/model/ReportSuggestion.kt
package nl.brul.api.features.report.model

/**
 * Laag: model (het domein).
 *
 * Een voorstel voor het type en de categorie van een nieuwe melding, gemaakt op basis van de
 * titel en de beschrijving die de gebruiker typt (S1).
 * Het is alleen een voorstel: de gebruiker bevestigt of kiest zelf iets anders, en daarna
 * controleert NewReport alles zoals altijd.
 *
 * `init` bewaakt dezelfde regels als NewReport (S2): de categorie moet bij het type passen en
 * `urgent` hoort alleen bij SAFETY. Zo kan ook de AI nooit een onmogelijke combinatie voorstellen;
 * een fout antwoord van de AI wordt gewoon geen voorstel.
 */
data class ReportSuggestion(
    val type: ReportType,
    val category: ReportCategory,
    val urgent: Boolean? = null,
    val source: Source
) {
    /** Wie het voorstel deed: de AI, of de vaste trefwoorden als de AI niet beschikbaar is. */
    enum class Source {
        AI,
        KEYWORDS
    }

    init {
        require(category in type.allowedCategories) {
            "Category $category does not belong to type $type."
        }

        require(type == ReportType.SAFETY || urgent == null) {
            "Only safety reports have an urgent value."
        }
    }
}
