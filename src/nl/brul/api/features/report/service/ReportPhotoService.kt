// src/nl/brul/api/features/report/service/ReportPhotoService.kt
package nl.brul.api.features.report.service

import java.time.Clock
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID
import nl.brul.api.common.error.AppException
import nl.brul.api.features.report.dto.ReportPhotoResponse
import nl.brul.api.features.report.mapper.toResponse
import nl.brul.api.features.report.model.ImageFormat
import nl.brul.api.features.report.model.Location
import nl.brul.api.features.report.model.LocationSource
import nl.brul.api.features.report.model.NewReportPhoto
import nl.brul.api.features.report.model.PhotoFile
import nl.brul.api.features.report.model.PhotoLocation
import nl.brul.api.features.report.model.Report
import nl.brul.api.features.report.model.ReportPhoto
import nl.brul.api.features.report.repository.PhotoStorage
import nl.brul.api.features.report.repository.ReportPhotoRepository
import nl.brul.api.features.report.repository.ReportRepository

/**
 * Business rules for photos of reports. Access checks happen in the routes, through `PhotoAccessService`.
 *
 * An upload: `PhotoRules` checks the photo (P2-P4), the location comes from the photo itself (G1) or else from
 * the app (G2), the file gets a random name and goes to [storage], and the data goes to the database.
 * [clock] is a parameter so tests can use a fixed time.
 */
class ReportPhotoService(
    private val reportRepository: ReportRepository,
    private val photoRepository: ReportPhotoRepository,
    private val storage: PhotoStorage,
    private val clock: Clock = Clock.systemUTC()
) {

    /** The owner of a report, so a route can check access before it adds a photo. 404 when unknown. */
    fun getReportOwnerId(reportId: Long): Long? =
        findReport(reportId).ownerId

    /** The uploader of a photo, so a route can check access before it deletes the photo. 404 when unknown. */
    fun getUploaderId(photoId: Long): Long? =
        findPhoto(photoId).uploaderId

    /** The server sets the file name, the time and the location; the client only sends the photo. */
    fun upload(reportId: Long, uploaderId: Long, bytes: ByteArray, appLocation: Location?): ReportPhotoResponse {
        findReport(reportId) // 404 when the report does not exist

        val format = PhotoRules.check(
            sizeBytes = bytes.size.toLong(),
            format = ImageFormat.detect(bytes),
            existingPhotoCount = photoRepository.countByReport(reportId)
        )

        val key = "${UUID.randomUUID()}.${format.extension}"
        storage.save(key, bytes)

        val photo = try {
            photoRepository.add(
                NewReportPhoto(
                    reportId = reportId,
                    uploaderId = uploaderId,
                    contentType = format.contentType,
                    sizeBytes = bytes.size.toLong(),
                    storageKey = key,
                    createdAt = Instant.now(clock).truncatedTo(ChronoUnit.SECONDS),
                    location = locationOf(bytes, format, appLocation)
                )
            )
        } catch (exception: Exception) {
            // Do not leave a file without data behind when saving the data fails.
            storage.delete(key)
            throw exception
        }

        return photo.toResponse()
    }

    /** All photos of a report, oldest first. 404 when the report does not exist. */
    fun listByReport(reportId: Long): List<ReportPhotoResponse> {
        findReport(reportId) // 404 when the report does not exist

        return photoRepository.findByReport(reportId).map { it.toResponse() }
    }

    fun getImage(photoId: Long): PhotoFile {
        val photo = findPhoto(photoId)
        val bytes = storage.load(photo.storageKey) ?: throw photoNotFound()

        return PhotoFile(photo.contentType, bytes)
    }

    fun delete(photoId: Long) {
        val photo = findPhoto(photoId)

        if (!photoRepository.delete(photoId)) {
            throw photoNotFound()
        }

        storage.delete(photo.storageKey)
    }

    /**
     * The file keys of a report. `ReportService` reads them before it deletes a report, because the database
     * removes the photo rows with it (CASCADE) but not the files.
     */
    fun storageKeysOf(reportId: Long): List<String> =
        photoRepository.findByReport(reportId).map { it.storageKey }

    /** Removes the files of a deleted report. */
    fun deleteFiles(keys: List<String>) {
        keys.forEach { storage.delete(it) }
    }

    /** G1: the GPS in the photo wins. G2: otherwise the location from the app. Neither: no location. */
    private fun locationOf(bytes: ByteArray, format: ImageFormat, appLocation: Location?): PhotoLocation? {
        val fromPhoto = if (format == ImageFormat.JPEG) PhotoGpsReader.read(bytes) else null

        return when {
            fromPhoto != null -> PhotoLocation(fromPhoto, LocationSource.PHOTO)
            appLocation != null -> PhotoLocation(appLocation, LocationSource.APP)
            else -> null
        }
    }

    private fun findReport(reportId: Long): Report =
        reportRepository.findById(reportId) ?: throw AppException.NotFound("Report not found", "REPORT_NOT_FOUND")

    private fun findPhoto(photoId: Long): ReportPhoto =
        photoRepository.findById(photoId) ?: throw photoNotFound()

    private fun photoNotFound() = AppException.NotFound("Photo not found", "PHOTO_NOT_FOUND")
}
