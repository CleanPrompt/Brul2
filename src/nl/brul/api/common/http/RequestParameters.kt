// src/nl/brul/api/common/http/RequestParameters.kt
package nl.brul.api.common.http

import io.ktor.server.application.ApplicationCall
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import nl.brul.api.common.error.AppException

/** Reads a positive id from the path, such as `{id}` in `/reports/{id}`, or throws 400 with the parameter name. */
fun ApplicationCall.idParameter(name: String): Long {
    val id = parameters[name]?.toLongOrNull()

    if (id == null || id <= 0) {
        throw AppException.BadRequest("Invalid $name")
    }

    return id
}

/**
 * The id of the logged-in user (the `sub` claim of the JWT). Only call inside `authenticate("auth-jwt")`;
 * the JWT check in `plugins/Security.kt` has already rejected tokens without a valid id.
 */
fun ApplicationCall.currentUserId(): Long =
    principal<JWTPrincipal>()
        ?.payload
        ?.subject
        ?.toLongOrNull()
        ?: throw AppException.Unauthorized("Authentication required")
