// test/nl/brul/api/features/report/service/ImageSanitizerTest.kt
package nl.brul.api.features.report.service

import java.io.ByteArrayInputStream
import javax.imageio.ImageIO
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import nl.brul.api.common.error.UnsupportedMediaTypeException
import nl.brul.api.features.report.model.ImageFormat
import nl.brul.api.support.TestImages
import nl.brul.api.support.containsText

class ImageSanitizerTest {

    // P4 (F6)
    @Test
    fun `location data is removed from a jpeg`() {
        val original = TestImages.jpegWithExif()

        assertTrue(original.containsText("GPSLatitude"))

        val clean = ImageSanitizer.sanitize(original, ImageFormat.JPEG)

        assertFalse(clean.containsText("GPSLatitude"))
        assertFalse(clean.containsText("Exif"))
        assertEquals(ImageFormat.JPEG, ImageFormat.detect(clean))
    }

    @Test
    fun `cleaned image keeps its size`() {
        val clean = ImageSanitizer.sanitize(TestImages.png(width = 64, height = 48), ImageFormat.PNG)
        val image = assertNotNull(ImageIO.read(ByteArrayInputStream(clean)))

        assertEquals(64, image.width)
        assertEquals(48, image.height)
        assertEquals(ImageFormat.PNG, ImageFormat.detect(clean))
    }

    @Test
    fun `broken image data is rejected`() {
        val broken = TestImages.jpeg().copyOfRange(0, 20)

        assertFailsWith<UnsupportedMediaTypeException> {
            ImageSanitizer.sanitize(broken, ImageFormat.JPEG)
        }
    }
}
