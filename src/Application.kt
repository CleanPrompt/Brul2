package org.jetbrains.amper.ktor

import io.ktor.server.application.*
import nl.brul.config.configureErrorHandling
import nl.brul.config.configureSerialization
import nl.brul.config.configureStaticContent

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

/**
 * @param seedData when true (the default when the server starts), the API
 * starts with realistic demo reports. Tests pass `false` to start empty.
 */
fun Application.module(seedData: Boolean = true) {

    configureSerialization()

    configureErrorHandling()

    configureRouting(seedData)

    configureStaticContent()
}
