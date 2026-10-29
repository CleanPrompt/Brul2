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
 * Laag: service (de regels).
 *
 * F6: haalt metadata zoals GPS-coördinaten uit een geüploade foto.
 * De afbeelding wordt ingelezen tot pixels en opnieuw opgeslagen. Alleen de pixels worden
 * teruggeschreven, dus de EXIF-gegevens (locatie, camera, tijd) zijn weg. De melding heeft al een
 * locatie; de foto hoeft niet ook te verraden waar de maker woont.
 * Let op: ook de EXIF-oriëntatie verdwijnt. De app moet de foto dus rechtop zetten vóór het
 * uploaden.
 */
object ImageSanitizer {

    private const val JPEG_QUALITY = 0.9f

    /**
     * Geeft de schone bytes terug; een kapot bestand geeft 415 (UNSUPPORTED_TYPE).
     * Een JPEG wordt opnieuw opgeslagen met kwaliteit 0,9 (90%).
     */
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
