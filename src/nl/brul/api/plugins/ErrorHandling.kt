// src/nl/brul/api/plugins/ErrorHandling.kt
package nl.brul.api.plugins

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.application.log
import io.ktor.server.plugins.BadRequestException
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.respond
import nl.brul.api.common.error.ApiError
import nl.brul.api.features.auth.service.EmailAlreadyRegisteredException
import nl.brul.api.features.auth.service.InvalidCredentialsException
import nl.brul.api.features.help.service.HelpRequestNotFoundException
import nl.brul.api.features.help.service.HelpParticipationForbiddenException
import nl.brul.api.features.help.service.HelpParticipationConflictException

fun Application.configureErrorHandling() {

    install(StatusPages) {

        exception<EmailAlreadyRegisteredException> { call, _ ->
            call.respond(
                HttpStatusCode.Conflict,
                ApiError("Email address is already registered.")
            )
        }

        exception<InvalidCredentialsException> { call, _ ->
            call.respond(
                HttpStatusCode.Unauthorized,
                ApiError("Invalid email or password.")
            )
        }

        exception<IllegalArgumentException> { call, _ ->
            call.respond(
                HttpStatusCode.BadRequest,
                ApiError("Invalid request.")
            )
        }

        exception<BadRequestException> { call, _ ->
            call.respond(
                HttpStatusCode.BadRequest,
                ApiError("Invalid request.")
            )
        }

        exception<HelpRequestNotFoundException> { call, _ ->
            call.respond(
                HttpStatusCode.NotFound,
                ApiError("Help request not found.")
            )
        }

        exception<HelpParticipationForbiddenException> { call, _ ->
            call.respond(
                HttpStatusCode.Forbidden,
                ApiError("You cannot join your own help request.")
            )
        }

        exception<HelpParticipationConflictException> { call, cause ->
            call.respond(
                HttpStatusCode.Conflict,
                ApiError(cause.message ?: "Participation conflict.")
            )
        }

        exception<Throwable> { call, cause ->
            call.application.log.error(
                "Unexpected server error",
                cause
            )

            call.respond(
                HttpStatusCode.InternalServerError,
                ApiError("Internal server error.")
            )
        }

        status(HttpStatusCode.NotFound) { call, _ ->
            call.respond(
                HttpStatusCode.NotFound,
                ApiError("Resource not found.")
            )
        }

        status(HttpStatusCode.MethodNotAllowed) { call, _ ->
            call.respond(
                HttpStatusCode.MethodNotAllowed,
                ApiError("Method not allowed.")
            )
        }
    }
}