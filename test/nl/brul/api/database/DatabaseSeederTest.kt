// test/nl/brul/api/database/DatabaseSeederTest.kt
package nl.brul.api.database

import nl.brul.api.features.help.repository.HelpParticipationsTable
import nl.brul.api.features.report.model.ReportCategory
import nl.brul.api.features.report.model.ReportType
import nl.brul.api.features.report.repository.ReportsTable
import nl.brul.api.features.user.model.UserRole
import nl.brul.api.features.user.repository.UsersTable
import nl.brul.api.features.vote.repository.VotesTable
import nl.brul.api.security.BCryptPasswordHasher
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DatabaseSeederTest {

    private fun createCleanDatabase(): Database {
        val database = Database.connect(
            url = "jdbc:h2:mem:seeder_test_${UUID.randomUUID()};DB_CLOSE_DELAY=-1",
            driver = "org.h2.Driver",
            user = "sa",
            password = ""
        )

        transaction(database) {
            SchemaUtils.create(
                UsersTable,
                ReportsTable,
                VotesTable,
                HelpParticipationsTable
            )
        }

        return database
    }

    @Test
    fun `seeder populates users, reports, votes and help participations`() {
        val database = createCleanDatabase()

        DatabaseSeeder.seed(database)

        transaction(database) {
            val users = UsersTable.selectAll().toList()
            val reports = ReportsTable.selectAll().toList()
            val votes = VotesTable.selectAll().toList()
            val participations = HelpParticipationsTable.selectAll().toList()

            // 6 users seeded (1 admin + 5 residents)
            assertEquals(6, users.size)
            val adminUser = users.firstOrNull { it[UsersTable.email] == "admin@example.com" }
            assertTrue(adminUser != null)
            assertEquals(UserRole.ADMIN, adminUser[UsersTable.role])

            val hasher = BCryptPasswordHasher()
            assertTrue(hasher.verify("AdminPassword123!", adminUser[UsersTable.passwordHash]))

            val anastasia = users.firstOrNull { it[UsersTable.email] == "anastasia@example.com" }
            assertTrue(anastasia != null)
            assertTrue(hasher.verify("Password123!", anastasia[UsersTable.passwordHash]))

            // Reports cover all 3 types
            assertEquals(13, reports.size)
            val reportTypes = reports.map { it[ReportsTable.type] }.toSet()
            assertTrue(ReportType.SAFETY.name in reportTypes)
            assertTrue(ReportType.NUISANCE.name in reportTypes)
            assertTrue(ReportType.HELP_REQUEST.name in reportTypes)

            // Categories are populated
            val categories = reports.map { it[ReportsTable.category] }.toSet()
            assertTrue(ReportCategory.SUSPICIOUS_SITUATION.name in categories)
            assertTrue(ReportCategory.NOISE.name in categories)
            assertTrue(ReportCategory.GROCERIES.name in categories)

            // Votes are populated
            assertTrue(votes.isNotEmpty())

            // Help participations are populated
            assertTrue(participations.isNotEmpty())
        }
    }

    @Test
    fun `seeder is idempotent and does not duplicate rows on multiple runs`() {
        val database = createCleanDatabase()

        // Run twice
        DatabaseSeeder.seed(database)
        DatabaseSeeder.seed(database)

        transaction(database) {
            assertEquals(6, UsersTable.selectAll().count())
            assertEquals(13, ReportsTable.selectAll().count())
        }
    }

    @Test
    fun `seed local persistent database`() {
        val database = Database.connect(
            url = "jdbc:h2:file:./data/brul;DB_CLOSE_ON_EXIT=FALSE",
            driver = "org.h2.Driver",
            user = "sa",
            password = ""
        )

        transaction(database) {
            SchemaUtils.create(
                UsersTable,
                ReportsTable,
                VotesTable,
                HelpParticipationsTable
            )
        }

        DatabaseSeeder.seed(database, force = true)

        transaction(database) {
            assertEquals(6, UsersTable.selectAll().count())
            assertEquals(13, ReportsTable.selectAll().count())
            assertTrue(VotesTable.selectAll().count() > 0)
            assertTrue(HelpParticipationsTable.selectAll().count() > 0)
        }
    }
}
