// src/nl/brul/api/features/help/repository/ExposedHelpParticipationRepository.kt
package nl.brul.api.features.help.repository

import java.time.Instant
import nl.brul.api.features.help.model.HelpParticipation
import nl.brul.api.features.help.model.ParticipationStatus
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

/**
 * Laag: repository (het geheugen).
 *
 * De echte implementatie van HelpParticipationRepository, met Exposed en H2.
 * Elke functie opent een `transaction`. Wordt hij aangeroepen binnen een blok van
 * TransactionRunner, dan doet hij mee in die buitenste transactie in plaats van een eigen te
 * starten. Zo kan de service lezen, beslissen en opslaan als één geheel.
 * De status staat als tekst in de database (`status.name`) en wordt bij het teruglezen weer een
 * enum (`ParticipationStatus.valueOf`).
 */
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

    override fun findByUser(
        userId: Long
    ): List<HelpParticipation> = transaction(database) {

        HelpParticipationsTable
            .selectAll()
            .where {
                HelpParticipationsTable.userId eq userId
            }
            .orderBy(HelpParticipationsTable.id)
            .map { it.toParticipation() }
    }

    override fun add(
        helpRequestId: Long,
        userId: Long,
        status: ParticipationStatus,
        joinedAt: Instant
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
            it[HelpParticipationsTable.status] = status.name
            it[HelpParticipationsTable.joinedAt] = joinedAt.toEpochMilli()
            it[HelpParticipationsTable.changedAt] = joinedAt.toEpochMilli()
        } get HelpParticipationsTable.id

        HelpParticipation(
            id = id,
            helpRequestId = helpRequestId,
            userId = userId,
            status = status,
            joinedAt = Instant.ofEpochMilli(joinedAt.toEpochMilli()),
            changedAt = Instant.ofEpochMilli(joinedAt.toEpochMilli())
        )
    }

    override fun update(
        participation: HelpParticipation
    ): Boolean = transaction(database) {

        HelpParticipationsTable.update(
            where = { HelpParticipationsTable.id eq participation.id }
        ) {
            it[status] = participation.status.name
            it[joinedAt] = participation.joinedAt.toEpochMilli()
            it[changedAt] = participation.changedAt.toEpochMilli()
        } > 0
    }

    override fun countSignedUp(
        helpRequestId: Long
    ): Int = transaction(database) {

        HelpParticipationsTable
            .selectAll()
            .where {
                (HelpParticipationsTable.helpRequestId eq helpRequestId) and
                        (HelpParticipationsTable.status eq ParticipationStatus.SIGNED_UP.name)
            }
            .count()
            .toInt()
    }

    /** Zet een databaserij om naar een HelpParticipation; tijden worden weer een `Instant`. */
    private fun ResultRow.toParticipation(): HelpParticipation =
        HelpParticipation(
            id = this[HelpParticipationsTable.id],
            helpRequestId = this[HelpParticipationsTable.helpRequestId],
            userId = this[HelpParticipationsTable.userId],
            status = ParticipationStatus.valueOf(
                this[HelpParticipationsTable.status]
            ),
            joinedAt = Instant.ofEpochMilli(
                this[HelpParticipationsTable.joinedAt]
            ),
            changedAt = Instant.ofEpochMilli(
                this[HelpParticipationsTable.changedAt]
            )
        )
}
