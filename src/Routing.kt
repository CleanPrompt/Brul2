package org.jetbrains.amper.ktor

import io.ktor.server.application.*
import io.ktor.server.routing.*
import nl.brul.persistence.InMemoryReportRepository
import nl.brul.routing.reportRoutes
import nl.brul.service.ReportService

fun Application.configureRouting() {

    val reportRepository = InMemoryReportRepository()
    val reportService = ReportService(reportRepository)

    routing {
        reportRoutes(reportService)
    }
}