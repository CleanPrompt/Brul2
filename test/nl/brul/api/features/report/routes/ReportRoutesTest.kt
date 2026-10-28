
// test/nl/brul/api/features/report/routes/ReportRoutesTest.kt
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
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.jwt.jwt
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.testApplication
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.serialization.json.Json
import java.time.Instant
import nl.brul.api.features.help.model.HelpParticipation
import nl.brul.api.features.help.model.ParticipationStatus
import nl.brul.api.features.help.repository.HelpParticipationRepository
import nl.brul.api.features.report.dto.CreateReportRequest
import nl.brul.api.features.report.dto.UpdateReportRequest
import nl.brul.api.features.report.model.HelpRequest
import nl.brul.api.features.report.model.NewReport
import nl.brul.api.features.report.model.NuisanceReport
import nl.brul.api.features.report.model.Report
import nl.brul.api.features.report.model.ReportType
import nl.brul.api.features.report.model.SafetyReport
import nl.brul.api.features.report.repository.ReportRepository
import nl.brul.api.features.report.service.ReportService
import nl.brul.api.plugins.configureErrorHandling
import nl.brul.api.support.TestSlots

class ReportRoutesTest {

    private val jwtSecret = "report-routes-test-secret"
    private val jwtIssuer = "brul-test"
    private val jwtAudience = "brul-test-users"

    private fun token(userId: Long = 1L): String =
        JWT.create()
            .withSubject(userId.toString())
            .withIssuer(jwtIssuer)
            .withAudience(jwtAudience)
            .sign(Algorithm.HMAC256(jwtSecret))

    private class FakeReportRepository : ReportRepository {

        private val reports = mutableMapOf<Long, Report>()
        private var nextId = 1L

        override fun create(report: NewReport): Report {
            val id = nextId++

            val created = when (report.type) {
                ReportType.SAFETY -> SafetyReport(
                    id = id,
                    title = report.title,
                    description = report.description,
                    category = report.category,
                    location = report.location,
                    urgent = requireNotNull(report.urgent),
                    ownerId = report.ownerId
                )

                ReportType.NUISANCE -> NuisanceReport(
                    id = id,
                    title = report.title,
                    description = report.description,
                    category = report.category,
                    location = report.location,
                    ownerId = report.ownerId
                )

                ReportType.HELP_REQUEST -> HelpRequest(
                    id = id,
                    title = report.title,
                    description = report.description,
                    category = report.category,
                    location = report.location,
                    volunteersNeeded = requireNotNull(
                        report.volunteersNeeded
                    ),
                    ownerId = report.ownerId,
                    slot = requireNotNull(report.slot)
                )
            }

            reports[id] = created
            return created
        }

        override fun findAll(): List<Report> =
            reports.values.sortedBy { it.id }

        override fun findById(id: Long): Report? =
            reports[id]

        override fun update(report: Report): Boolean {
            val existing = reports[report.id] ?: return false

            if (existing.type != report.type) {
                return false
            }

            reports[report.id] = report
            return true
        }

        override fun delete(id: Long): Boolean =
            reports.remove(id) != null
    }

    private class FakeParticipationRepository(
        private val participantCount: Int
    ) : HelpParticipationRepository {

        override fun findByHelpRequest(
            helpRequestId: Long
        ): List<HelpParticipation> =
            emptyList()

        override fun findByHelpRequestAndUser(
            helpRequestId: Long,
            userId: Long
        ): HelpParticipation? = null

        override fun findByUser(
            userId: Long
        ): List<HelpParticipation> =
            emptyList()

        override fun add(
            helpRequestId: Long,
            userId: Long,
            status: ParticipationStatus,
            joinedAt: Instant
        ): HelpParticipation {
            error("Not needed for this test.")
        }

        override fun update(
            participation: HelpParticipation
        ): Boolean = false

        override fun countSignedUp(
            helpRequestId: Long
        ): Int = participantCount
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

    private fun helpRequest(
        volunteersNeeded: Int
    ) = CreateReportRequest(
        type = "HELP_REQUEST",
        title = "Help with groceries",
        description = "I need help carrying groceries.",
        category = "GROCERIES",
        latitude = 52.0705,
        longitude = 4.3007,
        volunteersNeeded = volunteersNeeded,
        startsAt = TestSlots.STARTS_AT,
        endsAt = TestSlots.ENDS_AT
    )

    private fun updateHelpRequest(
        volunteersNeeded: Int
    ) = UpdateReportRequest(
        type = "HELP_REQUEST",
        title = "Help with groceries",
        description = "I need help carrying groceries.",
        category = "GROCERIES",
        latitude = 52.0705,
        longitude = 4.3007,
        status = "OPEN",
        volunteersNeeded = volunteersNeeded
    )

    private fun testReports(
        participantCount: Int = 0,
        block: suspend ApplicationTestBuilder.() -> Unit
    ) = testApplication {
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
                        if (
                            credential.payload.subject
                                ?.toLongOrNull() != null
                        ) {
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
                    reportRoutes(
                        ReportService(
                            repository = FakeReportRepository(),
                            helpParticipationRepository =
                                FakeParticipationRepository(
                                    participantCount
                                )
                        )
                    )
                }
            }
        }

        block()
    }

    @Test
    fun `get reports returns empty list`() = testReports {
        val response = client.get("/api/v1/reports")

        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals("[]", response.bodyAsText())
    }

    @Test
    fun `post report creates new report`() = testReports {
        val response = client.post("/api/v1/reports") {
            bearerAuth(token())
            contentType(ContentType.Application.Json)
            setBody(
                Json.encodeToString(
                    CreateReportRequest.serializer(),
                    createRequest()
                )
            )
        }

        assertEquals(HttpStatusCode.Created, response.status)

        val body = response.bodyAsText()

        assertTrue(body.contains("\"id\":1"))
        assertTrue(body.contains("\"type\":\"SAFETY\""))
        assertTrue(body.contains("\"ownerId\":1"))
    }

    @Test
    fun `get report by ID returns created report`() = testReports {
        client.post("/api/v1/reports") {
            bearerAuth(token())
            contentType(ContentType.Application.Json)
            setBody(
                Json.encodeToString(
                    CreateReportRequest.serializer(),
                    createRequest()
                )
            )
        }

        val response = client.get("/api/v1/reports/1")

        assertEquals(HttpStatusCode.OK, response.status)
        assertTrue(
            response.bodyAsText().contains(
                "\"title\":\"Unsafe crossing\""
            )
        )
    }

    @Test
    fun `get missing report returns 404`() = testReports {
        val response = client.get("/api/v1/reports/999")

        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun `put report updates existing report`() = testReports {
        client.post("/api/v1/reports") {
            bearerAuth(token())
            contentType(ContentType.Application.Json)
            setBody(
                Json.encodeToString(
                    CreateReportRequest.serializer(),
                    createRequest()
                )
            )
        }

        val request = UpdateReportRequest(
            type = "SAFETY",
            title = "Updated crossing",
            description = "The crossing is still dangerous",
            category = "TRAFFIC_SAFETY",
            latitude = 52.0705,
            longitude = 4.3007,
            status = "IN_PROGRESS",
            urgent = false
        )

        val response = client.put("/api/v1/reports/1") {
            bearerAuth(token())
            contentType(ContentType.Application.Json)
            setBody(
                Json.encodeToString(
                    UpdateReportRequest.serializer(),
                    request
                )
            )
        }

        assertEquals(HttpStatusCode.OK, response.status)

        val body = response.bodyAsText()

        assertTrue(body.contains("\"title\":\"Updated crossing\""))
        assertTrue(body.contains("\"status\":\"IN_PROGRESS\""))
        assertTrue(body.contains("\"ownerId\":1"))
    }

    @Test
    fun `delete report removes existing report`() = testReports {
        client.post("/api/v1/reports") {
            bearerAuth(token())
            contentType(ContentType.Application.Json)
            setBody(
                Json.encodeToString(
                    CreateReportRequest.serializer(),
                    createRequest()
                )
            )
        }

        val deleted = client.delete("/api/v1/reports/1") {
            bearerAuth(token())
        }

        val missing = client.get("/api/v1/reports/1")

        assertEquals(HttpStatusCode.NoContent, deleted.status)
        assertEquals(HttpStatusCode.NotFound, missing.status)
    }

    @Test
    fun `search reports filters by type`() = testReports {
        client.post("/api/v1/reports") {
            bearerAuth(token())
            contentType(ContentType.Application.Json)
            setBody(
                Json.encodeToString(
                    CreateReportRequest.serializer(),
                    createRequest()
                )
            )
        }

        val response = client.get(
            "/api/v1/reports?type=NUISANCE"
        )

        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals("[]", response.bodyAsText())
    }

    @Test
    fun `invalid report ID returns 400`() = testReports {
        val response = client.get("/api/v1/reports/abc")

        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun `invalid search radius returns 400`() = testReports {
        val response = client.get(
            "/api/v1/reports?radiusKm=-5&latitude=52&longitude=4"
        )

        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun `invalid report category returns 400`() = testReports {
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
    }

    @Test
    fun `post without JWT returns 401`() = testReports {
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
    fun `put without JWT returns 401`() = testReports {
        val response = client.put("/api/v1/reports/1")

        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `delete without JWT returns 401`() = testReports {
        val response = client.delete("/api/v1/reports/1")

        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `cannot reduce help request capacity below participants via API`() =
        testReports(participantCount = 3) {

            val created = client.post("/api/v1/reports") {
                bearerAuth(token())
                contentType(ContentType.Application.Json)
                setBody(
                    Json.encodeToString(
                        CreateReportRequest.serializer(),
                        helpRequest(volunteersNeeded = 5)
                    )
                )
            }

            assertEquals(HttpStatusCode.Created, created.status)

            val updated = client.put("/api/v1/reports/1") {
                bearerAuth(token())
                contentType(ContentType.Application.Json)
                setBody(
                    Json.encodeToString(
                        UpdateReportRequest.serializer(),
                        updateHelpRequest(volunteersNeeded = 2)
                    )
                )
            }

            assertEquals(HttpStatusCode.BadRequest, updated.status)

            val retrieved = client.get("/api/v1/reports/1")

            assertEquals(HttpStatusCode.OK, retrieved.status)
            assertTrue(
                retrieved.bodyAsText().contains(
                    "\"volunteersNeeded\":5"
                )
            )
        }

    @Test
    fun `can update help request capacity when enough places remain`() =
        testReports(participantCount = 3) {

            val created = client.post("/api/v1/reports") {
                bearerAuth(token())
                contentType(ContentType.Application.Json)
                setBody(
                    Json.encodeToString(
                        CreateReportRequest.serializer(),
                        helpRequest(volunteersNeeded = 5)
                    )
                )
            }

            assertEquals(HttpStatusCode.Created, created.status)

            val updated = client.put("/api/v1/reports/1") {
                bearerAuth(token())
                contentType(ContentType.Application.Json)
                setBody(
                    Json.encodeToString(
                        UpdateReportRequest.serializer(),
                        updateHelpRequest(volunteersNeeded = 3)
                    )
                )
            }

            assertEquals(HttpStatusCode.OK, updated.status)

            val retrieved = client.get("/api/v1/reports/1")

            assertEquals(HttpStatusCode.OK, retrieved.status)
            assertTrue(
                retrieved.bodyAsText().contains(
                    "\"volunteersNeeded\":3"
                )
            )
        }
}
