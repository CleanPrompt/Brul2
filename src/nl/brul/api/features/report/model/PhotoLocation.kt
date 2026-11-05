// src/nl/brul/api/features/report/model/PhotoLocation.kt
package nl.brul.api.features.report.model

/**
 * Where a photo was taken and where that position came from.
 * Keeping both in one class means a location can never exist without its source.
 */
data class PhotoLocation(
    val location: Location,
    val source: LocationSource
)

enum class LocationSource {
    /** From the EXIF data that the phone camera writes into the photo. */
    PHOTO,

    /** Sent by the app, because the photo itself had no GPS data. */
    APP
}
