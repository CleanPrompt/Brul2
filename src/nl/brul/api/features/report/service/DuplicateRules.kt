// src/nl/brul/api/features/report/service/DuplicateRules.kt
package nl.brul.api.features.report.service

import java.time.Duration
import java.time.Instant
import nl.brul.api.features.report.model.DuplicateCandidate
import nl.brul.api.features.report.model.DuplicateQuery
import nl.brul.api.features.report.model.Report
import nl.brul.api.features.report.model.ReportStatus

/**
 * Laag: service (de regels).
 *
 * De regels om dubbele meldingen te herkennen. Stap 1 (D1 t/m D4) beslist welke bestaande
 * meldingen het überhaupt kunnen zijn:
 *
 * - D1: hetzelfde type melding
 * - D2: nog niet opgelost (RESOLVED telt niet mee)
 * - D3: binnen 250 meter (Location.distanceTo)
 * - D4: niet ouder dan 14 dagen
 *
 * Stap 2 kiest daaruit de echte dubbele meldingen. Dat doet de AI (D6, zie ReportAssistantAi).
 * Is die er niet, dan geldt D5:
 *
 * - D5: de woorden lijken genoeg op elkaar (similarity minstens 0,25), of de melding ligt binnen
 *   50 meter én heeft dezelfde categorie
 *
 * Allemaal pure functies: de huidige tijd komt binnen als `now`, zodat je de regels met een vast
 * moment kunt testen.
 */
object DuplicateRules {

    /** D3: hoe ver een dubbele melding maximaal weg mag liggen. */
    const val MAX_DISTANCE_KM = 0.25

    /** D4: hoe oud een dubbele melding maximaal mag zijn. */
    val MAX_AGE: Duration = Duration.ofDays(14)

    /** D5: zo dichtbij, met dezelfde categorie, telt ook zonder gelijke woorden. */
    const val VERY_CLOSE_KM = 0.05

    /** D5: hoeveel de woorden minstens moeten lijken. */
    const val MIN_SIMILARITY = 0.25

    /** Meer kandidaten laten we de gebruiker niet zien. */
    const val MAX_RESULTS = 5

    /**
     * D1 t/m D4: de meldingen die het kunnen zijn, met hun afstand en hoeveel de woorden lijken.
     * Gesorteerd: meest gelijke tekst eerst, dan dichtstbij, dan het laagste id (voorspelbaar).
     */
    fun candidates(
        query: DuplicateQuery,
        reports: List<Report>,
        now: Instant
    ): List<DuplicateCandidate> {
        val oldestAllowed = now.minus(MAX_AGE).toEpochMilli()

        return reports
            .filter { it.type == query.type }                  // D1
            .filter { it.status != ReportStatus.RESOLVED }     // D2
            .filter { it.createdAt >= oldestAllowed }          // D4
            .map { report ->
                DuplicateCandidate(
                    report = report,
                    distanceKm = report.location.distanceTo(query.location),
                    similarity = similarity(query.text, "${report.title} ${report.description}")
                )
            }
            .filter { it.distanceKm <= MAX_DISTANCE_KM }       // D3
            .sortedWith(
                compareByDescending<DuplicateCandidate> { it.similarity }
                    .thenBy { it.distanceKm }
                    .thenBy { it.report.id }
            )
    }

    /** D5: zonder AI, is dit waarschijnlijk dezelfde situatie? */
    fun isLikelyDuplicate(query: DuplicateQuery, candidate: DuplicateCandidate): Boolean {
        val sameCategoryVeryClose =
            query.category != null &&
                    candidate.report.category == query.category &&
                    candidate.distanceKm <= VERY_CLOSE_KM

        return candidate.similarity >= MIN_SIMILARITY || sameCategoryVeryClose
    }

    /**
     * Hoeveel twee teksten op elkaar lijken: het aantal gedeelde woorden gedeeld door alle
     * verschillende woorden samen (de Jaccard-index). 0.0 is niets gemeen, 1.0 is gelijk.
     * Voorbeeld: "fiets gestolen station" en "gestolen fiets bij het station" geeft 1.0, want
     * "bij" en "het" tellen niet mee (stopwoorden).
     */
    fun similarity(first: String, second: String): Double {
        val a = TextWords.meaningful(first)
        val b = TextWords.meaningful(second)

        if (a.isEmpty() || b.isEmpty()) {
            return 0.0
        }

        return (a intersect b).size.toDouble() / (a union b).size
    }
}
