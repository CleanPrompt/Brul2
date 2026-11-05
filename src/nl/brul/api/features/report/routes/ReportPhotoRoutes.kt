// src/nl/brul/api/features/report/routes/ReportPhotoRoutes.kt
package nl.brul.api.features.report.routes

import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.PartData
import io.ktor.http.content.forEachPart
import io.ktor.server.application.ApplicationCall
import io.ktor.server.auth.authenticate
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
import nl.brul.api.common.error.AppException
import nl.brul.api.common.http.currentUserId
import nl.brul.api.common.http.idParameter
import nl.brul.api.features.report.mapper.appLocationOf
import nl.brul.api.features.report.model.Location
import nl.brul.api.features.report.service.PhotoAccessService
import nl.brul.api.features.report.service.PhotoRules
import nl.brul.api.features.report.service.ReportPhotoService

/** Room for the multipart boundaries and the form fields next to the photo. */
private const val MULTIPART_OVERHEAD_BYTES = 64L * 1024

/**
 * Photos of reports, under `/api/v1`.
 *
 * Viewing is public, like the reports themselves (decided in `docs/02-ontwerp-hulpvraag-reservering-en-fotos.md`).
 * Adding and deleting need a JWT; each of those routes starts with one access check from [PhotoAccessService].
 * To know the owner, the route first reads the report or photo, so an unknown id gives 404 before the check.
 */
fun Route.reportPhotoRoutes(photoService: ReportPhotoService, accessService: PhotoAccessService) {

    get("/reports/{id}/photos") {
        call.respond(photoService.listByReport(call.idParameter("id")))
    }

    get("/photos/{photoId}") {
        val image = photoService.getImage(call.idParameter("photoId"))
        call.respondBytes(image.bytes, ContentType.parse(image.contentType))
    }

    authenticate("auth-jwt") {

        // Multipart with the file in `photo` and, optionally, `latitude` and `longitude` from the app.
        post("/reports/{id}/photos") {
            val currentUserId = call.currentUserId()
            val reportId = call.idParameter("id")
            accessService.requireReportOwner(currentUserId, photoService.getReportOwnerId(reportId))

            val upload = call.receivePhotoUpload()
            val photo = photoService.upload(reportId, currentUserId, upload.bytes, upload.appLocation)
            call.respond(HttpStatusCode.Created, photo)
        }

        delete("/photos/{photoId}") {
            val photoId = call.idParameter("photoId")
            accessService.requireUploaderOrAdmin(call.currentUserId(), photoService.getUploaderId(photoId))

            photoService.delete(photoId)
            call.respond(HttpStatusCode.NoContent)
        }
    }
}

/** What an upload form contains. */
private class PhotoUpload(
    val bytes: ByteArray,
    val appLocation: Location?
)

/**
 * Reads the upload form. A request that is clearly too large is refused before it is read, and of the photo
 * the server never reads more than 5 MB + 1 byte: enough to know it is too large, so nobody can fill the memory.
 */
private suspend fun ApplicationCall.receivePhotoUpload(): PhotoUpload {
    val contentLength = request.contentLength()

    if (contentLength != null && contentLength > PhotoRules.MAX_PHOTO_BYTES + MULTIPART_OVERHEAD_BYTES) {
        throw PhotoRules.tooLarge()
    }

    var bytes: ByteArray? = null
    var latitude: String? = null
    var longitude: String? = null

    receiveMultipart().forEachPart { part ->
        when (part) {
            is PartData.FormItem -> when (part.name) {
                "latitude" -> latitude = part.value
                "longitude" -> longitude = part.value
            }

            is PartData.FileItem -> if (part.name == "photo") {
                bytes = part.provider().readBuffer(PhotoRules.MAX_PHOTO_BYTES + 1).readByteArray()
            }

            else -> Unit
        }

        part.release()
    }

    val photoBytes = bytes

    if (photoBytes == null || photoBytes.isEmpty()) {
        throw AppException.BadRequest("The file field 'photo' is required", "PHOTO_REQUIRED")
    }

    return PhotoUpload(photoBytes, appLocationOf(latitude, longitude))
}
