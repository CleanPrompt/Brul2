// src/nl/brul/api/features/help/model/HelpRequestPhase.kt
package nl.brul.api.features.help.model

import kotlinx.serialization.Serializable

/**
 * Laag: model (het domein).
 *
 * Waar een hulpvraag op dit moment staat: OPEN, FULL, IN_PROGRESS, COMPLETED of CLOSED.
 * De fase wordt nooit opgeslagen, maar telkens berekend uit het tijdslot, het aantal
 * vrijwilligers en de status van de melding (zie `HelpRequestBooking.phaseAt`).
 * Daardoor kan hij nooit verouderd zijn: om 10:00 begint een hulpvraag vanzelf, zonder dat er
 * ergens een taak hoeft te draaien die de status aanpast.
 */
@Serializable
enum class HelpRequestPhase {
    /** Vóór de start, met vrije plekken. */
    OPEN,

    /** Vóór de start, alle plekken bezet; nieuwe vrijwilligers komen op de wachtlijst. */
    FULL,

    /** Het tijdslot is begonnen. */
    IN_PROGRESS,

    /** Het tijdslot is voorbij. */
    COMPLETED,

    /** De eigenaar heeft de hulpvraag gesloten (status RESOLVED). */
    CLOSED
}
