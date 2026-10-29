// src/nl/brul/api/features/report/service/HelpRequestUpdateRules.kt
package nl.brul.api.features.report.service

import java.time.Instant
import nl.brul.api.common.error.DomainConflictException
import nl.brul.api.features.report.model.HelpRequest

/**
 * Laag: service (de regels).
 *
 * De regels voor het wijzigen van een bestaande hulpvraag:
 *
 * - R10: `volunteersNeeded` mag niet lager dan het aantal vrijwilligers dat al SIGNED_UP is
 * - R12: het tijdslot ligt vast zodra de hulpvraag begonnen is of er iemand is ingeschreven
 *   (409 SLOT_LOCKED); een nieuw tijdslot moet in de toekomst liggen
 *
 * Een pure functie: alles wat hij nodig heeft, komt binnen als parameter. Daardoor is hij te
 * testen zonder database en zonder echte klok (zie HelpRequestUpdateRulesTest).
 */
object HelpRequestUpdateRules {

    /** Gooit een exception als de wijziging niet mag; anders gebeurt er niets. */
    fun validate(
        existing: HelpRequest,
        updated: HelpRequest,
        signedUpCount: Int,
        now: Instant
    ) {
        // R10: never fewer places than volunteers already signed up.
        require(updated.volunteersNeeded >= signedUpCount) {
            "Volunteers needed cannot be lower than the current participant count."
        }

        if (updated.slot == existing.slot) {
            return
        }

        // R12: the slot is fixed once it has started or volunteers committed to it.
        if (existing.slot.hasStarted(now)) {
            throw DomainConflictException(
                code = "SLOT_LOCKED",
                message = "The time slot cannot be changed after the help request has started."
            )
        }

        if (signedUpCount > 0) {
            throw DomainConflictException(
                code = "SLOT_LOCKED",
                message = "The time slot cannot be changed while volunteers are signed up."
            )
        }

        require(!updated.slot.hasStarted(now)) {
            "A help request must start in the future."
        }
    }
}
