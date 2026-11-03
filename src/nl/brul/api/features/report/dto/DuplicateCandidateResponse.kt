// src/nl/brul/api/features/report/dto/DuplicateCandidateResponse.kt
package nl.brul.api.features.report.dto

import kotlinx.serialization.Serializable

/**
 * Laag: dto (de vertaler tussen JSON en Kotlin).
 *
 * Eén bestaande melding die misschien over hetzelfde gaat, met hoe ver weg die ligt (in hele
 * meters) en hoeveel de woorden lijken (0.0 tot 1.0, afgerond op twee decimalen).
 * De app kan dan vragen: "Bedoel je deze? Stem er dan op."
 * Wordt gemaakt met `DuplicateCandidate.toResponse()` in ReportMapper.kt.
 */
@Serializable
data class DuplicateCandidateResponse(
    val report: ReportResponse,
    val distanceMeters: Int,
    val similarity: Double
)
