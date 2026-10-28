// src/nl/brul/api/features/report/model/TimeSlot.kt
package nl.brul.api.features.report.model

import java.time.Duration
import java.time.Instant

/**
 * A period with a fixed start and end, like a reservation.
 *
 * Value object (just like [Location]): two slots with the same start and end
 * are equal, and a slot can never exist with its start after its end.
 * All methods are pure: the current time is passed in as `now`, so the
 * rules can be tested with any fixed moment.
 */
data class TimeSlot(
    val start: Instant,
    val end: Instant
) {
    init {
        require(start < end) {
            "Start must be before end."
        }
    }

    val duration: Duration
        get() = Duration.between(start, end)

    /**
     * Two slots overlap when one starts before the other ends.
     * Slots that only touch (10:00-12:00 and 12:00-14:00) do not overlap.
     */
    fun overlaps(other: TimeSlot): Boolean =
        start < other.end && other.start < end

    fun hasStarted(now: Instant): Boolean = now >= start

    fun hasEnded(now: Instant): Boolean = now >= end

    fun timeUntilStart(now: Instant): Duration =
        Duration.between(now, start)
}
