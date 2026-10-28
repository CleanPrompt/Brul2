// src/nl/brul/api/features/report/service/HelpRequestUpdateRules.kt
package nl.brul.api.features.report.service

import java.time.Instant
import nl.brul.api.common.error.DomainConflictException
import nl.brul.api.features.report.model.HelpRequest

/**
 * Rules for changing an existing help request.
 *
 * Pure function: everything it needs is passed in, so it can be tested
 * without a database or a real clock.
 */
object HelpRequestUpdateRules {

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
