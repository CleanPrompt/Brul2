// src/nl/brul/api/features/help/repository/HelpParticipationRepository.kt
package nl.brul.api.features.help.repository

import java.time.Instant
import nl.brul.api.features.help.model.HelpParticipation
import nl.brul.api.features.help.model.ParticipationStatus

interface HelpParticipationRepository {

    /** All participations of a help request, in every status. */
    fun findByHelpRequest(
        helpRequestId: Long
    ): List<HelpParticipation>

    fun findByHelpRequestAndUser(
        helpRequestId: Long,
        userId: Long
    ): HelpParticipation?

    /** All participations of one volunteer, in every status. */
    fun findByUser(
        userId: Long
    ): List<HelpParticipation>

    fun add(
        helpRequestId: Long,
        userId: Long,
        status: ParticipationStatus = ParticipationStatus.SIGNED_UP,
        joinedAt: Instant = Instant.now()
    ): HelpParticipation

    /** Saves status, joinedAt and changedAt of an existing participation. */
    fun update(
        participation: HelpParticipation
    ): Boolean

    /** Number of volunteers holding a place (status SIGNED_UP). */
    fun countSignedUp(
        helpRequestId: Long
    ): Int
}
