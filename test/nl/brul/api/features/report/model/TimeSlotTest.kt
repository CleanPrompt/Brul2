// test/nl/brul/api/features/report/model/TimeSlotTest.kt
package nl.brul.api.features.report.model

import java.time.Duration
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TimeSlotTest {

    private fun slot(start: String, end: String) =
        TimeSlot(
            Instant.parse("2026-11-20T${start}:00Z"),
            Instant.parse("2026-11-20T${end}:00Z")
        )

    // T1 (R1)
    @Test
    fun `slot cannot end before it starts`() {
        assertFailsWith<IllegalArgumentException> {
            slot("12:00", "10:00")
        }
    }

    @Test
    fun `slot cannot have zero length`() {
        assertFailsWith<IllegalArgumentException> {
            slot("10:00", "10:00")
        }
    }

    // T2 (R5)
    @Test
    fun `touching slots do not overlap`() {
        assertFalse(slot("10:00", "12:00").overlaps(slot("12:00", "14:00")))
        assertFalse(slot("12:00", "14:00").overlaps(slot("10:00", "12:00")))
    }

    @Test
    fun `partly overlapping slots overlap`() {
        assertTrue(slot("10:00", "12:00").overlaps(slot("11:00", "13:00")))
        assertTrue(slot("11:00", "13:00").overlaps(slot("10:00", "12:00")))
    }

    @Test
    fun `a slot inside another slot overlaps`() {
        assertTrue(slot("09:00", "17:00").overlaps(slot("10:00", "11:00")))
    }

    @Test
    fun `started and ended are inclusive at the boundary`() {
        val slot = slot("10:00", "12:00")

        assertFalse(slot.hasStarted(Instant.parse("2026-11-20T09:59:59Z")))
        assertTrue(slot.hasStarted(Instant.parse("2026-11-20T10:00:00Z")))
        assertFalse(slot.hasEnded(Instant.parse("2026-11-20T11:59:59Z")))
        assertTrue(slot.hasEnded(Instant.parse("2026-11-20T12:00:00Z")))
    }

    @Test
    fun `time until start and duration`() {
        val slot = slot("10:00", "12:00")

        assertEquals(Duration.ofHours(2), slot.duration)
        assertEquals(
            Duration.ofHours(2),
            slot.timeUntilStart(Instant.parse("2026-11-20T08:00:00Z"))
        )
    }

    @Test
    fun `help request cannot last longer than the maximum`() {
        assertFailsWith<IllegalArgumentException> {
            HelpRequest(
                id = 1,
                title = "Moving day",
                description = "A whole day of moving boxes",
                category = ReportCategory.MOVING,
                location = Location(52.0, 4.0),
                volunteersNeeded = 2,
                slot = slot("08:00", "17:00")
            )
        }
    }
}
