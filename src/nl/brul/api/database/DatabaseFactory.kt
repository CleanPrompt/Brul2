// src/nl/brul/api/database/DatabaseFactory.kt
package nl.brul.api.database

import io.ktor.server.application.Application
import io.ktor.server.application.log
import nl.brul.api.features.report.repository.ReportsTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

object DatabaseFactory {

    fun init(application: Application) {
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

        transaction(database) {
            SchemaUtils.create(ReportsTable)
        }

        application.log.info("Database initialized.")
    }
}