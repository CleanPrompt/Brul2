// src/nl/brul/api/Application.kt
package nl.brul.api

import io.github.cdimascio.dotenv.dotenv
import io.ktor.server.application.Application
import nl.brul.api.config.AppConfig
import nl.brul.api.database.DatabaseFactory
import nl.brul.api.plugins.configureErrorHandling
import nl.brul.api.plugins.configureRouting
import nl.brul.api.plugins.configureSecurity
import nl.brul.api.plugins.configureSerialization
import nl.brul.api.security.JwtConfig
import nl.brul.api.security.JwtService

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    val config = AppConfig.from(this)

    val dotenv = dotenv {
        ignoreIfMissing = true
    }

    fun setting(name: String): String {
        return dotenv[name]
            ?.takeIf { it.isNotBlank() }
            ?: error("$name environment variable is required.")
    }

    val jwtConfig = JwtConfig(
        secret = setting("JWT_SECRET"),
        issuer = setting("JWT_ISSUER"),
        audience = setting("JWT_AUDIENCE"),
        validityMillis = setting("JWT_VALIDITY_MILLIS")
            .toLongOrNull()
            ?: error("JWT_VALIDITY_MILLIS must be a valid number.")
    )

    val jwtService = JwtService(jwtConfig)

    configureSerialization()
    configureErrorHandling()
    configureSecurity(jwtConfig)

    val database = DatabaseFactory.init(this)

    configureRouting(
        config = config,
        database = database,
        jwtService = jwtService
    )
}
