// src/nl/brul/api/features/report/model/ImageFormat.kt
package nl.brul.api.features.report.model

/**
 * Laag: model (het domein).
 *
 * De toegestane bestandsformaten voor foto's: JPEG en PNG.
 * Het formaat wordt herkend aan de eerste bytes van het bestand (de "magic numbers"), niet aan de
 * bestandsnaam of de Content-Type-header: die kan iedereen zelf invullen.
 * `companion object`: hoort bij de enum zelf. Je roept het aan als `ImageFormat.detect(bytes)`.
 */
enum class ImageFormat(
    val contentType: String,
    val extension: String
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
