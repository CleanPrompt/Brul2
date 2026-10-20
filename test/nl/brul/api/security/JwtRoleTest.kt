// test/nl/brul/api/security/JwtRoleTest.kt
package nl.brul.api.security

import com.auth0.jwt.JWT
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import nl.brul.api.features.user.model.UserRole

class JwtRoleTest {

    private val service = JwtService(
        JwtConfig(
            secret = "this-is-a-test-secret-with-at-least-32-bytes",
            issuer = "brul-test",
            audience = "brul-users",
            validityMillis = 60_000L
        )
    )

    @Test
    fun `default token contains USER role`() {
        val token = service.generateToken(1L)

        val decoded = JWT.decode(token)

        assertEquals("USER", decoded.getClaim("role").asString())
        assertEquals("1", decoded.subject)
        assertEquals(1L, service.verifyToken(token))
    }

    @Test
    fun `admin token contains ADMIN role`() {
        val token = service.generateToken(
            2L,
            UserRole.ADMIN
        )

        val decoded = JWT.decode(token)

        assertEquals("ADMIN", decoded.getClaim("role").asString())
        assertEquals("2", decoded.subject)
        assertNotNull(service.verifyToken(token))
    }
}