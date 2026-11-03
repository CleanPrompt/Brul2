// src/nl/brul/api/features/report/model/DuplicateQuery.kt
package nl.brul.api.features.report.model

/**
 * Laag: model (het domein).
 *
 * De melding die iemand wil maken, maar nog niet heeft opgeslagen: genoeg om te zoeken naar
 * bestaande meldingen over dezelfde situatie.
 * `category` mag leeg zijn: de gebruiker heeft misschien nog niets gekozen.
 * `init` zorgt dat er tekst is om mee te vergelijken en dat een gekozen categorie bij het type
 * past.
 */
data class DuplicateQuery(
    val type: ReportType,
    val title: String,
    val description: String,
    val location: Location,
    val category: ReportCategory? = null
) {
    init {
        require(title.isNotBlank() || description.isNotBlank()) {
            "A title or description is needed to look for duplicates."
        }

        require(category == null || category in type.allowedCategories) {
            "Category $category does not belong to type $type."
        }
    }

    /** Titel en beschrijving samen: de tekst die vergeleken wordt. */
    val text: String
        get() = "$title $description"
}
