// src/nl/brul/api/features/report/model/DuplicateCandidate.kt
package nl.brul.api.features.report.model

/**
 * Laag: model (het domein).
 *
 * Een bestaande melding die misschien over dezelfde situatie gaat als de melding die iemand nu
 * wil maken (D1 t/m D6, zie DuplicateRules).
 * - `distanceKm`: hoe ver de bestaande melding van de nieuwe ligt;
 * - `similarity`: hoeveel de woorden lijken, van 0.0 (niets gelijk) tot 1.0 (dezelfde woorden).
 *
 * De app toont deze meldingen met de vraag "Bedoel je deze? Stem er dan op." Zo komen er minder
 * dubbele meldingen in de database.
 */
data class DuplicateCandidate(
    val report: Report,
    val distanceKm: Double,
    val similarity: Double
) {
    init {
        require(distanceKm >= 0.0) {
            "Distance cannot be negative."
        }

        require(similarity in 0.0..1.0) {
            "Similarity must be between 0 and 1."
        }
    }
}
