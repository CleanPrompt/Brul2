package nl.brul.routing

import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import nl.brul.dto.toResponse
import nl.brul.service.ReportService

fun Route.reportRoutes(
    reportService: ReportService
) {
    route("/reports") {

        get {
            val reports = reportService.getReports()

            val response = reports.map { report ->
                report.toResponse()
            }

            call.respond(response)
        }

        get("/{id}") {
            val reportId = call.parameters["id"]

            if (reportId == null) {
                call.respond(HttpStatusCode.BadRequest)
                return@get
            }

            val report = reportService.getReport(reportId)

            if (report == null) {
                call.respond(HttpStatusCode.NotFound)
                return@get
            }

            call.respond(
                HttpStatusCode.OK,
                report.toResponse()
            )
        }
    }
}