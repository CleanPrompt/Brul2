// test/nl/brul/api/features/report/routes/ReportAuthenticationFlowTest.kt
package nl.brul.api.features.report.routes

import com.auth0.jwt.JWT
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.post
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
import io.ktor.server.testing.testApplication
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import nl.brul.api.features.auth.routes.authRoutes
import nl.brul.api.features.auth.service.AuthService
import nl.brul.api.features.report.dto.CreateReportRequest
import nl.brul.api.features.report.dto.ReportResponse
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

class ReportAuthenticationFlowTest {

    @Test
    fun `registered user can log in and create an owned report`() = testApplication {
        val databaseName = "auth_flow_${UUID.randomUUID()}"

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
            secret = "auth-flow-test-secret-at-least-32-bytes",
            issuer = "brul-test",
            audience = "brul-test-users",
            validityMillis = 3_600_000L
        )

        val jwtService = JwtService(jwtConfig)

        application {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = false
                })
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

        // Register a user through the HTTP API.
        val registration = client.post("/api/v1/auth/register") {
            contentType(ContentType.Application.Json)
            setBody(
                """
                {
                    "username": "resident1",
                    "email": "resident@example.com",
                    "password": "SecurePassword123!"
                }
                """.trimIndent()
            )
        }

        assertEquals(HttpStatusCode.Created, registration.status)

        val registeredUserId = Json.parseToJsonElement(
            registration.bodyAsText()
        ).jsonObject["id"]?.jsonPrimitive?.content?.toLong()

        assertNotNull(registeredUserId)

        // Log in using the registered credentials.
        val login = client.post("/api/v1/auth/login") {
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

        assertEquals(HttpStatusCode.OK, login.status)

        val loginJson = Json.parseToJsonElement(
            login.bodyAsText()
        ).jsonObject

        val accessToken = loginJson["accessToken"]
            ?.jsonPrimitive
            ?.content

        assertNotNull(accessToken)

        // Verify that the login token contains the real user ID and role.
        val decodedToken = JWT.decode(accessToken)

        assertEquals(
            registeredUserId.toString(),
            decodedToken.subject
        )

        assertEquals(
            "USER",
            decodedToken.getClaim("role").asString()
        )

        // Create a report using the token returned by login.
        val reportRequest = CreateReportRequest(
            type = "SAFETY",
            title = "Unsafe crossing",
            description = "Cars drive too fast near the school",
            category = "TRAFFIC_SAFETY",
            latitude = 52.0705,
            longitude = 4.3007,
            urgent = true
        )

        val created = client.post("/api/v1/reports") {
            bearerAuth(accessToken)
            contentType(ContentType.Application.Json)
            setBody(
                Json.encodeToString(
                    CreateReportRequest.serializer(),
                    reportRequest
                )
            )
        }

        assertEquals(HttpStatusCode.Created, created.status)

        val createdReport = Json.decodeFromString(
            ReportResponse.serializer(),
            created.bodyAsText()
        )

        // Ownership must come from the authenticated user.
        assertEquals(
            registeredUserId,
            createdReport.ownerId
        )

        // Verify that ownership was persisted in H2.
        val storedReport = reportRepository.findById(createdReport.id)

        assertNotNull(storedReport)

        assertEquals(
            registeredUserId,
            storedReport.ownerId
        )
    }
}