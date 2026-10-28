// src/nl/brul/api/features/help/model/VolunteerRecord.kt
package nl.brul.api.features.help.model

import java.time.Instant
import nl.brul.api.features.report.model.TimeSlot

/**
 * Snapshot of a volunteer, with exactly what the booking rules need.
 *
 * The service fills it from the database; the rules themselves stay pure.
 */
data class VolunteerRecord(
    val userId: Long,
    /** Slots of other help requests where this volunteer is SIGNED_UP. */
    val commitments: List<TimeSlot> = emptyList(),
    /** Moments at which this volunteer was marked NO_SHOW. */
    val noShowTimes: List<Instant> = emptyList()
) {
    /** R5: true when the volunteer is already busy during [slot]. */
    fun hasConflictWith(slot: TimeSlot): Boolean =
        commitments.any { it.overlaps(slot) }

    /** No-shows inside the sliding window that ends at [now]. */
    fun recentNoShows(now: Instant): List<Instant> {
        val windowStart = now.minus(BookingRules.NO_SHOW_WINDOW)
        return noShowTimes.filter { it > windowStart && it <= now }
    }

    /** R11: blocked while there are too many recent no-shows. */
    fun isBlocked(now: Instant): Boolean =
        recentNoShows(now).size >= BookingRules.NO_SHOW_LIMIT

    /**
     * When the block ends: the moment the oldest of the counted no-shows
     * slides out of the window. Null when not blocked.
     */
    fun blockedUntil(now: Instant): Instant? {
        if (!isBlocked(now)) {
            return null
        }

        val newestFirst = recentNoShows(now).sortedDescending()
        return newestFirst[BookingRules.NO_SHOW_LIMIT - 1]
            .plus(BookingRules.NO_SHOW_WINDOW)
    }
}
