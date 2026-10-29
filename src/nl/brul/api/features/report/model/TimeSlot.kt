// src/nl/brul/api/features/report/model/TimeSlot.kt
package nl.brul.api.features.report.model

import java.time.Duration
import java.time.Instant

/**
 * Laag: model (het domein).
 *
 * Een periode met een vaste start en een vast eind, zoals een reservering. Elke hulpvraag heeft
 * er één (`slot`).
 * Value object, net als Location: twee tijdsloten met dezelfde start en hetzelfde eind zijn
 * gelijk (`data class`), en `init` zorgt dat de start altijd vóór het eind ligt (R1).
 * Alle functies zijn puur: de huidige tijd komt binnen als `now`, zodat je de regels met elk
 * vast moment kunt testen.
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

    /** Hoe lang het tijdslot duurt; een hulpvraag mag maximaal 8 uur duren. */
    val duration: Duration
        get() = Duration.between(start, end)

    /**
     * R5: twee tijdsloten overlappen als het ene begint voordat het andere eindigt.
     * Tijdsloten die elkaar alleen raken (10:00-12:00 en 12:00-14:00) overlappen niet.
     */
    fun overlaps(other: TimeSlot): Boolean =
        start < other.end && other.start < end

    /** Begonnen als `now` op of na de start ligt. */
    fun hasStarted(now: Instant): Boolean = now >= start

    /** Afgelopen als `now` op of na het eind ligt. */
    fun hasEnded(now: Instant): Boolean = now >= end

    /** Hoe lang het nog duurt tot de start; negatief als de start al voorbij is (R7). */
    fun timeUntilStart(now: Instant): Duration =
        Duration.between(now, start)
}
