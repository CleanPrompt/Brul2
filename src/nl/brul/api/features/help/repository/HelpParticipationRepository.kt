// src/nl/brul/api/features/help/repository/HelpParticipationRepository.kt
package nl.brul.api.features.help.repository

import nl.brul.api.features.help.model.HelpParticipation

interface HelpParticipationRepository {

    fun findByHelpRequest(
        helpRequestId: Long
    ): List<HelpParticipation>

    fun findByHelpRequestAndUser(
        helpRequestId: Long,
        userId: Long
    ): HelpParticipation?

    fun add(
        helpRequestId: Long,
        userId: Long
    ): HelpParticipation

    fun remove(
        helpRequestId: Long,
        userId: Long
    ): Boolean

    fun countByHelpRequest(
        helpRequestId: Long
    ): Int
}