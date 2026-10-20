// test/nl/brul/api/features/auth/routes/AuthRoutesTest.kt
package nl.brul.api.features.auth.routes

import io.ktor.client.request.post
import io.ktor.client.request.setBody
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
import nl.brul.api.features.user.model.User
import nl.brul.api.features.user.model.NewUser
import nl.brul.api.features.user.repository.UserRepository
import nl.brul.api.plugins.configureErrorHandling
import nl.brul.api.plugins.configureSerialization
import nl.brul.api.security.PasswordHasher
import io.ktor.client.statement.bodyAsText
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AuthRoutesTest {

    private class FakeUserRepository : UserRepository {
        private val users = mutableMapOf<Long, User>()
        private var nextId = 1L

        override fun create(user: NewUser): User {
            require(findByEmail(user.email) == null) {
                "Email address is already registered."
            }

            val createdUser = User(
                id = nextId++,
                username = user.username,
                email = user.email,
                passwordHash = user.passwordHash,
                createdAt = 1_700_000_000_000L
            )

            users[createdUser.id] = createdUser
            return createdUser
        }

        override fun findById(id: Long): User? {
            return users[id]
        }

        override fun findByEmail(email: String): User? {
            return users.values.firstOrNull {
                it.email.equals(email, ignoreCase = true)
            }
        }
    }

    private class FakePasswordHasher : PasswordHasher {

        override fun hash(password: String): String {
            return "hashed:$password"
        }

        override fun verify(
            password: String,
            passwordHash: String
        ): Boolean {
            return hash(password) == passwordHash
        }
    }

    private fun createService(): AuthService {
        return AuthService(
            userRepository = FakeUserRepository(),
            passwordHasher = FakePasswordHasher()
        )
    }

    @Test
    fun `register returns 201 Created`() = testApplication {
        application {
            configureSerialization()
            configureErrorHandling()

            routing {
                route("/api/v1") {
                    authRoutes(createService())
                }
            }
        }

        val response = client.post("/api/v1/auth/register") {
            contentType(ContentType.Application.Json)
            setBody(
                """
                {
                    "username": "Alice",
                    "email": "alice@example.com",
                    "password": "SecurePassword123"
                }
                """.trimIndent()
            )
        }

        assertEquals(HttpStatusCode.Created, response.status)
    }

    @Test
    fun `register returns user information without password hash`() = testApplication {
        application {
            configureSerialization()
            configureErrorHandling()

            routing {
                route("/api/v1") {
                    authRoutes(createService())
                }
            }
        }

        val response = client.post("/api/v1/auth/register") {
            contentType(ContentType.Application.Json)
            setBody(
                """
                {
                    "username": "Alice",
                    "email": "alice@example.com",
                    "password": "SecurePassword123"
                }
                """.trimIndent()
            )
        }

        val body = response.bodyAsText()
        val json = Json.parseToJsonElement(body).jsonObject

        assertEquals("Alice", json["username"]?.jsonPrimitive?.content)
        assertEquals("alice@example.com", json["email"]?.jsonPrimitive?.content)
        assertFalse(body.contains("passwordHash"))
        assertFalse(body.contains("SecurePassword123"))
        assertFalse(body.contains("hashed:"))
    }

    @Test
    fun `register returns 400 for invalid email`() = testApplication {
        application {
            configureSerialization()
            configureErrorHandling()

            routing {
                route("/api/v1") {
                    authRoutes(createService())
                }
            }
        }

        val response = client.post("/api/v1/auth/register") {
            contentType(ContentType.Application.Json)
            setBody(
                """
                {
                    "username": "Alice",
                    "email": "invalid-email",
                    "password": "SecurePassword123"
                }
                """.trimIndent()
            )
        }

        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun `register returns 409 for duplicate email`() = testApplication {
        val service = createService()

        application {
            configureSerialization()
            configureErrorHandling()

            routing {
                route("/api/v1") {
                    authRoutes(service)
                }
            }
        }

        val request = """
            {
                "username": "Alice",
                "email": "alice@example.com",
                "password": "SecurePassword123"
            }
        """.trimIndent()

        val firstResponse = client.post("/api/v1/auth/register") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }

        val secondResponse = client.post("/api/v1/auth/register") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }

        assertEquals(HttpStatusCode.Created, firstResponse.status)
        assertEquals(HttpStatusCode.Conflict, secondResponse.status)
    }

    @Test
    fun `login returns 200 for valid credentials`() = testApplication {
        val service = createService()

        service.register(
            username = "Alice",
            email = "alice@example.com",
            password = "SecurePassword123"
        )

        application {
            configureSerialization()
            configureErrorHandling()

            routing {
                route("/api/v1") {
                    authRoutes(service)
                }
            }
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
        assertTrue(body.contains("alice@example.com"))
        assertFalse(body.contains("passwordHash"))
        assertFalse(body.contains("SecurePassword123"))
    }

    @Test
    fun `login returns 401 for incorrect password`() = testApplication {
        val service = createService()

        service.register(
            username = "Alice",
            email = "alice@example.com",
            password = "SecurePassword123"
        )

        application {
            configureSerialization()
            configureErrorHandling()

            routing {
                route("/api/v1") {
                    authRoutes(service)
                }
            }
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
    fun `login returns 401 for unknown email`() = testApplication {
        application {
            configureSerialization()
            configureErrorHandling()

            routing {
                route("/api/v1") {
                    authRoutes(createService())
                }
            }
        }

        val response = client.post("/api/v1/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(
                """
                {
                    "email": "unknown@example.com",
                    "password": "SecurePassword123"
                }
                """.trimIndent()
            )
        }

        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `register returns 400 for missing password`() = testApplication {
        application {
            configureSerialization()
            configureErrorHandling()

            routing {
                route("/api/v1") {
                    authRoutes(createService())
                }
            }
        }

        val response = client.post("/api/v1/auth/register") {
            contentType(ContentType.Application.Json)
            setBody(
                """
                {
                    "username": "Alice",
                    "email": "alice@example.com"
                }
                """.trimIndent()
            )
        }

        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun `register returns 400 for malformed JSON`() = testApplication {
        application {
            configureSerialization()
            configureErrorHandling()

            routing {
                route("/api/v1") {
                    authRoutes(createService())
                }
            }
        }

        val response = client.post("/api/v1/auth/register") {
            contentType(ContentType.Application.Json)
            setBody("""{"username": "Alice", """)
        }

        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun `register rejects unknown JSON fields`() = testApplication {
        application {
            configureSerialization()
            configureErrorHandling()

            routing {
                route("/api/v1") {
                    authRoutes(createService())
                }
            }
        }

        val response = client.post("/api/v1/auth/register") {
            contentType(ContentType.Application.Json)
            setBody(
                """
                {
                    "username": "Alice",
                    "email": "alice@example.com",
                    "password": "SecurePassword123",
                    "isAdmin": true
                }
                """.trimIndent()
            )
        }

        assertEquals(HttpStatusCode.BadRequest, response.status)
    }
}