// src/nl/brul/api/features/vote/repository/ExposedVoteRepository.kt
package nl.brul.api.features.vote.repository

import nl.brul.api.features.vote.model.Vote
import nl.brul.api.features.vote.model.VoteType
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

class ExposedVoteRepository(
    private val database: Database
) : VoteRepository {

    override fun findByReportAndUser(
        reportId: Long,
        userId: Long
    ): Vote? = transaction(database) {

        VotesTable
            .selectAll()
            .where {
                (VotesTable.reportId eq reportId) and
                        (VotesTable.userId eq userId)
            }
            .singleOrNull()
            ?.toVote()
    }

    override fun findByReport(
        reportId: Long
    ): List<Vote> = transaction(database) {

        VotesTable
            .selectAll()
            .where {
                VotesTable.reportId eq reportId
            }
            .map { it.toVote() }
    }

    override fun save(
        reportId: Long,
        userId: Long,
        type: VoteType
    ): Vote = transaction(database) {

        require(reportId > 0) {
            "Report ID must be positive"
        }

        require(userId > 0) {
            "User ID must be positive"
        }

        val existing = VotesTable
            .selectAll()
            .where {
                (VotesTable.reportId eq reportId) and
                        (VotesTable.userId eq userId)
            }
            .singleOrNull()

        if (existing == null) {
            val statement = VotesTable.insert {
                it[VotesTable.reportId] = reportId
                it[VotesTable.userId] = userId
                it[VotesTable.type] = type
            }

            Vote(
                id = statement[VotesTable.id],
                reportId = reportId,
                userId = userId,
                type = type
            )
        } else {
            VotesTable.update({
                (VotesTable.reportId eq reportId) and
                        (VotesTable.userId eq userId)
            }) {
                it[VotesTable.type] = type
            }

            Vote(
                id = existing[VotesTable.id],
                reportId = reportId,
                userId = userId,
                type = type
            )
        }
    }

    override fun delete(
        reportId: Long,
        userId: Long
    ): Boolean = transaction(database) {

        VotesTable.deleteWhere {
            (VotesTable.reportId eq reportId) and
                    (VotesTable.userId eq userId)
        } > 0
    }

    private fun ResultRow.toVote(): Vote =
        Vote(
            id = this[VotesTable.id],
            reportId = this[VotesTable.reportId],
            userId = this[VotesTable.userId],
            type = this[VotesTable.type]
        )
}