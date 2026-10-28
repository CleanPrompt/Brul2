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
import nl.brul.api.common.error.DomainConflictException
import nl.brul.api.common.error.DomainForbiddenException
import nl.brul.api.common.error.DomainNotFoundException
import nl.brul.api.common.error.PayloadTooLargeException
import nl.brul.api.common.error.UnsupportedMediaTypeException
import nl.brul.api.features.auth.service.EmailAlreadyRegisteredException
import nl.brul.api.features.auth.service.InvalidCredentialsException

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

        // Broken business rules: the message explains, the code identifies
        // the rule (for example TIME_CONFLICT), so the app can react to it.

        exception<DomainNotFoundException> { call, cause ->
            call.respond(
                HttpStatusCode.NotFound,
                ApiError(cause.message ?: "Resource not found.", cause.code)
            )
        }

        exception<DomainForbiddenException> { call, cause ->
            call.respond(
                HttpStatusCode.Forbidden,
                ApiError(cause.message ?: "Forbidden.", cause.code)
            )
        }

        exception<DomainConflictException> { call, cause ->
            call.respond(
                HttpStatusCode.Conflict,
                ApiError(cause.message ?: "Conflict.", cause.code)
            )
        }

        exception<PayloadTooLargeException> { call, cause ->
            call.respond(
                HttpStatusCode.PayloadTooLarge,
                ApiError(cause.message ?: "Payload too large.", cause.code)
            )
        }

        exception<UnsupportedMediaTypeException> { call, cause ->
            call.respond(
                HttpStatusCode.UnsupportedMediaType,
                ApiError(cause.message ?: "Unsupported media type.", cause.code)
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
