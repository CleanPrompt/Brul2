// src/nl/brul/api/features/auth/routes/AuthRoutes.kt
package nl.brul.api.features.auth.routes

import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import nl.brul.api.features.auth.dto.LoginRequest
import nl.brul.api.features.auth.dto.LoginResponse
import nl.brul.api.features.auth.dto.RegisterRequest
import nl.brul.api.features.auth.dto.toAuthResponse
import nl.brul.api.features.auth.service.AuthService
import nl.brul.api.features.user.repository.UserRepository
import nl.brul.api.security.JwtService

fun Route.authRoutes(
    authService: AuthService,
    jwtService: JwtService? = null,
    userRepository: UserRepository? = null
) {
    route("/auth") {

        post("/register") {
            val request = call.receive<RegisterRequest>()

            val user = authService.register(
                username = request.username,
                email = request.email,
                password = request.password
            )

            call.respond(
                HttpStatusCode.Created,
                user.toAuthResponse()
            )
        }

        post("/login") {
            val request = call.receive<LoginRequest>()

            val user = authService.login(
                email = request.email,
                password = request.password
            )

            if (jwtService == null) {
                // Compatibility with existing standalone route tests.
                call.respond(
                    HttpStatusCode.OK,
                    user.toAuthResponse()
                )
            } else {
                call.respond(
                    HttpStatusCode.OK,
                    LoginResponse(
                        id = user.id,
                        username = user.username,
                        email = user.email,
                        createdAt = user.createdAt,
                        accessToken = jwtService.generateToken(
                            user.id,
                            user.role
                        ),
                        tokenType = "Bearer"
                    )
                )
            }
        }

        if (jwtService != null && userRepository != null) {
            authenticate("auth-jwt") {
                get("/me") {
                    val userId = call.principal<JWTPrincipal>()
                        ?.payload
                        ?.subject
                        ?.toLongOrNull()

                    if (userId == null) {
                        call.respond(HttpStatusCode.Unauthorized)
                        return@get
                    }

                    val user = userRepository.findById(userId)

                    if (user == null) {
                        call.respond(HttpStatusCode.Unauthorized)
                        return@get
                    }

                    call.respond(
                        HttpStatusCode.OK,
                        user.toAuthResponse()
                    )
                }
            }
        }
    }
}