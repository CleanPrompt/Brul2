// src/nl/brul/api/features/help/model/HelpParticipation.kt
package nl.brul.api.features.help.model

import java.time.Instant

/**
 * One volunteer's sign-up for one help request.
 *
 * Rows are never deleted: leaving, attending or not showing up are status
 * changes, so the history stays available (for example to count no-shows).
 */
data class HelpParticipation(
    val id: Long,
    val helpRequestId: Long,
    val userId: Long,
    val status: ParticipationStatus = ParticipationStatus.SIGNED_UP,
    /** Moment of (re)joining; decides the order on the waitlist. */
    val joinedAt: Instant = Instant.EPOCH,
    /** Moment of the last status change. */
    val changedAt: Instant = joinedAt
) {
    init {
        require(id > 0) {
            "Participation ID must be positive."
        }

        require(helpRequestId > 0) {
            "Help request ID must be positive."
        }

        require(userId > 0) {
            "User ID must be positive."
        }
    }

    val isActive: Boolean
        get() = status.isActive

    /**
     * Returns a copy with the new status, if the state machine allows it.
     * Signing up again after a cancellation puts you at the back of the queue.
     */
    fun moveTo(next: ParticipationStatus, now: Instant): HelpParticipation {
        require(status.canMoveTo(next)) {
            "Cannot change participation from $status to $next."
        }

        val rejoining = status == ParticipationStatus.CANCELLED

        return copy(
            status = next,
            joinedAt = if (rejoining) now else joinedAt,
            changedAt = now
        )
    }
}
