// src/nl/brul/api/features/help/model/VolunteerRecord.kt
package nl.brul.api.features.help.model

import java.time.Instant
import nl.brul.api.features.report.model.TimeSlot

/**
 * Laag: model (het domein).
 *
 * Een momentopname van één vrijwilliger, met precies wat de reserveringsregels nodig hebben:
 * zijn andere afspraken en zijn no-shows.
 * De service vult dit vanuit de database. De regels zelf hoeven de database dus niet te kennen
 * en zijn daardoor makkelijk te testen.
 */
data class VolunteerRecord(
    val userId: Long,
    /** Tijdsloten van andere hulpvragen waar deze vrijwilliger SIGNED_UP is. */
    val commitments: List<TimeSlot> = emptyList(),
    /** Momenten waarop deze vrijwilliger als NO_SHOW is gemarkeerd. */
    val noShowTimes: List<Instant> = emptyList()
) {
    /** R5: true als de vrijwilliger tijdens [slot] al ergens anders is ingeschreven. */
    fun hasConflictWith(slot: TimeSlot): Boolean =
        commitments.any { it.overlaps(slot) }

    /** De no-shows binnen het schuivende venster van 30 dagen dat eindigt op [now]. */
    fun recentNoShows(now: Instant): List<Instant> {
        val windowStart = now.minus(BookingRules.NO_SHOW_WINDOW)
        return noShowTimes.filter { it > windowStart && it <= now }
    }

    /** R11: geblokkeerd zolang er te veel recente no-shows zijn. */
    fun isBlocked(now: Instant): Boolean =
        recentNoShows(now).size >= BookingRules.NO_SHOW_LIMIT

    /**
     * Wanneer de blokkade afloopt: zodra de [BookingRules.NO_SHOW_LIMIT]-de no-show (geteld vanaf
     * de nieuwste) uit het venster schuift, zijn er te weinig over om te blokkeren.
     * `null` als de vrijwilliger niet geblokkeerd is.
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
