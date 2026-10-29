// src/nl/brul/api/features/help/repository/HelpParticipationRepository.kt
package nl.brul.api.features.help.repository

import java.time.Instant
import nl.brul.api.features.help.model.HelpParticipation
import nl.brul.api.features.help.model.ParticipationStatus

/**
 * Laag: repository (het geheugen).
 *
 * Interface met de functies om aanmeldingen op te slaan en op te halen.
 * De service kent alleen deze interface; in tests geef je een nep-repository mee
 * (Dependency Inversion uit SOLID). De echte implementatie is ExposedHelpParticipationRepository.
 * Er is geen `delete`: een aanmelding verdwijnt nooit, alleen de status verandert.
 */
interface HelpParticipationRepository {

    /** Alle aanmeldingen van een hulpvraag, in elke status. */
    fun findByHelpRequest(
        helpRequestId: Long
    ): List<HelpParticipation>

    /** De aanmelding van één vrijwilliger bij één hulpvraag, of `null`. */
    fun findByHelpRequestAndUser(
        helpRequestId: Long,
        userId: Long
    ): HelpParticipation?

    /** Alle aanmeldingen van één vrijwilliger, in elke status. */
    fun findByUser(
        userId: Long
    ): List<HelpParticipation>

    /** Nieuwe aanmelding; standaard SIGNED_UP, voor de wachtlijst geef je WAITLISTED mee. */
    fun add(
        helpRequestId: Long,
        userId: Long,
        status: ParticipationStatus = ParticipationStatus.SIGNED_UP,
        joinedAt: Instant = Instant.now()
    ): HelpParticipation

    /** Slaat status, `joinedAt` en `changedAt` van een bestaande aanmelding op. */
    fun update(
        participation: HelpParticipation
    ): Boolean

    /** Het aantal vrijwilligers dat een plek heeft (status SIGNED_UP). */
    fun countSignedUp(
        helpRequestId: Long
    ): Int
}
