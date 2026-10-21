// src/nl/brul/api/features/user/routes/UserRoutes.kt
package nl.brul.api.features.user.routes

import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.patch
import io.ktor.server.routing.route
import nl.brul.api.features.user.dto.UpdateUserRoleRequest
import nl.brul.api.features.user.dto.toUserResponse
import nl.brul.api.features.user.service.UserService

fun Route.userRoutes(userService: UserService) {

    authenticate("auth-jwt") {
        route("/users") {

            get {
                val actingUserId = call.principal<JWTPrincipal>()
                    ?.payload
                    ?.subject
                    ?.toLongOrNull()

                if (actingUserId == null) {
                    call.respond(HttpStatusCode.Unauthorized)
                    return@get
                }

                if (!userService.isAdmin(actingUserId)) {
                    call.respond(HttpStatusCode.Forbidden)
                    return@get
                }

                call.respond(
                    HttpStatusCode.OK,
                    userService.findAll().map { it.toUserResponse() }
                )
            }

            get("/{id}") {
                val actingUserId = call.principal<JWTPrincipal>()
                    ?.payload
                    ?.subject
                    ?.toLongOrNull()

                if (actingUserId == null) {
                    call.respond(HttpStatusCode.Unauthorized)
                    return@get
                }

                if (!userService.isAdmin(actingUserId)) {
                    call.respond(HttpStatusCode.Forbidden)
                    return@get
                }

                val id = call.parameters["id"]?.toLongOrNull()

                if (id == null || id <= 0) {
                    call.respond(HttpStatusCode.BadRequest)
                    return@get
                }

                val user = userService.findById(id)

                if (user == null) {
                    call.respond(HttpStatusCode.NotFound)
                    return@get
                }

                call.respond(
                    HttpStatusCode.OK,
                    user.toUserResponse()
                )
            }

            patch("/{id}/role") {
                val actingUserId = call.principal<JWTPrincipal>()
                    ?.payload
                    ?.subject
                    ?.toLongOrNull()

                if (actingUserId == null) {
                    call.respond(HttpStatusCode.Unauthorized)
                    return@patch
                }

                if (!userService.isAdmin(actingUserId)) {
                    call.respond(HttpStatusCode.Forbidden)
                    return@patch
                }

                val id = call.parameters["id"]?.toLongOrNull()

                if (id == null || id <= 0) {
                    call.respond(HttpStatusCode.BadRequest)
                    return@patch
                }

                val request = call.receive<UpdateUserRoleRequest>()

                val updatedUser = try {
                    userService.updateRole(
                        targetUserId = id,
                        actingUserId = actingUserId,
                        role = request.role
                    )
                } catch (exception: IllegalArgumentException) {
                    call.respond(HttpStatusCode.BadRequest)
                    return@patch
                }

                if (updatedUser == null) {
                    call.respond(HttpStatusCode.NotFound)
                    return@patch
                }

                call.respond(
                    HttpStatusCode.OK,
                    updatedUser.toUserResponse()
                )
            }
        }
    }
}