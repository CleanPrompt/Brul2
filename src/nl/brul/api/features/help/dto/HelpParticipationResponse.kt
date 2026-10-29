// src/nl/brul/api/features/help/dto/HelpParticipationResponse.kt
package nl.brul.api.features.help.dto

import kotlinx.serialization.Serializable
import nl.brul.api.features.help.model.HelpParticipation
import nl.brul.api.features.help.model.HelpRequestPhase
import nl.brul.api.features.help.model.ParticipationStatus
import nl.brul.api.features.help.service.AgendaItem
import nl.brul.api.features.help.service.HelpParticipationSummary
import nl.brul.api.features.help.service.JoinResult
import nl.brul.api.features.help.service.Reliability

/**
 * Laag: dto (de vertaler tussen JSON en Kotlin).
 *
 * De JSON van één aanmelding, bijvoorbeeld na aanmelden (201) of afmelden (200).
 * `joinedAt` is tekst in ISO-8601 (UTC); de Android-app zet dat zelf om naar lokale tijd.
 */
@Serializable
data class HelpParticipationResponse(
    val id: Long,
    val helpRequestId: Long,
    val userId: Long,
    val status: ParticipationStatus,
    val joinedAt: String,
    /** Alleen bij WAITLISTED: 1 betekent dat je de volgende bent. */
    val waitlistPosition: Int? = null
)

/**
 * Samenvatting van een hulpvraag voor GET /reports/{id}/participants: plekken, bezetting,
 * wachtlijst, fase en tijdslot.
 */
@Serializable
data class HelpParticipationSummaryResponse(
    val helpRequestId: Long,
    val volunteersNeeded: Int,
    val participantCount: Int,
    val availablePlaces: Int,
    val waitlistCount: Int,
    val phase: HelpRequestPhase,
    val startsAt: String,
    val endsAt: String
)

/** De body van PUT .../attendance: `{"attended": true}`, of `false` voor een no-show. */
@Serializable
data class AttendanceRequest(
    val attended: Boolean
)

/** Eén regel in de agenda van GET /users/me/participations. */
@Serializable
data class AgendaItemResponse(
    val participationId: Long,
    val helpRequestId: Long,
    val title: String,
    val status: ParticipationStatus,
    val phase: HelpRequestPhase,
    val startsAt: String,
    val endsAt: String
)

/** Het antwoord van GET /users/me/reliability (R11). */
@Serializable
data class ReliabilityResponse(
    val noShowsLast30Days: Int,
    val blocked: Boolean,
    val blockedUntil: String? = null
)

/**
 * Extension functions: zetten de objecten uit de service om naar de DTO's hierboven.
 * Tijden worden tekst met `toString()`, bijvoorbeeld "2026-11-20T10:00:00Z".
 */
fun HelpParticipation.toResponse(waitlistPosition: Int? = null) =
    HelpParticipationResponse(
        id = id,
        helpRequestId = helpRequestId,
        userId = userId,
        status = status,
        joinedAt = joinedAt.toString(),
        waitlistPosition = waitlistPosition
    )

fun JoinResult.toResponse() =
    participation.toResponse(waitlistPosition)

fun HelpParticipationSummary.toResponse() =
    HelpParticipationSummaryResponse(
        helpRequestId = helpRequestId,
        volunteersNeeded = volunteersNeeded,
        participantCount = participantCount,
        availablePlaces = availablePlaces,
        waitlistCount = waitlistCount,
        phase = phase,
        startsAt = slot.start.toString(),
        endsAt = slot.end.toString()
    )

fun AgendaItem.toResponse() =
    AgendaItemResponse(
        participationId = participation.id,
        helpRequestId = helpRequest.id,
        title = helpRequest.title,
        status = participation.status,
        phase = phase,
        startsAt = helpRequest.slot.start.toString(),
        endsAt = helpRequest.slot.end.toString()
    )

fun Reliability.toResponse() =
    ReliabilityResponse(
        noShowsLast30Days = noShowsInWindow,
        blocked = blocked,
        blockedUntil = blockedUntil?.toString()
    )
