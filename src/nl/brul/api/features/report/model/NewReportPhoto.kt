// src/nl/brul/api/features/report/model/NewReportPhoto.kt
package nl.brul.api.features.report.model

import java.time.Instant

/** A photo that is about to be saved; the database gives it its id. */
data class NewReportPhoto(
    val reportId: Long,
    val uploaderId: Long,
    val contentType: String,
    val sizeBytes: Long,
    val storageKey: String,
    val createdAt: Instant,
    val location: PhotoLocation?
)
