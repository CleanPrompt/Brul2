// test/nl/brul/api/features/help/routes/HelpParticipationApiTest.kt
package nl.brul.api.features.help.routes

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
import java.time.Instant
import java.util.Date
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import nl.brul.api.common.error.ApiError
import nl.brul.api.database.ExposedTransactionRunner
import nl.brul.api.features.help.dto.AgendaItemResponse
import nl.brul.api.features.help.dto.HelpParticipationResponse
import nl.brul.api.features.help.dto.HelpParticipationSummaryResponse
import nl.brul.api.features.help.dto.ReliabilityResponse
import nl.brul.api.features.help.model.HelpRequestPhase
import nl.brul.api.features.help.model.ParticipationStatus
import nl.brul.api.features.help.repository.ExposedHelpParticipationRepository
import nl.brul.api.features.help.service.HelpParticipationService
import nl.brul.api.features.report.model.TimeSlot
import nl.brul.api.features.report.repository.ExposedReportRepository
import nl.brul.api.plugins.configureErrorHandling
import nl.brul.api.support.TestClock
import nl.brul.api.support.TestDatabase
import nl.brul.api.support.createHelpRequest

/**
 * API tests for help participation (T11-T13).
 * The service runs on a fixed clock: "now" is 08:00, H runs 10:00-12:00.
 */
class HelpParticipationApiTest {

    private val secret = "help-participation-test-secret-at-least-32-bytes"
    private val issuer = "brul-help-test"
    private val audience = "brul-help-users"

    private val json = Json {
        ignoreUnknownKeys = false
    }

    private fun token(userId: Long): String =
        JWT.create()
            .withSubject(userId.toString())
            .withIssuer(issuer)
            .withAudience(audience)
            .withClaim("role", "USER")
            .withIssuedAt(Date())
            .withExpiresAt(Date(System.currentTimeMillis() + 3_600_000L))
            .sign(Algorithm.HMAC256(secret))

    private fun at(time: String): Instant = Instant.parse("2026-11-20T${time}:00Z")

    private class Api(
        val reports: ExposedReportRepository,
        val clock: TestClock,
        val helpRequestId: Long
    )

    private fun testHelpApi(
        places: Int = 1,
        block: suspend ApplicationTestBuilder.(Api) -> Unit
    ) = testApplication {
        val database = TestDatabase.create("help_api", users = 4)
        val clock = TestClock(at("08:00"))
        val reports = ExposedReportRepository(database)
        val participants = ExposedHelpParticipationRepository(database)

        val service = HelpParticipationService(
            participationRepository = participants,
            reportRepository = reports,
            clock = clock,
            transactions = ExposedTransactionRunner(database)
        )

        val helpRequest = reports.createHelpRequest(
            slot = TimeSlot(at("10:00"), at("12:00")),
            volunteersNeeded = places
        )

        application {
            install(ContentNegotiation) {
                json(json)
            }

            install(Authentication) {
                jwt("auth-jwt") {
                    verifier(
                        JWT.require(Algorithm.HMAC256(secret))
                            .withIssuer(issuer)
                            .withAudience(audience)
                            .build()
                    )

                    validate { credential ->
                        val userId = credential.payload.subject?.toLongOrNull()

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
                    helpParticipationRoutes(service)
                    volunteerRoutes(service)
                }
            }
        }

        block(Api(reports, clock, helpRequest.id))
    }

    private suspend fun ApplicationTestBuilder.join(requestId: Long, userId: Long) =
        client.post("/api/v1/reports/$requestId/participants") {
            bearerAuth(token(userId))
        }

    private suspend fun HttpResponse.participation() =
        json.decodeFromString(HelpParticipationResponse.serializer(), bodyAsText())

    private suspend fun HttpResponse.error() =
        json.decodeFromString(ApiError.serializer(), bodyAsText())

    private suspend fun ApplicationTestBuilder.summary(requestId: Long) =
        json.decodeFromString(
            HelpParticipationSummaryResponse.serializer(),
            client.get("/api/v1/reports/$requestId/participants").bodyAsText()
        )

    @Test
    fun `anonymous user can view the summary with time slot and phase`() =
        testHelpApi { api ->
            val summary = summary(api.helpRequestId)

            assertEquals(0, summary.participantCount)
            assertEquals(1, summary.availablePlaces)
            assertEquals(HelpRequestPhase.OPEN, summary.phase)
            assertEquals("2026-11-20T10:00:00Z", summary.startsAt)
            assertEquals("2026-11-20T12:00:00Z", summary.endsAt)
        }

    @Test
    fun `anonymous user cannot join`() =
        testHelpApi { api ->
            val response = client.post("/api/v1/reports/${api.helpRequestId}/participants")

            assertEquals(HttpStatusCode.Unauthorized, response.status)
        }

    @Test
    fun `authenticated user can join and leave`() =
        testHelpApi { api ->
            val joined = join(api.helpRequestId, 2)

            assertEquals(HttpStatusCode.Created, joined.status)
            assertEquals(ParticipationStatus.SIGNED_UP, joined.participation().status)

            val left = client.delete("/api/v1/reports/${api.helpRequestId}/participants/me") {
                bearerAuth(token(2))
            }

            assertEquals(HttpStatusCode.OK, left.status)
            assertEquals(ParticipationStatus.CANCELLED, left.participation().status)
            assertEquals(1, summary(api.helpRequestId).availablePlaces)
        }

    @Test
    fun `owner cannot join through the API`() =
        testHelpApi { api ->
            val response = join(api.helpRequestId, 1)

            assertEquals(HttpStatusCode.Forbidden, response.status)
            assertEquals("OWN_REQUEST", response.error().code)
        }

    @Test
    fun `leaving without participating returns 404`() =
        testHelpApi { api ->
            val response = client.delete("/api/v1/reports/${api.helpRequestId}/participants/me") {
                bearerAuth(token(2))
            }

            assertEquals(HttpStatusCode.NotFound, response.status)
            assertEquals("NOT_PARTICIPATING", response.error().code)
        }

    // T11 (R6 + R8)
    @Test
    fun `full request puts the next volunteer on the waitlist and promotes them`() =
        testHelpApi(places = 1) { api ->
            assertEquals(HttpStatusCode.Created, join(api.helpRequestId, 2).status)

            val waitlisted = join(api.helpRequestId, 3)

            assertEquals(HttpStatusCode.Created, waitlisted.status)

            val body = waitlisted.participation()

            assertEquals(ParticipationStatus.WAITLISTED, body.status)
            assertEquals(1, body.waitlistPosition)
            assertEquals(1, summary(api.helpRequestId).waitlistCount)

            val left = client.delete("/api/v1/reports/${api.helpRequestId}/participants/me") {
                bearerAuth(token(2))
            }

            assertEquals(HttpStatusCode.OK, left.status)

            val after = summary(api.helpRequestId)

            assertEquals(1, after.participantCount)
            assertEquals(0, after.waitlistCount)

            val list = json.decodeFromString(
                ListSerializer(HelpParticipationResponse.serializer()),
                client.get("/api/v1/reports/${api.helpRequestId}/participants/list").bodyAsText()
            )

            assertEquals(
                ParticipationStatus.SIGNED_UP,
                list.single { it.userId == 3L }.status
            )
        }

    // T12 (R5)
    @Test
    fun `overlapping sign-up is rejected with TIME_CONFLICT`() =
        testHelpApi(places = 2) { api ->
            val other = api.reports.createHelpRequest(
                slot = TimeSlot(at("11:00"), at("13:00")),
                ownerId = 4,
                title = "Overlapping request"
            )

            assertEquals(HttpStatusCode.Created, join(other.id, 2).status)

            val response = join(api.helpRequestId, 2)

            assertEquals(HttpStatusCode.Conflict, response.status)
            assertEquals("TIME_CONFLICT", response.error().code)
        }

    // T13 (R9)
    @Test
    fun `only the owner can record attendance, and only after the start`() =
        testHelpApi { api ->
            join(api.helpRequestId, 2)

            val path = "/api/v1/reports/${api.helpRequestId}/participants/2/attendance"
            val body = """{"attended": true}"""

            val anonymous = client.put(path) {
                contentType(ContentType.Application.Json)
                setBody(body)
            }

            assertEquals(HttpStatusCode.Unauthorized, anonymous.status)

            val tooEarly = client.put(path) {
                bearerAuth(token(1))
                contentType(ContentType.Application.Json)
                setBody(body)
            }

            assertEquals(HttpStatusCode.Conflict, tooEarly.status)
            assertEquals("NOT_STARTED", tooEarly.error().code)

            api.clock.set("2026-11-20T12:30:00Z")

            val notOwner = client.put(path) {
                bearerAuth(token(3))
                contentType(ContentType.Application.Json)
                setBody(body)
            }

            assertEquals(HttpStatusCode.Forbidden, notOwner.status)
            assertEquals("NOT_OWNER", notOwner.error().code)

            val owner = client.put(path) {
                bearerAuth(token(1))
                contentType(ContentType.Application.Json)
                setBody(body)
            }

            assertEquals(HttpStatusCode.OK, owner.status)
            assertEquals(ParticipationStatus.ATTENDED, owner.participation().status)
        }

    @Test
    fun `volunteer sees their agenda and reliability`() =
        testHelpApi { api ->
            join(api.helpRequestId, 2)

            val agenda = json.decodeFromString(
                ListSerializer(AgendaItemResponse.serializer()),
                client.get("/api/v1/users/me/participations?upcoming=true") {
                    bearerAuth(token(2))
                }.bodyAsText()
            )

            assertEquals(api.helpRequestId, agenda.single().helpRequestId)
            assertEquals(ParticipationStatus.SIGNED_UP, agenda.single().status)

            val reliability = json.decodeFromString(
                ReliabilityResponse.serializer(),
                client.get("/api/v1/users/me/reliability") {
                    bearerAuth(token(2))
                }.bodyAsText()
            )

            assertEquals(0, reliability.noShowsLast30Days)
            assertEquals(false, reliability.blocked)

            val anonymous = client.get("/api/v1/users/me/participations")

            assertEquals(HttpStatusCode.Unauthorized, anonymous.status)
        }
}
