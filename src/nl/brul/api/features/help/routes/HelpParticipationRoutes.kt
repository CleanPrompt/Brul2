// src/nl/brul/api/features/help/routes/HelpParticipationRoutes.kt
package nl.brul.api.features.help.routes

import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import nl.brul.api.features.help.dto.toResponse
import nl.brul.api.features.help.service.HelpParticipationService

fun Route.helpParticipationRoutes(
    service: HelpParticipationService
) {
    route("/reports/{id}/participants") {

        // Public summary of volunteers and availability.
        get {
            val id = parseHelpRequestId(call.parameters["id"])

            call.respond(
                HttpStatusCode.OK,
                service.getSummary(id).toResponse()
            )
        }

        // Public list of participant IDs.
        get("/list") {
            val id = parseHelpRequestId(call.parameters["id"])

            val participants = service
                .getParticipants(id)
                .map { it.toResponse() }

            call.respond(HttpStatusCode.OK, participants)
        }

        authenticate("auth-jwt") {

            // Join a help request.
            post {
                val userId = call.principal<JWTPrincipal>()
                    ?.payload?.subject?.toLongOrNull()

                if (userId == null || userId <= 0) {
                    call.respond(HttpStatusCode.Unauthorized)
                    return@post
                }

                val id = parseHelpRequestId(call.parameters["id"])

                val participation = service.join(
                    helpRequestId = id,
                    userId = userId
                )

                call.respond(
                    HttpStatusCode.Created,
                    participation.toResponse()
                )
            }

            // Leave a help request.
            delete("/me") {
                val userId = call.principal<JWTPrincipal>()
                    ?.payload?.subject?.toLongOrNull()

                if (userId == null || userId <= 0) {
                    call.respond(HttpStatusCode.Unauthorized)
                    return@delete
                }

                val id = parseHelpRequestId(call.parameters["id"])

                val removed = service.leave(
                    helpRequestId = id,
                    userId = userId
                )

                if (!removed) {
                    call.respond(HttpStatusCode.NotFound)
                    return@delete
                }

                call.respond(HttpStatusCode.NoContent)
            }
        }
    }
}

private fun parseHelpRequestId(value: String?): Long {
    val id = value?.toLongOrNull()

    require(id != null && id > 0) {
        "Help request ID must be a positive number."
    }

    return id
}