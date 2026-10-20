// src/nl/brul/api/plugins/Security.kt
package nl.brul.api.plugins

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.jwt.jwt
import nl.brul.api.security.JwtConfig

fun Application.configureSecurity(config: JwtConfig) {
    install(Authentication) {
        jwt("auth-jwt") {
            realm = "Brul2 API"

            verifier(
                JWT.require(Algorithm.HMAC256(config.secret))
                    .withIssuer(config.issuer)
                    .withAudience(config.audience)
                    .acceptLeeway(0)
                    .build()
            )

            validate { credential ->
                val payload = credential.payload
                val userId = payload.subject?.toLongOrNull()
                val issuedAt = payload.issuedAt?.time
                val expiresAt = payload.expiresAt?.time
                val now = System.currentTimeMillis()

                if (
                    userId != null &&
                    userId > 0 &&
                    issuedAt != null &&
                    issuedAt <= now &&
                    expiresAt != null &&
                    expiresAt > now
                ) {
                    JWTPrincipal(payload)
                } else {
                    null
                }
            }
        }
    }
}