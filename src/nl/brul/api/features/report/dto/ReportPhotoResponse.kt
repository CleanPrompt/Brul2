// src/nl/brul/api/features/report/dto/ReportPhotoResponse.kt
package nl.brul.api.features.report.dto

import kotlinx.serialization.Serializable

/**
 * Laag: dto (de vertaler tussen JSON en Kotlin).
 *
 * De JSON voor één foto. De afbeelding zelf zit er niet in: die haal je op via `url`
 * (GET /api/v1/photos/{id}). Zo blijft een lijst met foto's klein en snel.
 * `latitude` en `longitude` zijn de plek waar de foto genomen is; `locationSource` zegt of die
 * uit de foto zelf komt (PHOTO) of van de app (APP). Zonder locatie zijn ze alle drie `null`.
 * Wordt gemaakt met `ReportPhoto.toResponse()` in ReportMapper.kt.
 */
@Serializable
data class ReportPhotoResponse(
    val id: Long,
    val reportId: Long,
    val uploaderId: Long? = null,
    val contentType: String,
    val sizeBytes: Long,
    val createdAt: Long,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val locationSource: String? = null,
    val url: String
)
