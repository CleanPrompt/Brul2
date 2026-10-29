// src/nl/brul/api/features/report/model/ReportPhoto.kt
package nl.brul.api.features.report.model

import java.time.Instant

/**
 * Laag: model (het domein).
 *
 * Wat een foto laat zien: SITUATION hoort bij het melden, RESULT bij de afhandeling.
 * Wie welke soort mag plaatsen, regelt PhotoPolicy (F4 en F5).
 */
enum class PhotoKind {
    /** Gemaakt bij het melden: wat er aan de hand is. */
    SITUATION,

    /** Gemaakt achteraf: opgeruimd, gerepareerd of geholpen. */
    RESULT
}

/**
 * Laag: model (het domein).
 *
 * De toegestane bestandsformaten: JPEG en PNG (F1).
 * Het formaat wordt herkend aan de eerste bytes van het bestand (de "magic numbers"), niet aan
 * de Content-Type-header: die kan de app, of iemand met kwade bedoelingen, zelf invullen.
 * `companion object`: hoort bij de enum zelf, niet bij één waarde. Je roept het aan als
 * `ImageFormat.detect(bytes)`.
 */
enum class ImageFormat(
    val contentType: String,
    val formatName: String
) {
    JPEG("image/jpeg", "jpg"),
    PNG("image/png", "png");

    companion object {
        private val JPEG_SIGNATURE = byteArrayOf(0xFF.toByte(), 0xD8.toByte(), 0xFF.toByte())

        private val PNG_SIGNATURE = byteArrayOf(
            0x89.toByte(), 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A
        )

        /** Het formaat van deze bytes, of `null` als het geen JPEG of PNG is. */
        fun detect(bytes: ByteArray): ImageFormat? = when {
            bytes.startsWith(JPEG_SIGNATURE) -> JPEG
            bytes.startsWith(PNG_SIGNATURE) -> PNG
            else -> null
        }

        private fun ByteArray.startsWith(prefix: ByteArray): Boolean =
            size >= prefix.size && prefix.indices.all { this[it] == prefix[it] }
    }
}

/**
 * Laag: model (het domein).
 *
 * De gegevens van één opgeslagen foto bij een melding. De afbeelding zelf staat niet in de
 * database, maar in PhotoStorage onder [storageKey].
 * `uploaderId` mag leeg zijn: wordt de gebruiker verwijderd, dan blijft de foto bestaan.
 * `init` controleert de id's, dat de foto niet leeg is en dat er een sleutel is.
 */
data class ReportPhoto(
    val id: Long,
    val reportId: Long,
    val uploaderId: Long?,
    val kind: PhotoKind,
    val contentType: String,
    val sizeBytes: Long,
    val storageKey: String,
    val createdAt: Instant
) {
    init {
        require(id > 0) {
            "Photo ID must be positive."
        }

        require(reportId > 0) {
            "Report ID must be positive."
        }

        require(sizeBytes > 0) {
            "A photo cannot be empty."
        }

        require(storageKey.isNotBlank()) {
            "Storage key cannot be blank."
        }
    }
}
