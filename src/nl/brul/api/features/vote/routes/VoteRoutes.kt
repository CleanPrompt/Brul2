// src/nl/brul/api/features/vote/routes/VoteRoutes.kt
package nl.brul.api.features.vote.routes

import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.put
import io.ktor.server.routing.route
import nl.brul.api.features.report.service.ReportService
import nl.brul.api.features.vote.dto.VoteRequest
import nl.brul.api.features.vote.dto.toResponse
import nl.brul.api.features.vote.service.VoteService

fun Route.voteRoutes(
    voteService: VoteService,
    reportService: ReportService
) {
    route("/reports/{id}") {

        // Public vote summary.
        get("/votes") {
            val reportId = parseVoteReportId(call.parameters["id"])

            if (reportService.findById(reportId) == null) {
                call.respond(HttpStatusCode.NotFound)
                return@get
            }

            call.respond(
                HttpStatusCode.OK,
                voteService.getSummary(reportId).toResponse()
            )
        }

        authenticate("auth-jwt") {

            // Create or replace the authenticated user's vote.
            put("/vote") {
                val userId = call.principal<JWTPrincipal>()
                    ?.payload
                    ?.subject
                    ?.toLongOrNull()

                if (userId == null || userId <= 0) {
                    call.respond(HttpStatusCode.Unauthorized)
                    return@put
                }

                val reportId = parseVoteReportId(
                    call.parameters["id"]
                )

                val report = reportService.findById(reportId)

                if (report == null) {
                    call.respond(HttpStatusCode.NotFound)
                    return@put
                }

                if (report.ownerId == userId) {
                    call.respond(HttpStatusCode.Forbidden)
                    return@put
                }

                val request = call.receive<VoteRequest>()

                val existingVote = voteService.getUserVote(
                    reportId = reportId,
                    userId = userId
                )

                val vote = voteService.castVote(
                    reportId = reportId,
                    userId = userId,
                    type = request.type
                )

                call.respond(
                    if (existingVote == null) {
                        HttpStatusCode.Created
                    } else {
                        HttpStatusCode.OK
                    },
                    vote.toResponse()
                )
            }

            // Get the authenticated user's vote.
            get("/vote/me") {
                val userId = call.principal<JWTPrincipal>()
                    ?.payload
                    ?.subject
                    ?.toLongOrNull()

                if (userId == null || userId <= 0) {
                    call.respond(HttpStatusCode.Unauthorized)
                    return@get
                }

                val reportId = parseVoteReportId(
                    call.parameters["id"]
                )

                if (reportService.findById(reportId) == null) {
                    call.respond(HttpStatusCode.NotFound)
                    return@get
                }

                val vote = voteService.getUserVote(
                    reportId = reportId,
                    userId = userId
                )

                if (vote == null) {
                    call.respond(HttpStatusCode.NotFound)
                    return@get
                }

                call.respond(
                    HttpStatusCode.OK,
                    vote.toResponse()
                )
            }

            // Remove only the authenticated user's vote.
            delete("/vote") {
                val userId = call.principal<JWTPrincipal>()
                    ?.payload
                    ?.subject
                    ?.toLongOrNull()

                if (userId == null || userId <= 0) {
                    call.respond(HttpStatusCode.Unauthorized)
                    return@delete
                }

                val reportId = parseVoteReportId(
                    call.parameters["id"]
                )

                if (reportService.findById(reportId) == null) {
                    call.respond(HttpStatusCode.NotFound)
                    return@delete
                }

                val deleted = voteService.removeVote(
                    reportId = reportId,
                    userId = userId
                )

                if (!deleted) {
                    call.respond(HttpStatusCode.NotFound)
                    return@delete
                }

                call.respond(HttpStatusCode.NoContent)
            }
        }
    }
}

private fun parseVoteReportId(value: String?): Long {
    val id = value?.toLongOrNull()

    require(id != null && id > 0) {
        "Report ID must be a positive number."
    }

    return id
}