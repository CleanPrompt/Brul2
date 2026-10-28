// test/nl/brul/api/support/TestDatabase.kt
package nl.brul.api.support

import java.util.UUID
import nl.brul.api.database.DatabaseFactory
import nl.brul.api.features.report.model.HelpRequest
import nl.brul.api.features.report.model.Location
import nl.brul.api.features.report.model.NewReport
import nl.brul.api.features.report.model.ReportCategory
import nl.brul.api.features.report.model.ReportType
import nl.brul.api.features.report.model.TimeSlot
import nl.brul.api.features.report.repository.ReportRepository
import nl.brul.api.features.user.model.NewUser
import nl.brul.api.features.user.repository.ExposedUserRepository
import org.jetbrains.exposed.sql.Database

/** A fresh in-memory H2 database with the full schema and some users. */
object TestDatabase {

    fun create(name: String = "test", users: Int = 4): Database {
        val database = Database.connect(
            url = "jdbc:h2:mem:${name}_${UUID.randomUUID()};DB_CLOSE_DELAY=-1",
            driver = "org.h2.Driver",
            user = "sa",
            password = ""
        )

        DatabaseFactory.createSchema(database)

        // Users get IDs 1..users, needed for the foreign keys.
        val userRepository = ExposedUserRepository(database)

        repeat(users) { index ->
            val number = index + 1

            userRepository.create(
                NewUser(
                    username = "testuser$number",
                    email = "testuser$number@example.com",
                    passwordHash = "test-password-hash"
                )
            )
        }

        return database
    }
}

/** Creates a help request directly in the repository (no future-start check). */
fun ReportRepository.createHelpRequest(
    slot: TimeSlot = TestSlots.slot,
    volunteersNeeded: Int = 2,
    ownerId: Long = 1L,
    title: String = "Help with groceries"
): HelpRequest =
    create(
        NewReport(
            type = ReportType.HELP_REQUEST,
            title = title,
            description = "I need help carrying groceries.",
            category = ReportCategory.GROCERIES,
            location = Location(52.0705, 4.3007),
            volunteersNeeded = volunteersNeeded,
            ownerId = ownerId,
            slot = slot
        )
    ) as HelpRequest
