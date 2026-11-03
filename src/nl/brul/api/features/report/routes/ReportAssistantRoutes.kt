// src/nl/brul/api/features/report/routes/ReportAssistantRoutes.kt
package nl.brul.api.features.report.routes

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import nl.brul.api.features.report.dto.DuplicateCheckRequest
import nl.brul.api.features.report.dto.SuggestionRequest
import nl.brul.api.features.report.dto.toDuplicateQuery
import nl.brul.api.features.report.dto.toResponse
import nl.brul.api.features.report.service.ReportAssistantService

/**
 * Laag: routes (de voordeur).
 *
 * De endpoints van de meldassistent. De app roept ze aan terwijl iemand een melding invult,
 * nog vóór POST /reports. Er wordt niets opgeslagen.
 *
 * - POST /reports/suggestion: stelt type en categorie voor op basis van de tekst (S1).
 *   200 met het voorstel, of 204 No Content als er niets te herkennen is.
 * - POST /reports/duplicates: bestaande meldingen die waarschijnlijk over hetzelfde gaan
 *   (D1 t/m D6). 200 met een lijst, die ook leeg kan zijn.
 *
 * Allebei alleen voor ingelogde gebruikers (JWT): een AI-aanroep kost geld, dus niet iedereen op
 * internet mag hem gebruiken. Foute invoer geeft 400 via ErrorHandling.
 */
fun Route.reportAssistantRoutes(service: ReportAssistantService) {

    authenticate("auth-jwt") {

        route("/reports") {

            post("/suggestion") {
                if (!call.hasValidUser()) {
                    call.respond(HttpStatusCode.Unauthorized)
                    return@post
                }

                val request = call.receive<SuggestionRequest>()
                val suggestion = service.suggest(request.title, request.description)

                if (suggestion == null) {
                    call.respond(HttpStatusCode.NoContent)
                    return@post
                }

                call.respond(HttpStatusCode.OK, suggestion.toResponse())
            }

            post("/duplicates") {
                if (!call.hasValidUser()) {
                    call.respond(HttpStatusCode.Unauthorized)
                    return@post
                }

                val request = call.receive<DuplicateCheckRequest>()
                val duplicates = service.findDuplicates(request.toDuplicateQuery())

                call.respond(HttpStatusCode.OK, duplicates.map { it.toResponse() })
            }
        }
    }
}

/** Heeft het JWT-pasje een geldig, positief user-id? */
private fun ApplicationCall.hasValidUser(): Boolean {
    val userId = principal<JWTPrincipal>()
        ?.payload
        ?.subject
        ?.toLongOrNull()

    return userId != null && userId > 0
}
