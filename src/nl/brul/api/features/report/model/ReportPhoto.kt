// src/nl/brul/api/features/report/model/ReportPhoto.kt
package nl.brul.api.features.report.model

import java.time.Instant

/** What a photo shows. */
enum class PhotoKind {
    /** Taken when reporting: what is going on. */
    SITUATION,

    /** Taken afterwards: cleaned up, repaired or helped. */
    RESULT
}

/**
 * Supported image formats, recognised by the first bytes of the file
 * ("magic numbers") instead of trusting the Content-Type header (F1).
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

        fun detect(bytes: ByteArray): ImageFormat? = when {
            bytes.startsWith(JPEG_SIGNATURE) -> JPEG
            bytes.startsWith(PNG_SIGNATURE) -> PNG
            else -> null
        }

        private fun ByteArray.startsWith(prefix: ByteArray): Boolean =
            size >= prefix.size && prefix.indices.all { this[it] == prefix[it] }
    }
}

/** Metadata of a stored photo; the bytes live in PhotoStorage under [storageKey]. */
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
