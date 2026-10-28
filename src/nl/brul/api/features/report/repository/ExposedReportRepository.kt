// src/nl/brul/api/features/report/repository/ExposedReportRepository.kt
package nl.brul.api.features.report.repository

import nl.brul.api.features.report.model.HelpRequest
import nl.brul.api.features.report.model.Location
import nl.brul.api.features.report.model.NewReport
import nl.brul.api.features.report.model.NuisanceReport
import nl.brul.api.features.report.model.Report
import nl.brul.api.features.report.model.ReportCategory
import nl.brul.api.features.report.model.ReportStatus
import nl.brul.api.features.report.model.ReportType
import nl.brul.api.features.report.model.SafetyReport
import nl.brul.api.features.report.model.TimeSlot
import java.time.Instant
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.plus
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

/**
 * Laag: repository (het geheugen).
 *
 * De echte implementatie van ReportRepository, met de Exposed-bibliotheek en een H2-database.
 * Alles gebeurt in een `transaction`: gaat er iets mis, dan wordt alles teruggedraaid.
 *
 * - Nieuwe meldingen krijgen status OPEN, de huidige tijd en 0 views.
 * - `update` werkt alleen als het type gelijk blijft.
 * - `findByIdAndIncrementViews` telt de view op in de database zelf.
 *
 * De klok (`currentTimeMillis`) komt via de constructor binnen, zodat tests een vaste tijd kunnen
 * meegeven.
 */
class ExposedReportRepository(
    private val database: Database,
    private val currentTimeMillis: () -> Long = System::currentTimeMillis
) : ReportRepository {

    override fun create(report: NewReport): Report = transaction(database) {
        val timestamp = currentTimeMillis()

        require(timestamp > 0) {
            "Creation timestamp must be positive."
        }

        val id = ReportsTable.insert {
            it[type] = report.type.name
            it[ownerId] = report.ownerId
            it[title] = report.title
            it[description] = report.description
            it[category] = report.category.name
            it[latitude] = report.location.latitude
            it[longitude] = report.location.longitude
            it[status] = ReportStatus.OPEN.name
            it[urgent] = report.urgent
            it[volunteersNeeded] = report.volunteersNeeded
            it[slotStart] = report.slot?.start?.toEpochMilli()
            it[slotEnd] = report.slot?.end?.toEpochMilli()
            it[createdAt] = timestamp
            it[viewCount] = 0
        } get ReportsTable.id

        requireNotNull(findById(id)) {
            "Created report could not be retrieved."
        }
    }

    override fun findAll(): List<Report> = transaction(database) {
        ReportsTable
            .selectAll()
            .orderBy(ReportsTable.id)
            .map { row -> row.toReport() }
    }

    override fun findById(id: Long): Report? = transaction(database) {
        ReportsTable
            .selectAll()
            .where { ReportsTable.id eq id }
            .singleOrNull()
            ?.toReport()
    }

    override fun findByIdAndIncrementViews(id: Long): Report? =
        transaction(database) {
            val updatedRows = ReportsTable.update(
                where = { ReportsTable.id eq id }
            ) {
                it[viewCount] = viewCount + 1
            }

            if (updatedRows == 0) {
                return@transaction null
            }

            ReportsTable
                .selectAll()
                .where { ReportsTable.id eq id }
                .singleOrNull()
                ?.toReport()
        }

    override fun update(report: Report): Boolean = transaction(database) {
        val updatedRows = ReportsTable.update(
            where = {
                (ReportsTable.id eq report.id) and
                        (ReportsTable.type eq report.type.name)
            }
        ) {
            it[title] = report.title
            it[description] = report.description
            it[category] = report.category.name
            it[latitude] = report.location.latitude
            it[longitude] = report.location.longitude
            it[status] = report.status.name

            when (report) {
                is SafetyReport -> {
                    it[urgent] = report.urgent
                    it[volunteersNeeded] = null
                    it[slotStart] = null
                    it[slotEnd] = null
                }

                is NuisanceReport -> {
                    it[urgent] = null
                    it[volunteersNeeded] = null
                    it[slotStart] = null
                    it[slotEnd] = null
                }

                is HelpRequest -> {
                    it[urgent] = null
                    it[volunteersNeeded] = report.volunteersNeeded
                    it[slotStart] = report.slot.start.toEpochMilli()
                    it[slotEnd] = report.slot.end.toEpochMilli()
                }

                else -> error("Unsupported report type.")
            }
        }

        updatedRows > 0
    }

    override fun findByIdForUpdate(id: Long): Report? = transaction(database) {
        ReportsTable
            .selectAll()
            .where { ReportsTable.id eq id }
            .forUpdate()
            .singleOrNull()
            ?.toReport()
    }

    override fun delete(id: Long): Boolean = transaction(database) {
        ReportsTable.deleteWhere {
            ReportsTable.id eq id
        } > 0
    }

    /**
     * Zet een databaserij om naar het juiste soort Report.
     * De kolom `type` bepaalt of het een SafetyReport, NuisanceReport of HelpRequest wordt.
     * Zo komt er uit de database altijd weer een gecontroleerd domeinobject.
     */
    private fun ResultRow.toReport(): Report {
        val id = this[ReportsTable.id]
        val title = this[ReportsTable.title]
        val description = this[ReportsTable.description]

        val category = ReportCategory.valueOf(
            this[ReportsTable.category]
        )

        val location = Location(
            latitude = this[ReportsTable.latitude],
            longitude = this[ReportsTable.longitude]
        )

        val status = ReportStatus.valueOf(
            this[ReportsTable.status]
        )

        val createdAt = this[ReportsTable.createdAt]
        val viewCount = this[ReportsTable.viewCount]
        val ownerId = this[ReportsTable.ownerId]

        return when (
            ReportType.valueOf(this[ReportsTable.type])
        ) {
            ReportType.SAFETY -> SafetyReport(
                id = id,
                title = title,
                description = description,
                category = category,
                location = location,
                urgent = requireNotNull(this[ReportsTable.urgent]),
                status = status,
                createdAt = createdAt,
                viewCount = viewCount,
                ownerId = ownerId
            )

            ReportType.NUISANCE -> NuisanceReport(
                id = id,
                title = title,
                description = description,
                category = category,
                location = location,
                status = status,
                createdAt = createdAt,
                viewCount = viewCount,
                ownerId = ownerId
            )

            ReportType.HELP_REQUEST -> HelpRequest(
                id = id,
                title = title,
                description = description,
                category = category,
                location = location,
                volunteersNeeded = requireNotNull(
                    this[ReportsTable.volunteersNeeded]
                ),
                slot = TimeSlot(
                    start = Instant.ofEpochMilli(
                        requireNotNull(this[ReportsTable.slotStart])
                    ),
                    end = Instant.ofEpochMilli(
                        requireNotNull(this[ReportsTable.slotEnd])
                    )
                ),
                status = status,
                createdAt = createdAt,
                viewCount = viewCount,
                ownerId = ownerId
            )
        }
    }
}