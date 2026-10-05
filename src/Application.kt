package org.jetbrains.amper.ktor

import io.ktor.server.application.*
import nl.brul.config.configureSerialization
import nl.brul.config.configureStaticContent

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {

    configureSerialization()

    configureRouting()

    configureStaticContent()
}