// src/nl/brul/api/features/report/model/ReportPhoto.kt
package nl.brul.api.features.report.model

import java.time.Instant

/**
 * The data of one photo of a report. The image itself is not in the database but in `PhotoStorage`,
 * under [storageKey].
 */
data class ReportPhoto(
    val id: Long,
    val reportId: Long,
    /** The user who added the photo; `null` when that user no longer exists. */
    val uploaderId: Long?,
    val contentType: String,
    val sizeBytes: Long,
    val storageKey: String,
    /** Set by the server when the photo is added (UTC, whole seconds); never changed afterwards. */
    val createdAt: Instant,
    /** Where the photo was taken, or `null` when neither the photo nor the app gave a location. */
    val location: PhotoLocation?
)
