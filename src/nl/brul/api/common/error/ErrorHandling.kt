// src/nl/brul/api/common/error/ErrorHandling.kt
package nl.brul.api.common.error

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.application.log
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.respond

fun Application.configureErrorHandling() {
    install(StatusPages) {
        exception<IllegalArgumentException> { call, _ ->
            call.respond(
                HttpStatusCode.BadRequest,
                ApiError("Invalid request.")
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
    }
}
