// test/nl/brul/api/features/report/routes/ReportPhotoRoutesTest.kt
package nl.brul.api.features.report.routes

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.delete
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsBytes
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
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
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import nl.brul.api.features.report.InMemoryPhotoStorage
import nl.brul.api.features.report.PhotoTestData
import nl.brul.api.features.report.dto.CreateReportRequest
import nl.brul.api.features.report.dto.ReportPhotoResponse
import nl.brul.api.features.report.dto.ReportResponse
import nl.brul.api.features.report.repository.ExposedReportPhotoRepository
import nl.brul.api.features.report.repository.ExposedReportRepository
import nl.brul.api.features.report.repository.ReportPhotosTable
import nl.brul.api.features.report.repository.ReportsTable
import nl.brul.api.features.report.service.ReportPhotoService
import nl.brul.api.features.report.service.ReportService
import nl.brul.api.features.user.model.NewUser
import nl.brul.api.features.user.repository.ExposedUserRepository
import nl.brul.api.features.user.repository.UsersTable
import nl.brul.api.plugins.configureErrorHandling
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

/**
 * API tests for photos with GPS, with a real H2 database (so ExposedReportPhotoRepository is
 * tested too) and photo files in memory.
 */
class ReportPhotoRoutesTest {

    private val secret = "report-photo-routes-test-secret"
    private val issuer = "brul-test"
    private val audience = "brul-test-users"

    private val json = Json { ignoreUnknownKeys = false }

    private fun token(userId: Long, role: String = "USER"): String =
        JWT.create()
            .withSubject(userId.toString())
            .withIssuer(issuer)
            .withAudience(audience)
            .withClaim("role", role)
            .sign(Algorithm.HMAC256(secret))

    private fun testPhotos(
        block: suspend ApplicationTestBuilder.(InMemoryPhotoStorage) -> Unit
    ) = testApplication {
        val database = Database.connect(
            url = "jdbc:h2:mem:photo_routes_${UUID.randomUUID()};DB_CLOSE_DELAY=-1",
            driver = "org.h2.Driver",
            user = "sa",
            password = ""
        )

        transaction(database) {
            SchemaUtils.create(UsersTable, ReportsTable, ReportPhotosTable)
        }

        // Users 1, 2 and 3 for the foreign keys.
        val users = ExposedUserRepository(database)

        (1..3).forEach { number ->
            users.create(
                NewUser(
                    username = "user$number",
                    email = "user$number@example.com",
                    passwordHash = "test-password-hash"
                )
            )
        }

        val storage = InMemoryPhotoStorage()
        val reports = ExposedReportRepository(database)

        val photoService = ReportPhotoService(
            reportRepository = reports,
            photoRepository = ExposedReportPhotoRepository(database),
            storage = storage
        )

        val reportService = ReportService(repository = reports, photoService = photoService)

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

                        if (userId != null && userId > 0) JWTPrincipal(credential.payload) else null
                    }
                }
            }

            configureErrorHandling()

            routing {
                route("/api/v1") {
                    reportRoutes(reportService)
                    reportPhotoRoutes(photoService)
                }
            }
        }

        block(storage)
    }

    /** Creates a nuisance report owned by user 1 and returns its id. */
    private suspend fun ApplicationTestBuilder.createReport(): Long {
        val response = client.post("/api/v1/reports") {
            bearerAuth(token(1))
            contentType(ContentType.Application.Json)
            setBody(
                Json.encodeToString(
                    CreateReportRequest.serializer(),
                    CreateReportRequest(
                        type = "NUISANCE",
                        title = "Graffiti on the bridge",
                        description = "Large graffiti on the pedestrian bridge",
                        category = "VANDALISM",
                        latitude = 51.5866,
                        longitude = 4.7760
                    )
                )
            )
        }

        assertEquals(HttpStatusCode.Created, response.status)

        return json.decodeFromString(ReportResponse.serializer(), response.bodyAsText()).id
    }

    private suspend fun ApplicationTestBuilder.upload(
        reportId: Long,
        userId: Long?,
        bytes: ByteArray,
        latitude: String? = null,
        longitude: String? = null
    ): HttpResponse =
        client.post("/api/v1/reports/$reportId/photos") {
            if (userId != null) {
                bearerAuth(token(userId))
            }

            setBody(
                MultiPartFormDataContent(
                    formData {
                        if (latitude != null) append("latitude", latitude)
                        if (longitude != null) append("longitude", longitude)
                        append(
                            "photo",
                            bytes,
                            Headers.build {
                                append(HttpHeaders.ContentType, "image/jpeg")
                                append(HttpHeaders.ContentDisposition, "filename=\"photo.jpg\"")
                            }
                        )
                    }
                )
            )
        }

    private suspend fun HttpResponse.photo(): ReportPhotoResponse =
        json.decodeFromString(ReportPhotoResponse.serializer(), bodyAsText())

    @Test
    fun `owner uploads a photo and its GPS comes from the photo`() = testPhotos { storage ->
        val reportId = createReport()

        val response = upload(reportId, 1, PhotoTestData.jpegWithGps(51.5856, 4.7763))

        assertEquals(HttpStatusCode.Created, response.status)

        val photo = response.photo()

        assertTrue(abs(assertNotNull(photo.latitude) - 51.5856) < 0.00001)
        assertTrue(abs(assertNotNull(photo.longitude) - 4.7763) < 0.00001)
        assertEquals("PHOTO", photo.locationSource)
        assertEquals(1, storage.files.size)
    }

    @Test
    fun `without GPS in the photo the app coordinates are used`() = testPhotos {
        val reportId = createReport()

        val photo = upload(reportId, 1, PhotoTestData.jpeg(), "51.59", "4.78").photo()

        assertEquals(51.59, photo.latitude)
        assertEquals(4.78, photo.longitude)
        assertEquals("APP", photo.locationSource)
    }

    @Test
    fun `without any GPS the location is empty`() = testPhotos {
        val reportId = createReport()

        val photo = upload(reportId, 1, PhotoTestData.jpeg()).photo()

        assertNull(photo.latitude)
        assertNull(photo.locationSource)
    }

    @Test
    fun `anyone can list photos and download the image`() = testPhotos {
        val reportId = createReport()
        val bytes = PhotoTestData.jpegWithGps(51.5856, 4.7763)
        val photo = upload(reportId, 1, bytes).photo()

        val list = json.decodeFromString(
            ListSerializer(ReportPhotoResponse.serializer()),
            client.get("/api/v1/reports/$reportId/photos").bodyAsText()
        )

        assertEquals(listOf(photo.id), list.map { it.id })
        assertEquals("PHOTO", list.single().locationSource)

        val image = client.get(photo.url)

        assertEquals(HttpStatusCode.OK, image.status)
        assertTrue(bytes.contentEquals(image.bodyAsBytes()))
    }

    @Test
    fun `upload without JWT returns 401`() = testPhotos {
        val reportId = createReport()

        assertEquals(HttpStatusCode.Unauthorized, upload(reportId, null, PhotoTestData.jpeg()).status)
    }

    @Test
    fun `someone else than the owner gets 403`() = testPhotos {
        val reportId = createReport()

        assertEquals(HttpStatusCode.Forbidden, upload(reportId, 2, PhotoTestData.jpeg()).status)
    }

    @Test
    fun `a text file gets 415`() = testPhotos {
        val reportId = createReport()

        val response = upload(reportId, 1, "not an image".toByteArray())

        assertEquals(HttpStatusCode.UnsupportedMediaType, response.status)
    }

    @Test
    fun `the fourth photo gets 409`() = testPhotos {
        val reportId = createReport()

        repeat(3) {
            assertEquals(HttpStatusCode.Created, upload(reportId, 1, PhotoTestData.jpeg()).status)
        }

        assertEquals(HttpStatusCode.Conflict, upload(reportId, 1, PhotoTestData.jpeg()).status)
    }

    @Test
    fun `only one app coordinate gets 400`() = testPhotos {
        val reportId = createReport()

        val response = upload(reportId, 1, PhotoTestData.jpeg(), latitude = "51.59")

        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun `photos of a missing report get 404`() = testPhotos {
        assertEquals(HttpStatusCode.NotFound, client.get("/api/v1/reports/99/photos").status)
        assertEquals(HttpStatusCode.NotFound, upload(99, 1, PhotoTestData.jpeg()).status)
    }

    @Test
    fun `uploader deletes a photo, someone else cannot`() = testPhotos { storage ->
        val reportId = createReport()
        val photo = upload(reportId, 1, PhotoTestData.jpeg()).photo()

        val forbidden = client.delete("/api/v1/photos/${photo.id}") { bearerAuth(token(2)) }
        assertEquals(HttpStatusCode.Forbidden, forbidden.status)

        val deleted = client.delete("/api/v1/photos/${photo.id}") { bearerAuth(token(1)) }
        assertEquals(HttpStatusCode.NoContent, deleted.status)
        assertTrue(storage.files.isEmpty())
        assertEquals(HttpStatusCode.NotFound, client.get(photo.url).status)
    }

    @Test
    fun `deleting a report removes its photo files`() = testPhotos { storage ->
        val reportId = createReport()
        upload(reportId, 1, PhotoTestData.jpeg())

        val response = client.delete("/api/v1/reports/$reportId") { bearerAuth(token(1)) }

        assertEquals(HttpStatusCode.NoContent, response.status)
        assertTrue(storage.files.isEmpty())
    }
}
