package nl.brul.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class LocationTest {

    @Test
    fun `valid coordinates create location`() {
        val location = Location(
            latitude = 52.0705,
            longitude = 4.3007
        )

        assertEquals(52.0705, location.latitude)
        assertEquals(4.3007, location.longitude)
    }

    @Test
    fun `latitude outside valid range is rejected`() {
        assertFailsWith<IllegalArgumentException> {
            Location(
                latitude = 100.0,
                longitude = 4.3007
            )
        }
    }

    @Test
    fun `longitude outside valid range is rejected`() {
        assertFailsWith<IllegalArgumentException> {
            Location(
                latitude = 52.0705,
                longitude = 200.0
            )
        }
    }
}