// src/nl/brul/api/features/report/repository/ReportPhotoRepository.kt
package nl.brul.api.features.report.repository

import nl.brul.api.features.report.model.NewReportPhoto
import nl.brul.api.features.report.model.ReportPhoto

/** Photo data in the database. The real version is `ExposedReportPhotoRepository`; tests use a fake. */
interface ReportPhotoRepository {

    /** Saves [photo] and returns it with the id the database gave it. */
    fun add(photo: NewReportPhoto): ReportPhoto

    fun findById(id: Long): ReportPhoto?

    /** All photos of a report, oldest first. */
    fun findByReport(reportId: Long): List<ReportPhoto>

    fun countByReport(reportId: Long): Int

    /** `true` when a photo was deleted. */
    fun delete(id: Long): Boolean
}
