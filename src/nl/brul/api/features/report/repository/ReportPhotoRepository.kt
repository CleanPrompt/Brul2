// src/nl/brul/api/features/report/repository/ReportPhotoRepository.kt
package nl.brul.api.features.report.repository

import nl.brul.api.features.report.model.Location
import nl.brul.api.features.report.model.ReportPhoto

/**
 * Laag: repository (het geheugen).
 *
 * Interface: de functies om fotogegevens op te slaan en op te halen.
 * De echte implementatie is ExposedReportPhotoRepository; tests gebruiken een nep-versie.
 */
interface ReportPhotoRepository {

    fun add(
        reportId: Long,
        uploaderId: Long,
        contentType: String,
        sizeBytes: Long,
        storageKey: String,
        createdAt: Long,
        location: Location?,
        locationSource: ReportPhoto.LocationSource?
    ): ReportPhoto

    fun findById(id: Long): ReportPhoto?

    /** Alle foto's van een melding, oudste eerst. */
    fun findByReport(reportId: Long): List<ReportPhoto>

    fun countByReport(reportId: Long): Int

    fun delete(id: Long): Boolean
}
