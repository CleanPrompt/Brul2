// test/nl/brul/api/features/help/model/VolunteerRecordTest.kt
package nl.brul.api.features.help.model

import java.time.Duration
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class VolunteerRecordTest {

    private val now = Instant.parse("2026-11-20T08:00:00Z")

    private fun daysAgo(days: Long): Instant = now.minus(Duration.ofDays(days))

    // T9 (R11)
    @Test
    fun `three no-shows within thirty days block new sign-ups`() {
        val record = VolunteerRecord(
            userId = 2,
            noShowTimes = listOf(daysAgo(29), daysAgo(10), daysAgo(1))
        )

        assertTrue(record.isBlocked(now))
    }

    // T9 (R11)
    @Test
    fun `a no-show older than thirty days no longer counts`() {
        val record = VolunteerRecord(
            userId = 2,
            noShowTimes = listOf(daysAgo(31), daysAgo(10), daysAgo(1))
        )

        assertFalse(record.isBlocked(now))
        assertEquals(2, record.recentNoShows(now).size)
    }

    @Test
    fun `block ends when the oldest counted no-show leaves the window`() {
        val record = VolunteerRecord(
            userId = 2,
            noShowTimes = listOf(daysAgo(29), daysAgo(10), daysAgo(1))
        )

        assertEquals(daysAgo(29).plus(Duration.ofDays(30)), record.blockedUntil(now))
        assertFalse(record.isBlocked(record.blockedUntil(now)!!))
    }

    @Test
    fun `not blocked means no end date`() {
        assertNull(VolunteerRecord(userId = 2).blockedUntil(now))
    }
}
