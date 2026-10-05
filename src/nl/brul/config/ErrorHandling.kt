package nl.brul.config

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import nl.brul.dto.response.ErrorResponse
import nl.brul.service.ConflictException
import nl.brul.service.ForbiddenException
import nl.brul.service.ReportNotFoundException

fun Application.configureErrorHandling() {

    install(StatusPages) {

        exception<IllegalArgumentException> { call, cause ->
            call.respond(
                status = HttpStatusCode.BadRequest,
                message = ErrorResponse(
                    status = HttpStatusCode.BadRequest.value,
                    error = HttpStatusCode.BadRequest.description,
                    message = cause.message ?: "Invalid request."
                )
            )
        }

        exception<ReportNotFoundException> { call, cause ->
            call.respond(
                status = HttpStatusCode.NotFound,
                message = ErrorResponse(
                    status = HttpStatusCode.NotFound.value,
                    error = HttpStatusCode.NotFound.description,
                    message = cause.message ?: "Report not found."
                )
            )
        }

        exception<ForbiddenException> { call, cause ->
            call.respond(
                status = HttpStatusCode.Forbidden,
                message = ErrorResponse(
                    status = HttpStatusCode.Forbidden.value,
                    error = HttpStatusCode.Forbidden.description,
                    message = cause.message ?: "Forbidden."
                )
            )
        }

        exception<ConflictException> { call, cause ->
            call.respond(
                status = HttpStatusCode.Conflict,
                message = ErrorResponse(
                    status = HttpStatusCode.Conflict.value,
                    error = HttpStatusCode.Conflict.description,
                    message = cause.message ?: "Conflict."
                )
            )
        }

        exception<IllegalStateException> { call, cause ->
            call.respond(
                status = HttpStatusCode.Conflict,
                message = ErrorResponse(
                    status = HttpStatusCode.Conflict.value,
                    error = HttpStatusCode.Conflict.description,
                    message = cause.message ?: "The requested action is not allowed."
                )
            )
        }
    }
}