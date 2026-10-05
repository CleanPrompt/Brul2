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
                parseEnumParameter<ReportType>(
                    name = "type",
                    value = call.request.queryParameters["type"]
                )

            val status =
                parseEnumParameter<ReportStatus>(
                    name = "status",
                    value = call.request.queryParameters["status"]
                )

            val urgency =
                parseEnumParameter<Urgency>(
                    name = "urgency",
                    value = call.request.queryParameters["urgency"]
                )

            val nuisanceType =
                parseEnumParameter<NuisanceType>(
                    name = "nuisanceType",
                    value = call.request.queryParameters["nuisanceType"]
                )

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
                status = HttpStatusCode.OK,
                message = response
            )
        }

        get("/{id}") {

            val reportId =
                requireReportId(
                    call.parameters["id"]
                )

            val report =
                reportService.getReport(
                    reportId = reportId
                )

            call.respond(
                status = HttpStatusCode.OK,
                message = report.toResponse()
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
                status = HttpStatusCode.Created,
                message = report.toResponse()
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
                status = HttpStatusCode.Created,
                message = report.toResponse()
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
                status = HttpStatusCode.Created,
                message = report.toResponse()
            )
        }

        put("/{id}") {

            val reportId =
                requireReportId(
                    call.parameters["id"]
                )

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

            call.respond(
                status = HttpStatusCode.OK,
                message = report.toResponse()
            )
        }

        delete("/{id}") {

            val reportId =
                requireReportId(
                    call.parameters["id"]
                )

            val currentUser =
                temporaryCurrentUser()

            reportService.deleteReport(
                reportId = reportId,
                currentUser = currentUser
            )

            call.respond(
                HttpStatusCode.NoContent
            )
        }

        post("/{id}/claim") {

            val reportId =
                requireReportId(
                    call.parameters["id"]
                )

            val currentUser =
                temporaryCurrentUser()

            val report =
                reportService.claimHelpRequest(
                    reportId = reportId,
                    currentUser = currentUser
                )

            call.respond(
                status = HttpStatusCode.OK,
                message = report.toResponse()
            )
        }

        post("/{id}/resolve") {

            val reportId =
                requireReportId(
                    call.parameters["id"]
                )

            val currentUser =
                temporaryCurrentUser()

            val report =
                reportService.resolveReport(
                    reportId = reportId,
                    currentUser = currentUser
                )

            call.respond(
                status = HttpStatusCode.OK,
                message = report.toResponse()
            )
        }
    }
}

private fun requireReportId(
    reportId: String?
): String {

    require(
        !reportId.isNullOrBlank()
    ) {
        "Report id is required."
    }

    return reportId
}

private inline fun <reified T : Enum<T>> parseEnumParameter(
    name: String,
    value: String?
): T? {

    if (value.isNullOrBlank()) {
        return null
    }

    return enumValues<T>()
        .find { enumValue ->
            enumValue.name.equals(
                other = value,
                ignoreCase = true
            )
        }
        ?: throw IllegalArgumentException(
            "Invalid value '$value' for query parameter '$name'."
        )
}

private fun temporaryCurrentUser(): User {
    return User(
        id = "user-1",
        username = "testuser",
        role = Role.USER
    )
}