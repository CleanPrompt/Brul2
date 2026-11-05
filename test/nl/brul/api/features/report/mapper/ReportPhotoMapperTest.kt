// test/nl/brul/api/features/report/mapper/ReportPhotoMapperTest.kt
package nl.brul.api.features.report.mapper

import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import nl.brul.api.common.error.AppException
import nl.brul.api.features.report.model.Location
import nl.brul.api.features.report.model.LocationSource
import nl.brul.api.features.report.model.PhotoLocation
import nl.brul.api.features.report.model.ReportPhoto

class ReportPhotoMapperTest {

    private fun photo(location: PhotoLocation?) = ReportPhoto(
        id = 7,
        reportId = 1,
        uploaderId = 1,
        contentType = "image/jpeg",
        sizeBytes = 10,
        storageKey = "key.jpg",
        createdAt = Instant.parse("2026-11-05T19:30:00Z"),
        location = location
    )

    @Test
    fun `the response has the location, its source, an ISO time and the image url`() {
        val response = photo(PhotoLocation(Location(51.5, 4.6), LocationSource.APP)).toResponse()

        assertEquals(51.5, response.latitude)
        assertEquals(4.6, response.longitude)
        assertEquals("APP", response.locationSource)
        assertEquals("2026-11-05T19:30:00Z", response.createdAt)
        assertEquals("/api/v1/photos/7", response.url)
    }

    @Test
    fun `without a location all three location fields are empty`() {
        val response = photo(location = null).toResponse()

        assertNull(response.latitude)
        assertNull(response.longitude)
        assertNull(response.locationSource)
    }

    @Test
    fun `the app location needs both numbers or neither`() {
        assertEquals(Location(51.5, 4.6), appLocationOf(" 51.5 ", "4.6"))
        assertNull(appLocationOf(null, null))
        assertNull(appLocationOf("", " "))

        assertFailsWith<AppException.BadRequest> { appLocationOf("51.5", null) }
        assertFailsWith<AppException.BadRequest> { appLocationOf("north", "4.6") }
    }

    @Test
    fun `an app location outside the earth is a 400`() {
        assertFailsWith<AppException.BadRequest> { appLocationOf("91", "4.6") }
        assertFailsWith<AppException.BadRequest> { appLocationOf("51.5", "-181") }
    }
}
