// src/nl/brul/api/Application.kt
package nl.brul.api

import io.ktor.server.application.Application
import nl.brul.api.config.AppConfig
import nl.brul.api.database.DatabaseFactory
import nl.brul.api.plugins.configureErrorHandling
import nl.brul.api.plugins.configureRouting
import nl.brul.api.plugins.configureSerialization

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    val config = AppConfig.from(this)

    configureSerialization()
    configureErrorHandling()

    val database = DatabaseFactory.init(this)

    configureRouting(config, database)
}