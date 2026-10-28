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

@Serializable
data class HelpParticipationResponse(
    val id: Long,
    val helpRequestId: Long,
    val userId: Long,
    val status: ParticipationStatus,
    val joinedAt: String,
    /** Only when WAITLISTED: 1 means next in line. */
    val waitlistPosition: Int? = null
)

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

@Serializable
data class AttendanceRequest(
    val attended: Boolean
)

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

@Serializable
data class ReliabilityResponse(
    val noShowsLast30Days: Int,
    val blocked: Boolean,
    val blockedUntil: String? = null
)

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
