// src/nl/brul/api/plugins/Routing.kt
package nl.brul.api.plugins

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import kotlinx.serialization.Serializable
import java.nio.file.Path
import java.time.Clock
import nl.brul.api.config.AppConfig
import nl.brul.api.database.ExposedTransactionRunner
import nl.brul.api.features.auth.routes.authRoutes
import nl.brul.api.features.auth.service.AuthService
import nl.brul.api.features.help.repository.ExposedHelpParticipationRepository
import nl.brul.api.features.help.routes.helpParticipationRoutes
import nl.brul.api.features.help.routes.volunteerRoutes
import nl.brul.api.features.help.service.HelpParticipationService
import nl.brul.api.features.report.repository.ExposedReportRepository
import nl.brul.api.features.report.repository.ExposedReportPhotoRepository
import nl.brul.api.features.report.repository.FileSystemPhotoStorage
import nl.brul.api.features.report.repository.PhotoStorage
import nl.brul.api.features.report.routes.reportPhotoRoutes
import nl.brul.api.features.report.routes.reportRoutes
import nl.brul.api.features.report.service.ReportPhotoService
import nl.brul.api.features.report.service.ReportService
import nl.brul.api.features.user.repository.ExposedUserRepository
import nl.brul.api.features.user.routes.userRoutes
import nl.brul.api.features.user.service.UserService
import nl.brul.api.features.vote.repository.ExposedVoteRepository
import nl.brul.api.features.vote.routes.voteRoutes
import nl.brul.api.features.vote.service.VoteService
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
    jwtService: JwtService,
    clock: Clock = Clock.systemUTC(),
    photoStorage: PhotoStorage = FileSystemPhotoStorage(Path.of(config.uploadsDir))
) {
    val transactions = ExposedTransactionRunner(database)

    val reportRepository = ExposedReportRepository(database)
    val voteRepository = ExposedVoteRepository(database)
    val helpParticipationRepository =
        ExposedHelpParticipationRepository(database)
    val photoRepository = ExposedReportPhotoRepository(database)

    val helpParticipationService = HelpParticipationService(
        participationRepository = helpParticipationRepository,
        reportRepository = reportRepository,
        clock = clock,
        transactions = transactions
    )

    val photoService = ReportPhotoService(
        reportRepository = reportRepository,
        photoRepository = photoRepository,
        storage = photoStorage,
        participationRepository = helpParticipationRepository,
        clock = clock
    )

    val reportService = ReportService(
        repository = reportRepository,
        voteRepository = voteRepository,
        helpParticipationRepository = helpParticipationRepository,
        clock = clock,
        participationService = helpParticipationService,
        photoService = photoService
    )

    val voteService = VoteService(
        voteRepository = voteRepository,
        reportRepository = reportRepository
    )

    val userRepository = ExposedUserRepository(database)
    val passwordHasher = BCryptPasswordHasher()

    val authService = AuthService(
        userRepository = userRepository,
        passwordHasher = passwordHasher
    )

    val userService = UserService(userRepository)

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

            reportRoutes(reportService, photoService)

            reportPhotoRoutes(photoService)

            voteRoutes(
                voteService = voteService,
                reportService = reportService
            )

            helpParticipationRoutes(helpParticipationService)

            volunteerRoutes(helpParticipationService)

            authRoutes(
                authService = authService,
                jwtService = jwtService,
                userRepository = userRepository
            )

            userRoutes(userService)
        }
    }
}