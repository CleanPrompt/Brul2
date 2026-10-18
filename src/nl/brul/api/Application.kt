// src/nl/brul/api/Application.kt
package nl.brul.api

import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import kotlinx.serialization.json.Json
import nl.brul.api.common.error.configureErrorHandling
import nl.brul.api.config.AppConfig

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    val config = AppConfig.from(this)

    install(ContentNegotiation) {
        json(
            Json {
                ignoreUnknownKeys = false
            }
        )
    }

    configureErrorHandling()
    configureRouting(config)
}
