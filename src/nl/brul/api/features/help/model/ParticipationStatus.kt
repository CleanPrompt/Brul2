// src/nl/brul/api/features/help/model/ParticipationStatus.kt
package nl.brul.api.features.help.model

import kotlinx.serialization.Serializable

/**
 * The life cycle of one volunteer's sign-up for one help request.
 *
 * The allowed transitions form a small state machine. It lives on the enum
 * itself, so no code can move a participation into an impossible state.
 *
 *   WAITLISTED -> SIGNED_UP (promoted, R8) | CANCELLED
 *   SIGNED_UP  -> CANCELLED | LATE_CANCELLED (R7) | ATTENDED | NO_SHOW (R9)
 *   CANCELLED  -> SIGNED_UP | WAITLISTED (signing up again, R4)
 *   LATE_CANCELLED, ATTENDED, NO_SHOW are final.
 */
@Serializable
enum class ParticipationStatus {
    WAITLISTED,
    SIGNED_UP,
    CANCELLED,
    LATE_CANCELLED,
    ATTENDED,
    NO_SHOW;

    /** Active participations still count: a place or a spot on the waitlist. */
    val isActive: Boolean
        get() = this == WAITLISTED || this == SIGNED_UP

    fun canMoveTo(next: ParticipationStatus): Boolean = when (this) {
        WAITLISTED -> next == SIGNED_UP || next == CANCELLED
        SIGNED_UP -> next in setOf(CANCELLED, LATE_CANCELLED, ATTENDED, NO_SHOW)
        CANCELLED -> next == SIGNED_UP || next == WAITLISTED
        LATE_CANCELLED, ATTENDED, NO_SHOW -> false
    }
}
