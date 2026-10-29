// src/nl/brul/api/features/help/model/BookingRules.kt
package nl.brul.api.features.help.model

import java.time.Duration

/**
 * Laag: model (het domein).
 *
 * De afgesproken getallen achter de reserveringsregels, op één plek.
 * Pas je ze hier aan, dan volgen alle regels en tests vanzelf.
 * `object`: er bestaat er precies één van. `const val` is een vaste waarde die al bij het
 * compileren bekend is; een `Duration` kan dat niet, daarom is dat een gewone `val`.
 */
object BookingRules {

    /** R7: wie minstens zo lang vóór de start afmeldt, meldt gewoon af (CANCELLED). */
    val LATE_CANCELLATION_WINDOW: Duration = Duration.ofHours(2)

    /** R11: zoveel no-shows binnen [NO_SHOW_WINDOW] blokkeren nieuwe aanmeldingen. */
    const val NO_SHOW_LIMIT = 3

    /** R11: hoe ver terug no-shows meetellen. */
    val NO_SHOW_WINDOW: Duration = Duration.ofDays(30)
}
