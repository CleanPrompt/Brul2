// src/nl/brul/api/features/report/dto/ReportPhotoResponse.kt
package nl.brul.api.features.report.dto

import kotlinx.serialization.Serializable
import nl.brul.api.features.report.model.ReportPhoto

/**
 * Laag: dto (de vertaler tussen JSON en Kotlin).
 *
 * De JSON met de gegevens van één foto. De afbeelding zelf zit er niet in: die haal je op via
 * `url` (GET /api/v1/photos/{id}). Zo blijft een lijst met foto's klein en snel.
 */
@Serializable
data class ReportPhotoResponse(
    val id: Long,
    val reportId: Long,
    val uploaderId: Long? = null,
    val kind: String,
    val contentType: String,
    val sizeBytes: Long,
    val createdAt: String,
    /** Relatieve URL die de afbeelding zelf teruggeeft. */
    val url: String
)

/** Zet een ReportPhoto om naar de JSON hierboven en bouwt de `url`. */
fun ReportPhoto.toResponse(): ReportPhotoResponse =
    ReportPhotoResponse(
        id = id,
        reportId = reportId,
        uploaderId = uploaderId,
        kind = kind.name,
        contentType = contentType,
        sizeBytes = sizeBytes,
        createdAt = createdAt.toString(),
        url = "/api/v1/photos/$id"
    )
