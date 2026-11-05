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
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
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
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import nl.brul.api.common.error.ErrorResponse
import nl.brul.api.features.report.FakePhotoRepository
import nl.brul.api.features.report.FakePhotoUserRepository
import nl.brul.api.features.report.FakeReportRepository
import nl.brul.api.features.report.InMemoryPhotoStorage
import nl.brul.api.features.report.PhotoTestData
import nl.brul.api.features.report.dto.ReportPhotoResponse
import nl.brul.api.features.report.service.PhotoAccessService
import nl.brul.api.features.report.service.PhotoRules
import nl.brul.api.features.report.service.ReportPhotoService
import nl.brul.api.features.report.service.ReportService
import nl.brul.api.plugins.configureErrorHandling

/**
 * API tests for photos with fakes, so no database is needed (the Exposed repository has its own test).
 * Report 1 belongs to user 1; user 2 is someone else and user 3 is an admin (see `FakePhotoUserRepository`).
 */
class ReportPhotoRoutesTest {

    private val secret = "report-photo-routes-test-secret"
    private val issuer = "brul-test"
    private val audience = "brul-test-users"

    private val json = Json { ignoreUnknownKeys = false }

    private fun token(userId: Long): String =
        JWT.create()
            .withSubject(userId.toString())
            .withIssuer(issuer)
            .withAudience(audience)
            .sign(Algorithm.HMAC256(secret))

    private fun testPhotos(block: suspend ApplicationTestBuilder.(InMemoryPhotoStorage) -> Unit) = testApplication {
        val reports = FakeReportRepository(PhotoTestData.nuisance(id = 1, ownerId = 1))
        val storage = InMemoryPhotoStorage()
        val photoService = ReportPhotoService(reports, FakePhotoRepository(), storage)
        val accessService = PhotoAccessService(FakePhotoUserRepository())
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
                    reportPhotoRoutes(photoService, accessService)
                }
            }
        }

        block(storage)
    }

    private suspend fun ApplicationTestBuilder.upload(
        reportId: Any = 1,
        userId: Long? = 1,
        bytes: ByteArray? = PhotoTestData.jpeg(),
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
                        if (bytes != null) {
                            append(
                                "photo",
                                bytes,
                                Headers.build {
                                    append(HttpHeaders.ContentType, "image/jpeg")
                                    append(HttpHeaders.ContentDisposition, "filename=\"photo.jpg\"")
                                }
                            )
                        }
                    }
                )
            )
        }

    private suspend fun HttpResponse.photo(): ReportPhotoResponse =
        json.decodeFromString(ReportPhotoResponse.serializer(), bodyAsText())

    private suspend fun HttpResponse.errorCode(): String =
        json.decodeFromString(ErrorResponse.serializer(), bodyAsText()).code

    @Test
    fun `the owner uploads a photo and its GPS comes from the photo`() = testPhotos { storage ->
        val response = upload(bytes = PhotoTestData.jpegWithGps(51.5856, 4.7763))

        assertEquals(HttpStatusCode.Created, response.status)

        val photo = response.photo()

        assertTrue(abs(assertNotNull(photo.latitude) - 51.5856) < 0.00001)
        assertTrue(abs(assertNotNull(photo.longitude) - 4.7763) < 0.00001)
        assertEquals("PHOTO", photo.locationSource)
        assertEquals(1, storage.files.size)
    }

    @Test
    fun `without GPS in the photo the app coordinates are used`() = testPhotos {
        val photo = upload(latitude = "51.59", longitude = "4.78").photo()

        assertEquals(51.59, photo.latitude)
        assertEquals(4.78, photo.longitude)
        assertEquals("APP", photo.locationSource)
    }

    @Test
    fun `anyone can list the photos and download the image without logging in`() = testPhotos {
        val bytes = PhotoTestData.jpegWithGps(51.5856, 4.7763)
        val photo = upload(bytes = bytes).photo()

        val list = json.decodeFromString(
            ListSerializer(ReportPhotoResponse.serializer()),
            client.get("/api/v1/reports/1/photos").bodyAsText()
        )

        assertEquals(listOf(photo.id), list.map { it.id })

        val image = client.get(photo.url)

        assertEquals(HttpStatusCode.OK, image.status)
        assertTrue(bytes.contentEquals(image.bodyAsBytes()))
    }

    @Test
    fun `returns 401 without a token`() = testPhotos {
        assertEquals(HttpStatusCode.Unauthorized, upload(userId = null).status)
    }

    @Test
    fun `returns 403 when someone else than the owner uploads`() = testPhotos {
        val response = upload(userId = 2)

        assertEquals(HttpStatusCode.Forbidden, response.status)
        assertEquals("FORBIDDEN", response.errorCode())
    }

    @Test
    fun `returns 404 for an unknown report`() = testPhotos {
        val list = client.get("/api/v1/reports/99/photos")

        assertEquals(HttpStatusCode.NotFound, list.status)
        assertEquals("REPORT_NOT_FOUND", list.errorCode())
        assertEquals(HttpStatusCode.NotFound, upload(reportId = 99).status)
    }

    @Test
    fun `returns 400 for an id that is not a number`() = testPhotos {
        val response = client.get("/api/v1/reports/abc/photos")

        assertEquals(HttpStatusCode.BadRequest, response.status)
        assertEquals("INVALID_REQUEST", response.errorCode())
    }

    @Test
    fun `returns 400 without a photo or with only one coordinate`() = testPhotos {
        val noPhoto = upload(bytes = null)
        assertEquals(HttpStatusCode.BadRequest, noPhoto.status)
        assertEquals("PHOTO_REQUIRED", noPhoto.errorCode())

        val oneCoordinate = upload(latitude = "51.59")
        assertEquals(HttpStatusCode.BadRequest, oneCoordinate.status)
        assertEquals("INVALID_REQUEST", oneCoordinate.errorCode())
    }

    @Test
    fun `returns 413 for a photo larger than 5 MB and stores nothing`() = testPhotos { storage ->
        val response = upload(bytes = ByteArray(PhotoRules.MAX_PHOTO_BYTES.toInt() + 1))

        assertEquals(HttpStatusCode.PayloadTooLarge, response.status)
        assertEquals("PHOTO_TOO_LARGE", response.errorCode())
        assertTrue(storage.files.isEmpty())
    }

    @Test
    fun `returns 415 for a file that is not a JPEG or PNG`() = testPhotos {
        val response = upload(bytes = "not an image".toByteArray())

        assertEquals(HttpStatusCode.UnsupportedMediaType, response.status)
        assertEquals("UNSUPPORTED_PHOTO_TYPE", response.errorCode())
    }

    @Test
    fun `returns 409 for the fourth photo`() = testPhotos {
        repeat(3) {
            assertEquals(HttpStatusCode.Created, upload().status)
        }

        val response = upload()

        assertEquals(HttpStatusCode.Conflict, response.status)
        assertEquals("PHOTO_LIMIT_REACHED", response.errorCode())
    }

    @Test
    fun `the uploader deletes a photo and then it is gone`() = testPhotos { storage ->
        val photo = upload().photo()

        val deleted = client.delete("/api/v1/photos/${photo.id}") { bearerAuth(token(1)) }

        assertEquals(HttpStatusCode.NoContent, deleted.status)
        assertTrue(storage.files.isEmpty())

        val image = client.get(photo.url)
        assertEquals(HttpStatusCode.NotFound, image.status)
        assertEquals("PHOTO_NOT_FOUND", image.errorCode())
    }

    @Test
    fun `someone else cannot delete a photo, an admin can`() = testPhotos {
        val photo = upload().photo()

        val forbidden = client.delete("/api/v1/photos/${photo.id}") { bearerAuth(token(2)) }
        assertEquals(HttpStatusCode.Forbidden, forbidden.status)

        val byAdmin = client.delete("/api/v1/photos/${photo.id}") { bearerAuth(token(3)) }
        assertEquals(HttpStatusCode.NoContent, byAdmin.status)
    }

    @Test
    fun `deleting a report removes its photo files`() = testPhotos { storage ->
        upload()

        val response = client.delete("/api/v1/reports/1") { bearerAuth(token(1)) }

        assertEquals(HttpStatusCode.NoContent, response.status)
        assertTrue(storage.files.isEmpty())
    }
}
