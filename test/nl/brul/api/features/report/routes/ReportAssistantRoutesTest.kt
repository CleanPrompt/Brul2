// test/nl/brul/api/features/report/routes/ReportAssistantRoutesTest.kt
package nl.brul.api.features.report.routes

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.post
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
import nl.brul.api.features.report.dto.DuplicateCheckRequest
import nl.brul.api.features.report.dto.SuggestionRequest
import nl.brul.api.features.report.model.Location
import nl.brul.api.features.report.model.NewReport
import nl.brul.api.features.report.model.NuisanceReport
import nl.brul.api.features.report.model.Report
import nl.brul.api.features.report.model.ReportCategory
import nl.brul.api.features.report.repository.ReportRepository
import nl.brul.api.features.report.service.ReportAssistantService
import nl.brul.api.plugins.configureErrorHandling

/** The report assistant endpoints, without AI (keyword and word rules only). */
class ReportAssistantRoutesTest {

    private val jwtSecret = "report-assistant-test-secret"
    private val jwtIssuer = "brul-test"
    private val jwtAudience = "brul-test-users"

    private fun token(userId: Long = 1L): String =
        JWT.create()
            .withSubject(userId.toString())
            .withIssuer(jwtIssuer)
            .withAudience(jwtAudience)
            .sign(Algorithm.HMAC256(jwtSecret))

    private class FakeReportRepository(
        private val reports: List<Report>
    ) : ReportRepository {

        override fun create(report: NewReport): Report =
            error("Not needed for this test.")

        override fun findAll(): List<Report> = reports

        override fun findById(id: Long): Report? = reports.firstOrNull { it.id == id }

        override fun update(report: Report): Boolean = false

        override fun delete(id: Long): Boolean = false
    }

    private val existingWaste = NuisanceReport(
        id = 1,
        title = "Vuilniszakken bij de container",
        description = "Vuilniszakken naast de container",
        category = ReportCategory.WASTE,
        location = Location(51.5505, 4.6300)
    )

    private fun testAssistant(
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
                        if (credential.payload.subject?.toLongOrNull() != null) {
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
                    reportAssistantRoutes(
                        ReportAssistantService(
                            repository = FakeReportRepository(listOf(existingWaste)),
                            ai = null
                        )
                    )
                }
            }
        }

        block()
    }

    private fun suggestionBody(title: String, description: String) =
        Json.encodeToString(SuggestionRequest.serializer(), SuggestionRequest(title, description))

    private fun duplicateBody(type: String = "NUISANCE") =
        Json.encodeToString(
            DuplicateCheckRequest.serializer(),
            DuplicateCheckRequest(
                type = type,
                title = "Afval naast container",
                description = "Vuilniszakken naast de container in de straat",
                latitude = 51.5500,
                longitude = 4.6300
            )
        )

    @Test
    fun `suggestion without JWT returns 401`() = testAssistant {
        val response = client.post("/api/v1/reports/suggestion") {
            contentType(ContentType.Application.Json)
            setBody(suggestionBody("Fiets gestolen", "Bij het station"))
        }

        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `suggestion returns type and category`() = testAssistant {
        val response = client.post("/api/v1/reports/suggestion") {
            bearerAuth(token())
            contentType(ContentType.Application.Json)
            setBody(suggestionBody("Fiets gestolen", "Mijn fiets is gestolen bij het station"))
        }

        assertEquals(HttpStatusCode.OK, response.status)

        val body = response.bodyAsText()

        assertTrue(body.contains("\"type\":\"SAFETY\""))
        assertTrue(body.contains("\"category\":\"THEFT\""))
        assertTrue(body.contains("\"source\":\"KEYWORDS\""))
    }

    @Test
    fun `unknown text returns 204`() = testAssistant {
        val response = client.post("/api/v1/reports/suggestion") {
            bearerAuth(token())
            contentType(ContentType.Application.Json)
            setBody(suggestionBody("Hallo", "Even een vraagje"))
        }

        assertEquals(HttpStatusCode.NoContent, response.status)
    }

    @Test
    fun `empty text returns 400`() = testAssistant {
        val response = client.post("/api/v1/reports/suggestion") {
            bearerAuth(token())
            contentType(ContentType.Application.Json)
            setBody(suggestionBody(" ", ""))
        }

        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun `duplicates returns the nearby similar report`() = testAssistant {
        val response = client.post("/api/v1/reports/duplicates") {
            bearerAuth(token())
            contentType(ContentType.Application.Json)
            setBody(duplicateBody())
        }

        assertEquals(HttpStatusCode.OK, response.status)

        val body = response.bodyAsText()

        assertTrue(body.contains("\"id\":1"))
        assertTrue(body.contains("\"distanceMeters\":"))
    }

    @Test
    fun `duplicates of another type returns an empty list`() = testAssistant {
        val response = client.post("/api/v1/reports/duplicates") {
            bearerAuth(token())
            contentType(ContentType.Application.Json)
            setBody(duplicateBody(type = "SAFETY"))
        }

        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals("[]", response.bodyAsText())
    }

    @Test
    fun `duplicates with an invalid type returns 400`() = testAssistant {
        val response = client.post("/api/v1/reports/duplicates") {
            bearerAuth(token())
            contentType(ContentType.Application.Json)
            setBody(duplicateBody(type = "UFO"))
        }

        assertEquals(HttpStatusCode.BadRequest, response.status)
    }
}
