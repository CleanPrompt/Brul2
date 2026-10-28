// src/nl/brul/api/features/help/model/HelpRequestPhase.kt
package nl.brul.api.features.help.model

import kotlinx.serialization.Serializable

/**
 * Where a help request stands right now.
 *
 * Never stored: it is derived from the time slot, the number of
 * volunteers and the report status (see HelpRequestBooking.phaseAt).
 * Because of that it can never be out of date.
 */
@Serializable
enum class HelpRequestPhase {
    /** Before the start, with free places. */
    OPEN,

    /** Before the start, all places taken; new volunteers go on the waitlist. */
    FULL,

    /** The slot has started. */
    IN_PROGRESS,

    /** The slot has ended. */
    COMPLETED,

    /** The owner closed the request (status RESOLVED). */
    CLOSED
}
