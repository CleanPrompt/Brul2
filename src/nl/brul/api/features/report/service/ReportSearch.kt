// src/nl/brul/api/features/report/service/ReportSearch.kt
package nl.brul.api.features.report.service

import nl.brul.api.features.report.model.Location
import nl.brul.api.features.report.model.ReportCategory
import nl.brul.api.features.report.model.ReportType

/**
 * Laag: service (de regels).
 *
 * De zes manieren om zoekresultaten te sorteren:
 * ID_ASC, ID_DESC, NEAREST (dichtstbij, locatie verplicht), NEWEST, MOST_VIEWED en MOST_LIKED.
 * MOST_LIKED gebruikt de stemmen uit de feature vote.
 */
enum class ReportSort {
    ID_ASC,
    ID_DESC,
    NEAREST,
    NEWEST,
    MOST_VIEWED,
    MOST_LIKED
}

/**
 * Laag: service (de regels).
 *
 * Alle zoekwensen bij elkaar: zoekwoord, type, categorie, locatie, straal en sortering.
 * Controleert zichzelf in `init`: een straal moet positief en eindig zijn en vereist een locatie;
 * sorteren op NEAREST vereist ook een locatie.
 * Wordt in de route gevuld met de query-parameters en doorgegeven aan `ReportService.search()`.
 */
data class ReportSearch(
    val query: String? = null,
    val type: ReportType? = null,
    val category: ReportCategory? = null,
    val location: Location? = null,
    val radiusKm: Double? = null,
    val sort: ReportSort = ReportSort.ID_ASC
) {
    init {
        if (radiusKm != null) {
            require(radiusKm.isFinite() && radiusKm > 0) {
                "Search radius must be a positive finite number."
            }
            require(location != null) {
                "A location is required when filtering by radius."
            }
        }

        if (sort == ReportSort.NEAREST) {
            require(location != null) {
                "A location is required when sorting by distance."
            }
        }
    }
}