// test/nl/brul/api/features/auth/routes/AuthApiIntegrationTest.kt
package nl.brul.api.features.auth.routes

import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
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
import nl.brul.api.plugins.configureSerialization
import nl.brul.api.security.BCryptPasswordHasher
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class AuthApiIntegrationTest {

    private fun createRepository(): ExposedUserRepository {
        val database = Database.connect(
            url = "jdbc:h2:mem:auth_api_${UUID.randomUUID()};DB_CLOSE_DELAY=-1",
            driver = "org.h2.Driver",
            user = "sa",
            password = ""
        )

        transaction(database) {
            SchemaUtils.create(UsersTable)
        }

        return ExposedUserRepository(database)
    }

    private fun createService(
        repository: ExposedUserRepository
    ): AuthService {
        return AuthService(
            userRepository = repository,
            passwordHasher = BCryptPasswordHasher(workFactor = 10)
        )
    }

    private fun registerJson(
        username: String = "Alice",
        email: String = "alice@example.com",
        password: String = "SecurePassword123"
    ): String {
        return """
            {
                "username": "$username",
                "email": "$email",
                "password": "$password"
            }
        """.trimIndent()
    }

    @Test
    fun `register persists user in H2 database`() = testApplication {
        val repository = createRepository()
        val service = createService(repository)

        application {
            configureSerialization()
            configureErrorHandling()

            routing {
                route("/api/v1") {
                    authRoutes(service)
                }
            }
        }

        val response = client.post("/api/v1/auth/register") {
            contentType(ContentType.Application.Json)
            setBody(registerJson())
        }

        assertEquals(HttpStatusCode.Created, response.status)

        val storedUser = repository.findByEmail("alice@example.com")

        assertNotNull(storedUser)
        assertEquals("Alice", storedUser.username)
        assertEquals("alice@example.com", storedUser.email)
    }

    @Test
    fun `register stores a real BCrypt password hash`() = testApplication {
        val repository = createRepository()
        val service = createService(repository)

        application {
            configureSerialization()
            configureErrorHandling()

            routing {
                route("/api/v1") {
                    authRoutes(service)
                }
            }
        }

        val password = "SecurePassword123"

        val response = client.post("/api/v1/auth/register") {
            contentType(ContentType.Application.Json)
            setBody(registerJson(password = password))
        }

        assertEquals(HttpStatusCode.Created, response.status)

        val storedUser = repository.findByEmail("alice@example.com")
        assertNotNull(storedUser)

        assertNotEquals(password, storedUser.passwordHash)
        assertTrue(storedUser.passwordHash.startsWith("\$2a\$"))

        val hasher = BCryptPasswordHasher(workFactor = 10)

        assertTrue(
            hasher.verify(password, storedUser.passwordHash)
        )
    }

    @Test
    fun `registered user can log in through HTTP`() = testApplication {
        val repository = createRepository()
        val service = createService(repository)

        application {
            configureSerialization()
            configureErrorHandling()

            routing {
                route("/api/v1") {
                    authRoutes(service)
                }
            }
        }

        val registerResponse = client.post("/api/v1/auth/register") {
            contentType(ContentType.Application.Json)
            setBody(registerJson())
        }

        assertEquals(HttpStatusCode.Created, registerResponse.status)

        val loginResponse = client.post("/api/v1/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(
                """
                {
                    "email": "alice@example.com",
                    "password": "SecurePassword123"
                }
                """.trimIndent()
            )
        }

        assertEquals(HttpStatusCode.OK, loginResponse.status)

        val json = Json.parseToJsonElement(
            loginResponse.bodyAsText()
        ).jsonObject

        assertEquals(
            "alice@example.com",
            json["email"]?.jsonPrimitive?.content
        )
    }

    @Test
    fun `login rejects incorrect password with real BCrypt`() = testApplication {
        val repository = createRepository()
        val service = createService(repository)

        application {
            configureSerialization()
            configureErrorHandling()

            routing {
                route("/api/v1") {
                    authRoutes(service)
                }
            }
        }

        client.post("/api/v1/auth/register") {
            contentType(ContentType.Application.Json)
            setBody(registerJson())
        }

        val response = client.post("/api/v1/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(
                """
                {
                    "email": "alice@example.com",
                    "password": "WrongPassword"
                }
                """.trimIndent()
            )
        }

        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `duplicate email returns 409 with real database`() = testApplication {
        val repository = createRepository()
        val service = createService(repository)

        application {
            configureSerialization()
            configureErrorHandling()

            routing {
                route("/api/v1") {
                    authRoutes(service)
                }
            }
        }

        val firstResponse = client.post("/api/v1/auth/register") {
            contentType(ContentType.Application.Json)
            setBody(registerJson())
        }

        val secondResponse = client.post("/api/v1/auth/register") {
            contentType(ContentType.Application.Json)
            setBody(
                registerJson(
                    username = "Bob",
                    email = "ALICE@EXAMPLE.COM"
                )
            )
        }

        assertEquals(HttpStatusCode.Created, firstResponse.status)
        assertEquals(HttpStatusCode.Conflict, secondResponse.status)
    }

    @Test
    fun `registration response never exposes password hash`() = testApplication {
        val repository = createRepository()
        val service = createService(repository)

        application {
            configureSerialization()
            configureErrorHandling()

            routing {
                route("/api/v1") {
                    authRoutes(service)
                }
            }
        }

        val response = client.post("/api/v1/auth/register") {
            contentType(ContentType.Application.Json)
            setBody(registerJson())
        }

        assertEquals(HttpStatusCode.Created, response.status)

        val body = response.bodyAsText()
        val json = Json.parseToJsonElement(body).jsonObject

        assertFalse(json.containsKey("password"))
        assertFalse(json.containsKey("passwordHash"))
        assertFalse(body.contains("SecurePassword123"))
        assertFalse(body.contains("\$2a\$"))
    }

    @Test
    fun `login response never exposes password hash`() = testApplication {
        val repository = createRepository()
        val service = createService(repository)

        application {
            configureSerialization()
            configureErrorHandling()

            routing {
                route("/api/v1") {
                    authRoutes(service)
                }
            }
        }

        client.post("/api/v1/auth/register") {
            contentType(ContentType.Application.Json)
            setBody(registerJson())
        }

        val response = client.post("/api/v1/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(
                """
                {
                    "email": "alice@example.com",
                    "password": "SecurePassword123"
                }
                """.trimIndent()
            )
        }

        assertEquals(HttpStatusCode.OK, response.status)

        val body = response.bodyAsText()
        val json = Json.parseToJsonElement(body).jsonObject

        assertFalse(json.containsKey("password"))
        assertFalse(json.containsKey("passwordHash"))
        assertFalse(body.contains("SecurePassword123"))
        assertFalse(body.contains("\$2a\$"))
    }

    @Test
    fun `email normalization works with real database`() = testApplication {
        val repository = createRepository()
        val service = createService(repository)

        application {
            configureSerialization()
            configureErrorHandling()

            routing {
                route("/api/v1") {
                    authRoutes(service)
                }
            }
        }

        val registerResponse = client.post("/api/v1/auth/register") {
            contentType(ContentType.Application.Json)
            setBody(
                registerJson(
                    username = "Alice",
                    email = "ALICE@EXAMPLE.COM"
                )
            )
        }

        assertEquals(HttpStatusCode.Created, registerResponse.status)

        val storedUser = repository.findByEmail("alice@example.com")
        assertNotNull(storedUser)
        assertEquals("alice@example.com", storedUser.email)

        val loginResponse = client.post("/api/v1/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(
                """
                {
                    "email": "  ALICE@EXAMPLE.COM  ",
                    "password": "SecurePassword123"
                }
                """.trimIndent()
            )
        }

        assertEquals(HttpStatusCode.OK, loginResponse.status)
    }
}