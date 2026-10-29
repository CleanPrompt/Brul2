// src/nl/brul/api/features/report/service/PhotoPolicy.kt
package nl.brul.api.features.report.service

import java.time.Instant
import nl.brul.api.features.help.model.ParticipationStatus
import nl.brul.api.features.report.model.HelpRequest
import nl.brul.api.features.report.model.ImageFormat
import nl.brul.api.features.report.model.PhotoKind
import nl.brul.api.features.report.model.Report
import nl.brul.api.features.report.model.ReportStatus

/** Alles wat de fotoregels moeten weten over één uploadpoging. */
data class PhotoUploadRequest(
    val report: Report,
    val uploaderId: Long,
    val kind: PhotoKind,
    val sizeBytes: Long,
    val format: ImageFormat?,
    val existingPhotoCount: Int,
    /** De aanmeldstatus van de uploader, als de melding een hulpvraag is. */
    val uploaderParticipation: ParticipationStatus?
)

/** De uitkomst van [PhotoPolicy.check]: toegestaan, of afgewezen met een reden. */
sealed interface PhotoDecision {
    data object Allowed : PhotoDecision
    data class Rejected(val reason: PhotoRejection) : PhotoDecision
}

/** Waarom een foto is afgewezen; ReportPhotoService maakt er de juiste HTTP-fout van. */
enum class PhotoRejection {
    TOO_LARGE,
    UNSUPPORTED_TYPE,
    NOT_ALLOWED,
    REPORT_RESOLVED,
    NOT_STARTED,
    PHOTO_LIMIT
}

/**
 * Laag: service (de regels).
 *
 * De regels F1 t/m F5 voor foto's, als één pure functie.
 * Het uploaden zelf is techniek; de domeinlogica zit in wie welke foto wanneer mag toevoegen, en
 * hoe dat samenhangt met de status en het tijdslot van de melding:
 *
 * - F1: alleen JPEG of PNG
 * - F2: maximaal 5 MB
 * - F3: maximaal 3 foto's per melding
 * - F4: een SITUATION-foto plaatst alleen de eigenaar, en niet meer als de melding RESOLVED is
 * - F5: een RESULT-foto bij een hulpvraag plaatst alleen een vrijwilliger (SIGNED_UP of
 *   ATTENDED), en pas vanaf de start van het tijdslot
 *
 * Net als bij de reserveringsregels komt de uitkomst terug als waarde (PhotoDecision) in plaats
 * van als exception.
 */
object PhotoPolicy {

    /** F2: maximaal 5 MB per foto. */
    const val MAX_PHOTO_BYTES: Long = 5L * 1024 * 1024

    /** F3: maximaal 3 foto's per melding. */
    const val MAX_PHOTOS_PER_REPORT = 3

    /**
     * Mag deze foto erbij? De controles staan in een vaste volgorde; de eerste die niet klopt,
     * bepaalt de reden.
     */
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

    /** F4 en F5: mag deze gebruiker dit soort foto plaatsen? */
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
