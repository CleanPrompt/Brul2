// src/nl/brul/api/features/report/routes/ReportPhotoRoutes.kt
package nl.brul.api.features.report.routes

import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.PartData
import io.ktor.http.content.forEachPart
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.request.contentLength
import io.ktor.server.request.receiveMultipart
import io.ktor.server.response.respond
import io.ktor.server.response.respondBytes
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.utils.io.readBuffer
import kotlinx.io.readByteArray
import nl.brul.api.common.error.ApiError
import nl.brul.api.features.report.dto.toResponse
import nl.brul.api.features.report.model.Location
import nl.brul.api.features.report.service.PhotoDeleteResult
import nl.brul.api.features.report.service.PhotoRejection
import nl.brul.api.features.report.service.PhotoRules
import nl.brul.api.features.report.service.PhotoUploadResult
import nl.brul.api.features.report.service.ReportPhotoService

/** Ruimte voor de multipart-scheidingen en de velden naast de foto. */
private const val MULTIPART_OVERHEAD_BYTES = 64L * 1024

/**
 * Laag: routes (de voordeur).
 *
 * Foto's bij meldingen; in Routing.kt komt /api/v1 voor de paden.
 *
 * - GET /reports/{id}/photos: de gegevens van alle foto's bij een melding, met hun locatie
 *   (openbaar, net als de meldingen zelf)
 * - GET /photos/{photoId}: de afbeelding zelf, JPEG of PNG (openbaar)
 * - POST /reports/{id}/photos: een foto plaatsen, alleen de eigenaar van de melding (JWT).
 *   Multipart met het bestand in `photo` en, optioneel, `latitude` en `longitude`. Die worden
 *   alleen gebruikt als de foto zelf geen GPS bevat.
 * - DELETE /photos/{photoId}: verwijderen, door de uploader of een ADMIN
 *
 * Statuscodes: 201 geplaatst, 400 foute invoer, 401 niet ingelogd, 403 niet toegestaan, 404 niet
 * gevonden, 409 al 3 foto's, 413 te groot, 415 geen JPEG of PNG.
 * Een te groot verzoek wordt al op de Content-Length geweigerd, en van de foto leest de route
 * nooit meer dan 5 MB + 1 byte. Zo kan niemand de server vol laten lopen.
 */
fun Route.reportPhotoRoutes(service: ReportPhotoService) {

    get("/reports/{id}/photos") {
        val reportId = parsePositiveId(call.parameters["id"], "Report ID")

        val photos = service.list(reportId)

        if (photos == null) {
            call.respond(HttpStatusCode.NotFound)
            return@get
        }

        call.respond(HttpStatusCode.OK, photos.map { it.toResponse() })
    }

    get("/photos/{photoId}") {
        val photoId = parsePositiveId(call.parameters["photoId"], "Photo ID")

        val loaded = service.load(photoId)

        if (loaded == null) {
            call.respond(HttpStatusCode.NotFound)
            return@get
        }

        val (photo, bytes) = loaded

        call.respondBytes(
            bytes = bytes,
            contentType = ContentType.parse(photo.contentType),
            status = HttpStatusCode.OK
        )
    }

    authenticate("auth-jwt") {

        post("/reports/{id}/photos") {
            val userId = call.principal<JWTPrincipal>()
                ?.payload
                ?.subject
                ?.toLongOrNull()

            if (userId == null || userId <= 0) {
                call.respond(HttpStatusCode.Unauthorized)
                return@post
            }

            val reportId = parsePositiveId(call.parameters["id"], "Report ID")

            // Refuse obviously too large requests before reading them.
            val contentLength = call.request.contentLength()

            if (contentLength != null &&
                contentLength > PhotoRules.MAX_PHOTO_BYTES + MULTIPART_OVERHEAD_BYTES
            ) {
                call.respond(HttpStatusCode.PayloadTooLarge, ApiError(messageFor(PhotoRejection.TOO_LARGE)))
                return@post
            }

            var bytes: ByteArray? = null
            var latitude: String? = null
            var longitude: String? = null

            call.receiveMultipart().forEachPart { part ->
                when (part) {
                    is PartData.FormItem -> when (part.name) {
                        "latitude" -> latitude = part.value
                        "longitude" -> longitude = part.value
                    }

                    is PartData.FileItem -> if (part.name == "photo") {
                        // Read at most one byte more than allowed: enough to
                        // know the photo is too large, without reading it all.
                        bytes = part.provider()
                            .readBuffer(PhotoRules.MAX_PHOTO_BYTES + 1)
                            .readByteArray()
                    }

                    else -> Unit
                }

                part.release()
            }

            val photoBytes = bytes

            require(photoBytes != null && photoBytes.isNotEmpty()) {
                "The file field 'photo' is required."
            }

            val result = service.upload(
                reportId = reportId,
                uploaderId = userId,
                bytes = photoBytes,
                appLocation = parseLocation(latitude, longitude)
            )

            when (result) {
                is PhotoUploadResult.Saved ->
                    call.respond(HttpStatusCode.Created, result.photo.toResponse())

                is PhotoUploadResult.Rejected ->
                    call.respond(statusFor(result.reason), ApiError(messageFor(result.reason)))
            }
        }

        delete("/photos/{photoId}") {
            val principal = call.principal<JWTPrincipal>()
            val userId = principal?.payload?.subject?.toLongOrNull()

            if (userId == null || userId <= 0) {
                call.respond(HttpStatusCode.Unauthorized)
                return@delete
            }

            val photoId = parsePositiveId(call.parameters["photoId"], "Photo ID")
            val isAdmin = principal.payload.getClaim("role").asString() == "ADMIN"

            when (service.delete(photoId, actingUserId = userId, isAdmin = isAdmin)) {
                PhotoDeleteResult.DELETED -> call.respond(HttpStatusCode.NoContent)
                PhotoDeleteResult.NOT_FOUND -> call.respond(HttpStatusCode.NotFound)
                PhotoDeleteResult.FORBIDDEN -> call.respond(HttpStatusCode.Forbidden)
            }
        }
    }
}

/** Welke HTTP-status hoort bij welke afwijzing. */
private fun statusFor(reason: PhotoRejection): HttpStatusCode = when (reason) {
    PhotoRejection.REPORT_NOT_FOUND -> HttpStatusCode.NotFound
    PhotoRejection.NOT_OWNER -> HttpStatusCode.Forbidden
    PhotoRejection.TOO_LARGE -> HttpStatusCode.PayloadTooLarge
    PhotoRejection.UNSUPPORTED_TYPE -> HttpStatusCode.UnsupportedMediaType
    PhotoRejection.PHOTO_LIMIT -> HttpStatusCode.Conflict
}

private fun messageFor(reason: PhotoRejection): String = when (reason) {
    PhotoRejection.REPORT_NOT_FOUND -> "Report not found."
    PhotoRejection.NOT_OWNER -> "Only the owner of the report can add photos."
    PhotoRejection.TOO_LARGE -> "A photo can be at most ${PhotoRules.MAX_PHOTO_BYTES / (1024 * 1024)} MB."
    PhotoRejection.UNSUPPORTED_TYPE -> "Only JPEG and PNG photos are accepted."
    PhotoRejection.PHOTO_LIMIT -> "A report can have at most ${PhotoRules.MAX_PHOTOS_PER_REPORT} photos."
}

/**
 * Maakt een Location van de optionele velden `latitude` en `longitude`.
 * Allebei of geen van beide; geen geldig getal of buiten bereik geeft 400.
 */
private fun parseLocation(latitude: String?, longitude: String?): Location? {
    if (latitude.isNullOrBlank() && longitude.isNullOrBlank()) {
        return null
    }

    val lat = latitude?.trim()?.toDoubleOrNull()
    val lon = longitude?.trim()?.toDoubleOrNull()

    require(lat != null && lon != null) {
        "Latitude and longitude must be valid numbers and given together."
    }

    return Location(lat, lon)
}

/** Zet een id uit de URL om naar een getal; geen geldig positief getal geeft 400. */
private fun parsePositiveId(value: String?, name: String): Long {
    val id = value?.toLongOrNull()

    require(id != null && id > 0) {
        "$name must be a positive number."
    }

    return id
}
