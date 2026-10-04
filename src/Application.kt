package org.jetbrains.amper.ktor

import io.ktor.server.application.*
import nl.brul.config.configureSerialization

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    configureSerialization()
    configureRouting()
}