// src/nl/brul/api/features/report/repository/ExposedReportPhotoRepository.kt
package nl.brul.api.features.report.repository

import nl.brul.api.features.report.model.Location
import nl.brul.api.features.report.model.ReportPhoto
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

/**
 * Laag: repository (het geheugen).
 *
 * De echte ReportPhotoRepository, met Exposed en H2. Alles gebeurt in een `transaction`.
 * Bij het teruglezen worden `latitude` en `longitude` weer een Location, en `location_source`
 * weer de enum LocationSource.
 */
class ExposedReportPhotoRepository(
    private val database: Database
) : ReportPhotoRepository {

    override fun add(
        reportId: Long,
        uploaderId: Long,
        contentType: String,
        sizeBytes: Long,
        storageKey: String,
        createdAt: Long,
        location: Location?,
        locationSource: ReportPhoto.LocationSource?
    ): ReportPhoto = transaction(database) {
        val id = ReportPhotosTable.insert {
            it[ReportPhotosTable.reportId] = reportId
            it[ReportPhotosTable.uploaderId] = uploaderId
            it[ReportPhotosTable.contentType] = contentType
            it[ReportPhotosTable.sizeBytes] = sizeBytes
            it[ReportPhotosTable.storageKey] = storageKey
            it[ReportPhotosTable.createdAt] = createdAt
            it[latitude] = location?.latitude
            it[longitude] = location?.longitude
            it[ReportPhotosTable.locationSource] = locationSource?.name
        } get ReportPhotosTable.id

        requireNotNull(findById(id)) {
            "Created photo could not be retrieved."
        }
    }

    override fun findById(id: Long): ReportPhoto? = transaction(database) {
        ReportPhotosTable
            .selectAll()
            .where { ReportPhotosTable.id eq id }
            .singleOrNull()
            ?.toPhoto()
    }

    override fun findByReport(reportId: Long): List<ReportPhoto> = transaction(database) {
        ReportPhotosTable
            .selectAll()
            .where { ReportPhotosTable.reportId eq reportId }
            .orderBy(ReportPhotosTable.id)
            .map { it.toPhoto() }
    }

    override fun countByReport(reportId: Long): Int = transaction(database) {
        ReportPhotosTable
            .selectAll()
            .where { ReportPhotosTable.reportId eq reportId }
            .count()
            .toInt()
    }

    override fun delete(id: Long): Boolean = transaction(database) {
        ReportPhotosTable.deleteWhere { ReportPhotosTable.id eq id } > 0
    }

    private fun ResultRow.toPhoto(): ReportPhoto {
        val latitude = this[ReportPhotosTable.latitude]
        val longitude = this[ReportPhotosTable.longitude]

        val location = if (latitude != null && longitude != null) {
            Location(latitude, longitude)
        } else {
            null
        }

        return ReportPhoto(
            id = this[ReportPhotosTable.id],
            reportId = this[ReportPhotosTable.reportId],
            uploaderId = this[ReportPhotosTable.uploaderId],
            contentType = this[ReportPhotosTable.contentType],
            sizeBytes = this[ReportPhotosTable.sizeBytes],
            storageKey = this[ReportPhotosTable.storageKey],
            createdAt = this[ReportPhotosTable.createdAt],
            location = location,
            locationSource = location?.let {
                this[ReportPhotosTable.locationSource]?.let { ReportPhoto.LocationSource.valueOf(it) }
            }
        )
    }
}
