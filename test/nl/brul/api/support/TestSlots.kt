// test/nl/brul/api/support/TestSlots.kt
package nl.brul.api.support

import java.time.Duration
import java.time.Instant
import java.time.temporal.ChronoUnit
import nl.brul.api.features.report.model.TimeSlot

/**
 * Time slots for tests that only need "some valid help request".
 *
 * The default slot is a week from now, 10:00-12:00 UTC, so it is always in
 * the future (R1) regardless of when the tests run. Tests about the timing
 * rules themselves use a fixed clock instead.
 */
object TestSlots {

    val slot: TimeSlot = run {
        val start = Instant.now()
            .truncatedTo(ChronoUnit.DAYS)
            .plus(Duration.ofDays(7))
            .plus(Duration.ofHours(10))

        TimeSlot(start, start.plus(Duration.ofHours(2)))
    }

    val STARTS_AT: String = slot.start.toString()
    val ENDS_AT: String = slot.end.toString()
}
