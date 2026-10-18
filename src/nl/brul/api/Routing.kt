// src/nl/brul/api/Routing.kt
package nl.brul.api

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import kotlinx.serialization.Serializable
import nl.brul.api.config.AppConfig

@Serializable
data class HealthResponse(
    val status: String,
    val service: String
)

fun Application.configureRouting(config: AppConfig) {
    routing {
        route("/api/v1") {
            get("/health") {
                call.respond(
                    HttpStatusCode.OK,
                    HealthResponse(
                        status = "ok",
                        service = config.name
                    )
                )
            }
        }
    }
}
