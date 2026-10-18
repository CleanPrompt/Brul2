// src/nl/brul/api/Routing.kt
package nl.brul.api

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    routing {
        route("/api/v1") {
            get("/health") {
                call.respondText("OK")
            }
        }
    }
}
