// test/nl/brul/api/support/TestImages.kt
package nl.brul.api.support

import java.awt.Color
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO

/** Small generated images for photo tests. */
object TestImages {

    fun jpeg(width: Int = 40, height: Int = 30): ByteArray = encode("jpg", width, height)

    fun png(width: Int = 40, height: Int = 30): ByteArray = encode("png", width, height)

    /**
     * A JPEG with an EXIF block (APP1) right after the start marker, like a
     * phone photo. The block contains the text "GPSLatitude" so a test can
     * check that it is gone after cleaning.
     */
    fun jpegWithExif(): ByteArray {
        val plain = jpeg()

        // Minimal little-endian TIFF header + some recognisable text.
        val payload = "Exif".toByteArray() + byteArrayOf(0, 0) +
                byteArrayOf(0x49, 0x49, 0x2A, 0x00, 0x08, 0x00, 0x00, 0x00, 0x00, 0x00) +
                "GPSLatitude=51.5866".toByteArray()

        val length = payload.size + 2
        val app1 = byteArrayOf(
            0xFF.toByte(), 0xE1.toByte(),
            (length shr 8).toByte(), (length and 0xFF).toByte()
        ) + payload

        // Keep the SOI marker (first two bytes), insert APP1 after it.
        return plain.copyOfRange(0, 2) + app1 + plain.copyOfRange(2, plain.size)
    }

    private fun encode(format: String, width: Int, height: Int): ByteArray {
        val image = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
        val graphics = image.createGraphics()

        graphics.color = Color(30, 120, 200)
        graphics.fillRect(0, 0, width, height)
        graphics.color = Color.YELLOW
        graphics.fillOval(5, 5, width / 2, height / 2)
        graphics.dispose()

        val output = ByteArrayOutputStream()
        ImageIO.write(image, format, output)
        return output.toByteArray()
    }
}

fun ByteArray.containsText(text: String): Boolean =
    String(this, Charsets.ISO_8859_1).contains(text)
