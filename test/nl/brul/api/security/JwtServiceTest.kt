// test/nl/brul/api/security/JwtServiceTest.kt
package nl.brul.api.security

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import java.util.Date
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class JwtServiceTest {

    private val secret =
        "brul-test-secret-with-at-least-32-bytes"

    private val config = JwtConfig(
        secret = secret,
        issuer = "brul-api",
        audience = "brul-client",
        validityMillis = 3_600_000L
    )

    private val service = JwtService(config)

    private fun createToken(
        userId: String = "42",
        issuedAt: Long = System.currentTimeMillis() - 60_000L,
        expiresAt: Long = System.currentTimeMillis() + 3_600_000L
    ): String {
        return JWT.create()
            .withIssuer(config.issuer)
            .withAudience(config.audience)
            .withSubject(userId)
            .withIssuedAt(Date(issuedAt))
            .withExpiresAt(Date(expiresAt))
            .sign(Algorithm.HMAC256(secret))
    }

    @Test
    fun `generated token contains the correct user ID`() {
        val token = service.generateToken(42L)

        assertEquals(42L, service.verifyToken(token))
    }

    @Test
    fun `generated token contains issuer and audience`() {
        val token = service.generateToken(42L)
        val decoded = JWT.decode(token)

        assertEquals("brul-api", decoded.issuer)
        assertTrue(decoded.audience.contains("brul-client"))
    }

    @Test
    fun `generated token contains expiration time`() {
        val before = System.currentTimeMillis()

        val token = service.generateToken(42L)
        val decoded = JWT.decode(token)

        val after = System.currentTimeMillis()
        val expiresAt = decoded.expiresAt.time

        assertTrue(expiresAt >= before + config.validityMillis - 1000L)
        assertTrue(expiresAt <= after + config.validityMillis)
    }

    @Test
    fun `invalid signature is rejected`() {
        val otherService = JwtService(
            config.copy(
                secret = "another-test-secret-with-at-least-32-bytes"
            )
        )

        val token = otherService.generateToken(42L)

        assertNull(service.verifyToken(token))
    }

    @Test
    fun `wrong issuer is rejected`() {
        val otherService = JwtService(
            config.copy(issuer = "another-api")
        )

        assertNull(service.verifyToken(otherService.generateToken(42L)))
    }

    @Test
    fun `wrong audience is rejected`() {
        val otherService = JwtService(
            config.copy(audience = "another-client")
        )

        assertNull(service.verifyToken(otherService.generateToken(42L)))
    }

    @Test
    fun `expired token is rejected`() {
        val now = System.currentTimeMillis()

        val token = createToken(
            issuedAt = now - 7_200_000L,
            expiresAt = now - 3_600_000L
        )

        assertNull(service.verifyToken(token))
    }

    @Test
    fun `future issued token is rejected`() {
        val now = System.currentTimeMillis()

        val token = createToken(
            issuedAt = now + 3_600_000L,
            expiresAt = now + 7_200_000L
        )

        assertNull(service.verifyToken(token))
    }

    @Test
    fun `blank and malformed tokens are rejected`() {
        assertNull(service.verifyToken(""))
        assertNull(service.verifyToken("not-a-jwt"))
    }

    @Test
    fun `invalid user ID is rejected`() {
        assertFailsWith<IllegalArgumentException> {
            service.generateToken(0L)
        }

        assertFailsWith<IllegalArgumentException> {
            service.generateToken(-1L)
        }
    }

    @Test
    fun `JWT configuration rejects short secrets`() {
        assertFailsWith<IllegalArgumentException> {
            JwtConfig(
                secret = "short",
                issuer = "brul-api",
                audience = "brul-client",
                validityMillis = 3_600_000L
            )
        }
    }

    @Test
    fun `different user IDs produce different tokens`() {
        val first = service.generateToken(1L)
        val second = service.generateToken(2L)

        assertNotEquals(first, second)
    }
}