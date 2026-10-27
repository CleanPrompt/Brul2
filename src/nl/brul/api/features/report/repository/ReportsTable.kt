// src/nl/brul/api/features/report/repository/ReportsTable.kt
package nl.brul.api.features.report.repository

import nl.brul.api.features.user.repository.UsersTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table

/**
 * Laag: repository (het geheugen).
 *
 * Beschrijft de tabel `reports` in de database. Alle drie de soorten meldingen staan in deze ene
 * tabel;
 * de kolom `type` zegt welk soort het is.
 * `urgent` en `volunteers_needed` mogen leeg zijn (nullable), omdat ze niet bij elk soort horen.
 * `owner_id` verwijst naar de tabel `users`. Wordt een gebruiker verwijderd, dan wordt dit veld
 * leeg (SET_NULL)
 * en blijft de melding bestaan.
 * `object`: er bestaat maar één tabelbeschrijving.
 */
object ReportsTable : Table("reports") {
    val id = long("id").autoIncrement()

    val ownerId = long("owner_id")
        .references(
            UsersTable.id,
            onDelete = ReferenceOption.SET_NULL
        )
        .nullable()

    val type = varchar("type", 30)
    val title = varchar("title", 120)
    val description = text("description")
    val latitude = double("latitude")
    val longitude = double("longitude")
    val status = varchar("status", 30)
    val urgent = bool("urgent").nullable()
    val category = varchar("category", 100)
    val volunteersNeeded = integer("volunteers_needed").nullable()
    val createdAt = long("created_at").default(0L)
    val viewCount = integer("view_count").default(0)

    override val primaryKey = PrimaryKey(id)
}