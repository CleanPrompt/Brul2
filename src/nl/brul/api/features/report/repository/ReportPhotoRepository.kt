// src/nl/brul/api/features/report/repository/ReportPhotoRepository.kt
package nl.brul.api.features.report.repository

import java.time.Instant
import nl.brul.api.features.report.model.PhotoKind
import nl.brul.api.features.report.model.ReportPhoto
import nl.brul.api.features.user.repository.UsersTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

/**
 * Laag: repository (het geheugen).
 *
 * Beschrijft de tabel `report_photos`: de gegevens van elke foto, niet de afbeelding zelf.
 * Wordt de melding verwijderd, dan verdwijnen de rijen mee (CASCADE). Wordt de uploader
 * verwijderd, dan wordt `uploader_id` leeg (SET_NULL) en blijft de foto bestaan.
 */
object ReportPhotosTable : Table("report_photos") {
    val id = long("id").autoIncrement()

    val reportId = long("report_id").references(
        ReportsTable.id,
        onDelete = ReferenceOption.CASCADE
    )

    val uploaderId = long("uploader_id")
        .references(
            UsersTable.id,
            onDelete = ReferenceOption.SET_NULL
        )
        .nullable()

    val kind = varchar("kind", 20)
    val contentType = varchar("content_type", 50)
    val sizeBytes = long("size_bytes")
    val storageKey = varchar("storage_key", 100)
    val createdAt = long("created_at")

    override val primaryKey = PrimaryKey(id)
}

/**
 * Laag: repository (het geheugen).
 *
 * Interface met de functies om foto-gegevens op te slaan en op te halen. ReportPhotoService kent
 * alleen deze interface; de echte implementatie is ExposedReportPhotoRepository.
 */
interface ReportPhotoRepository {

    fun add(
        reportId: Long,
        uploaderId: Long,
        kind: PhotoKind,
        contentType: String,
        sizeBytes: Long,
        storageKey: String,
        createdAt: Instant
    ): ReportPhoto

    fun findById(id: Long): ReportPhoto?

    fun findByReport(reportId: Long): List<ReportPhoto>

    fun countByReport(reportId: Long): Int

    /** Is er minstens één foto van deze soort? Nodig voor F8. */
    fun hasKind(reportId: Long, kind: PhotoKind): Boolean

    fun delete(id: Long): Boolean
}

/** De echte implementatie met Exposed en H2; elke functie draait in een `transaction`. */
class ExposedReportPhotoRepository(
    private val database: Database
) : ReportPhotoRepository {

    override fun add(
        reportId: Long,
        uploaderId: Long,
        kind: PhotoKind,
        contentType: String,
        sizeBytes: Long,
        storageKey: String,
        createdAt: Instant
    ): ReportPhoto = transaction(database) {
        val id = ReportPhotosTable.insert {
            it[ReportPhotosTable.reportId] = reportId
            it[ReportPhotosTable.uploaderId] = uploaderId
            it[ReportPhotosTable.kind] = kind.name
            it[ReportPhotosTable.contentType] = contentType
            it[ReportPhotosTable.sizeBytes] = sizeBytes
            it[ReportPhotosTable.storageKey] = storageKey
            it[ReportPhotosTable.createdAt] = createdAt.toEpochMilli()
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

    override fun hasKind(reportId: Long, kind: PhotoKind): Boolean = transaction(database) {
        ReportPhotosTable
            .selectAll()
            .where {
                (ReportPhotosTable.reportId eq reportId) and
                        (ReportPhotosTable.kind eq kind.name)
            }
            .count() > 0
    }

    override fun delete(id: Long): Boolean = transaction(database) {
        ReportPhotosTable.deleteWhere { ReportPhotosTable.id eq id } > 0
    }

    /** Zet een databaserij om naar een ReportPhoto. */
    private fun ResultRow.toPhoto(): ReportPhoto =
        ReportPhoto(
            id = this[ReportPhotosTable.id],
            reportId = this[ReportPhotosTable.reportId],
            uploaderId = this[ReportPhotosTable.uploaderId],
            kind = PhotoKind.valueOf(this[ReportPhotosTable.kind]),
            contentType = this[ReportPhotosTable.contentType],
            sizeBytes = this[ReportPhotosTable.sizeBytes],
            storageKey = this[ReportPhotosTable.storageKey],
            createdAt = Instant.ofEpochMilli(this[ReportPhotosTable.createdAt])
        )
}
