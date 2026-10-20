// test/nl/brul/api/features/report/routes/ReportJwtHardeningIntegrationTest.kt
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
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

class ReportJwtHardeningIntegrationTest {

    private val json = Json {
        ignoreUnknownKeys = false
    }

    private val secret = "jwt-hardening-test-secret-at-least-32-bytes"
    private val issuer = "brul-hardening-test"
    private val audience = "brul-hardening-users"

    private fun testWithDatabase(
        block: suspend ApplicationTestBuilder.(Database) -> Unit
    ) = testApplication {
        val databaseName = "jwt_hardening_${UUID.randomUUID()}"

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
            secret = secret,
            issuer = issuer,
            audience = audience,
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

        val token = json.parseToJsonElement(
            response.bodyAsText()
        ).jsonObject["accessToken"]?.jsonPrimitive?.content

        return assertNotNull(token)
    }

    private suspend fun ApplicationTestBuilder.createReport(
        token: String
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
            bearerAuth(token)
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

    private fun updateRequest() = UpdateReportRequest(
        type = "SAFETY",
        title = "Changed title",
        description = "Changed description",
        category = "TRAFFIC_SAFETY",
        latitude = 52.0705,
        longitude = 4.3007,
        status = "IN_PROGRESS",
        urgent = false
    )

    private fun signedToken(
        subject: String? = "1",
        tokenIssuer: String? = issuer,
        tokenAudience: String? = audience,
        role: String? = "USER",
        signingSecret: String = secret,
        expiresAt: Long = System.currentTimeMillis() + 3_600_000L
    ): String {
        val builder = JWT.create()
            .withIssuedAt(Date())
            .withExpiresAt(Date(expiresAt))

        if (subject != null) {
            builder.withSubject(subject)
        }

        if (tokenIssuer != null) {
            builder.withIssuer(tokenIssuer)
        }

        if (tokenAudience != null) {
            builder.withAudience(tokenAudience)
        }

        if (role != null) {
            builder.withClaim("role", role)
        }

        return builder.sign(Algorithm.HMAC256(signingSecret))
    }

    private suspend fun ApplicationTestBuilder.assertPostUnauthorized(
        token: String
    ) {
        val response = client.post("/api/v1/reports") {
            bearerAuth(token)
            contentType(ContentType.Application.Json)
            setBody(
                json.encodeToString(
                    CreateReportRequest.serializer(),
                    CreateReportRequest(
                        type = "SAFETY",
                        title = "Unauthorized report",
                        description = "This must not be created",
                        category = "TRAFFIC_SAFETY",
                        latitude = 52.0705,
                        longitude = 4.3007,
                        urgent = true
                    )
                )
            )
        }

        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    // ---------------------------------------------------------
    // Invalid JWT subject
    // ---------------------------------------------------------

    @Test
    fun `JWT without subject cannot create report`() =
        testWithDatabase {
            assertPostUnauthorized(
                signedToken(subject = null)
            )
        }

    @Test
    fun `JWT with non numeric subject cannot create report`() =
        testWithDatabase {
            assertPostUnauthorized(
                signedToken(subject = "not-a-user-id")
            )
        }

    @Test
    fun `JWT with zero subject cannot create report`() =
        testWithDatabase {
            assertPostUnauthorized(
                signedToken(subject = "0")
            )
        }

    @Test
    fun `JWT with negative subject cannot create report`() =
        testWithDatabase {
            assertPostUnauthorized(
                signedToken(subject = "-10")
            )
        }

    // ---------------------------------------------------------
    // Invalid JWT issuer and audience
    // ---------------------------------------------------------

    @Test
    fun `JWT with wrong issuer cannot create report`() =
        testWithDatabase {
            assertPostUnauthorized(
                signedToken(tokenIssuer = "wrong-issuer")
            )
        }

    @Test
    fun `JWT without issuer cannot create report`() =
        testWithDatabase {
            assertPostUnauthorized(
                signedToken(tokenIssuer = null)
            )
        }

    @Test
    fun `JWT with wrong audience cannot create report`() =
        testWithDatabase {
            assertPostUnauthorized(
                signedToken(tokenAudience = "wrong-audience")
            )
        }

    @Test
    fun `JWT without audience cannot create report`() =
        testWithDatabase {
            assertPostUnauthorized(
                signedToken(tokenAudience = null)
            )
        }

    // ---------------------------------------------------------
    // Unknown user and forged role
    // ---------------------------------------------------------

    @Test
    fun `JWT for nonexistent user is not accepted by profile endpoint`() =
        testWithDatabase {
            val token = signedToken(subject = "999999")

            val response = client.get("/api/v1/auth/me") {
                bearerAuth(token)
            }

            assertEquals(HttpStatusCode.Unauthorized, response.status)
        }

    @Test
    fun `JWT signed with wrong secret cannot grant ADMIN access`() =
        testWithDatabase {
            register("owner", "owner@example.com")

            val ownerToken = login("owner@example.com")
            val report = createReport(ownerToken)

            val forgedToken = signedToken(
                subject = "999",
                role = "ADMIN",
                signingSecret = "forged-admin-token-secret-at-least-32-bytes"
            )

            val response = client.delete(
                "/api/v1/reports/${report.id}"
            ) {
                bearerAuth(forgedToken)
            }

            assertEquals(HttpStatusCode.Unauthorized, response.status)

            val retrieved = client.get(
                "/api/v1/reports/${report.id}"
            )

            assertEquals(HttpStatusCode.OK, retrieved.status)
        }

    // ---------------------------------------------------------
    // Role changes and existing JWTs
    // ---------------------------------------------------------

    @Test
    fun `old USER token does not gain ADMIN rights after promotion`() =
        testWithDatabase { database ->
            register("owner", "owner@example.com")

            val otherId = register(
                "other",
                "other@example.com"
            )

            val ownerToken = login("owner@example.com")
            val oldUserToken = login("other@example.com")

            val report = createReport(ownerToken)

            transaction(database) {
                UsersTable.update({ UsersTable.id eq otherId }) {
                    it[role] = UserRole.ADMIN
                }
            }

            // Existing JWT still contains the original USER role.
            assertEquals(
                "USER",
                JWT.decode(oldUserToken).getClaim("role").asString()
            )

            val response = client.delete(
                "/api/v1/reports/${report.id}"
            ) {
                bearerAuth(oldUserToken)
            }

            assertEquals(HttpStatusCode.Forbidden, response.status)

            val retrieved = client.get(
                "/api/v1/reports/${report.id}"
            )

            assertEquals(HttpStatusCode.OK, retrieved.status)
        }

    @Test
    fun `new ADMIN token grants delete rights after promotion`() =
        testWithDatabase { database ->
            register("owner", "owner@example.com")

            val otherId = register(
                "other",
                "other@example.com"
            )

            val ownerToken = login("owner@example.com")
            val report = createReport(ownerToken)

            transaction(database) {
                UsersTable.update({ UsersTable.id eq otherId }) {
                    it[role] = UserRole.ADMIN
                }
            }

            val adminToken = login("other@example.com")

            assertEquals(
                "ADMIN",
                JWT.decode(adminToken).getClaim("role").asString()
            )

            val response = client.delete(
                "/api/v1/reports/${report.id}"
            ) {
                bearerAuth(adminToken)
            }

            assertEquals(HttpStatusCode.NoContent, response.status)
        }

    // ---------------------------------------------------------
    // Protected routes and public read access
    // ---------------------------------------------------------

    @Test
    fun `invalid JWT cannot update an existing report`() =
        testWithDatabase {
            register("owner", "owner@example.com")

            val ownerToken = login("owner@example.com")
            val report = createReport(ownerToken)

            val response = client.put(
                "/api/v1/reports/${report.id}"
            ) {
                bearerAuth(
                    signedToken(tokenIssuer = "wrong-issuer")
                )
                contentType(ContentType.Application.Json)
                setBody(
                    json.encodeToString(
                        UpdateReportRequest.serializer(),
                        updateRequest()
                    )
                )
            }

            assertEquals(HttpStatusCode.Unauthorized, response.status)

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
    fun `invalid JWT cannot delete an existing report`() =
        testWithDatabase {
            register("owner", "owner@example.com")

            val ownerToken = login("owner@example.com")
            val report = createReport(ownerToken)

            val response = client.delete(
                "/api/v1/reports/${report.id}"
            ) {
                bearerAuth(
                    signedToken(tokenAudience = "wrong-audience")
                )
            }

            assertEquals(HttpStatusCode.Unauthorized, response.status)

            val retrieved = client.get(
                "/api/v1/reports/${report.id}"
            )

            assertEquals(HttpStatusCode.OK, retrieved.status)
        }

    @Test
    fun `public report list works without authentication`() =
        testWithDatabase {
            val response = client.get("/api/v1/reports")

            assertEquals(HttpStatusCode.OK, response.status)
        }

    @Test
    fun `public report details work without authentication`() =
        testWithDatabase {
            register("owner", "owner@example.com")

            val ownerToken = login("owner@example.com")
            val report = createReport(ownerToken)

            val response = client.get(
                "/api/v1/reports/${report.id}"
            )

            assertEquals(HttpStatusCode.OK, response.status)
        }
}