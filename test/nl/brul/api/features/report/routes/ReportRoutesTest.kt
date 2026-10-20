// test/nl/brul/api/features/report/routes/ReportRoutesTest.kt
package nl.brul.api.features.report.routes

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
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.serialization.json.Json
import nl.brul.api.plugins.configureErrorHandling
import nl.brul.api.features.report.dto.CreateReportRequest
import nl.brul.api.features.report.dto.UpdateReportRequest
import nl.brul.api.features.report.model.HelpRequest
import nl.brul.api.features.report.model.NuisanceReport
import nl.brul.api.features.report.model.Report
import nl.brul.api.features.report.model.ReportType
import nl.brul.api.features.report.model.SafetyReport
import nl.brul.api.features.report.model.NewReport
import nl.brul.api.features.report.repository.ReportRepository
import nl.brul.api.features.report.service.ReportService

class ReportRoutesTest {

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
                    urgent = requireNotNull(report.urgent)
                )

                ReportType.NUISANCE -> NuisanceReport(
                    id = id,
                    title = report.title,
                    description = report.description,
                    category = report.category,
                    location = report.location
                )

                ReportType.HELP_REQUEST -> HelpRequest(
                    id = id,
                    title = report.title,
                    description = report.description,
                    category = report.category,
                    location = report.location,
                    volunteersNeeded = requireNotNull(
                        report.volunteersNeeded
                    )
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

    private fun createRequest() = CreateReportRequest(
        type = "SAFETY",
        title = "Unsafe crossing",
        description = "Cars drive too fast near the school",
        category = "TRAFFIC_SAFETY",
        latitude = 52.0705,
        longitude = 4.3007,
        urgent = true
    )

    private fun testReports(
        block: suspend ApplicationTestBuilder.() -> Unit
    ) = testApplication {
        application {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = false
                })
            }

            configureErrorHandling()

            routing {
                route("/api/v1") {
                    reportRoutes(
                        ReportService(FakeReportRepository())
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
    }

    @Test
    fun `get report by ID returns created report`() = testReports {
        client.post("/api/v1/reports") {
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
    }

    @Test
    fun `delete report removes existing report`() = testReports {
        client.post("/api/v1/reports") {
            contentType(ContentType.Application.Json)
            setBody(
                Json.encodeToString(
                    CreateReportRequest.serializer(),
                    createRequest()
                )
            )
        }

        val deleted = client.delete("/api/v1/reports/1")
        val missing = client.get("/api/v1/reports/1")

        assertEquals(HttpStatusCode.NoContent, deleted.status)
        assertEquals(HttpStatusCode.NotFound, missing.status)
    }

    @Test
    fun `search reports filters by type`() = testReports {
        client.post("/api/v1/reports") {
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
}