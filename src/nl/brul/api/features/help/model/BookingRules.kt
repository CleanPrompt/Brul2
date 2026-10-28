// src/nl/brul/api/features/help/model/BookingRules.kt
package nl.brul.api.features.help.model

import java.time.Duration

/**
 * The agreed numbers behind the booking rules, in one place.
 * Change them here and every rule and test follows.
 */
object BookingRules {

    /** R7: leaving at least this long before the start is a normal cancellation. */
    val LATE_CANCELLATION_WINDOW: Duration = Duration.ofHours(2)

    /** R11: this many no-shows within [NO_SHOW_WINDOW] blocks new sign-ups. */
    const val NO_SHOW_LIMIT = 3

    /** R11: how far back no-shows are counted. */
    val NO_SHOW_WINDOW: Duration = Duration.ofDays(30)
}
