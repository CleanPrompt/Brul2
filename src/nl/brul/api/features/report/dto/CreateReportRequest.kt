// src/nl/brul/api/features/report/dto/CreateReportRequest.kt
package nl.brul.api.features.report.dto

import kotlinx.serialization.Serializable

/**
 * Laag: dto (de vertaler tussen JSON en Kotlin).
 *
 * De JSON die binnenkomt bij het aanmaken van een nieuwe melding (POST /api/v1/reports).
 * Alles staat hier nog als simpele tekst en getallen, bijvoorbeeld type = "SAFETY", en wordt nog
 * niet gecontroleerd.
 * `toNewReport()` in ReportMapper.kt zet dit om naar een gecontroleerd `NewReport`.
 *
 * `urgent` hoort alleen bij SAFETY, `volunteersNeeded` alleen bij HELP_REQUEST.
 * `@Serializable` laat Kotlin dit automatisch van en naar JSON omzetten.
 */
@Serializable
data class CreateReportRequest(
    val type: String,
    val title: String,
    val description: String,
    val category: String,
    val latitude: Double,
    val longitude: Double,
    val urgent: Boolean? = null,
    val volunteersNeeded: Int? = null,
    // Help requests only: ISO-8601 in UTC, e.g. "2026-11-20T10:00:00Z".
    val startsAt: String? = null,
    val endsAt: String? = null
)
