// src/nl/brul/api/features/report/dto/SuggestionRequest.kt
package nl.brul.api.features.report.dto

import kotlinx.serialization.Serializable

/**
 * Laag: dto (de vertaler tussen JSON en Kotlin).
 *
 * De JSON voor POST /api/v1/reports/suggestion: de tekst die de gebruiker tot nu toe heeft
 * getypt. Minstens één van de twee moet tekst bevatten.
 * Voorbeeld: `{"title": "Fiets weg", "description": "Mijn fiets is vannacht gestolen"}`
 */
@Serializable
data class SuggestionRequest(
    val title: String = "",
    val description: String = ""
)
