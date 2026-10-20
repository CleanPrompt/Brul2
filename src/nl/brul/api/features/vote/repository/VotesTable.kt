// src/nl/brul/api/features/vote/repository/VotesTable.kt
package nl.brul.api.features.vote.repository

import nl.brul.api.features.report.repository.ReportsTable
import nl.brul.api.features.vote.model.VoteType
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table

object VotesTable : Table("votes") {

    val id = long("id").autoIncrement()

    val reportId = long("report_id").references(
        ReportsTable.id,
        onDelete = ReferenceOption.CASCADE
    )

    val userId = long("user_id")

    val type = enumerationByName<VoteType>(
        "type",
        10
    )

    override val primaryKey = PrimaryKey(id)

    init {
        uniqueIndex(
            "uq_votes_report_user",
            reportId,
            userId
        )

        index(false, reportId)
    }
}