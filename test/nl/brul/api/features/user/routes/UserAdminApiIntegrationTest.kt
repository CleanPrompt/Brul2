// test/nl/brul/api/features/user/routes/UserAdminApiIntegrationTest.kt
package nl.brul.api.features.user.routes

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.client.request.patch
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.jwt.jwt
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.testApplication
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import nl.brul.api.features.user.model.NewUser
import nl.brul.api.features.user.model.UserRole
import nl.brul.api.features.user.repository.ExposedUserRepository
import nl.brul.api.features.user.repository.UsersTable
import nl.brul.api.features.user.service.UserService
import nl.brul.api.plugins.configureErrorHandling
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class UserAdminApiIntegrationTest {

    private val jwtSecret = "user-admin-integration-test-secret"
    private val jwtIssuer = "brul-test"
    private val jwtAudience = "brul-test-users"

    private val json = Json {
        ignoreUnknownKeys = false
    }

    private fun token(
        userId: Long,
        role: String = "USER"
    ): String {
        return JWT.create()
            .withSubject(userId.toString())
            .withIssuer(jwtIssuer)
            .withAudience(jwtAudience)
            .withClaim("role", role)
            .sign(Algorithm.HMAC256(jwtSecret))
    }

    private fun testWithUsers(
        block: suspend ApplicationTestBuilder.(
            ExposedUserRepository
        ) -> Unit
    ) = testApplication {
        val databaseName = "user_admin_${UUID.randomUUID()}"

        val database = Database.connect(
            url = "jdbc:h2:mem:$databaseName;DB_CLOSE_DELAY=-1",
            driver = "org.h2.Driver",
            user = "sa",
            password = ""
        )

        transaction(database) {
            SchemaUtils.create(UsersTable)
        }

        val repository = ExposedUserRepository(database)

        val admin = repository.create(
            NewUser(
                username = "Admin",
                email = "admin@example.com",
                passwordHash = "admin-password-hash"
            )
        )

        val alice = repository.create(
            NewUser(
                username = "Alice",
                email = "alice@example.com",
                passwordHash = "alice-password-hash"
            )
        )

        val bob = repository.create(
            NewUser(
                username = "Bob",
                email = "bob@example.com",
                passwordHash = "bob-password-hash"
            )
        )

        assertEquals(1L, admin.id)
        assertEquals(2L, alice.id)
        assertEquals(3L, bob.id)

        repository.updateRole(admin.id, UserRole.ADMIN)

        val service = UserService(repository)

        application {
            install(ContentNegotiation) {
                json(json)
            }

            install(Authentication) {
                jwt("auth-jwt") {
                    verifier(
                        JWT.require(Algorithm.HMAC256(jwtSecret))
                            .withIssuer(jwtIssuer)
                            .withAudience(jwtAudience)
                            .build()
                    )

                    validate { credential ->
                        val userId = credential.payload.subject
                            ?.toLongOrNull()

                        if (userId != null && userId > 0) {
                            JWTPrincipal(credential.payload)
                        } else {
                            null
                        }
                    }
                }
            }

            configureErrorHandling()

            routing {
                route("/api/v1") {
                    userRoutes(service)
                }
            }
        }

        block(repository)
    }

    @Test
    fun `admin can list all users`() = testWithUsers { _ ->
        val response = client.get("/api/v1/users") {
            bearerAuth(token(userId = 1L, role = "ADMIN"))
        }

        assertEquals(HttpStatusCode.OK, response.status)

        val users = json.parseToJsonElement(
            response.bodyAsText()
        ).jsonArray

        assertEquals(3, users.size)

        assertEquals(
            "Admin",
            users[0].jsonObject["username"]?.jsonPrimitive?.content
        )

        assertEquals(
            "Alice",
            users[1].jsonObject["username"]?.jsonPrimitive?.content
        )

        assertEquals(
            "Bob",
            users[2].jsonObject["username"]?.jsonPrimitive?.content
        )
    }

    @Test
    fun `user list does not expose password hashes`() =
        testWithUsers { _ ->
            val response = client.get("/api/v1/users") {
                bearerAuth(token(userId = 1L, role = "ADMIN"))
            }

            assertEquals(HttpStatusCode.OK, response.status)

            val body = response.bodyAsText()

            assertFalse(body.contains("passwordHash"))
            assertFalse(body.contains("password_hash"))
            assertFalse(body.contains("admin-password-hash"))
            assertFalse(body.contains("alice-password-hash"))
            assertFalse(body.contains("bob-password-hash"))
        }

    @Test
    fun `regular user cannot list users`() = testWithUsers { _ ->
        val response = client.get("/api/v1/users") {
            bearerAuth(token(userId = 2L))
        }

        assertEquals(HttpStatusCode.Forbidden, response.status)
    }

    @Test
    fun `listing users without token returns 401`() =
        testWithUsers { _ ->
            val response = client.get("/api/v1/users")

            assertEquals(
                HttpStatusCode.Unauthorized,
                response.status
            )
        }

    @Test
    fun `admin can retrieve user by id`() = testWithUsers { _ ->
        val response = client.get("/api/v1/users/2") {
            bearerAuth(token(userId = 1L, role = "ADMIN"))
        }

        assertEquals(HttpStatusCode.OK, response.status)

        val user = json.parseToJsonElement(
            response.bodyAsText()
        ).jsonObject

        assertEquals("2", user["id"]?.jsonPrimitive?.content)
        assertEquals(
            "Alice",
            user["username"]?.jsonPrimitive?.content
        )
        assertEquals(
            "alice@example.com",
            user["email"]?.jsonPrimitive?.content
        )
        assertEquals(
            "USER",
            user["role"]?.jsonPrimitive?.content
        )

        assertFalse(response.bodyAsText().contains("passwordHash"))
    }

    @Test
    fun `admin gets 404 for unknown user`() = testWithUsers { _ ->
        val response = client.get("/api/v1/users/999") {
            bearerAuth(token(userId = 1L, role = "ADMIN"))
        }

        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun `invalid user id returns 400`() = testWithUsers { _ ->
        val response = client.get("/api/v1/users/invalid") {
            bearerAuth(token(userId = 1L, role = "ADMIN"))
        }

        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun `regular user cannot retrieve another user`() =
        testWithUsers { _ ->
            val response = client.get("/api/v1/users/3") {
                bearerAuth(token(userId = 2L))
            }

            assertEquals(HttpStatusCode.Forbidden, response.status)
        }

    @Test
    fun `admin can promote user to admin`() = testWithUsers { repository ->
        val response = client.patch("/api/v1/users/2/role") {
            bearerAuth(token(userId = 1L, role = "ADMIN"))
            contentType(ContentType.Application.Json)
            setBody("""{"role":"ADMIN"}""")
        }

        assertEquals(HttpStatusCode.OK, response.status)

        val responseUser = json.parseToJsonElement(
            response.bodyAsText()
        ).jsonObject

        assertEquals(
            "ADMIN",
            responseUser["role"]?.jsonPrimitive?.content
        )

        val storedUser = repository.findById(2L)

        assertNotNull(storedUser)
        assertEquals(UserRole.ADMIN, storedUser.role)
    }

    @Test
    fun `admin can demote another admin`() = testWithUsers { repository ->
        repository.updateRole(2L, UserRole.ADMIN)

        val response = client.patch("/api/v1/users/2/role") {
            bearerAuth(token(userId = 1L, role = "ADMIN"))
            contentType(ContentType.Application.Json)
            setBody("""{"role":"USER"}""")
        }

        assertEquals(HttpStatusCode.OK, response.status)

        val storedUser = repository.findById(2L)

        assertNotNull(storedUser)
        assertEquals(UserRole.USER, storedUser.role)
    }

    @Test
    fun `regular user cannot change roles`() =
        testWithUsers { repository ->
            val response = client.patch("/api/v1/users/3/role") {
                bearerAuth(token(userId = 2L))
                contentType(ContentType.Application.Json)
                setBody("""{"role":"ADMIN"}""")
            }

            assertEquals(HttpStatusCode.Forbidden, response.status)

            val storedUser = repository.findById(3L)

            assertNotNull(storedUser)
            assertEquals(UserRole.USER, storedUser.role)
        }

    @Test
    fun `role update without token returns 401`() =
        testWithUsers { repository ->
            val response = client.patch("/api/v1/users/2/role") {
                contentType(ContentType.Application.Json)
                setBody("""{"role":"ADMIN"}""")
            }

            assertEquals(
                HttpStatusCode.Unauthorized,
                response.status
            )

            assertEquals(
                UserRole.USER,
                repository.findById(2L)?.role
            )
        }

    @Test
    fun `invalid role returns 400`() = testWithUsers { repository ->
        val response = client.patch("/api/v1/users/2/role") {
            bearerAuth(token(userId = 1L, role = "ADMIN"))
            contentType(ContentType.Application.Json)
            setBody("""{"role":"SUPER_ADMIN"}""")
        }

        assertEquals(HttpStatusCode.BadRequest, response.status)

        assertEquals(
            UserRole.USER,
            repository.findById(2L)?.role
        )
    }

    @Test
    fun `admin cannot remove own admin role`() =
        testWithUsers { repository ->
            val response = client.patch("/api/v1/users/1/role") {
                bearerAuth(token(userId = 1L, role = "ADMIN"))
                contentType(ContentType.Application.Json)
                setBody("""{"role":"USER"}""")
            }

            assertEquals(HttpStatusCode.BadRequest, response.status)

            assertEquals(
                UserRole.ADMIN,
                repository.findById(1L)?.role
            )
        }

    @Test
    fun `role update returns 404 for unknown user`() =
        testWithUsers { _ ->
            val response = client.patch("/api/v1/users/999/role") {
                bearerAuth(token(userId = 1L, role = "ADMIN"))
                contentType(ContentType.Application.Json)
                setBody("""{"role":"ADMIN"}""")
            }

            assertEquals(HttpStatusCode.NotFound, response.status)
        }

    @Test
    fun `demoted admin immediately loses admin access`() =
        testWithUsers { repository ->
            repository.updateRole(2L, UserRole.ADMIN)

            val oldAdminToken = token(
                userId = 2L,
                role = "ADMIN"
            )

            val beforeDemotion = client.get("/api/v1/users") {
                bearerAuth(oldAdminToken)
            }

            assertEquals(
                HttpStatusCode.OK,
                beforeDemotion.status
            )

            val demotion = client.patch("/api/v1/users/2/role") {
                bearerAuth(token(userId = 1L, role = "ADMIN"))
                contentType(ContentType.Application.Json)
                setBody("""{"role":"USER"}""")
            }

            assertEquals(HttpStatusCode.OK, demotion.status)

            // The old JWT still contains ADMIN, but the database does not.
            val afterDemotion = client.get("/api/v1/users") {
                bearerAuth(oldAdminToken)
            }

            assertEquals(
                HttpStatusCode.Forbidden,
                afterDemotion.status
            )
        }

    @Test
    fun `fake admin role in JWT does not grant admin access`() =
        testWithUsers { _ ->
            val response = client.get("/api/v1/users") {
                bearerAuth(
                    token(
                        userId = 2L,
                        role = "ADMIN"
                    )
                )
            }

            assertEquals(HttpStatusCode.Forbidden, response.status)
        }

    @Test
    fun `unknown authenticated user cannot access admin routes`() =
        testWithUsers { _ ->
            val response = client.get("/api/v1/users") {
                bearerAuth(
                    token(
                        userId = 999L,
                        role = "ADMIN"
                    )
                )
            }

            assertEquals(HttpStatusCode.Forbidden, response.status)
        }

    @Test
    fun `admin can update role without exposing password hash`() =
        testWithUsers { _ ->
            val response = client.patch("/api/v1/users/2/role") {
                bearerAuth(token(userId = 1L, role = "ADMIN"))
                contentType(ContentType.Application.Json)
                setBody("""{"role":"ADMIN"}""")
            }

            assertEquals(HttpStatusCode.OK, response.status)

            val body = response.bodyAsText()

            assertTrue(body.contains("ADMIN"))
            assertFalse(body.contains("passwordHash"))
            assertFalse(body.contains("alice-password-hash"))
        }
}