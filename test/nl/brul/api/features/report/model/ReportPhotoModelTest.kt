// test/nl/brul/api/features/report/model/ReportPhotoModelTest.kt
package nl.brul.api.features.report.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import nl.brul.api.features.report.PhotoTestData

/** The photo model: format detection and the checks in ReportPhoto.init. */
class ReportPhotoModelTest {

    private fun photo(
        location: Location? = null,
        source: ReportPhoto.LocationSource? = null,
        size: Long = 10
    ) = ReportPhoto(1, 1, 1, "image/jpeg", size, "key.jpg", 1_000, location, source)

    @Test
    fun `format is recognised by its bytes`() {
        assertEquals(ImageFormat.JPEG, ImageFormat.detect(PhotoTestData.jpeg()))
        assertEquals(ImageFormat.PNG, ImageFormat.detect(PhotoTestData.png()))
        assertNull(ImageFormat.detect("GIF89a".toByteArray()))
    }

    @Test
    fun `photo with location and source is valid`() {
        val photo = photo(Location(51.5, 4.6), ReportPhoto.LocationSource.PHOTO)

        assertEquals(ReportPhoto.LocationSource.PHOTO, photo.locationSource)
    }

    @Test
    fun `location without source is rejected`() {
        assertFailsWith<IllegalArgumentException> { photo(location = Location(51.5, 4.6)) }
    }

    @Test
    fun `source without location is rejected`() {
        assertFailsWith<IllegalArgumentException> { photo(source = ReportPhoto.LocationSource.APP) }
    }

    @Test
    fun `empty photo is rejected`() {
        assertFailsWith<IllegalArgumentException> { photo(size = 0) }
    }
}
