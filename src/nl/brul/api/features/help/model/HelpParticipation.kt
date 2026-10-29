// src/nl/brul/api/features/help/model/HelpParticipation.kt
package nl.brul.api.features.help.model

import java.time.Instant

/**
 * Laag: model (het domein).
 *
 * Eén aanmelding van één vrijwilliger voor één hulpvraag, met een status (ParticipationStatus).
 * Rijen worden nooit verwijderd: afmelden, komen opdagen of wegblijven zijn statuswijzigingen.
 * Zo blijft de geschiedenis bewaard, bijvoorbeeld om no-shows te tellen (R11).
 * `init` controleert dat alle id's positief zijn.
 */
data class HelpParticipation(
    val id: Long,
    val helpRequestId: Long,
    val userId: Long,
    val status: ParticipationStatus = ParticipationStatus.SIGNED_UP,
    /** Moment van (opnieuw) aanmelden; bepaalt de volgorde op de wachtlijst. */
    val joinedAt: Instant = Instant.EPOCH,
    /** Moment van de laatste statuswijziging. */
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
     * Geeft een kopie met de nieuwe status, als de toestandsmachine dat toestaat; anders volgt een
     * IllegalArgumentException.
     * Wie zich na een afmelding opnieuw aanmeldt, sluit achteraan aan: `joinedAt` wordt dan `now`.
     * `copy` maakt een nieuw object; het oude blijft onveranderd (immutable).
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
