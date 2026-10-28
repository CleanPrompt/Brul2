// src/nl/brul/api/features/report/service/PhotoPolicy.kt
package nl.brul.api.features.report.service

import java.time.Instant
import nl.brul.api.features.help.model.ParticipationStatus
import nl.brul.api.features.report.model.HelpRequest
import nl.brul.api.features.report.model.ImageFormat
import nl.brul.api.features.report.model.PhotoKind
import nl.brul.api.features.report.model.Report
import nl.brul.api.features.report.model.ReportStatus

/** Everything the photo rules need to know about one upload attempt. */
data class PhotoUploadRequest(
    val report: Report,
    val uploaderId: Long,
    val kind: PhotoKind,
    val sizeBytes: Long,
    val format: ImageFormat?,
    val existingPhotoCount: Int,
    /** The uploader's participation status if the report is a help request. */
    val uploaderParticipation: ParticipationStatus?
)

sealed interface PhotoDecision {
    data object Allowed : PhotoDecision
    data class Rejected(val reason: PhotoRejection) : PhotoDecision
}

enum class PhotoRejection {
    TOO_LARGE,
    UNSUPPORTED_TYPE,
    NOT_ALLOWED,
    REPORT_RESOLVED,
    NOT_STARTED,
    PHOTO_LIMIT
}

/**
 * Rules F1-F5 for photos, as one pure function.
 *
 * The upload itself is technique; the domain logic is in who may add which
 * photo when, and how that follows the status and the time slot.
 */
object PhotoPolicy {

    /** F2 */
    const val MAX_PHOTO_BYTES: Long = 5L * 1024 * 1024

    /** F3 */
    const val MAX_PHOTOS_PER_REPORT = 3

    fun check(request: PhotoUploadRequest, now: Instant): PhotoDecision {
        val report = request.report

        fun reject(reason: PhotoRejection) = PhotoDecision.Rejected(reason)

        return when {
            // F2
            request.sizeBytes > MAX_PHOTO_BYTES -> reject(PhotoRejection.TOO_LARGE)

            // F1
            request.format == null -> reject(PhotoRejection.UNSUPPORTED_TYPE)

            // F4 and F5: who may add this kind of photo
            !mayUpload(request) -> reject(PhotoRejection.NOT_ALLOWED)

            // F4: no new situation photos on a closed report
            request.kind == PhotoKind.SITUATION && report.status == ReportStatus.RESOLVED ->
                reject(PhotoRejection.REPORT_RESOLVED)

            // F5: a help result can only exist once the help has started
            request.kind == PhotoKind.RESULT && report is HelpRequest &&
                    !report.slot.hasStarted(now) ->
                reject(PhotoRejection.NOT_STARTED)

            // F3
            request.existingPhotoCount >= MAX_PHOTOS_PER_REPORT ->
                reject(PhotoRejection.PHOTO_LIMIT)

            else -> PhotoDecision.Allowed
        }
    }

    private fun mayUpload(request: PhotoUploadRequest): Boolean {
        val isOwner = request.report.ownerId == request.uploaderId

        return when (request.kind) {
            PhotoKind.SITUATION -> isOwner

            PhotoKind.RESULT -> if (request.report is HelpRequest) {
                // F5: the volunteers who did the work show the result.
                request.uploaderParticipation == ParticipationStatus.SIGNED_UP ||
                        request.uploaderParticipation == ParticipationStatus.ATTENDED
            } else {
                // Safety and nuisance reports: the owner shows it is solved (F8).
                isOwner
            }
        }
    }
}
