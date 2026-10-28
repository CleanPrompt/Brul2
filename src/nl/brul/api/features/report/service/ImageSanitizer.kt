// src/nl/brul/api/features/report/service/ImageSanitizer.kt
package nl.brul.api.features.report.service

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import javax.imageio.IIOImage
import javax.imageio.ImageIO
import javax.imageio.ImageWriteParam
import nl.brul.api.common.error.UnsupportedMediaTypeException
import nl.brul.api.features.report.model.ImageFormat

/**
 * F6: removes metadata such as GPS coordinates from an uploaded photo.
 *
 * The image is decoded to pixels and encoded again. Only the pixels are
 * written back, so EXIF data (location, camera, time) is gone.
 * Note: the EXIF orientation is lost too, so the app should rotate the
 * photo upright before uploading.
 */
object ImageSanitizer {

    private const val JPEG_QUALITY = 0.9f

    fun sanitize(bytes: ByteArray, format: ImageFormat): ByteArray {
        // A damaged file either returns null or throws while decoding.
        val image = runCatching { ImageIO.read(ByteArrayInputStream(bytes)) }.getOrNull()
            ?: throw UnsupportedMediaTypeException(
                code = "UNSUPPORTED_TYPE",
                message = "The photo could not be read as an image."
            )

        val output = ByteArrayOutputStream()

        when (format) {
            ImageFormat.PNG -> ImageIO.write(image, format.formatName, output)

            ImageFormat.JPEG -> {
                val writer = ImageIO.getImageWritersByFormatName(format.formatName).next()

                val params = writer.defaultWriteParam.apply {
                    compressionMode = ImageWriteParam.MODE_EXPLICIT
                    compressionQuality = JPEG_QUALITY
                }

                ImageIO.createImageOutputStream(output).use { stream ->
                    writer.output = stream
                    writer.write(null, IIOImage(image, null, null), params)
                }

                writer.dispose()
            }
        }

        return output.toByteArray()
    }
}
