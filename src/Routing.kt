package org.jetbrains.amper.ktor

import io.ktor.server.application.*
import io.ktor.server.routing.*
import nl.brul.persistence.InMemoryReportRepository
import nl.brul.persistence.ReportSeedData
import nl.brul.routing.reportRoutes
import nl.brul.service.ReportService

fun Application.configureRouting(seedData: Boolean = false) {

    val initialReports =
        if (seedData) {
            ReportSeedData.reports()
        } else {
            emptyList()
        }

    val reportRepository = InMemoryReportRepository(initialReports)
    val reportService = ReportService(reportRepository)

    routing {
        reportRoutes(reportService)
    }
}
