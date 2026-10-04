package nl.brul.routing

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
    }
}