// test/nl/brul/api/features/report/routes/ReportApiIntegrationTest.kt
package nl.brul.api.features.report.routes

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
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
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import nl.brul.api.features.report.dto.CreateReportRequest
import nl.brul.api.features.report.dto.ReportResponse
import nl.brul.api.features.report.dto.UpdateReportRequest
import nl.brul.api.features.report.repository.ExposedReportRepository
import nl.brul.api.features.report.repository.ReportsTable
import nl.brul.api.features.report.service.ReportService
import nl.brul.api.plugins.configureErrorHandling
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

class ReportApiIntegrationTest {

    private val jwtSecret = "report-integration-test-secret"
    private val jwtIssuer = "brul-test"
    private val jwtAudience = "brul-test-users"

    private fun token(
        userId: Long = 1L,
        role: String = "USER"
    ): String =
        JWT.create()
            .withSubject(userId.toString())
            .withIssuer(jwtIssuer)
            .withAudience(jwtAudience)
            .withClaim("role", role)
            .sign(Algorithm.HMAC256(jwtSecret))

    private fun testReportsWithDatabase(
        block: suspend ApplicationTestBuilder.() -> Unit
    ) = testApplication {
        val databaseName = "report_api_${UUID.randomUUID()}"

        val database = Database.connect(
            url = "jdbc:h2:mem:$databaseName;DB_CLOSE_DELAY=-1",
            driver = "org.h2.Driver",
            user = "sa",
            password = ""
        )

        transaction(database) {
            SchemaUtils.create(ReportsTable)
        }

        val repository = ExposedReportRepository(database)
        val service = ReportService(repository)

        application {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = false
                })
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
                        val userId =
                            credential.payload.subject?.toLongOrNull()

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
                    reportRoutes(service)
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

    private suspend fun ApplicationTestBuilder.createSafetyReport(): HttpResponse {
        return client.post("/api/v1/reports") {
            bearerAuth(token())
            contentType(ContentType.Application.Json)
            setBody(
                Json.encodeToString(
                    CreateReportRequest.serializer(),
                    createRequest()
                )
            )
        }
    }

    @Test
    fun `post report persists data in H2`() = testReportsWithDatabase {
        val created = createSafetyReport()

        assertEquals(HttpStatusCode.Created, created.status)

        val createdReport = Json.decodeFromString(
            ReportResponse.serializer(),
            created.bodyAsText()
        )

        assertEquals(1L, createdReport.ownerId)
        assertEquals(0, createdReport.viewCount)

        val retrieved = client.get(
            "/api/v1/reports/${createdReport.id}"
        )

        assertEquals(HttpStatusCode.OK, retrieved.status)

        val retrievedReport = Json.decodeFromString(
            ReportResponse.serializer(),
            retrieved.bodyAsText()
        )

        assertEquals(createdReport.id, retrievedReport.id)
        assertEquals("Unsafe crossing", retrievedReport.title)
        assertEquals("SAFETY", retrievedReport.type)
        assertEquals("TRAFFIC_SAFETY", retrievedReport.category)
        assertEquals(1, retrievedReport.viewCount)
        assertEquals(1L, retrievedReport.ownerId)
        assertTrue(retrievedReport.createdAt > 0)
    }

    @Test
    fun `get reports reads stored data from H2`() = testReportsWithDatabase {
        createSafetyReport()

        val response = client.get("/api/v1/reports")

        assertEquals(HttpStatusCode.OK, response.status)

        val reports = Json.decodeFromString(
            ListSerializer(ReportResponse.serializer()),
            response.bodyAsText()
        )

        assertEquals(1, reports.size)
        assertEquals("Unsafe crossing", reports.first().title)
        assertEquals(0, reports.first().viewCount)
    }

    @Test
    fun `put report updates stored data in H2`() = testReportsWithDatabase {
        val created = createSafetyReport()

        val original = Json.decodeFromString(
            ReportResponse.serializer(),
            created.bodyAsText()
        )

        val updateRequest = UpdateReportRequest(
            type = "SAFETY",
            title = "Crossing needs attention",
            description = "The crossing remains dangerous",
            category = "TRAFFIC_SAFETY",
            latitude = 52.0705,
            longitude = 4.3007,
            status = "IN_PROGRESS",
            urgent = false
        )

        val updated = client.put(
            "/api/v1/reports/${original.id}"
        ) {
            bearerAuth(token())
            contentType(ContentType.Application.Json)
            setBody(
                Json.encodeToString(
                    UpdateReportRequest.serializer(),
                    updateRequest
                )
            )
        }

        assertEquals(HttpStatusCode.OK, updated.status)

        val updatedReport = Json.decodeFromString(
            ReportResponse.serializer(),
            updated.bodyAsText()
        )

        // PUT must not increment the view count.
        assertEquals(original.viewCount, updatedReport.viewCount)

        val retrieved = client.get(
            "/api/v1/reports/${original.id}"
        )

        val report = Json.decodeFromString(
            ReportResponse.serializer(),
            retrieved.bodyAsText()
        )

        assertEquals("Crossing needs attention", report.title)
        assertEquals("IN_PROGRESS", report.status)
        assertEquals(false, report.urgent)
        assertEquals(original.createdAt, report.createdAt)
        assertEquals(original.viewCount + 1, report.viewCount)
        assertEquals(original.ownerId, report.ownerId)
    }

    @Test
    fun `delete report removes stored data from H2`() = testReportsWithDatabase {
        val created = createSafetyReport()

        val report = Json.decodeFromString(
            ReportResponse.serializer(),
            created.bodyAsText()
        )

        val deleted = client.delete(
            "/api/v1/reports/${report.id}"
        ) {
            bearerAuth(token())
        }

        assertEquals(HttpStatusCode.NoContent, deleted.status)

        val retrieved = client.get(
            "/api/v1/reports/${report.id}"
        )

        assertEquals(HttpStatusCode.NotFound, retrieved.status)
    }

    @Test
    fun `invalid category returns 400`() = testReportsWithDatabase {
        val request = createRequest().copy(
            category = "UNKNOWN"
        )

        val response = client.post("/api/v1/reports") {
            bearerAuth(token())
            contentType(ContentType.Application.Json)
            setBody(
                Json.encodeToString(
                    CreateReportRequest.serializer(),
                    request
                )
            )
        }

        assertEquals(HttpStatusCode.BadRequest, response.status)
        assertTrue(
            response.bodyAsText().contains("Invalid request.")
        )
    }

    @Test
    fun `missing required JSON field returns 400`() = testReportsWithDatabase {
        val response = client.post("/api/v1/reports") {
            bearerAuth(token())
            contentType(ContentType.Application.Json)
            setBody(
                """
                {
                    "type": "SAFETY",
                    "title": "Unsafe crossing",
                    "category": "TRAFFIC_SAFETY",
                    "latitude": 52.0705,
                    "longitude": 4.3007,
                    "urgent": true
                }
                """.trimIndent()
            )
        }

        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun `wrong JSON datatype returns 400`() = testReportsWithDatabase {
        val response = client.post("/api/v1/reports") {
            bearerAuth(token())
            contentType(ContentType.Application.Json)
            setBody(
                """
                {
                    "type": "SAFETY",
                    "title": "Unsafe crossing",
                    "description": "Cars drive too fast near the school",
                    "category": "TRAFFIC_SAFETY",
                    "latitude": "not-a-number",
                    "longitude": 4.3007,
                    "urgent": true
                }
                """.trimIndent()
            )
        }

        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun `invalid search parameters return 400`() = testReportsWithDatabase {
        val response = client.get(
            "/api/v1/reports?sort=NEAREST"
        )

        assertEquals(HttpStatusCode.BadRequest, response.status)
        assertTrue(
            response.bodyAsText().contains("Invalid request.")
        )
    }

    @Test
    fun `post without JWT returns 401`() = testReportsWithDatabase {
        val response = client.post("/api/v1/reports") {
            contentType(ContentType.Application.Json)
            setBody(
                Json.encodeToString(
                    CreateReportRequest.serializer(),
                    createRequest()
                )
            )
        }

        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `put without JWT returns 401`() = testReportsWithDatabase {
        val created = createSafetyReport()

        val report = Json.decodeFromString(
            ReportResponse.serializer(),
            created.bodyAsText()
        )

        val response = client.put("/api/v1/reports/${report.id}")

        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `delete without JWT returns 401`() = testReportsWithDatabase {
        val created = createSafetyReport()

        val report = Json.decodeFromString(
            ReportResponse.serializer(),
            created.bodyAsText()
        )

        val response = client.delete("/api/v1/reports/${report.id}")

        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `another user cannot update report`() = testReportsWithDatabase {
        val created = createSafetyReport()

        val report = Json.decodeFromString(
            ReportResponse.serializer(),
            created.bodyAsText()
        )

        val request = UpdateReportRequest(
            type = "SAFETY",
            title = "Unauthorized update",
            description = "This update must be rejected",
            category = "TRAFFIC_SAFETY",
            latitude = 52.0705,
            longitude = 4.3007,
            status = "IN_PROGRESS",
            urgent = false
        )

        val response = client.put("/api/v1/reports/${report.id}") {
            bearerAuth(token(userId = 2L))
            contentType(ContentType.Application.Json)
            setBody(
                Json.encodeToString(
                    UpdateReportRequest.serializer(),
                    request
                )
            )
        }

        assertEquals(HttpStatusCode.Forbidden, response.status)

        val retrieved = client.get("/api/v1/reports/${report.id}")
        val unchanged = Json.decodeFromString(
            ReportResponse.serializer(),
            retrieved.bodyAsText()
        )

        assertEquals("Unsafe crossing", unchanged.title)
    }

    @Test
    fun `another user cannot delete report`() = testReportsWithDatabase {
        val created = createSafetyReport()

        val report = Json.decodeFromString(
            ReportResponse.serializer(),
            created.bodyAsText()
        )

        val response = client.delete("/api/v1/reports/${report.id}") {
            bearerAuth(token(userId = 2L))
        }

        assertEquals(HttpStatusCode.Forbidden, response.status)

        val retrieved = client.get("/api/v1/reports/${report.id}")

        assertEquals(HttpStatusCode.OK, retrieved.status)
    }

    @Test
    fun `admin can delete another user's report`() = testReportsWithDatabase {
        val created = createSafetyReport()

        val report = Json.decodeFromString(
            ReportResponse.serializer(),
            created.bodyAsText()
        )

        val response = client.delete("/api/v1/reports/${report.id}") {
            bearerAuth(token(userId = 2L, role = "ADMIN"))
        }

        assertEquals(HttpStatusCode.NoContent, response.status)

        val retrieved = client.get("/api/v1/reports/${report.id}")

        assertEquals(HttpStatusCode.NotFound, retrieved.status)
    }

    @Test
    fun `admin cannot update another user's report`() = testReportsWithDatabase {
        val created = createSafetyReport()

        val report = Json.decodeFromString(
            ReportResponse.serializer(),
            created.bodyAsText()
        )

        val request = UpdateReportRequest(
            type = "SAFETY",
            title = "Admin update",
            description = "Admins cannot edit other users' reports",
            category = "TRAFFIC_SAFETY",
            latitude = 52.0705,
            longitude = 4.3007,
            status = "IN_PROGRESS",
            urgent = false
        )

        val response = client.put("/api/v1/reports/${report.id}") {
            bearerAuth(token(userId = 2L, role = "ADMIN"))
            contentType(ContentType.Application.Json)
            setBody(
                Json.encodeToString(
                    UpdateReportRequest.serializer(),
                    request
                )
            )
        }

        assertEquals(HttpStatusCode.Forbidden, response.status)
    }
}