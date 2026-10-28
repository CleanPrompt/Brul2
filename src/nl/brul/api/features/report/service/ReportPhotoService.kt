// src/nl/brul/api/features/report/service/ReportPhotoService.kt
package nl.brul.api.features.report.service

import java.time.Clock
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID
import nl.brul.api.common.error.DomainConflictException
import nl.brul.api.common.error.DomainForbiddenException
import nl.brul.api.common.error.DomainNotFoundException
import nl.brul.api.common.error.PayloadTooLargeException
import nl.brul.api.common.error.UnsupportedMediaTypeException
import nl.brul.api.features.help.repository.HelpParticipationRepository
import nl.brul.api.features.report.model.HelpRequest
import nl.brul.api.features.report.model.ImageFormat
import nl.brul.api.features.report.model.PhotoKind
import nl.brul.api.features.report.model.ReportPhoto
import nl.brul.api.features.report.repository.PhotoStorage
import nl.brul.api.features.report.repository.ReportPhotoRepository
import nl.brul.api.features.report.repository.ReportRepository

class ReportNotFoundException :
    DomainNotFoundException(
        code = "REPORT_NOT_FOUND",
        message = "Report not found."
    )

class PhotoNotFoundException :
    DomainNotFoundException(
        code = "PHOTO_NOT_FOUND",
        message = "Photo not found."
    )

/**
 * Photos of reports (F1-F8).
 *
 * The rules are in [PhotoPolicy]; this service gathers the facts the policy
 * needs, cleans the image ([ImageSanitizer]) and stores bytes and metadata.
 */
class ReportPhotoService(
    private val reportRepository: ReportRepository,
    private val photoRepository: ReportPhotoRepository,
    private val storage: PhotoStorage,
    private val participationRepository: HelpParticipationRepository,
    private val clock: Clock = Clock.systemUTC()
) {

    fun upload(
        reportId: Long,
        uploaderId: Long,
        kind: PhotoKind,
        bytes: ByteArray
    ): ReportPhoto {
        val report = findReport(reportId)
        val format = ImageFormat.detect(bytes)

        val participation = if (report is HelpRequest) {
            participationRepository
                .findByHelpRequestAndUser(reportId, uploaderId)
                ?.status
        } else {
            null
        }

        val request = PhotoUploadRequest(
            report = report,
            uploaderId = uploaderId,
            kind = kind,
            sizeBytes = bytes.size.toLong(),
            format = format,
            existingPhotoCount = photoRepository.countByReport(reportId),
            uploaderParticipation = participation
        )

        val decision = PhotoPolicy.check(request, now())

        if (decision is PhotoDecision.Rejected) {
            throw uploadError(decision.reason)
        }

        // F6: strip location and other metadata before storing anything.
        val cleanBytes = ImageSanitizer.sanitize(bytes, requireNotNull(format))
        val key = "${UUID.randomUUID()}.${format.formatName}"

        storage.save(key, cleanBytes)

        return try {
            photoRepository.add(
                reportId = reportId,
                uploaderId = uploaderId,
                kind = kind,
                contentType = format.contentType,
                sizeBytes = cleanBytes.size.toLong(),
                storageKey = key,
                createdAt = now()
            )
        } catch (exception: Exception) {
            // Do not leave an orphan file when saving the metadata fails.
            storage.delete(key)
            throw exception
        }
    }

    fun list(reportId: Long): List<ReportPhoto> {
        findReport(reportId)
        return photoRepository.findByReport(reportId)
    }

    fun count(reportId: Long): Int =
        photoRepository.countByReport(reportId)

    fun hasResultPhoto(reportId: Long): Boolean =
        photoRepository.hasKind(reportId, PhotoKind.RESULT)

    /** The photo metadata together with its bytes. */
    fun load(photoId: Long): Pair<ReportPhoto, ByteArray> {
        val photo = findPhoto(photoId)
        val bytes = storage.load(photo.storageKey) ?: throw PhotoNotFoundException()

        return photo to bytes
    }

    /** F7: the uploader or an admin may delete a photo. */
    fun delete(
        photoId: Long,
        actingUserId: Long,
        isAdmin: Boolean
    ) {
        val photo = findPhoto(photoId)

        if (photo.uploaderId != actingUserId && !isAdmin) {
            throw DomainForbiddenException(
                code = "NOT_ALLOWED",
                message = "Only the uploader or an admin can delete this photo."
            )
        }

        photoRepository.delete(photoId)
        storage.delete(photo.storageKey)
    }

    /** Keys of the files that belong to a report (read before deleting it). */
    fun storageKeysOf(reportId: Long): List<String> =
        photoRepository.findByReport(reportId).map { it.storageKey }

    /**
     * F7: the database cascade removes the photo rows of a deleted report,
     * but not the files. ReportService calls this after a successful delete.
     */
    fun deleteFiles(keys: List<String>) {
        keys.forEach { storage.delete(it) }
    }

    private fun findReport(reportId: Long) =
        reportRepository.findById(reportId) ?: throw ReportNotFoundException()

    private fun findPhoto(photoId: Long): ReportPhoto {
        require(photoId > 0) {
            "Photo ID must be positive."
        }

        return photoRepository.findById(photoId) ?: throw PhotoNotFoundException()
    }

    private fun now(): Instant =
        clock.instant().truncatedTo(ChronoUnit.MILLIS)

    private fun uploadError(reason: PhotoRejection): RuntimeException = when (reason) {
        PhotoRejection.TOO_LARGE ->
            PayloadTooLargeException(
                code = "TOO_LARGE",
                message = "A photo can be at most ${PhotoPolicy.MAX_PHOTO_BYTES / (1024 * 1024)} MB."
            )

        PhotoRejection.UNSUPPORTED_TYPE ->
            UnsupportedMediaTypeException(
                code = "UNSUPPORTED_TYPE",
                message = "Only JPEG and PNG photos are accepted."
            )

        PhotoRejection.NOT_ALLOWED ->
            DomainForbiddenException(
                code = "NOT_ALLOWED",
                message = "You are not allowed to add this kind of photo to this report."
            )

        PhotoRejection.REPORT_RESOLVED ->
            DomainConflictException(
                code = "REPORT_RESOLVED",
                message = "A resolved report does not accept new situation photos."
            )

        PhotoRejection.NOT_STARTED ->
            DomainConflictException(
                code = "NOT_STARTED",
                message = "A result photo can only be added once the help request has started."
            )

        PhotoRejection.PHOTO_LIMIT ->
            DomainConflictException(
                code = "PHOTO_LIMIT",
                message = "A report can have at most ${PhotoPolicy.MAX_PHOTOS_PER_REPORT} photos."
            )
    }
}
