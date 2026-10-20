// src/nl/brul/api/features/vote/service/VoteService.kt
package nl.brul.api.features.vote.service

import nl.brul.api.features.report.repository.ReportRepository
import nl.brul.api.features.vote.model.Vote
import nl.brul.api.features.vote.model.VoteSummary
import nl.brul.api.features.vote.model.VoteType
import nl.brul.api.features.vote.repository.VoteRepository

class VoteService(
    private val voteRepository: VoteRepository,
    private val reportRepository: ReportRepository
) {

    fun castVote(
        reportId: Long,
        userId: Long,
        type: VoteType
    ): Vote {
        require(reportId > 0) {
            "Report ID must be positive"
        }

        require(userId > 0) {
            "User ID must be positive"
        }

        val report = reportRepository.findById(reportId)
            ?: throw NoSuchElementException(
                "Report not found"
            )

        require(report.ownerId != userId) {
            "Users cannot vote on their own reports"
        }

        return voteRepository.save(
            reportId = reportId,
            userId = userId,
            type = type
        )
    }

    fun removeVote(
        reportId: Long,
        userId: Long
    ): Boolean {
        require(reportId > 0) {
            "Report ID must be positive"
        }

        require(userId > 0) {
            "User ID must be positive"
        }

        return voteRepository.delete(
            reportId = reportId,
            userId = userId
        )
    }

    fun getUserVote(
        reportId: Long,
        userId: Long
    ): Vote? {
        require(reportId > 0)
        require(userId > 0)

        return voteRepository.findByReportAndUser(
            reportId,
            userId
        )
    }

    fun getSummary(reportId: Long): VoteSummary {
        require(reportId > 0) {
            "Report ID must be positive"
        }

        reportRepository.findById(reportId)
            ?: throw NoSuchElementException(
                "Report not found"
            )

        val votes = voteRepository.findByReport(reportId)

        return VoteSummary(
            reportId = reportId,
            likes = votes.count {
                it.type == VoteType.LIKE
            },
            dislikes = votes.count {
                it.type == VoteType.DISLIKE
            }
        )
    }
}