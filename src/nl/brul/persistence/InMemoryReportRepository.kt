package nl.brul.persistence

import nl.brul.domain.Report

class InMemoryReportRepository(
    initialReports: List<Report> = emptyList()
) : InMemoryRepository<Report, String>(
    idOf = { report -> report.id }
), ReportRepository {

    init {
        initialReports.forEach { report -> save(report) }
    }
}
