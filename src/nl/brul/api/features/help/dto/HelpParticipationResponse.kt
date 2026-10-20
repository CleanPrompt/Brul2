// src/nl/brul/api/features/help/dto/HelpParticipationResponse.kt
package nl.brul.api.features.help.dto

import kotlinx.serialization.Serializable
import nl.brul.api.features.help.model.HelpParticipation
import nl.brul.api.features.help.model.HelpParticipationStatus
import nl.brul.api.features.help.service.HelpParticipationSummary

@Serializable
data class HelpParticipationResponse(
    val id: Long,
    val helpRequestId: Long,
    val userId: Long
)

@Serializable
data class HelpParticipationSummaryResponse(
    val helpRequestId: Long,
    val volunteersNeeded: Int,
    val participantCount: Int,
    val availablePlaces: Int,
    val status: HelpParticipationStatus
)

fun HelpParticipation.toResponse() =
    HelpParticipationResponse(
        id = id,
        helpRequestId = helpRequestId,
        userId = userId
    )

fun HelpParticipationSummary.toResponse() =
    HelpParticipationSummaryResponse(
        helpRequestId = helpRequestId,
        volunteersNeeded = volunteersNeeded,
        participantCount = participantCount,
        availablePlaces = availablePlaces,
        status = status
    )