// test/nl/brul/api/features/report/service/PhotoGpsReaderTest.kt
package nl.brul.api.features.report.service

import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import nl.brul.api.features.report.PhotoTestData

/** G1: reading GPS coordinates from the EXIF data of a JPEG. */
class PhotoGpsReaderTest {

    private fun assertClose(expected: Double, actual: Double) {
        assertTrue(abs(expected - actual) < 0.00001, "expected $expected but was $actual")
    }

    @Test
    fun `reads latitude and longitude from a phone photo`() {
        val location = assertNotNull(PhotoGpsReader.read(PhotoTestData.jpegWithGps(51.5856, 4.7763)))

        assertClose(51.5856, location.latitude)
        assertClose(4.7763, location.longitude)
    }

    @Test
    fun `reads big-endian EXIF as well`() {
        val bytes = PhotoTestData.jpegWithGps(52.0705, 4.3007, bigEndian = true)
        val location = assertNotNull(PhotoGpsReader.read(bytes))

        assertClose(52.0705, location.latitude)
        assertClose(4.3007, location.longitude)
    }

    @Test
    fun `south and west become negative`() {
        val location = assertNotNull(PhotoGpsReader.read(PhotoTestData.jpegWithGps(-33.8688, -70.6693)))

        assertClose(-33.8688, location.latitude)
        assertClose(-70.6693, location.longitude)
    }

    @Test
    fun `photo without EXIF has no location`() {
        assertNull(PhotoGpsReader.read(PhotoTestData.jpeg()))
    }

    @Test
    fun `PNG has no location`() {
        assertNull(PhotoGpsReader.read(PhotoTestData.png()))
    }

    @Test
    fun `zero zero means no GPS fix`() {
        assertNull(PhotoGpsReader.read(PhotoTestData.jpegWithGps(0.0, 0.0)))
    }

    @Test
    fun `garbage gives no location and no crash`() {
        assertNull(PhotoGpsReader.read(ByteArray(100) { it.toByte() }))
        assertNull(PhotoGpsReader.read(ByteArray(0)))
    }

    @Test
    fun `a cut-off file gives no location and no crash`() {
        val full = PhotoTestData.jpegWithGps(51.5856, 4.7763)

        listOf(3, 10, 30, 60, 90, 120).forEach { size ->
            assertNull(PhotoGpsReader.read(full.copyOf(size)))
        }
    }
}
