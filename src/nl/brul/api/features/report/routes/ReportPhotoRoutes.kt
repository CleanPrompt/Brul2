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
import io.ktor.server.routing.route
import io.ktor.utils.io.readBuffer
import kotlinx.io.readByteArray
import nl.brul.api.common.error.PayloadTooLargeException
import nl.brul.api.features.report.dto.toResponse
import nl.brul.api.features.report.model.PhotoKind
import nl.brul.api.features.report.service.PhotoPolicy
import nl.brul.api.features.report.service.ReportPhotoService

/** Ruimte voor de multipart-scheidingen en het veld "kind" naast de foto. */
private const val MULTIPART_OVERHEAD_BYTES = 64L * 1024

/**
 * Laag: routes (de voordeur).
 *
 * Foto's bij meldingen; in Routing.kt komt /api/v1 voor de paden.
 *
 * - GET /reports/{id}/photos: de gegevens van alle foto's bij een melding, openbaar
 * - GET /photos/{photoId}: de afbeelding zelf (JPEG of PNG), openbaar
 * - POST /reports/{id}/photos: een foto plaatsen, JWT nodig; multipart met de velden `kind`
 *   (SITUATION of RESULT) en `photo` (het bestand)
 * - DELETE /photos/{photoId}: verwijderen, door de uploader of een ADMIN (F7)
 *
 * Lezen is openbaar, net als de meldingen zelf (zo afgesproken in het ontwerp).
 * F2 bij het uploaden: een te groot verzoek wordt al op de Content-Length geweigerd, en van de
 * foto leest de route nooit meer dan 5 MB + 1 byte. Zo kan niemand de server vol laten lopen.
 */
fun Route.reportPhotoRoutes(service: ReportPhotoService) {

    get("/reports/{id}/photos") {
        val reportId = parsePositiveId(call.parameters["id"], "Report ID")

        call.respond(
            HttpStatusCode.OK,
            service.list(reportId).map { it.toResponse() }
        )
    }

    route("/photos/{photoId}") {
        get {
            val photoId = parsePositiveId(call.parameters["photoId"], "Photo ID")
            val (photo, bytes) = service.load(photoId)

            call.respondBytes(
                bytes = bytes,
                contentType = ContentType.parse(photo.contentType),
                status = HttpStatusCode.OK
            )
        }
    }

    authenticate("auth-jwt") {

        /*
         * Multipart upload with two fields:
         *   kind  = SITUATION or RESULT
         *   photo = the image file (JPEG or PNG, at most 5 MB)
         */
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

            // F2: refuse obviously too large requests before reading them.
            val contentLength = call.request.contentLength()

            if (contentLength != null &&
                contentLength > PhotoPolicy.MAX_PHOTO_BYTES + MULTIPART_OVERHEAD_BYTES
            ) {
                throw photoTooLarge()
            }

            var kind: PhotoKind? = null
            var bytes: ByteArray? = null

            call.receiveMultipart().forEachPart { part ->
                when (part) {
                    is PartData.FormItem -> if (part.name == "kind") {
                        kind = PhotoKind.entries.firstOrNull { it.name == part.value }
                            ?: throw IllegalArgumentException("Invalid photo kind.")
                    }

                    is PartData.FileItem -> if (part.name == "photo") {
                        // Read at most one byte more than allowed: enough to
                        // know the photo is too large, without reading it all.
                        bytes = part.provider()
                            .readBuffer(PhotoPolicy.MAX_PHOTO_BYTES + 1)
                            .readByteArray()
                    }

                    else -> Unit
                }

                part.release()
            }

            val photoKind = requireNotNull(kind) {
                "The field 'kind' is required."
            }

            val photoBytes = bytes

            require(photoBytes != null && photoBytes.isNotEmpty()) {
                "The file field 'photo' is required."
            }

            val photo = service.upload(
                reportId = reportId,
                uploaderId = userId,
                kind = photoKind,
                bytes = photoBytes
            )

            call.respond(HttpStatusCode.Created, photo.toResponse())
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

            service.delete(photoId, actingUserId = userId, isAdmin = isAdmin)

            call.respond(HttpStatusCode.NoContent)
        }
    }
}

/** De fout voor F2: 413 Payload Too Large met code TOO_LARGE. */
private fun photoTooLarge() = PayloadTooLargeException(
    code = "TOO_LARGE",
    message = "A photo can be at most ${PhotoPolicy.MAX_PHOTO_BYTES / (1024 * 1024)} MB."
)

/** Zet een id uit de URL om naar een getal; geen geldig positief getal geeft 400. */
private fun parsePositiveId(value: String?, name: String): Long {
    val id = value?.toLongOrNull()

    require(id != null && id > 0) {
        "$name must be a positive number."
    }

    return id
}
