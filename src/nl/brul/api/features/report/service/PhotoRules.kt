// src/nl/brul/api/features/report/service/PhotoRules.kt
package nl.brul.api.features.report.service

import nl.brul.api.features.report.model.ImageFormat

/** Waarom een foto niet geplaatst mag worden; de route maakt er de juiste HTTP-status van. */
enum class PhotoRejection {
    /** De melding bestaat niet (404). */
    REPORT_NOT_FOUND,

    /** Alleen de eigenaar van de melding mag foto's toevoegen (403). */
    NOT_OWNER,

    /** Groter dan 5 MB (413). */
    TOO_LARGE,

    /** Geen JPEG of PNG (415). */
    UNSUPPORTED_TYPE,

    /** Er staan al 3 foto's bij de melding (409). */
    PHOTO_LIMIT
}

/**
 * Laag: service (de regels).
 *
 * De regels voor het toevoegen van een foto, als één pure functie:
 *
 * - P1: alleen de eigenaar van de melding voegt foto's toe
 * - P2: maximaal 5 MB
 * - P3: alleen JPEG of PNG (herkend aan de bytes, zie ImageFormat)
 * - P4: maximaal 3 foto's per melding
 *
 * De controles staan in een vaste volgorde; de eerste die niet klopt bepaalt de reden.
 * `null` betekent: het mag.
 */
object PhotoRules {

    /** P2: maximaal 5 MB per foto. */
    const val MAX_PHOTO_BYTES: Long = 5L * 1024 * 1024

    /** P4: maximaal 3 foto's per melding. */
    const val MAX_PHOTOS_PER_REPORT = 3

    fun check(
        isOwner: Boolean,
        sizeBytes: Long,
        format: ImageFormat?,
        existingPhotoCount: Int
    ): PhotoRejection? = when {
        !isOwner -> PhotoRejection.NOT_OWNER                                    // P1
        sizeBytes > MAX_PHOTO_BYTES -> PhotoRejection.TOO_LARGE                 // P2
        format == null -> PhotoRejection.UNSUPPORTED_TYPE                       // P3
        existingPhotoCount >= MAX_PHOTOS_PER_REPORT -> PhotoRejection.PHOTO_LIMIT // P4
        else -> null
    }
}
