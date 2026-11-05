// src/nl/brul/api/features/report/mapper/ReportPhotoMapper.kt
package nl.brul.api.features.report.mapper

import nl.brul.api.common.error.AppException
import nl.brul.api.common.error.requireValid
import nl.brul.api.features.report.dto.ReportPhotoResponse
import nl.brul.api.features.report.model.Location
import nl.brul.api.features.report.model.ReportPhoto

/** The JSON for the app. The image itself is not included; the app downloads it from [ReportPhotoResponse.url]. */
fun ReportPhoto.toResponse(): ReportPhotoResponse =
    ReportPhotoResponse(
        id = id,
        reportId = reportId,
        uploaderId = uploaderId,
        contentType = contentType,
        sizeBytes = sizeBytes,
        createdAt = createdAt.toString(),
        latitude = location?.location?.latitude,
        longitude = location?.location?.longitude,
        locationSource = location?.source?.name,
        url = "/api/v1/photos/$id"
    )

/**
 * The optional `latitude` and `longitude` fields of an upload form as a [Location].
 * Both or neither must be sent; anything else is a 400.
 */
fun appLocationOf(latitude: String?, longitude: String?): Location? {
    if (latitude.isNullOrBlank() && longitude.isNullOrBlank()) {
        return null
    }

    val lat = latitude?.trim()?.toDoubleOrNull()
    val lon = longitude?.trim()?.toDoubleOrNull()

    if (lat == null || lon == null) {
        throw AppException.BadRequest("Latitude and longitude must be valid numbers and sent together")
    }

    requireValid(lat in -90.0..90.0 && lon in -180.0..180.0) {
        "Latitude must be between -90 and 90, longitude between -180 and 180"
    }

    return Location(lat, lon)
}
