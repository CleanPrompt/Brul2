package nl.brul.routing

import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import nl.brul.domain.Location
import nl.brul.domain.NuisanceType
import nl.brul.domain.ReportStatus
import nl.brul.domain.ReportType
import nl.brul.domain.Role
import nl.brul.domain.Urgency
import nl.brul.domain.User
import nl.brul.dto.request.CreateHelpRequestRequest
import nl.brul.dto.request.CreateNuisanceReportRequest
import nl.brul.dto.request.CreateSafetyReportRequest
import nl.brul.dto.request.UpdateReportRequest
import nl.brul.dto.toResponse
import nl.brul.service.ReportService

fun Route.reportRoutes(
    reportService: ReportService
) {
    route("/reports") {

        get {
            val search =
                call.request.queryParameters["search"]

            val type =
                call.request.queryParameters["type"]
                    ?.uppercase()
                    ?.let { value ->
                        ReportType.entries.find { reportType ->
                            reportType.name == value
                        }
                    }

            val status =
                call.request.queryParameters["status"]
                    ?.uppercase()
                    ?.let { value ->
                        ReportStatus.entries.find { reportStatus ->
                            reportStatus.name == value
                        }
                    }

            val urgency =
                call.request.queryParameters["urgency"]
                    ?.uppercase()
                    ?.let { value ->
                        Urgency.entries.find { reportUrgency ->
                            reportUrgency.name == value
                        }
                    }

            val nuisanceType =
                call.request.queryParameters["nuisanceType"]
                    ?.uppercase()
                    ?.let { value ->
                        NuisanceType.entries.find { type ->
                            type.name == value
                        }
                    }

            val reports =
                reportService.getReports(
                    search = search,
                    type = type,
                    status = status,
                    urgency = urgency,
                    nuisanceType = nuisanceType
                )

            val response =
                reports.map { report ->
                    report.toResponse()
                }

            call.respond(
                HttpStatusCode.OK,
                response
            )
        }

        get("/{id}") {
            val reportId =
                call.parameters["id"]

            if (reportId == null) {
                call.respond(
                    HttpStatusCode.BadRequest
                )
                return@get
            }

            val report =
                reportService.getReport(reportId)

            if (report == null) {
                call.respond(
                    HttpStatusCode.NotFound
                )
                return@get
            }

            call.respond(
                HttpStatusCode.OK,
                report.toResponse()
            )
        }

        post("/safety") {
            val request =
                call.receive<CreateSafetyReportRequest>()

            val currentUser =
                temporaryCurrentUser()

            val report =
                reportService.createSafetyReport(
                    title = request.title,
                    description = request.description,
                    photo = request.photo,
                    location = Location(
                        latitude = request.latitude,
                        longitude = request.longitude
                    ),
                    urgency = request.urgency,
                    currentUser = currentUser
                )

            call.respond(
                HttpStatusCode.Created,
                report.toResponse()
            )
        }

        post("/nuisance") {
            val request =
                call.receive<CreateNuisanceReportRequest>()

            val currentUser =
                temporaryCurrentUser()

            val report =
                reportService.createNuisanceReport(
                    title = request.title,
                    description = request.description,
                    photo = request.photo,
                    location = Location(
                        latitude = request.latitude,
                        longitude = request.longitude
                    ),
                    nuisanceType = request.nuisanceType,
                    currentUser = currentUser
                )

            call.respond(
                HttpStatusCode.Created,
                report.toResponse()
            )
        }

        post("/help") {
            val request =
                call.receive<CreateHelpRequestRequest>()

            val currentUser =
                temporaryCurrentUser()

            val report =
                reportService.createHelpRequest(
                    title = request.title,
                    description = request.description,
                    photo = request.photo,
                    location = Location(
                        latitude = request.latitude,
                        longitude = request.longitude
                    ),
                    currentUser = currentUser
                )

            call.respond(
                HttpStatusCode.Created,
                report.toResponse()
            )
        }

        put("/{id}") {
            val reportId =
                call.parameters["id"]

            if (reportId == null) {
                call.respond(
                    HttpStatusCode.BadRequest
                )
                return@put
            }

            val request =
                call.receive<UpdateReportRequest>()

            val currentUser =
                temporaryCurrentUser()

            val report =
                reportService.updateReport(
                    reportId = reportId,
                    title = request.title,
                    description = request.description,
                    photo = request.photo,
                    location = Location(
                        latitude = request.latitude,
                        longitude = request.longitude
                    ),
                    currentUser = currentUser
                )

            if (report == null) {
                call.respond(
                    HttpStatusCode.NotFound
                )
                return@put
            }

            call.respond(
                HttpStatusCode.OK,
                report.toResponse()
            )
        }

        delete("/{id}") {
            val reportId =
                call.parameters["id"]

            if (reportId == null) {
                call.respond(
                    HttpStatusCode.BadRequest
                )
                return@delete
            }

            val currentUser =
                temporaryCurrentUser()

            val deleted =
                reportService.deleteReport(
                    reportId = reportId,
                    currentUser = currentUser
                )

            if (!deleted) {
                call.respond(
                    HttpStatusCode.NotFound
                )
                return@delete
            }

            call.respond(
                HttpStatusCode.NoContent
            )
        }

        post("/{id}/claim") {
            val reportId =
                call.parameters["id"]

            if (reportId == null) {
                call.respond(
                    HttpStatusCode.BadRequest
                )
                return@post
            }

            val currentUser =
                temporaryCurrentUser()

            val claimed =
                reportService.claimHelpRequest(
                    reportId = reportId,
                    currentUser = currentUser
                )

            if (!claimed) {
                call.respond(
                    HttpStatusCode.Conflict
                )
                return@post
            }

            val report =
                reportService.getReport(reportId)

            if (report == null) {
                call.respond(
                    HttpStatusCode.NotFound
                )
                return@post
            }

            call.respond(
                HttpStatusCode.OK,
                report.toResponse()
            )
        }

        post("/{id}/resolve") {
            val reportId =
                call.parameters["id"]

            if (reportId == null) {
                call.respond(
                    HttpStatusCode.BadRequest
                )
                return@post
            }

            val currentUser =
                temporaryCurrentUser()

            val resolved =
                reportService.resolveReport(
                    reportId = reportId,
                    currentUser = currentUser
                )

            if (!resolved) {
                call.respond(
                    HttpStatusCode.NotFound
                )
                return@post
            }

            val report =
                reportService.getReport(reportId)

            if (report == null) {
                call.respond(
                    HttpStatusCode.NotFound
                )
                return@post
            }

            call.respond(
                HttpStatusCode.OK,
                report.toResponse()
            )
        }
    }
}

private fun temporaryCurrentUser(): User {
    return User(
        id = "user-1",
        username = "testuser",
        role = Role.USER
    )
}