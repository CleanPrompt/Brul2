// src/nl/brul/api/features/report/dto/UpdateReportRequest.kt
package nl.brul.api.features.report.dto

import kotlinx.serialization.Serializable

/**
 * Laag: dto (de vertaler tussen JSON en Kotlin).
 *
 * De JSON bij het wijzigen van een melding (PUT /api/v1/reports/{id}).
 * Dezelfde velden als CreateReportRequest, plus de nieuwe `status`.
 * `toReport(existing)` in ReportMapper.kt maakt hier het juiste soort Report van.
 */
@Serializable
data class UpdateReportRequest(
    val type: String,
    val title: String,
    val description: String,
    val category: String,
    val latitude: Double,
    val longitude: Double,
    val status: String,
    val urgent: Boolean? = null,
    val volunteersNeeded: Int? = null,
    // Help requests only: ISO-8601 in UTC, e.g. "2026-11-20T10:00:00Z".
    val startsAt: String? = null,
    val endsAt: String? = null
)
