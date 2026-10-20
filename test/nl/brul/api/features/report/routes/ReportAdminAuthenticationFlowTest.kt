// test/nl/brul/api/features/report/routes/ReportAdminAuthenticationFlowTest.kt
package nl.brul.api.features.report.routes

import com.auth0.jwt.JWT
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
import nl.brul.api.features.report.dto.UpdateReportRequest
import nl.brul.api.features.report.repository.ExposedReportRepository
import nl.brul.api.features.report.repository.ReportsTable
import nl.brul.api.features.report.service.ReportService
import nl.brul.api.features.user.model.UserRole
import nl.brul.api.features.user.repository.ExposedUserRepository
import nl.brul.api.features.user.repository.UsersTable
import nl.brul.api.plugins.configureErrorHandling
import nl.brul.api.plugins.configureSecurity
import nl.brul.api.security.BCryptPasswordHasher
import nl.brul.api.security.JwtConfig
import nl.brul.api.security.JwtService
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.update
import org.jetbrains.exposed.sql.transactions.transaction

class ReportAdminAuthenticationFlowTest {

    private val json = Json {
        ignoreUnknownKeys = false
    }

    private fun testWithDatabase(
        block: suspend ApplicationTestBuilder.(Database) -> Unit
    ) = testApplication {
        val databaseName = "admin_flow_${UUID.randomUUID()}"

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
            secret = "admin-flow-test-secret-at-least-32-bytes",
            issuer = "brul-test",
            audience = "brul-test-users",
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

        block(database)
    }

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

        val accessToken = json.parseToJsonElement(
            response.bodyAsText()
        ).jsonObject["accessToken"]?.jsonPrimitive?.content

        return assertNotNull(accessToken)
    }

    private suspend fun ApplicationTestBuilder.createReport(
        accessToken: String
    ): ReportResponse {
        val request = CreateReportRequest(
            type = "SAFETY",
            title = "Unsafe crossing",
            description = "Cars drive too fast near the school",
            category = "TRAFFIC_SAFETY",
            latitude = 52.0705,
            longitude = 4.3007,
            urgent = true
        )

        val response = client.post("/api/v1/reports") {
            bearerAuth(accessToken)
            contentType(ContentType.Application.Json)
            setBody(
                json.encodeToString(
                    CreateReportRequest.serializer(),
                    request
                )
            )
        }

        assertEquals(HttpStatusCode.Created, response.status)

        return json.decodeFromString(
            ReportResponse.serializer(),
            response.bodyAsText()
        )
    }

    private fun promoteToAdmin(
        database: Database,
        userId: Long
    ) {
        transaction(database) {
            UsersTable.update({ UsersTable.id eq userId }) {
                it[role] = UserRole.ADMIN
            }
        }
    }

    @Test
    fun `real ADMIN login can delete another user's report`() =
        testWithDatabase { database ->

            val ownerId = register(
                username = "owner",
                email = "owner@example.com"
            )

            val adminId = register(
                username = "admin",
                email = "admin@example.com"
            )

            val ownerToken = login("owner@example.com")

            val report = createReport(ownerToken)

            assertEquals(ownerId, report.ownerId)

            // Assign ADMIN only through trusted database access.
            promoteToAdmin(database, adminId)

            // Login must read the updated role from the database.
            val adminToken = login("admin@example.com")

            val decodedToken = JWT.decode(adminToken)

            assertEquals(
                adminId.toString(),
                decodedToken.subject
            )

            assertEquals(
                "ADMIN",
                decodedToken.getClaim("role").asString()
            )

            // ADMIN deletes a report belonging to another user.
            val deleted = client.delete(
                "/api/v1/reports/${report.id}"
            ) {
                bearerAuth(adminToken)
            }

            assertEquals(HttpStatusCode.NoContent, deleted.status)

            val retrieved = client.get(
                "/api/v1/reports/${report.id}"
            )

            assertEquals(HttpStatusCode.NotFound, retrieved.status)
        }

    @Test
    fun `real ADMIN login cannot update another user's report`() =
        testWithDatabase { database ->

            val ownerId = register(
                username = "owner",
                email = "owner@example.com"
            )

            val adminId = register(
                username = "admin",
                email = "admin@example.com"
            )

            val ownerToken = login("owner@example.com")

            val report = createReport(ownerToken)

            assertEquals(ownerId, report.ownerId)

            promoteToAdmin(database, adminId)

            val adminToken = login("admin@example.com")

            assertEquals(
                "ADMIN",
                JWT.decode(adminToken).getClaim("role").asString()
            )

            val updateRequest = UpdateReportRequest(
                type = "SAFETY",
                title = "Unauthorized admin update",
                description = "Admins cannot edit other users' reports",
                category = "TRAFFIC_SAFETY",
                latitude = 52.0705,
                longitude = 4.3007,
                status = "IN_PROGRESS",
                urgent = false
            )

            val updated = client.put(
                "/api/v1/reports/${report.id}"
            ) {
                bearerAuth(adminToken)
                contentType(ContentType.Application.Json)
                setBody(
                    json.encodeToString(
                        UpdateReportRequest.serializer(),
                        updateRequest
                    )
                )
            }

            assertEquals(HttpStatusCode.Forbidden, updated.status)

            // The report must remain unchanged.
            val retrieved = client.get(
                "/api/v1/reports/${report.id}"
            )

            assertEquals(HttpStatusCode.OK, retrieved.status)

            val unchanged = json.decodeFromString(
                ReportResponse.serializer(),
                retrieved.bodyAsText()
            )

            assertEquals("Unsafe crossing", unchanged.title)
            assertEquals(ownerId, unchanged.ownerId)
        }
}