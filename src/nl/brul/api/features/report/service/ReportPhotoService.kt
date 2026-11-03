// src/nl/brul/api/features/report/service/ReportPhotoService.kt
package nl.brul.api.features.report.service

import java.util.UUID
import nl.brul.api.features.report.model.ImageFormat
import nl.brul.api.features.report.model.Location
import nl.brul.api.features.report.model.ReportPhoto
import nl.brul.api.features.report.repository.PhotoStorage
import nl.brul.api.features.report.repository.ReportPhotoRepository
import nl.brul.api.features.report.repository.ReportRepository

/** De uitkomst van een upload: opgeslagen, of afgewezen met een reden. */
sealed interface PhotoUploadResult {
    data class Saved(val photo: ReportPhoto) : PhotoUploadResult
    data class Rejected(val reason: PhotoRejection) : PhotoUploadResult
}

/** De uitkomst van verwijderen. */
enum class PhotoDeleteResult {
    DELETED,
    NOT_FOUND,
    FORBIDDEN
}

/**
 * Laag: service (de regels).
 *
 * Foto's bij meldingen. Bij het uploaden:
 *
 * 1. PhotoRules beslist of het mag (P1 t/m P4).
 * 2. De GPS-coördinaten komen uit de foto zelf (PhotoGpsReader, G1). Heeft de foto geen GPS,
 *    dan gebruiken we de locatie die de app meestuurt (G2). Geen van beide: geen locatie.
 * 3. Het bestand krijgt een willekeurige naam (UUID) en wordt opgeslagen in PhotoStorage.
 * 4. De gegevens, met de locatie, gaan naar de database. Mislukt dat, dan wordt het bestand weer
 *    verwijderd, zodat er geen los bestand achterblijft.
 *
 * Fouten komen terug als waarde (PhotoUploadResult, PhotoDeleteResult) in plaats van als
 * exception; de route maakt er de juiste HTTP-status van.
 * Krijgt zijn hulpjes via de constructor (dependency injection); de klok kan in tests vast.
 */
class ReportPhotoService(
    private val reportRepository: ReportRepository,
    private val photoRepository: ReportPhotoRepository,
    private val storage: PhotoStorage,
    private val currentTimeMillis: () -> Long = System::currentTimeMillis
) {

    fun upload(
        reportId: Long,
        uploaderId: Long,
        bytes: ByteArray,
        appLocation: Location? = null
    ): PhotoUploadResult {
        require(reportId > 0 && uploaderId > 0) {
            "IDs must be positive."
        }

        val report = reportRepository.findById(reportId)
            ?: return PhotoUploadResult.Rejected(PhotoRejection.REPORT_NOT_FOUND)

        val format = ImageFormat.detect(bytes)

        val rejection = PhotoRules.check(
            isOwner = report.ownerId == uploaderId,
            sizeBytes = bytes.size.toLong(),
            format = format,
            existingPhotoCount = photoRepository.countByReport(reportId)
        )

        if (rejection != null) {
            return PhotoUploadResult.Rejected(rejection)
        }

        val (location, source) = locationOf(bytes, format, appLocation)
        val key = "${UUID.randomUUID()}.${requireNotNull(format).extension}"

        storage.save(key, bytes)

        val photo = try {
            photoRepository.add(
                reportId = reportId,
                uploaderId = uploaderId,
                contentType = format.contentType,
                sizeBytes = bytes.size.toLong(),
                storageKey = key,
                createdAt = currentTimeMillis(),
                location = location,
                locationSource = source
            )
        } catch (exception: Exception) {
            // Do not leave an orphan file when saving the metadata fails.
            storage.delete(key)
            throw exception
        }

        return PhotoUploadResult.Saved(photo)
    }

    /** Alle foto's van een melding, of `null` als de melding niet bestaat. */
    fun list(reportId: Long): List<ReportPhoto>? {
        require(reportId > 0) {
            "Report ID must be positive."
        }

        reportRepository.findById(reportId) ?: return null

        return photoRepository.findByReport(reportId)
    }

    /** De gegevens van een foto samen met de afbeelding, of `null` als hij niet bestaat. */
    fun load(photoId: Long): Pair<ReportPhoto, ByteArray>? {
        require(photoId > 0) {
            "Photo ID must be positive."
        }

        val photo = photoRepository.findById(photoId) ?: return null
        val bytes = storage.load(photo.storageKey) ?: return null

        return photo to bytes
    }

    /** De uploader of een admin mag een foto verwijderen. */
    fun delete(photoId: Long, actingUserId: Long, isAdmin: Boolean): PhotoDeleteResult {
        require(photoId > 0) {
            "Photo ID must be positive."
        }

        val photo = photoRepository.findById(photoId)
            ?: return PhotoDeleteResult.NOT_FOUND

        if (photo.uploaderId != actingUserId && !isAdmin) {
            return PhotoDeleteResult.FORBIDDEN
        }

        photoRepository.delete(photoId)
        storage.delete(photo.storageKey)

        return PhotoDeleteResult.DELETED
    }

    /**
     * De bestandssleutels van een melding. ReportService leest ze vóór het verwijderen van een
     * melding, want daarna zijn de rijen weg (CASCADE).
     */
    fun storageKeysOf(reportId: Long): List<String> =
        photoRepository.findByReport(reportId).map { it.storageKey }

    /** Verwijdert de bestanden van een verwijderde melding. */
    fun deleteFiles(keys: List<String>) {
        keys.forEach { storage.delete(it) }
    }

    /** G1 en G2: eerst de GPS in de foto, anders de locatie van de app. */
    private fun locationOf(
        bytes: ByteArray,
        format: ImageFormat?,
        appLocation: Location?
    ): Pair<Location?, ReportPhoto.LocationSource?> {
        val fromPhoto = if (format == ImageFormat.JPEG) PhotoGpsReader.read(bytes) else null

        return when {
            fromPhoto != null -> fromPhoto to ReportPhoto.LocationSource.PHOTO
            appLocation != null -> appLocation to ReportPhoto.LocationSource.APP
            else -> null to null
        }
    }
}
