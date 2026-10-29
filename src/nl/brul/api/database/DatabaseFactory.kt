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

/**
 * Laag: database (de verbinding met H2).
 *
 * Maakt bij het opstarten de verbinding met de database, bouwt de tabellen op en vult de database
 * met testdata als `database.seed` aan staat (standaard aan).
 * `object`: er bestaat er precies één van; je hoeft er niets van te maken.
 */
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
     * Maakt ontbrekende tabellen aan en werkt een bestaande lokale database bij (zie
     * DatabaseMigrations), zodat niemand data/ met de hand hoeft te verwijderen.
     * Staat los van `init`, zodat tests en de seeder dezelfde opbouw kunnen gebruiken.
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
