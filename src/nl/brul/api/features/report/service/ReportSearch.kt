// src/nl/brul/api/features/report/service/ReportSearch.kt
package nl.brul.api.features.report.service

import nl.brul.api.features.report.model.Location
import nl.brul.api.features.report.model.ReportCategory
import nl.brul.api.features.report.model.ReportType

enum class ReportSort {
    ID_ASC,
    ID_DESC,
    NEAREST,
    NEWEST,
    MOST_VIEWED
}

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