package nl.brul.persistence

import nl.brul.domain.Report

class InMemoryReportRepository : ReportRepository {

    private val reports = mutableListOf<Report>()

    override fun findAll(): List<Report> {
        return reports.toList()
    }

    override fun findById(id: String): Report? {
        return reports.find { report ->
            report.id == id
        }
    }

    override fun save(report: Report): Report {
        reports.add(report)

        return report
    }

    override fun update(report: Report): Report? {
        val index = reports.indexOfFirst { existingReport ->
            existingReport.id == report.id
        }

        if (index == -1) {
            return null
        }

        reports[index] = report

        return report
    }

    override fun deleteById(id: String): Boolean {
        return reports.removeIf { report ->
            report.id == id
        }
    }
}