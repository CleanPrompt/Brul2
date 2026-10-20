// test/nl/brul/api/features/vote/routes/VoteApiIntegrationTest.kt
package nl.brul.api.features.vote.routes

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.delete
import io.ktor.client.request.get
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
import java.util.Date
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlinx.serialization.json.Json
import nl.brul.api.features.report.dto.CreateReportRequest
import nl.brul.api.features.report.dto.toNewReport
import nl.brul.api.features.report.repository.ExposedReportRepository
import nl.brul.api.features.report.repository.ReportsTable
import nl.brul.api.features.report.service.ReportService
import nl.brul.api.features.vote.dto.VoteResponse
import nl.brul.api.features.vote.dto.VoteSummaryResponse
import nl.brul.api.features.vote.repository.ExposedVoteRepository
import nl.brul.api.features.vote.repository.VotesTable
import nl.brul.api.features.vote.service.VoteService
import nl.brul.api.plugins.configureErrorHandling
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

class VoteApiIntegrationTest {

    private val json = Json {
        ignoreUnknownKeys = false
    }

    private val secret = "vote-integration-test-secret-at-least-32-bytes"
    private val issuer = "brul-vote-test"
    private val audience = "brul-vote-users"

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

    private fun voteBody(type: String): String =
        """{"type":"$type"}"""

    private fun testVotes(
        block: suspend ApplicationTestBuilder.(Long) -> Unit
    ) = testApplication {
        val databaseName = "vote_api_${UUID.randomUUID()}"

        val database = Database.connect(
            url = "jdbc:h2:mem:$databaseName;DB_CLOSE_DELAY=-1",
            driver = "org.h2.Driver",
            user = "sa",
            password = ""
        )

        transaction(database) {
            SchemaUtils.create(
                ReportsTable,
                VotesTable
            )
        }

        val reportRepository = ExposedReportRepository(database)
        val voteRepository = ExposedVoteRepository(database)

        val reportService = ReportService(
            repository = reportRepository,
            voteRepository = voteRepository
        )

        val voteService = VoteService(
            voteRepository = voteRepository,
            reportRepository = reportRepository
        )

        // Owner ID 1 creates the report.
        val report = reportService.create(
            CreateReportRequest(
                type = "SAFETY",
                title = "Unsafe crossing",
                description = "Cars drive too fast near the school",
                category = "TRAFFIC_SAFETY",
                latitude = 52.0705,
                longitude = 4.3007,
                urgent = true
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
                    voteRoutes(voteService, reportService)
                }
            }
        }

        block(report.id)
    }

    @Test
    fun `vote summary is public and initially empty`() =
        testVotes { reportId ->
            val response = client.get(
                "/api/v1/reports/$reportId/votes"
            )

            assertEquals(HttpStatusCode.OK, response.status)

            val summary = json.decodeFromString(
                VoteSummaryResponse.serializer(),
                response.bodyAsText()
            )

            assertEquals(0, summary.likes)
            assertEquals(0, summary.dislikes)
            assertEquals(0, summary.totalVotes)
            assertEquals(0, summary.score)
        }

    @Test
    fun `authenticated user can like a report`() =
        testVotes { reportId ->
            val response = client.put(
                "/api/v1/reports/$reportId/vote"
            ) {
                bearerAuth(token(2))
                contentType(ContentType.Application.Json)
                setBody(voteBody("LIKE"))
            }

            assertEquals(
                HttpStatusCode.Created,
                response.status
            )

            val vote = json.decodeFromString(
                VoteResponse.serializer(),
                response.bodyAsText()
            )

            assertEquals(reportId, vote.reportId)
            assertEquals(2L, vote.userId)
            assertEquals("LIKE", vote.type.name)
        }

    @Test
    fun `authenticated user can dislike a report`() =
        testVotes { reportId ->
            val response = client.put(
                "/api/v1/reports/$reportId/vote"
            ) {
                bearerAuth(token(2))
                contentType(ContentType.Application.Json)
                setBody(voteBody("DISLIKE"))
            }

            assertEquals(
                HttpStatusCode.Created,
                response.status
            )

            val vote = json.decodeFromString(
                VoteResponse.serializer(),
                response.bodyAsText()
            )

            assertEquals("DISLIKE", vote.type.name)
        }

    @Test
    fun `changing a vote updates existing vote`() =
        testVotes { reportId ->
            val path = "/api/v1/reports/$reportId/vote"

            val first = client.put(path) {
                bearerAuth(token(2))
                contentType(ContentType.Application.Json)
                setBody(voteBody("LIKE"))
            }

            assertEquals(HttpStatusCode.Created, first.status)

            val originalVote = json.decodeFromString(
                VoteResponse.serializer(),
                first.bodyAsText()
            )

            val changed = client.put(path) {
                bearerAuth(token(2))
                contentType(ContentType.Application.Json)
                setBody(voteBody("DISLIKE"))
            }

            assertEquals(HttpStatusCode.OK, changed.status)

            val changedVote = json.decodeFromString(
                VoteResponse.serializer(),
                changed.bodyAsText()
            )

            assertEquals(originalVote.id, changedVote.id)
            assertEquals("DISLIKE", changedVote.type.name)

            val summaryResponse = client.get(
                "/api/v1/reports/$reportId/votes"
            )

            val summary = json.decodeFromString(
                VoteSummaryResponse.serializer(),
                summaryResponse.bodyAsText()
            )

            assertEquals(0, summary.likes)
            assertEquals(1, summary.dislikes)
            assertEquals(1, summary.totalVotes)
        }

    @Test
    fun `report owner cannot vote on own report`() =
        testVotes { reportId ->
            val response = client.put(
                "/api/v1/reports/$reportId/vote"
            ) {
                bearerAuth(token(1))
                contentType(ContentType.Application.Json)
                setBody(voteBody("LIKE"))
            }

            assertEquals(
                HttpStatusCode.Forbidden,
                response.status
            )

            val summaryResponse = client.get(
                "/api/v1/reports/$reportId/votes"
            )

            val summary = json.decodeFromString(
                VoteSummaryResponse.serializer(),
                summaryResponse.bodyAsText()
            )

            assertEquals(0, summary.totalVotes)
        }

    @Test
    fun `anonymous user cannot vote`() =
        testVotes { reportId ->
            val response = client.put(
                "/api/v1/reports/$reportId/vote"
            ) {
                contentType(ContentType.Application.Json)
                setBody(voteBody("LIKE"))
            }

            assertEquals(
                HttpStatusCode.Unauthorized,
                response.status
            )
        }

    @Test
    fun `user can retrieve own vote`() =
        testVotes { reportId ->
            client.put("/api/v1/reports/$reportId/vote") {
                bearerAuth(token(2))
                contentType(ContentType.Application.Json)
                setBody(voteBody("LIKE"))
            }

            val response = client.get(
                "/api/v1/reports/$reportId/vote/me"
            ) {
                bearerAuth(token(2))
            }

            assertEquals(HttpStatusCode.OK, response.status)

            val vote = json.decodeFromString(
                VoteResponse.serializer(),
                response.bodyAsText()
            )

            assertEquals(2L, vote.userId)
            assertEquals("LIKE", vote.type.name)
        }

    @Test
    fun `user cannot retrieve another user's vote`() =
        testVotes { reportId ->
            client.put("/api/v1/reports/$reportId/vote") {
                bearerAuth(token(2))
                contentType(ContentType.Application.Json)
                setBody(voteBody("LIKE"))
            }

            val response = client.get(
                "/api/v1/reports/$reportId/vote/me"
            ) {
                bearerAuth(token(3))
            }

            assertEquals(HttpStatusCode.NotFound, response.status)
        }

    @Test
    fun `user can remove own vote`() =
        testVotes { reportId ->
            val path = "/api/v1/reports/$reportId/vote"

            client.put(path) {
                bearerAuth(token(2))
                contentType(ContentType.Application.Json)
                setBody(voteBody("LIKE"))
            }

            val response = client.delete(path) {
                bearerAuth(token(2))
            }

            assertEquals(
                HttpStatusCode.NoContent,
                response.status
            )

            val summaryResponse = client.get(
                "/api/v1/reports/$reportId/votes"
            )

            val summary = json.decodeFromString(
                VoteSummaryResponse.serializer(),
                summaryResponse.bodyAsText()
            )

            assertEquals(0, summary.totalVotes)
        }

    @Test
    fun `different user cannot remove another user's vote`() =
        testVotes { reportId ->
            val path = "/api/v1/reports/$reportId/vote"

            client.put(path) {
                bearerAuth(token(2))
                contentType(ContentType.Application.Json)
                setBody(voteBody("LIKE"))
            }

            val response = client.delete(path) {
                bearerAuth(token(3))
            }

            assertEquals(HttpStatusCode.NotFound, response.status)

            val summaryResponse = client.get(
                "/api/v1/reports/$reportId/votes"
            )

            val summary = json.decodeFromString(
                VoteSummaryResponse.serializer(),
                summaryResponse.bodyAsText()
            )

            assertEquals(1, summary.totalVotes)
        }

    @Test
    fun `voting on unknown report returns 404`() =
        testVotes {
            val response = client.put(
                "/api/v1/reports/999999/vote"
            ) {
                bearerAuth(token(2))
                contentType(ContentType.Application.Json)
                setBody(voteBody("LIKE"))
            }

            assertEquals(HttpStatusCode.NotFound, response.status)
        }

    @Test
    fun `invalid vote type returns 400`() =
        testVotes { reportId ->
            val response = client.put(
                "/api/v1/reports/$reportId/vote"
            ) {
                bearerAuth(token(2))
                contentType(ContentType.Application.Json)
                setBody(voteBody("INVALID"))
            }

            assertEquals(
                HttpStatusCode.BadRequest,
                response.status
            )
        }

    @Test
    fun `multiple users contribute to vote summary`() =
        testVotes { reportId ->
            val path = "/api/v1/reports/$reportId/vote"

            client.put(path) {
                bearerAuth(token(2))
                contentType(ContentType.Application.Json)
                setBody(voteBody("LIKE"))
            }

            client.put(path) {
                bearerAuth(token(3))
                contentType(ContentType.Application.Json)
                setBody(voteBody("LIKE"))
            }

            client.put(path) {
                bearerAuth(token(4))
                contentType(ContentType.Application.Json)
                setBody(voteBody("DISLIKE"))
            }

            val response = client.get(
                "/api/v1/reports/$reportId/votes"
            )

            val summary = json.decodeFromString(
                VoteSummaryResponse.serializer(),
                response.bodyAsText()
            )

            assertEquals(2, summary.likes)
            assertEquals(1, summary.dislikes)
            assertEquals(3, summary.totalVotes)
            assertEquals(1, summary.score)
        }
}