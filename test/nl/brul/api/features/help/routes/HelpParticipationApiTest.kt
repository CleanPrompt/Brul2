// test/nl/brul/api/features/help/routes/HelpParticipationApiTest.kt
package nl.brul.api.features.help.routes

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
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
import java.util.Date
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.serialization.json.Json
import nl.brul.api.features.help.dto.HelpParticipationSummaryResponse
import nl.brul.api.features.help.repository.ExposedHelpParticipationRepository
import nl.brul.api.features.help.repository.HelpParticipationsTable
import nl.brul.api.features.help.service.HelpParticipationService
import nl.brul.api.features.report.dto.CreateReportRequest
import nl.brul.api.features.report.dto.toNewReport
import nl.brul.api.features.report.repository.ExposedReportRepository
import nl.brul.api.features.report.repository.ReportsTable
import nl.brul.api.features.user.model.NewUser
import nl.brul.api.features.user.repository.ExposedUserRepository
import nl.brul.api.features.user.repository.UsersTable
import nl.brul.api.plugins.configureErrorHandling
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

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
            .withExpiresAt(
                Date(System.currentTimeMillis() + 3_600_000L)
            )
            .sign(Algorithm.HMAC256(secret))

    private fun testHelpApi(
        block: suspend ApplicationTestBuilder.(Long) -> Unit
    ) = testApplication {
        val database = Database.connect(
            url = "jdbc:h2:mem:help_api_${UUID.randomUUID()};DB_CLOSE_DELAY=-1",
            driver = "org.h2.Driver",
            user = "sa",
            password = ""
        )

        transaction(database) {
            SchemaUtils.create(
                UsersTable,
                ReportsTable,
                HelpParticipationsTable
            )
        }

        // Create users with IDs 1, 2 and 3 for foreign key constraints.
        val users = ExposedUserRepository(database)

        repeat(3) { index ->
            val userNumber = index + 1

            users.create(
                NewUser(
                    username = "testuser$userNumber",
                    email = "testuser$userNumber@example.com",
                    passwordHash = "test-password-hash"
                )
            )
        }

        val reports = ExposedReportRepository(database)
        val participants = ExposedHelpParticipationRepository(database)

        val service = HelpParticipationService(
            participationRepository = participants,
            reportRepository = reports
        )

        val report = reports.create(
            CreateReportRequest(
                type = "HELP_REQUEST",
                title = "Help with groceries",
                description = "I need help with shopping",
                category = "GROCERIES",
                latitude = 52.0705,
                longitude = 4.3007,
                volunteersNeeded = 1
            ).toNewReport().copy(ownerId = 1L)
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
                    helpParticipationRoutes(service)
                }
            }
        }

        block(report.id)
    }

    @Test
    fun `anonymous user can view participation summary`() =
        testHelpApi { reportId ->
            val response = client.get(
                "/api/v1/reports/$reportId/participants"
            )

            assertEquals(HttpStatusCode.OK, response.status)

            val summary = json.decodeFromString(
                HelpParticipationSummaryResponse.serializer(),
                response.bodyAsText()
            )

            assertEquals(0, summary.participantCount)
            assertEquals(1, summary.availablePlaces)
        }

    @Test
    fun `anonymous user cannot join`() =
        testHelpApi { reportId ->
            val response = client.post(
                "/api/v1/reports/$reportId/participants"
            )

            assertEquals(HttpStatusCode.Unauthorized, response.status)
        }

    @Test
    fun `authenticated user can join and leave`() =
        testHelpApi { reportId ->
            val path = "/api/v1/reports/$reportId/participants"

            val joined = client.post(path) {
                bearerAuth(token(2))
            }

            assertEquals(HttpStatusCode.Created, joined.status)

            val full = client.get(path)

            val summary = json.decodeFromString(
                HelpParticipationSummaryResponse.serializer(),
                full.bodyAsText()
            )

            assertEquals(1, summary.participantCount)
            assertEquals(0, summary.availablePlaces)

            val left = client.delete("$path/me") {
                bearerAuth(token(2))
            }

            assertEquals(HttpStatusCode.NoContent, left.status)

            val availableAgain = client.get(path)

            val updatedSummary = json.decodeFromString(
                HelpParticipationSummaryResponse.serializer(),
                availableAgain.bodyAsText()
            )

            assertEquals(0, updatedSummary.participantCount)
            assertEquals(1, updatedSummary.availablePlaces)
        }

    @Test
    fun `owner cannot join through API`() =
        testHelpApi { reportId ->
            val response = client.post(
                "/api/v1/reports/$reportId/participants"
            ) {
                bearerAuth(token(1))
            }

            assertEquals(HttpStatusCode.Forbidden, response.status)
        }

    @Test
    fun `full request rejects another participant`() =
        testHelpApi { reportId ->
            val path = "/api/v1/reports/$reportId/participants"

            val first = client.post(path) {
                bearerAuth(token(2))
            }

            assertEquals(HttpStatusCode.Created, first.status)

            val second = client.post(path) {
                bearerAuth(token(3))
            }

            assertEquals(HttpStatusCode.Conflict, second.status)
        }
}