// src/nl/brul/api/features/report/routes/ReportRoutes.kt
package nl.brul.api.features.report.routes

import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.route
import nl.brul.api.features.report.dto.CreateReportRequest
import nl.brul.api.features.report.dto.UpdateReportRequest
import nl.brul.api.features.report.dto.toNewReport
import nl.brul.api.features.report.dto.toReport
import nl.brul.api.features.report.dto.toResponse
import nl.brul.api.features.report.model.Location
import nl.brul.api.features.report.model.ReportCategory
import nl.brul.api.features.report.model.ReportType
import nl.brul.api.features.report.service.ReportSearch
import nl.brul.api.features.report.service.ReportService
import nl.brul.api.features.report.service.ReportSort

fun Route.reportRoutes(service: ReportService) {

    route("/reports") {

        get {
            val parameters = call.request.queryParameters

            val latitude = parameters["latitude"]?.toDoubleOrNull()
            val longitude = parameters["longitude"]?.toDoubleOrNull()

            require(
                (parameters["latitude"] == null || latitude != null) &&
                        (parameters["longitude"] == null || longitude != null)
            ) {
                "Latitude and longitude must be valid numbers."
            }

            require((latitude == null) == (longitude == null)) {
                "Latitude and longitude must be provided together."
            }

            val location = if (latitude != null && longitude != null) {
                Location(latitude, longitude)
            } else {
                null
            }

            val radiusKm = parameters["radiusKm"]?.let { value ->
                value.toDoubleOrNull()
                    ?: throw IllegalArgumentException(
                        "Search radius must be a valid number."
                    )
            }

            val type = parameters["type"]?.let { value ->
                parseEnum<ReportType>(value, "report type")
            }

            val category = parameters["category"]?.let { value ->
                parseEnum<ReportCategory>(value, "report category")
            }

            val sort = parameters["sort"]?.let { value ->
                parseEnum<ReportSort>(value, "sort option")
            } ?: ReportSort.ID_ASC

            val search = ReportSearch(
                query = parameters["query"],
                type = type,
                category = category,
                location = location,
                radiusKm = radiusKm,
                sort = sort
            )

            val reports = service.search(search).map { it.toResponse() }

            call.respond(HttpStatusCode.OK, reports)
        }

        get("/{id}") {
            val id = parseReportId(call.parameters["id"])

            val report = service.findById(id)

            if (report == null) {
                call.respond(HttpStatusCode.NotFound)
                return@get
            }

            call.respond(HttpStatusCode.OK, report.toResponse())
        }

        post {
            val request = call.receive<CreateReportRequest>()
            val report = service.create(request.toNewReport())

            call.respond(HttpStatusCode.Created, report.toResponse())
        }

        put("/{id}") {
            val id = parseReportId(call.parameters["id"])

            val existing = service.findById(id)

            if (existing == null) {
                call.respond(HttpStatusCode.NotFound)
                return@put
            }

            val request = call.receive<UpdateReportRequest>()
            val updatedReport = request.toReport(existing)

            val updated = service.update(updatedReport)

            if (!updated) {
                call.respond(HttpStatusCode.NotFound)
                return@put
            }

            call.respond(
                HttpStatusCode.OK,
                requireNotNull(service.findById(id)).toResponse()
            )
        }

        delete("/{id}") {
            val id = parseReportId(call.parameters["id"])

            val deleted = service.delete(id)

            if (!deleted) {
                call.respond(HttpStatusCode.NotFound)
                return@delete
            }

            call.respond(HttpStatusCode.NoContent)
        }
    }
}

private fun parseReportId(value: String?): Long {
    val id = value?.toLongOrNull()

    require(id != null && id > 0) {
        "Report ID must be a positive number."
    }

    return id
}

private inline fun <reified T : Enum<T>> parseEnum(
    value: String,
    fieldName: String
): T {
    return enumValues<T>().firstOrNull { it.name == value }
        ?: throw IllegalArgumentException("Invalid $fieldName: $value.")
}