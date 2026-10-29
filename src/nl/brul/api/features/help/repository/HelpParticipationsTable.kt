// src/nl/brul/api/features/help/repository/HelpParticipationsTable.kt
package nl.brul.api.features.help.repository

import nl.brul.api.features.report.repository.ReportsTable
import nl.brul.api.features.user.repository.UsersTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table

/**
 * Laag: repository (het geheugen).
 *
 * Beschrijft de tabel `help_participations`: wie zich voor welke hulpvraag heeft aangemeld.
 * Per vrijwilliger per hulpvraag is er precies één rij; de unieke index op
 * (`help_request_id`, `user_id`) dwingt dat in de database zelf af.
 * Afmelden of komen opdagen verandert alleen de `status` (zie ParticipationStatus).
 * `joined_at` en `changed_at` zijn milliseconden sinds 1970, net als `created_at` bij meldingen.
 * Wordt de hulpvraag of de gebruiker verwijderd, dan verdwijnen de aanmeldingen mee (CASCADE).
 * In een bestaande database voegt DatabaseMigrations de nieuwe kolommen toe.
 */
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