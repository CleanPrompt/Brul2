// src/nl/brul/api/features/report/service/ReportService.kt
package nl.brul.api.features.report.service

import nl.brul.api.features.report.model.Report
import nl.brul.api.features.report.repository.NewReport
import nl.brul.api.features.report.repository.ReportRepository

class ReportService(
    private val repository: ReportRepository
) {

    fun create(report: NewReport): Report {
        return repository.create(report)
    }

    fun findAll(): List<Report> {
        return repository.findAll()
    }

    fun findById(id: Long): Report? {
        require(id > 0) {
            "Report ID must be positive."
        }

        return repository.findById(id)
    }

    fun update(report: Report): Boolean {
        return repository.update(report)
    }

    fun delete(id: Long): Boolean {
        require(id > 0) {
            "Report ID must be positive."
        }

        return repository.delete(id)
    }
}