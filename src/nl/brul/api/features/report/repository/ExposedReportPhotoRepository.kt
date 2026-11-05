// src/nl/brul/api/features/report/repository/ExposedReportPhotoRepository.kt
package nl.brul.api.features.report.repository

import nl.brul.api.features.report.database.ReportPhotosTable
import nl.brul.api.features.report.mapper.toReportPhoto
import nl.brul.api.features.report.model.NewReportPhoto
import nl.brul.api.features.report.model.ReportPhoto
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

/** [ReportPhotoRepository] with Exposed and H2. Every function runs in its own transaction. */
class ExposedReportPhotoRepository(
    private val database: Database
) : ReportPhotoRepository {

    override fun add(photo: NewReportPhoto): ReportPhoto = transaction(database) {
        // Exposed retries a failed transaction; an insert should fail once, not three times.
        maxAttempts = 1

        val id = ReportPhotosTable.insert {
            it[reportId] = photo.reportId
            it[uploaderId] = photo.uploaderId
            it[contentType] = photo.contentType
            it[sizeBytes] = photo.sizeBytes
            it[storageKey] = photo.storageKey
            it[createdAt] = photo.createdAt.toEpochMilli()
            it[latitude] = photo.location?.location?.latitude
            it[longitude] = photo.location?.location?.longitude
            it[locationSource] = photo.location?.source
        } get ReportPhotosTable.id

        ReportPhoto(
            id = id,
            reportId = photo.reportId,
            uploaderId = photo.uploaderId,
            contentType = photo.contentType,
            sizeBytes = photo.sizeBytes,
            storageKey = photo.storageKey,
            createdAt = photo.createdAt,
            location = photo.location
        )
    }

    override fun findById(id: Long): ReportPhoto? = transaction(database) {
        ReportPhotosTable
            .selectAll()
            .where { ReportPhotosTable.id eq id }
            .singleOrNull()
            ?.toReportPhoto()
    }

    override fun findByReport(reportId: Long): List<ReportPhoto> = transaction(database) {
        ReportPhotosTable
            .selectAll()
            .where { ReportPhotosTable.reportId eq reportId }
            .orderBy(ReportPhotosTable.id)
            .map { it.toReportPhoto() }
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
}
