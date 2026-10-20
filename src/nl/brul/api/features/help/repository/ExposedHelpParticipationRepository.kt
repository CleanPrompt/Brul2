// src/nl/brul/api/features/help/repository/ExposedHelpParticipationRepository.kt
package nl.brul.api.features.help.repository

import nl.brul.api.features.help.model.HelpParticipation
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

class ExposedHelpParticipationRepository(
    private val database: Database
) : HelpParticipationRepository {

    override fun findByHelpRequest(
        helpRequestId: Long
    ): List<HelpParticipation> = transaction(database) {

        HelpParticipationsTable
            .selectAll()
            .where {
                HelpParticipationsTable.helpRequestId eq helpRequestId
            }
            .orderBy(HelpParticipationsTable.id)
            .map { it.toParticipation() }
    }

    override fun findByHelpRequestAndUser(
        helpRequestId: Long,
        userId: Long
    ): HelpParticipation? = transaction(database) {

        HelpParticipationsTable
            .selectAll()
            .where {
                (HelpParticipationsTable.helpRequestId eq helpRequestId) and
                        (HelpParticipationsTable.userId eq userId)
            }
            .singleOrNull()
            ?.toParticipation()
    }

    override fun add(
        helpRequestId: Long,
        userId: Long
    ): HelpParticipation = transaction(database) {

        require(helpRequestId > 0) {
            "Help request ID must be positive."
        }

        require(userId > 0) {
            "User ID must be positive."
        }

        val id = HelpParticipationsTable.insert {
            it[HelpParticipationsTable.helpRequestId] = helpRequestId
            it[HelpParticipationsTable.userId] = userId
        } get HelpParticipationsTable.id

        HelpParticipation(
            id = id,
            helpRequestId = helpRequestId,
            userId = userId
        )
    }

    override fun remove(
        helpRequestId: Long,
        userId: Long
    ): Boolean = transaction(database) {

        HelpParticipationsTable.deleteWhere {
            (HelpParticipationsTable.helpRequestId eq helpRequestId) and
                    (HelpParticipationsTable.userId eq userId)
        } > 0
    }

    override fun countByHelpRequest(
        helpRequestId: Long
    ): Int = transaction(database) {

        HelpParticipationsTable
            .selectAll()
            .where {
                HelpParticipationsTable.helpRequestId eq helpRequestId
            }
            .count()
            .toInt()
    }

    private fun ResultRow.toParticipation(): HelpParticipation =
        HelpParticipation(
            id = this[HelpParticipationsTable.id],
            helpRequestId = this[HelpParticipationsTable.helpRequestId],
            userId = this[HelpParticipationsTable.userId]
        )
}