// src/nl/brul/api/features/report/database/ReportPhotosTable.kt
package nl.brul.api.features.report.database

import nl.brul.api.features.report.model.LocationSource
import nl.brul.api.features.report.repository.ReportsTable
import nl.brul.api.features.user.repository.UsersTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table

/** The data of every photo. The image itself is stored in `PhotoStorage`, under `storage_key`. */
object ReportPhotosTable : Table("report_photos") {
    val id = long("id").autoIncrement()

    /** CASCADE: when a report is deleted, its photo rows go too. `ReportService` removes the files. */
    val reportId = long("report_id").references(ReportsTable.id, onDelete = ReferenceOption.CASCADE)

    /** SET_NULL: when a user is deleted, the photo stays. */
    val uploaderId = long("uploader_id")
        .references(UsersTable.id, onDelete = ReferenceOption.SET_NULL)
        .nullable()

    val contentType = varchar("content_type", 30)
    val sizeBytes = long("size_bytes")
    val storageKey = varchar("storage_key", 100).uniqueIndex()

    /** Epoch milliseconds, like `created_at` in `reports`; the mapper turns it into an `Instant`. */
    val createdAt = long("created_at")

    /** All three are empty when the photo has no location. The source is stored as the enum name (`PHOTO`). */
    val latitude = double("latitude").nullable()
    val longitude = double("longitude").nullable()
    val locationSource = enumerationByName("location_source", 10, LocationSource::class).nullable()

    override val primaryKey = PrimaryKey(id)

    init {
        index(false, reportId)
    }
}
