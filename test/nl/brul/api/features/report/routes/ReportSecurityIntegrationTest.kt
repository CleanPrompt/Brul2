// test/nl/brul/api/features/report/routes/ReportSecurityIntegrationTest.kt
package nl.brul.api.features.report.routes

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.install
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.testApplication
import java.util.Date
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import nl.brul.api.features.auth.routes.authRoutes
import nl.brul.api.features.auth.service.AuthService
import nl.brul.api.features.report.dto.CreateReportRequest
import nl.brul.api.features.report.dto.ReportResponse
import nl.brul.api.features.report.dto.UpdateReportRequest
import nl.brul.api.features.report.repository.ExposedReportRepository
import nl.brul.api.features.report.repository.ReportsTable
import nl.brul.api.features.report.service.ReportService
import nl.brul.api.features.user.repository.ExposedUserRepository
import nl.brul.api.features.user.repository.UsersTable
import nl.brul.api.plugins.configureErrorHandling
import nl.brul.api.plugins.configureSecurity
import nl.brul.api.security.BCryptPasswordHasher
import nl.brul.api.security.JwtConfig
import nl.brul.api.security.JwtService
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

class ReportSecurityIntegrationTest {

    private val json = Json {
        ignoreUnknownKeys = false
    }

    private val jwtSecret = "security-integration-test-secret-at-least-32-bytes"
    private val jwtIssuer = "brul-security-test"
    private val jwtAudience = "brul-security-users"

    private fun testWithDatabase(
        block: suspend ApplicationTestBuilder.() -> Unit
    ) = testApplication {
        val databaseName = "security_${UUID.randomUUID()}"

        val database = Database.connect(
            url = "jdbc:h2:mem:$databaseName;DB_CLOSE_DELAY=-1",
            driver = "org.h2.Driver",
            user = "sa",
            password = ""
        )

        transaction(database) {
            SchemaUtils.create(UsersTable, ReportsTable)
        }

        val userRepository = ExposedUserRepository(database)
        val reportRepository = ExposedReportRepository(database)

        val authService = AuthService(
            userRepository = userRepository,
            passwordHasher = BCryptPasswordHasher()
        )

        val reportService = ReportService(reportRepository)

        val jwtConfig = JwtConfig(
            secret = jwtSecret,
            issuer = jwtIssuer,
            audience = jwtAudience,
            validityMillis = 3_600_000L
        )

        val jwtService = JwtService(jwtConfig)

        application {
            install(ContentNegotiation) {
                json(json)
            }

            configureSecurity(jwtConfig)
            configureErrorHandling()

            routing {
                route("/api/v1") {
                    authRoutes(
                        authService = authService,
                        jwtService = jwtService,
                        userRepository = userRepository
                    )

                    reportRoutes(reportService)
                }
            }
        }

        block()
    }

    private fun createRequest() = CreateReportRequest(
        type = "SAFETY",
        title = "Unsafe crossing",
        description = "Cars drive too fast near the school",
        category = "TRAFFIC_SAFETY",
        latitude = 52.0705,
        longitude = 4.3007,
        urgent = true
    )

    private fun updateRequest() = UpdateReportRequest(
        type = "SAFETY",
        title = "Updated crossing",
        description = "The crossing remains dangerous",
        category = "TRAFFIC_SAFETY",
        latitude = 52.0705,
        longitude = 4.3007,
        status = "IN_PROGRESS",
        urgent = false
    )

    private suspend fun ApplicationTestBuilder.register(
        username: String,
        email: String
    ): Long {
        val response = client.post("/api/v1/auth/register") {
            contentType(ContentType.Application.Json)
            setBody(
                """
                {
                    "username": "$username",
                    "email": "$email",
                    "password": "SecurePassword123!"
                }
                """.trimIndent()
            )
        }

        assertEquals(HttpStatusCode.Created, response.status)

        val userId = json.parseToJsonElement(
            response.bodyAsText()
        ).jsonObject["id"]?.jsonPrimitive?.content?.toLong()

        return assertNotNull(userId)
    }

    private suspend fun ApplicationTestBuilder.login(
        email: String
    ): String {
        val response = client.post("/api/v1/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(
                """
                {
                    "email": "$email",
                    "password": "SecurePassword123!"
                }
                """.trimIndent()
            )
        }

        assertEquals(HttpStatusCode.OK, response.status)

        val token = json.parseToJsonElement(
            response.bodyAsText()
        ).jsonObject["accessToken"]?.jsonPrimitive?.content

        return assertNotNull(token)
    }

    private suspend fun ApplicationTestBuilder.createReport(
        token: String
    ): ReportResponse {
        val response = client.post("/api/v1/reports") {
            bearerAuth(token)
            contentType(ContentType.Application.Json)
            setBody(
                json.encodeToString(
                    CreateReportRequest.serializer(),
                    createRequest()
                )
            )
        }

        assertEquals(HttpStatusCode.Created, response.status)

        return json.decodeFromString(
            ReportResponse.serializer(),
            response.bodyAsText()
        )
    }

    private fun expiredToken(userId: Long): String =
        JWT.create()
            .withSubject(userId.toString())
            .withIssuer(jwtIssuer)
            .withAudience(jwtAudience)
            .withClaim("role", "USER")
            .withIssuedAt(Date(System.currentTimeMillis() - 7_200_000L))
            .withExpiresAt(Date(System.currentTimeMillis() - 3_600_000L))
            .sign(Algorithm.HMAC256(jwtSecret))

    private fun wrongSignatureToken(userId: Long): String =
        JWT.create()
            .withSubject(userId.toString())
            .withIssuer(jwtIssuer)
            .withAudience(jwtAudience)
            .withClaim("role", "USER")
            .withIssuedAt(Date())
            .withExpiresAt(Date(System.currentTimeMillis() + 3_600_000L))
            .sign(
                Algorithm.HMAC256(
                    "another-secret-with-at-least-32-bytes"
                )
            )

    // ---------------------------------------------------------
    // JWT authentication tests
    // ---------------------------------------------------------

    @Test
    fun `POST report without token returns 401`() =
        testWithDatabase {
            val response = client.post("/api/v1/reports") {
                contentType(ContentType.Application.Json)
                setBody(
                    json.encodeToString(
                        CreateReportRequest.serializer(),
                        createRequest()
                    )
                )
            }

            assertEquals(HttpStatusCode.Unauthorized, response.status)
        }

    @Test
    fun `POST report with malformed token returns 401`() =
        testWithDatabase {
            val response = client.post("/api/v1/reports") {
                bearerAuth("not-a-valid-jwt")
                contentType(ContentType.Application.Json)
                setBody(
                    json.encodeToString(
                        CreateReportRequest.serializer(),
                        createRequest()
                    )
                )
            }

            assertEquals(HttpStatusCode.Unauthorized, response.status)
        }

    @Test
    fun `POST report with expired token returns 401`() =
        testWithDatabase {
            val userId = register(
                username = "owner",
                email = "owner@example.com"
            )

            val response = client.post("/api/v1/reports") {
                bearerAuth(expiredToken(userId))
                contentType(ContentType.Application.Json)
                setBody(
                    json.encodeToString(
                        CreateReportRequest.serializer(),
                        createRequest()
                    )
                )
            }

            assertEquals(HttpStatusCode.Unauthorized, response.status)
        }

    @Test
    fun `POST report with wrong signature returns 401`() =
        testWithDatabase {
            val userId = register(
                username = "owner",
                email = "owner@example.com"
            )

            val response = client.post("/api/v1/reports") {
                bearerAuth(wrongSignatureToken(userId))
                contentType(ContentType.Application.Json)
                setBody(
                    json.encodeToString(
                        CreateReportRequest.serializer(),
                        createRequest()
                    )
                )
            }

            assertEquals(HttpStatusCode.Unauthorized, response.status)
        }

    @Test
    fun `PUT report with expired token returns 401`() =
        testWithDatabase {
            val userId = register(
                username = "owner",
                email = "owner@example.com"
            )

            val ownerToken = login("owner@example.com")
            val report = createReport(ownerToken)

            val response = client.put(
                "/api/v1/reports/${report.id}"
            ) {
                bearerAuth(expiredToken(userId))
                contentType(ContentType.Application.Json)
                setBody(
                    json.encodeToString(
                        UpdateReportRequest.serializer(),
                        updateRequest()
                    )
                )
            }

            assertEquals(HttpStatusCode.Unauthorized, response.status)
        }

    @Test
    fun `DELETE report with expired token returns 401`() =
        testWithDatabase {
            val userId = register(
                username = "owner",
                email = "owner@example.com"
            )

            val ownerToken = login("owner@example.com")
            val report = createReport(ownerToken)

            val response = client.delete(
                "/api/v1/reports/${report.id}"
            ) {
                bearerAuth(expiredToken(userId))
            }

            assertEquals(HttpStatusCode.Unauthorized, response.status)

            val retrieved = client.get(
                "/api/v1/reports/${report.id}"
            )

            assertEquals(HttpStatusCode.OK, retrieved.status)
        }

    // ---------------------------------------------------------
    // Ownership authorization tests
    // ---------------------------------------------------------

    @Test
    fun `different USER cannot update another user's report`() =
        testWithDatabase {
            register("owner", "owner@example.com")
            register("other", "other@example.com")

            val ownerToken = login("owner@example.com")
            val otherToken = login("other@example.com")

            val report = createReport(ownerToken)

            val response = client.put(
                "/api/v1/reports/${report.id}"
            ) {
                bearerAuth(otherToken)
                contentType(ContentType.Application.Json)
                setBody(
                    json.encodeToString(
                        UpdateReportRequest.serializer(),
                        updateRequest()
                    )
                )
            }

            assertEquals(HttpStatusCode.Forbidden, response.status)

            val retrieved = client.get(
                "/api/v1/reports/${report.id}"
            )

            val unchanged = json.decodeFromString(
                ReportResponse.serializer(),
                retrieved.bodyAsText()
            )

            assertEquals("Unsafe crossing", unchanged.title)
        }

    @Test
    fun `different USER cannot delete another user's report`() =
        testWithDatabase {
            register("owner", "owner@example.com")
            register("other", "other@example.com")

            val ownerToken = login("owner@example.com")
            val otherToken = login("other@example.com")

            val report = createReport(ownerToken)

            val response = client.delete(
                "/api/v1/reports/${report.id}"
            ) {
                bearerAuth(otherToken)
            }

            assertEquals(HttpStatusCode.Forbidden, response.status)

            val retrieved = client.get(
                "/api/v1/reports/${report.id}"
            )

            assertEquals(HttpStatusCode.OK, retrieved.status)
        }

    // ---------------------------------------------------------
    // Public read access tests
    // ---------------------------------------------------------

    @Test
    fun `GET reports remains public`() =
        testWithDatabase {
            val response = client.get("/api/v1/reports")

            assertEquals(HttpStatusCode.OK, response.status)
        }

    @Test
    fun `GET report by ID remains public`() =
        testWithDatabase {
            register("owner", "owner@example.com")

            val ownerToken = login("owner@example.com")
            val report = createReport(ownerToken)

            val response = client.get(
                "/api/v1/reports/${report.id}"
            )

            assertEquals(HttpStatusCode.OK, response.status)
        }

    // ---------------------------------------------------------
    // Registration security tests
    // ---------------------------------------------------------

    @Test
    fun `registration cannot assign ADMIN role`() =
        testWithDatabase {
            val response = client.post("/api/v1/auth/register") {
                contentType(ContentType.Application.Json)
                setBody(
                    """
                    {
                        "username": "attacker",
                        "email": "attacker@example.com",
                        "password": "SecurePassword123!",
                        "role": "ADMIN"
                    }
                    """.trimIndent()
                )
            }

            // Unknown JSON fields must be rejected.
            assertEquals(HttpStatusCode.BadRequest, response.status)

            // The rejected registration must not create an account.
            val loginResponse = client.post("/api/v1/auth/login") {
                contentType(ContentType.Application.Json)
                setBody(
                    """
                    {
                        "email": "attacker@example.com",
                        "password": "SecurePassword123!"
                    }
                    """.trimIndent()
                )
            }

            assertTrue(loginResponse.status != HttpStatusCode.OK)
        }

    @Test
    fun `registered user receives USER role and no password hash`() =
        testWithDatabase {
            register(
                username = "resident",
                email = "resident@example.com"
            )

            val loginResponse = client.post("/api/v1/auth/login") {
                contentType(ContentType.Application.Json)
                setBody(
                    """
                    {
                        "email": "resident@example.com",
                        "password": "SecurePassword123!"
                    }
                    """.trimIndent()
                )
            }

            assertEquals(HttpStatusCode.OK, loginResponse.status)

            val responseBody = loginResponse.bodyAsText()

            assertFalse(responseBody.contains("passwordHash"))
            assertFalse(responseBody.contains("SecurePassword123!"))

            val token = json.parseToJsonElement(
                responseBody
            ).jsonObject["accessToken"]?.jsonPrimitive?.content

            val decodedToken = JWT.decode(assertNotNull(token))

            assertEquals(
                "USER",
                decodedToken.getClaim("role").asString()
            )
        }
}