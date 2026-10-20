// src/nl/brul/api/features/help/repository/HelpParticipationsTable.kt
package nl.brul.api.features.help.repository

import nl.brul.api.features.report.repository.ReportsTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table

object HelpParticipationsTable : Table("help_participations") {

    val id = long("id").autoIncrement()

    val helpRequestId = long("help_request_id").references(
        ReportsTable.id,
        onDelete = ReferenceOption.CASCADE
    )

    val userId = long("user_id")

    override val primaryKey = PrimaryKey(id)

    init {
        uniqueIndex(
            "uq_help_participation_request_user",
            helpRequestId,
            userId
        )
    }
}