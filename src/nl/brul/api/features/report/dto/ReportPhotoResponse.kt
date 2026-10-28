// src/nl/brul/api/features/report/dto/ReportPhotoResponse.kt
package nl.brul.api.features.report.dto

import kotlinx.serialization.Serializable
import nl.brul.api.features.report.model.ReportPhoto

@Serializable
data class ReportPhotoResponse(
    val id: Long,
    val reportId: Long,
    val uploaderId: Long? = null,
    val kind: String,
    val contentType: String,
    val sizeBytes: Long,
    val createdAt: String,
    /** Relative URL that returns the image itself. */
    val url: String
)

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
