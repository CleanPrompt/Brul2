// test/nl/brul/api/features/report/model/ReportPhotoModelTest.kt
package nl.brul.api.features.report.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import nl.brul.api.features.report.PhotoTestData

/** The image format is recognised by the bytes of the file, not by its name or header. */
class ReportPhotoModelTest {

    @Test
    fun `a JPEG and a PNG are recognised by their bytes`() {
        assertEquals(ImageFormat.JPEG, ImageFormat.detect(PhotoTestData.jpeg()))
        assertEquals(ImageFormat.PNG, ImageFormat.detect(PhotoTestData.png()))
    }

    @Test
    fun `other files are not recognised`() {
        assertNull(ImageFormat.detect("GIF89a".toByteArray()))
        assertNull(ImageFormat.detect("%PDF-1.7".toByteArray()))
        assertNull(ImageFormat.detect(ByteArray(0)))
    }

    @Test
    fun `each format has its content type and extension`() {
        assertEquals("image/jpeg", ImageFormat.JPEG.contentType)
        assertEquals("png", ImageFormat.PNG.extension)
    }
}
