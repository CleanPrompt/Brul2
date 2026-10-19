// src/nl/brul/api/plugins/Routing.kt
package nl.brul.api.plugins

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import kotlinx.serialization.Serializable
import nl.brul.api.config.AppConfig
import nl.brul.api.features.report.repository.ExposedReportRepository
import nl.brul.api.features.report.routes.reportRoutes
import nl.brul.api.features.report.service.ReportService
import org.jetbrains.exposed.sql.Database

@Serializable
data class HealthResponse(
    val status: String,
    val service: String
)

fun Application.configureRouting(
    config: AppConfig,
    database: Database
) {
    val reportRepository = ExposedReportRepository(database)
    val reportService = ReportService(reportRepository)

    routing {
        route("/api/v1") {

            get("/health") {
                call.respond(
                    HttpStatusCode.OK,
                    HealthResponse(
                        status = "ok",
                        service = config.name
                    )
                )
            }

            reportRoutes(reportService)
        }
    }
}