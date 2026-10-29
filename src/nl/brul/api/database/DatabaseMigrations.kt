// src/nl/brul/api/database/DatabaseMigrations.kt
package nl.brul.api.database

import java.time.Duration
import nl.brul.api.features.help.repository.HelpParticipationsTable
import nl.brul.api.features.report.model.ReportType
import nl.brul.api.features.report.repository.ReportsTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.SqlExpressionBuilder.isNull
import org.jetbrains.exposed.sql.SqlExpressionBuilder.plus
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.or
import org.jetbrains.exposed.sql.update
import org.slf4j.LoggerFactory

/**
 * Laag: database (de verbinding met H2).
 *
 * Kleine, expliciete migraties voor databases van vóór Periode 2.
 * `SchemaUtils.create` maakt alleen tabellen die nog niet bestaan; aan een bestaande tabel voegt
 * het nooit kolommen toe. Deze statements voegen de nieuwe kolommen toe als ze ontbreken, zodat
 * een bestaande data/brul.mv.db blijft werken. Op een nieuwe database veranderen ze niets, en
 * twee keer draaien kan ook geen kwaad (`IF NOT EXISTS`).
 */
object DatabaseMigrations {

    private val logger = LoggerFactory.getLogger(DatabaseMigrations::class.java)

    /** Oude hulpvragen zonder tijdslot krijgen er een: de dag na het aanmaken, 2 uur lang. */
    private val legacySlotOffset = Duration.ofDays(1).toMillis()
    private val legacySlotLength = Duration.ofHours(2).toMillis()

    /** Wordt bij het opstarten aangeroepen door DatabaseFactory, binnen dezelfde transactie. */
    fun migrate(transaction: Transaction) {
        // Time slot of help requests (R1).
        transaction.addColumnIfMissing(ReportsTable.slotStart, "BIGINT NULL")
        transaction.addColumnIfMissing(ReportsTable.slotEnd, "BIGINT NULL")

        // Status history of participations (ParticipationStatus).
        transaction.addColumnIfMissing(HelpParticipationsTable.status, "VARCHAR(20) DEFAULT 'SIGNED_UP' NOT NULL")
        transaction.addColumnIfMissing(HelpParticipationsTable.joinedAt, "BIGINT DEFAULT 0 NOT NULL")
        transaction.addColumnIfMissing(HelpParticipationsTable.changedAt, "BIGINT DEFAULT 0 NOT NULL")

        val updated = ReportsTable.update(
            where = {
                (ReportsTable.type eq ReportType.HELP_REQUEST.name) and
                        (ReportsTable.slotStart.isNull() or ReportsTable.slotEnd.isNull())
            }
        ) {
            it[slotStart] = ReportsTable.createdAt + legacySlotOffset
            it[slotEnd] = ReportsTable.createdAt + (legacySlotOffset + legacySlotLength)
        }

        if (updated > 0) {
            logger.info("Gave $updated existing help request(s) a default time slot.")
        }
    }

    // identity() quotes names the same way Exposed does when it creates tables.
    private fun Transaction.addColumnIfMissing(column: Column<*>, definition: String) {
        exec(
            "ALTER TABLE ${identity(column.table)} " +
                    "ADD COLUMN IF NOT EXISTS ${identity(column)} $definition"
        )
    }
}
