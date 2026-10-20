// src/nl/brul/api/security/JwtService.kt
package nl.brul.api.security

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.JWTVerificationException
import java.util.Date
import nl.brul.api.features.user.model.UserRole

class JwtService(
    private val config: JwtConfig
) {
    private val algorithm = Algorithm.HMAC256(config.secret)

    private val verifier = JWT
        .require(algorithm)
        .withIssuer(config.issuer)
        .withAudience(config.audience)
        .acceptLeeway(0)
        .build()

    fun generateToken(
        userId: Long,
        role: UserRole = UserRole.USER
    ): String {
        require(userId > 0) {
            "User ID must be positive."
        }

        val issuedAt = System.currentTimeMillis()
        val expiresAt = Math.addExact(
            issuedAt,
            config.validityMillis
        )

        return JWT.create()
            .withIssuer(config.issuer)
            .withAudience(config.audience)
            .withSubject(userId.toString())
            .withClaim("role", role.name)
            .withIssuedAt(Date(issuedAt))
            .withExpiresAt(Date(expiresAt))
            .sign(algorithm)
    }

    fun verifyToken(token: String): Long? {
        if (token.isBlank()) {
            return null
        }

        return try {
            val decoded = verifier.verify(token)

            val userId = decoded.subject?.toLongOrNull()
                ?: return null

            if (userId <= 0) {
                return null
            }

            val issuedAt = decoded.issuedAt?.time
                ?: return null

            val expiresAt = decoded.expiresAt?.time
                ?: return null

            val now = System.currentTimeMillis()

            if (issuedAt > now || expiresAt <= now) {
                return null
            }

            userId
        } catch (_: JWTVerificationException) {
            null
        } catch (_: IllegalArgumentException) {
            null
        }
    }
}