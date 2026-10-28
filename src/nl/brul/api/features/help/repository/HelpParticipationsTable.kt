// src/nl/brul/api/features/help/repository/HelpParticipationsTable.kt
package nl.brul.api.features.help.repository

import nl.brul.api.features.report.repository.ReportsTable
import nl.brul.api.features.user.repository.UsersTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table

object HelpParticipationsTable : Table("help_participations") {
    val id = long("id").autoIncrement()

    val helpRequestId = long("help_request_id").references(
        ReportsTable.id,
        onDelete = ReferenceOption.CASCADE
    )

    val userId = long("user_id").references(
        UsersTable.id,
        onDelete = ReferenceOption.CASCADE
    )

    // One row per volunteer per help request; leaving or attending only
    // changes the status (see ParticipationStatus).
    val status = varchar("status", 20).default("SIGNED_UP")

    // Epoch milliseconds, like created_at elsewhere.
    val joinedAt = long("joined_at").default(0L)
    val changedAt = long("changed_at").default(0L)

    override val primaryKey = PrimaryKey(id)

    init {
        uniqueIndex(
            "uq_help_participation_request_user",
            helpRequestId,
            userId
        )
    }
}