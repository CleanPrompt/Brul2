// src/nl/brul/api/database/DatabaseFactory.kt
package nl.brul.api.database

import io.ktor.server.application.Application
import io.ktor.server.application.log
import nl.brul.api.features.help.repository.HelpParticipationsTable
import nl.brul.api.features.report.repository.ReportPhotosTable
import nl.brul.api.features.report.repository.ReportsTable
import nl.brul.api.features.user.repository.UsersTable
import nl.brul.api.features.vote.repository.VotesTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

object DatabaseFactory {

    fun init(application: Application): Database {
        val config = application.environment.config

        val url = config.property("database.url").getString()
        val driver = config.property("database.driver").getString()
        val user = config.property("database.user").getString()
        val password = config.property("database.password").getString()

        val database = Database.connect(
            url = url,
            driver = driver,
            user = user,
            password = password
        )

        createSchema(database)

        val shouldSeed = config.propertyOrNull("database.seed")
            ?.getString()
            ?.toBooleanStrictOrNull() ?: true

        if (shouldSeed) {
            DatabaseSeeder.seed(database)
        }

        application.log.info("Database initialized.")

        return database
    }

    /**
     * Creates missing tables and brings an existing local database up to
     * date (see DatabaseMigrations), so nobody has to delete data/ by hand.
     */
    fun createSchema(database: Database) {
        transaction(database) {
            SchemaUtils.create(
                UsersTable,
                ReportsTable,
                VotesTable,
                HelpParticipationsTable,
                ReportPhotosTable
            )

            DatabaseMigrations.migrate(this)
        }
    }
}
