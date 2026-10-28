// src/nl/brul/api/features/help/routes/HelpParticipationRoutes.kt
package nl.brul.api.features.help.routes

import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.RoutingCall
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.route
import nl.brul.api.features.help.dto.AttendanceRequest
import nl.brul.api.features.help.dto.toResponse
import nl.brul.api.features.help.service.HelpParticipationService

fun Route.helpParticipationRoutes(
    service: HelpParticipationService
) {
    route("/reports/{id}/participants") {

        // Public summary: places, waitlist and phase.
        get {
            val id = parseHelpRequestId(call.parameters["id"])

            call.respond(
                HttpStatusCode.OK,
                service.getSummary(id).toResponse()
            )
        }

        // Public list of participations with their status.
        get("/list") {
            val id = parseHelpRequestId(call.parameters["id"])

            val participants = service
                .getParticipants(id)
                .map { it.toResponse() }

            call.respond(HttpStatusCode.OK, participants)
        }

        authenticate("auth-jwt") {

            // Sign up: 201 with status SIGNED_UP, or WAITLISTED plus position.
            post {
                val userId = call.authenticatedUserId()
                    ?: return@post call.respond(HttpStatusCode.Unauthorized)

                val id = parseHelpRequestId(call.parameters["id"])

                val result = service.join(
                    helpRequestId = id,
                    userId = userId
                )

                call.respond(
                    HttpStatusCode.Created,
                    result.toResponse()
                )
            }

            // Leave: 200 with the new status (CANCELLED or LATE_CANCELLED).
            delete("/me") {
                val userId = call.authenticatedUserId()
                    ?: return@delete call.respond(HttpStatusCode.Unauthorized)

                val id = parseHelpRequestId(call.parameters["id"])

                val participation = service.leave(
                    helpRequestId = id,
                    userId = userId
                )

                call.respond(HttpStatusCode.OK, participation.toResponse())
            }

            // R9: the owner records attendance after the start.
            put("/{userId}/attendance") {
                val actingUserId = call.authenticatedUserId()
                    ?: return@put call.respond(HttpStatusCode.Unauthorized)

                val id = parseHelpRequestId(call.parameters["id"])

                val volunteerUserId = call.parameters["userId"]?.toLongOrNull()

                require(volunteerUserId != null && volunteerUserId > 0) {
                    "User ID must be a positive number."
                }

                val request = call.receive<AttendanceRequest>()

                val participation = service.markAttendance(
                    helpRequestId = id,
                    actingUserId = actingUserId,
                    volunteerUserId = volunteerUserId,
                    attended = request.attended
                )

                call.respond(HttpStatusCode.OK, participation.toResponse())
            }
        }
    }
}

/**
 * Personal volunteer data for the Android app.
 * Paths start with /users/me, next to the user feature's /users/{id}.
 */
fun Route.volunteerRoutes(
    service: HelpParticipationService
) {
    authenticate("auth-jwt") {
        route("/users/me") {

            // The agenda: ?upcoming=true leaves out finished and cancelled items.
            get("/participations") {
                val userId = call.authenticatedUserId()
                    ?: return@get call.respond(HttpStatusCode.Unauthorized)

                val upcomingOnly = call.request.queryParameters["upcoming"]
                    ?.toBooleanStrictOrNull()
                    ?: false

                call.respond(
                    HttpStatusCode.OK,
                    service.getAgenda(userId, upcomingOnly).map { it.toResponse() }
                )
            }

            // R11: no-shows in the last 30 days and whether that blocks sign-ups.
            get("/reliability") {
                val userId = call.authenticatedUserId()
                    ?: return@get call.respond(HttpStatusCode.Unauthorized)

                call.respond(
                    HttpStatusCode.OK,
                    service.getReliability(userId).toResponse()
                )
            }
        }
    }
}

private fun RoutingCall.authenticatedUserId(): Long? =
    principal<JWTPrincipal>()
        ?.payload
        ?.subject
        ?.toLongOrNull()
        ?.takeIf { it > 0 }

private fun parseHelpRequestId(value: String?): Long {
    val id = value?.toLongOrNull()

    require(id != null && id > 0) {
        "Help request ID must be a positive number."
    }

    return id
}
