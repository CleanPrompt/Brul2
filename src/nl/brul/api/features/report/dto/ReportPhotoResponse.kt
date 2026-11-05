// src/nl/brul/api/features/report/dto/ReportPhotoResponse.kt
package nl.brul.api.features.report.dto

import kotlinx.serialization.Serializable

/**
 * One photo as JSON. `latitude` and `longitude` are where the photo was taken; `locationSource` says whether
 * that came from the photo itself (`PHOTO`) or from the app (`APP`). Without a location all three are `null`.
 */
@Serializable
data class ReportPhotoResponse(
    val id: Long,
    val reportId: Long,
    val uploaderId: Long? = null,
    val contentType: String,
    val sizeBytes: Long,
    /** ISO 8601 in UTC, e.g. `2026-11-05T19:30:00Z`. */
    val createdAt: String,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val locationSource: String? = null,
    /** Where the app downloads the image: `GET /api/v1/photos/{id}`. */
    val url: String
)
