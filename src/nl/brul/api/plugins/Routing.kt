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
import nl.brul.api.features.auth.routes.authRoutes
import nl.brul.api.features.auth.service.AuthService
import nl.brul.api.features.report.repository.ExposedReportRepository
import nl.brul.api.features.report.routes.reportRoutes
import nl.brul.api.features.report.service.ReportService
import nl.brul.api.features.user.repository.ExposedUserRepository
import nl.brul.api.security.BCryptPasswordHasher
import nl.brul.api.security.JwtService
import org.jetbrains.exposed.sql.Database

@Serializable
data class HealthResponse(
    val status: String,
    val service: String
)

fun Application.configureRouting(
    config: AppConfig,
    database: Database,
    jwtService: JwtService
) {
    val reportRepository = ExposedReportRepository(database)
    val reportService = ReportService(reportRepository)

    val userRepository = ExposedUserRepository(database)
    val passwordHasher = BCryptPasswordHasher()

    val authService = AuthService(
        userRepository = userRepository,
        passwordHasher = passwordHasher
    )

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

            authRoutes(
                authService = authService,
                jwtService = jwtService,
                userRepository = userRepository
            )
        }
    }
}