// src/nl/brul/api/features/vote/repository/VoteRepository.kt
package nl.brul.api.features.vote.repository

import nl.brul.api.features.vote.model.Vote
import nl.brul.api.features.vote.model.VoteType

interface VoteRepository {

    fun findByReportAndUser(
        reportId: Long,
        userId: Long
    ): Vote?

    fun findByReport(reportId: Long): List<Vote>

    fun save(
        reportId: Long,
        userId: Long,
        type: VoteType
    ): Vote

    fun delete(
        reportId: Long,
        userId: Long
    ): Boolean
}