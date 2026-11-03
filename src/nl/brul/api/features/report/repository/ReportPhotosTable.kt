// src/nl/brul/api/features/report/repository/ReportPhotosTable.kt
package nl.brul.api.features.report.repository

import nl.brul.api.features.user.repository.UsersTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table

/**
 * Laag: repository (het geheugen).
 *
 * De tabel `report_photos`: de gegevens van elke foto. De afbeelding zelf staat in PhotoStorage.
 * - `report_id` verwijst naar `reports` met CASCADE: verdwijnt de melding, dan ook de foto-rijen.
 *   De bestanden ruimt ReportService zelf op.
 * - `uploader_id` verwijst naar `users` met SET_NULL: verdwijnt de gebruiker, dan blijft de foto.
 * - `latitude`, `longitude` en `location_source` zijn leeg als de foto geen locatie heeft.
 */
object ReportPhotosTable : Table("report_photos") {
    val id = long("id").autoIncrement()

    val reportId = long("report_id").references(
        ReportsTable.id,
        onDelete = ReferenceOption.CASCADE
    )

    val uploaderId = long("uploader_id")
        .references(UsersTable.id, onDelete = ReferenceOption.SET_NULL)
        .nullable()

    val contentType = varchar("content_type", 30)
    val sizeBytes = long("size_bytes")
    val storageKey = varchar("storage_key", 100).uniqueIndex()
    val createdAt = long("created_at")

    val latitude = double("latitude").nullable()
    val longitude = double("longitude").nullable()
    val locationSource = varchar("location_source", 10).nullable()

    override val primaryKey = PrimaryKey(id)

    init {
        index(false, reportId)
    }
}
