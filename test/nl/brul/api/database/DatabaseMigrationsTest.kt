// test/nl/brul/api/database/DatabaseMigrationsTest.kt
package nl.brul.api.database

import java.time.Duration
import java.time.Instant
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import nl.brul.api.features.help.model.ParticipationStatus
import nl.brul.api.features.help.repository.ExposedHelpParticipationRepository
import nl.brul.api.features.report.model.HelpRequest
import nl.brul.api.features.report.repository.ExposedReportRepository
import nl.brul.api.features.user.model.NewUser
import nl.brul.api.features.user.repository.ExposedUserRepository
import nl.brul.api.features.user.repository.UsersTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction

/**
 * A database from Periode 1 (without time slots and participation status)
 * must keep working after the update, without deleting data/ by hand.
 */
class DatabaseMigrationsTest {

    private val createdAt = Instant.parse("2026-10-01T09:00:00Z")

    /** The reports table exactly as Periode 1 defined it (no time slot). */
    private object OldReportsTable : Table("reports") {
        val id = long("id").autoIncrement()
        val ownerId = long("owner_id").references(UsersTable.id, onDelete = ReferenceOption.SET_NULL).nullable()
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

    /** The participations table as Periode 1 defined it (no status). */
    private object OldParticipationsTable : Table("help_participations") {
        val id = long("id").autoIncrement()
        val helpRequestId = long("help_request_id").references(OldReportsTable.id, onDelete = ReferenceOption.CASCADE)
        val userId = long("user_id").references(UsersTable.id, onDelete = ReferenceOption.CASCADE)

        override val primaryKey = PrimaryKey(id)
    }

    private fun createOldDatabase(): Database {
        val database = Database.connect(
            url = "jdbc:h2:mem:legacy_${UUID.randomUUID()};DB_CLOSE_DELAY=-1",
            driver = "org.h2.Driver",
            user = "sa",
            password = ""
        )

        transaction(database) {
            SchemaUtils.create(UsersTable, OldReportsTable, OldParticipationsTable)
        }

        val users = ExposedUserRepository(database)

        users.create(NewUser("owner", "owner@example.com", "hash"))
        users.create(NewUser("helper", "helper@example.com", "hash"))

        transaction(database) {
            OldReportsTable.insert {
                it[ownerId] = 1L
                it[type] = "HELP_REQUEST"
                it[title] = "Old request"
                it[description] = "Created in Periode 1"
                it[latitude] = 51.58
                it[longitude] = 4.77
                it[status] = "OPEN"
                it[category] = "GROCERIES"
                it[volunteersNeeded] = 2
                it[createdAt] = this@DatabaseMigrationsTest.createdAt.toEpochMilli()
            }

            OldParticipationsTable.insert {
                it[helpRequestId] = 1L
                it[userId] = 2L
            }
        }

        return database
    }

    @Test
    fun `old help requests get a default time slot`() {
        val database = createOldDatabase()

        DatabaseFactory.createSchema(database)

        val report = ExposedReportRepository(database).findById(1)

        assertIs<HelpRequest>(report)
        assertEquals(createdAt.plus(Duration.ofDays(1)), report.slot.start)
        assertEquals(Duration.ofHours(2), report.slot.duration)
    }

    @Test
    fun `old participations become signed up`() {
        val database = createOldDatabase()

        DatabaseFactory.createSchema(database)

        val participation = ExposedHelpParticipationRepository(database)
            .findByHelpRequestAndUser(1, 2)

        assertEquals(ParticipationStatus.SIGNED_UP, participation?.status)
    }

    @Test
    fun `running the migrations twice changes nothing`() {
        val database = createOldDatabase()

        DatabaseFactory.createSchema(database)
        DatabaseFactory.createSchema(database)

        val report = ExposedReportRepository(database).findById(1)

        assertIs<HelpRequest>(report)
        assertEquals(createdAt.plus(Duration.ofDays(1)), report.slot.start)
    }
}
