// src/nl/brul/api/features/report/dto/DuplicateCheckRequest.kt
package nl.brul.api.features.report.dto

import kotlinx.serialization.Serializable

/**
 * Laag: dto (de vertaler tussen JSON en Kotlin).
 *
 * De JSON voor POST /api/v1/reports/duplicates: de melding die de gebruiker wil maken, nog vóór
 * het opslaan. `category` mag weg als de gebruiker nog niets gekozen heeft.
 * Voorbeeld:
 * `{"type": "NUISANCE", "title": "Afval", "description": "Vuilniszakken bij de container",
 *   "latitude": 51.55, "longitude": 4.63, "category": "WASTE"}`
 * `toDuplicateQuery()` in ReportMapper.kt zet dit om naar een gecontroleerde DuplicateQuery.
 */
@Serializable
data class DuplicateCheckRequest(
    val type: String,
    val title: String = "",
    val description: String = "",
    val latitude: Double,
    val longitude: Double,
    val category: String? = null
)
