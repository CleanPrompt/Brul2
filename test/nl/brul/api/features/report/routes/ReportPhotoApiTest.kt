// test/nl/brul/api/features/report/routes/ReportPhotoApiTest.kt
package nl.brul.api.features.report.routes

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.delete
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
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
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import nl.brul.api.common.error.ApiError
import nl.brul.api.features.help.repository.ExposedHelpParticipationRepository
import nl.brul.api.features.report.dto.CreateReportRequest
import nl.brul.api.features.report.dto.ReportPhotoResponse
import nl.brul.api.features.report.dto.ReportResponse
import nl.brul.api.features.report.dto.UpdateReportRequest
import nl.brul.api.features.report.repository.ExposedReportPhotoRepository
import nl.brul.api.features.report.repository.ExposedReportRepository
import nl.brul.api.features.report.repository.InMemoryPhotoStorage
import nl.brul.api.features.report.service.ReportPhotoService
import nl.brul.api.features.report.service.ReportService
import nl.brul.api.plugins.configureErrorHandling
import nl.brul.api.support.TestDatabase
import nl.brul.api.support.TestImages
import nl.brul.api.support.containsText

/** API tests for photos of reports (F1-F8, P1, P5, P6). */
class ReportPhotoApiTest {

    private val secret = "report-photo-test-secret-at-least-32-bytes"
    private val issuer = "brul-photo-test"
    private val audience = "brul-photo-users"

    private val json = Json {
        ignoreUnknownKeys = false
    }

    private fun token(userId: Long, role: String = "USER"): String =
        JWT.create()
            .withSubject(userId.toString())
            .withIssuer(issuer)
            .withAudience(audience)
            .withClaim("role", role)
            .sign(Algorithm.HMAC256(secret))

    private fun testPhotoApi(
        block: suspend ApplicationTestBuilder.(InMemoryPhotoStorage) -> Unit
    ) = testApplication {
        val database = TestDatabase.create("photo_api", users = 3)
        val storage = InMemoryPhotoStorage()
        val reports = ExposedReportRepository(database)
        val participations = ExposedHelpParticipationRepository(database)

        val photoService = ReportPhotoService(
            reportRepository = reports,
            photoRepository = ExposedReportPhotoRepository(database),
            storage = storage,
            participationRepository = participations
        )

        val reportService = ReportService(
            repository = reports,
            helpParticipationRepository = participations,
            photoService = photoService
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
                    reportRoutes(reportService, photoService)
                    reportPhotoRoutes(photoService)
                }
            }
        }

        block(storage)
    }

    /** Creates a nuisance report owned by user 1 and returns its id. */
    private suspend fun ApplicationTestBuilder.createNuisanceReport(): Long {
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
        kind: String = "SITUATION",
        contentType: String = "image/jpeg"
    ): HttpResponse =
        client.post("/api/v1/reports/$reportId/photos") {
            if (userId != null) {
                bearerAuth(token(userId))
            }

            setBody(
                MultiPartFormDataContent(
                    formData {
                        append("kind", kind)
                        append(
                            "photo",
                            bytes,
                            Headers.build {
                                append(HttpHeaders.ContentType, contentType)
                                append(HttpHeaders.ContentDisposition, "filename=\"photo.jpg\"")
                            }
                        )
                    }
                )
            )
        }

    private suspend fun HttpResponse.error() =
        json.decodeFromString(ApiError.serializer(), bodyAsText())

    @Test
    fun `owner uploads a photo and anyone can view it`() =
        testPhotoApi { storage ->
            val reportId = createNuisanceReport()

            val uploaded = upload(reportId, userId = 1, bytes = TestImages.jpegWithExif())

            assertEquals(HttpStatusCode.Created, uploaded.status)

            val photo = json.decodeFromString(ReportPhotoResponse.serializer(), uploaded.bodyAsText())

            assertEquals("SITUATION", photo.kind)
            assertEquals("image/jpeg", photo.contentType)
            assertEquals(1, storage.keys.size)

            val list = json.decodeFromString(
                ListSerializer(ReportPhotoResponse.serializer()),
                client.get("/api/v1/reports/$reportId/photos").bodyAsText()
            )

            assertEquals(listOf(photo.id), list.map { it.id })

            val image = client.get(photo.url)

            assertEquals(HttpStatusCode.OK, image.status)
            assertEquals("image/jpeg", image.headers[HttpHeaders.ContentType])

            // F6: the served photo no longer contains the location data.
            assertTrue(!image.bodyAsBytes().containsText("GPSLatitude"))

            val report = json.decodeFromString(
                ReportResponse.serializer(),
                client.get("/api/v1/reports/$reportId").bodyAsText()
            )

            assertEquals(1, report.photoCount)
        }

    @Test
    fun `anonymous user cannot upload`() =
        testPhotoApi {
            val reportId = createNuisanceReport()

            assertEquals(
                HttpStatusCode.Unauthorized,
                upload(reportId, userId = null, bytes = TestImages.jpeg()).status
            )
        }

    // P1 at API level (F1)
    @Test
    fun `a pdf sent as image jpeg is refused`() =
        testPhotoApi { storage ->
            val reportId = createNuisanceReport()

            val response = upload(reportId, 1, "%PDF-1.7 not an image".toByteArray())

            assertEquals(HttpStatusCode.UnsupportedMediaType, response.status)
            assertEquals("UNSUPPORTED_TYPE", response.error().code)
            assertTrue(storage.keys.isEmpty())
        }

    // P5 (F2)
    @Test
    fun `a photo over the limit is refused and nothing is stored`() =
        testPhotoApi { storage ->
            val reportId = createNuisanceReport()

            val sixMegabytes = TestImages.jpeg() + ByteArray(6 * 1024 * 1024)

            val response = upload(reportId, 1, sixMegabytes)

            assertEquals(HttpStatusCode.PayloadTooLarge, response.status)
            assertEquals("TOO_LARGE", response.error().code)
            assertTrue(storage.keys.isEmpty())
        }

    @Test
    fun `only the owner adds situation photos (F4)`() =
        testPhotoApi {
            val reportId = createNuisanceReport()

            val response = upload(reportId, userId = 2, bytes = TestImages.jpeg())

            assertEquals(HttpStatusCode.Forbidden, response.status)
            assertEquals("NOT_ALLOWED", response.error().code)
        }

    @Test
    fun `a report holds at most three photos (F3)`() =
        testPhotoApi {
            val reportId = createNuisanceReport()

            repeat(3) {
                assertEquals(HttpStatusCode.Created, upload(reportId, 1, TestImages.png(), contentType = "image/png").status)
            }

            val fourth = upload(reportId, 1, TestImages.jpeg())

            assertEquals(HttpStatusCode.Conflict, fourth.status)
            assertEquals("PHOTO_LIMIT", fourth.error().code)
        }

    // P6 (F7)
    @Test
    fun `deleting a report also deletes its photo files`() =
        testPhotoApi { storage ->
            val reportId = createNuisanceReport()

            upload(reportId, 1, TestImages.jpeg())
            upload(reportId, 1, TestImages.jpeg())

            assertEquals(2, storage.keys.size)

            val deleted = client.delete("/api/v1/reports/$reportId") {
                bearerAuth(token(1))
            }

            assertEquals(HttpStatusCode.NoContent, deleted.status)
            assertTrue(storage.keys.isEmpty())
        }

    @Test
    fun `only the uploader or an admin deletes a photo (F7)`() =
        testPhotoApi { storage ->
            val reportId = createNuisanceReport()

            val photo = json.decodeFromString(
                ReportPhotoResponse.serializer(),
                upload(reportId, 1, TestImages.jpeg()).bodyAsText()
            )

            val stranger = client.delete("/api/v1/photos/${photo.id}") {
                bearerAuth(token(2))
            }

            assertEquals(HttpStatusCode.Forbidden, stranger.status)

            val admin = client.delete("/api/v1/photos/${photo.id}") {
                bearerAuth(token(3, role = "ADMIN"))
            }

            assertEquals(HttpStatusCode.NoContent, admin.status)
            assertTrue(storage.keys.isEmpty())
            assertEquals(HttpStatusCode.NotFound, client.get(photo.url).status)
        }

    // F8
    @Test
    fun `nuisance report can only be resolved with a result photo`() =
        testPhotoApi {
            val reportId = createNuisanceReport()

            suspend fun resolve() = client.put("/api/v1/reports/$reportId") {
                bearerAuth(token(1))
                contentType(ContentType.Application.Json)
                setBody(
                    Json.encodeToString(
                        UpdateReportRequest.serializer(),
                        UpdateReportRequest(
                            type = "NUISANCE",
                            title = "Graffiti on the bridge",
                            description = "Large graffiti on the pedestrian bridge",
                            category = "VANDALISM",
                            latitude = 51.5866,
                            longitude = 4.7760,
                            status = "RESOLVED"
                        )
                    )
                )
            }

            val withoutPhoto = resolve()

            assertEquals(HttpStatusCode.Conflict, withoutPhoto.status)
            assertEquals("RESULT_PHOTO_REQUIRED", withoutPhoto.error().code)

            assertEquals(
                HttpStatusCode.Created,
                upload(reportId, 1, TestImages.jpeg(), kind = "RESULT").status
            )

            assertEquals(HttpStatusCode.OK, resolve().status)
        }
}
