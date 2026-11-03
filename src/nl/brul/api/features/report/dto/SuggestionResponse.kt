// src/nl/brul/api/features/report/dto/SuggestionResponse.kt
package nl.brul.api.features.report.dto

import kotlinx.serialization.Serializable

/**
 * Laag: dto (de vertaler tussen JSON en Kotlin).
 *
 * Het voorstel dat terug gaat naar de app, bijvoorbeeld
 * `{"type": "SAFETY", "category": "THEFT", "urgent": false, "source": "AI"}`.
 * `urgent` is alleen gevuld bij SAFETY. `source` zegt of de AI of de trefwoorden het voorstel
 * deden. De app vult hiermee het formulier alvast in; de gebruiker kan het nog aanpassen.
 * Wordt gemaakt met `ReportSuggestion.toResponse()` in ReportMapper.kt.
 */
@Serializable
data class SuggestionResponse(
    val type: String,
    val category: String,
    val urgent: Boolean? = null,
    val source: String
)
