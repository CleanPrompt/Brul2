// test/nl/brul/api/features/auth/routes/AuthJwtIntegrationTest.kt
package nl.brul.api.features.auth.routes

import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import io.ktor.server.testing.testApplication
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import nl.brul.api.features.auth.service.AuthService
import nl.brul.api.features.user.repository.ExposedUserRepository
import nl.brul.api.features.user.repository.UsersTable
import nl.brul.api.plugins.configureErrorHandling
import nl.brul.api.plugins.configureSecurity
import nl.brul.api.plugins.configureSerialization
import nl.brul.api.security.BCryptPasswordHasher
import nl.brul.api.security.JwtConfig
import nl.brul.api.security.JwtService
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class AuthJwtIntegrationTest {

    private val jwtConfig = JwtConfig(
        secret = "brul2-integration-test-secret-at-least-32-bytes",
        issuer = "brul-api",
        audience = "brul-client",
        validityMillis = 3_600_000L
    )

    private fun createRepository(): ExposedUserRepository {
        val database = Database.connect(
            url = "jdbc:h2:mem:auth_jwt_${UUID.randomUUID()};DB_CLOSE_DELAY=-1",
            driver = "org.h2.Driver",
            user = "sa",
            password = ""
        )

        transaction(database) {
            SchemaUtils.create(UsersTable)
        }

        return ExposedUserRepository(database)
    }

    private fun registerBody(): String = """
        {
            "username": "Alice",
            "email": "alice@example.com",
            "password": "SecurePassword123"
        }
    """.trimIndent()

    private fun loginBody(): String = """
        {
            "email": "alice@example.com",
            "password": "SecurePassword123"
        }
    """.trimIndent()

    @Test
    fun `login returns a valid bearer token`() = testApplication {
        val repository = createRepository()
        val jwtService = JwtService(jwtConfig)
        val authService = AuthService(
            userRepository = repository,
            passwordHasher = BCryptPasswordHasher(workFactor = 10)
        )

        application {
            configureSerialization()
            configureErrorHandling()
            configureSecurity(jwtConfig)

            routing {
                route("/api/v1") {
                    authRoutes(authService, jwtService, repository)
                }
            }
        }

        val register = client.post("/api/v1/auth/register") {
            contentType(ContentType.Application.Json)
            setBody(registerBody())
        }

        assertEquals(HttpStatusCode.Created, register.status)

        val login = client.post("/api/v1/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(loginBody())
        }

        assertEquals(HttpStatusCode.OK, login.status)

        val json = Json.parseToJsonElement(
            login.bodyAsText()
        ).jsonObject

        val token = json["accessToken"]?.jsonPrimitive?.content

        assertNotNull(token)
        assertEquals("Bearer", json["tokenType"]?.jsonPrimitive?.content)

        val userId = json["id"]?.jsonPrimitive?.content?.toLong()
        assertNotNull(userId)

        assertEquals(userId, jwtService.verifyToken(token))

        assertFalse(json.containsKey("password"))
        assertFalse(json.containsKey("passwordHash"))
    }

    @Test
    fun `authenticated user can retrieve own profile`() = testApplication {
        val repository = createRepository()
        val jwtService = JwtService(jwtConfig)
        val authService = AuthService(
            userRepository = repository,
            passwordHasher = BCryptPasswordHasher(workFactor = 10)
        )

        application {
            configureSerialization()
            configureErrorHandling()
            configureSecurity(jwtConfig)

            routing {
                route("/api/v1") {
                    authRoutes(authService, jwtService, repository)
                }
            }
        }

        client.post("/api/v1/auth/register") {
            contentType(ContentType.Application.Json)
            setBody(registerBody())
        }

        val login = client.post("/api/v1/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(loginBody())
        }

        val token = Json.parseToJsonElement(
            login.bodyAsText()
        ).jsonObject["accessToken"]?.jsonPrimitive?.content

        assertNotNull(token)

        val response = client.get("/api/v1/auth/me") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }

        assertEquals(HttpStatusCode.OK, response.status)

        val profile = Json.parseToJsonElement(
            response.bodyAsText()
        ).jsonObject

        assertEquals(
            "Alice",
            profile["username"]?.jsonPrimitive?.content
        )
        assertEquals(
            "alice@example.com",
            profile["email"]?.jsonPrimitive?.content
        )
        assertFalse(profile.containsKey("passwordHash"))
    }

    @Test
    fun `missing bearer token returns unauthorized`() = testApplication {
        val repository = createRepository()
        val jwtService = JwtService(jwtConfig)
        val authService = AuthService(
            repository,
            BCryptPasswordHasher(workFactor = 10)
        )

        application {
            configureSerialization()
            configureErrorHandling()
            configureSecurity(jwtConfig)

            routing {
                route("/api/v1") {
                    authRoutes(authService, jwtService, repository)
                }
            }
        }

        val response = client.get("/api/v1/auth/me")

        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `invalid bearer token returns unauthorized`() = testApplication {
        val repository = createRepository()
        val jwtService = JwtService(jwtConfig)
        val authService = AuthService(
            repository,
            BCryptPasswordHasher(workFactor = 10)
        )

        application {
            configureSerialization()
            configureErrorHandling()
            configureSecurity(jwtConfig)

            routing {
                route("/api/v1") {
                    authRoutes(authService, jwtService, repository)
                }
            }
        }

        val response = client.get("/api/v1/auth/me") {
            header(HttpHeaders.Authorization, "Bearer invalid-token")
        }

        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `token for nonexistent user returns unauthorized`() = testApplication {
        val repository = createRepository()
        val jwtService = JwtService(jwtConfig)
        val authService = AuthService(
            repository,
            BCryptPasswordHasher(workFactor = 10)
        )

        application {
            configureSerialization()
            configureErrorHandling()
            configureSecurity(jwtConfig)

            routing {
                route("/api/v1") {
                    authRoutes(authService, jwtService, repository)
                }
            }
        }

        val token = jwtService.generateToken(999_999L)

        val response = client.get("/api/v1/auth/me") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }

        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `registration remains public without a token`() = testApplication {
        val repository = createRepository()
        val jwtService = JwtService(jwtConfig)
        val authService = AuthService(
            repository,
            BCryptPasswordHasher(workFactor = 10)
        )

        application {
            configureSerialization()
            configureErrorHandling()
            configureSecurity(jwtConfig)

            routing {
                route("/api/v1") {
                    authRoutes(authService, jwtService, repository)
                }
            }
        }

        val response = client.post("/api/v1/auth/register") {
            contentType(ContentType.Application.Json)
            setBody(registerBody())
        }

        assertEquals(HttpStatusCode.Created, response.status)
    }
}