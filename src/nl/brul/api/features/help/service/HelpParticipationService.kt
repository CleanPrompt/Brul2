// src/nl/brul/api/features/help/service/HelpParticipationService.kt
package nl.brul.api.features.help.service

import nl.brul.api.features.help.model.HelpParticipation
import nl.brul.api.features.help.model.HelpParticipationStatus
import nl.brul.api.features.help.repository.HelpParticipationRepository
import nl.brul.api.features.report.model.HelpRequest
import nl.brul.api.features.report.model.ReportStatus
import nl.brul.api.features.report.repository.ReportRepository

data class HelpParticipationSummary(
    val helpRequestId: Long,
    val volunteersNeeded: Int,
    val participantCount: Int,
    val availablePlaces: Int,
    val status: HelpParticipationStatus
)

class HelpParticipationService(
    private val participationRepository: HelpParticipationRepository,
    private val reportRepository: ReportRepository
) {

    fun join(
        helpRequestId: Long,
        userId: Long
    ): HelpParticipation {
        require(userId > 0) {
            "User ID must be positive."
        }

        val request = findHelpRequest(helpRequestId)

        if (request.ownerId == userId) {
            throw HelpParticipationForbiddenException()
        }

        if (request.status != ReportStatus.OPEN) {
            throw HelpParticipationConflictException(
                "This help request is closed."
            )
        }

        val existing = participationRepository
            .findByHelpRequestAndUser(helpRequestId, userId)

        if (existing != null) {
            throw HelpParticipationConflictException(
                "You already participate in this help request."
            )
        }

        val participantCount =
            participationRepository.countByHelpRequest(helpRequestId)

        if (participantCount >= request.volunteersNeeded) {
            throw HelpParticipationConflictException(
                "This help request is full."
            )
        }

        return participationRepository.add(
            helpRequestId = helpRequestId,
            userId = userId
        )
    }

    fun leave(
        helpRequestId: Long,
        userId: Long
    ): Boolean {
        require(userId > 0) {
            "User ID must be positive."
        }

        findHelpRequest(helpRequestId)

        return participationRepository.remove(
            helpRequestId = helpRequestId,
            userId = userId
        )
    }

    fun getParticipants(
        helpRequestId: Long
    ): List<HelpParticipation> {
        findHelpRequest(helpRequestId)

        return participationRepository.findByHelpRequest(
            helpRequestId
        )
    }

    fun getSummary(
        helpRequestId: Long
    ): HelpParticipationSummary {
        val request = findHelpRequest(helpRequestId)

        val count = participationRepository.countByHelpRequest(
            helpRequestId
        )

        val available = (request.volunteersNeeded - count)
            .coerceAtLeast(0)

        val status = when {
            request.status != ReportStatus.OPEN ->
                HelpParticipationStatus.CLOSED

            available == 0 ->
                HelpParticipationStatus.FULL

            else ->
                HelpParticipationStatus.OPEN
        }

        return HelpParticipationSummary(
            helpRequestId = helpRequestId,
            volunteersNeeded = request.volunteersNeeded,
            participantCount = count,
            availablePlaces = available,
            status = status
        )
    }

    private fun findHelpRequest(id: Long): HelpRequest {
        require(id > 0) {
            "Help request ID must be positive."
        }

        return reportRepository.findById(id) as? HelpRequest
            ?: throw HelpRequestNotFoundException()
    }
}