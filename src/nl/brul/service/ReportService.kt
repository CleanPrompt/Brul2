package nl.brul.service

import nl.brul.domain.HelpRequest
import nl.brul.domain.Report
import nl.brul.domain.Role
import nl.brul.domain.User
import nl.brul.persistence.ReportRepository

class ReportService(
    private val reportRepository: ReportRepository
) {

    fun getReports(): List<Report> {
        return reportRepository.findAll()
    }

    fun getReport(reportId: String): Report? {
        return reportRepository.findById(reportId)
    }

    fun createReport(report: Report): Report {
        return reportRepository.save(report)
    }

    fun updateReport(
        report: Report,
        currentUser: User
    ): Report? {
        val existingReport = reportRepository.findById(report.id)
            ?: return null

        if (!canManageReport(existingReport, currentUser)) {
            return null
        }

        return reportRepository.update(report)
    }

    fun deleteReport(
        reportId: String,
        currentUser: User
    ): Boolean {
        val report = reportRepository.findById(reportId)
            ?: return false

        if (!canManageReport(report, currentUser)) {
            return false
        }

        return reportRepository.deleteById(reportId)
    }

    fun claimHelpRequest(
        reportId: String,
        currentUser: User
    ): Boolean {
        val report = reportRepository.findById(reportId)
            ?: return false

        if (report !is HelpRequest) {
            return false
        }

        if (report.createdBy.id == currentUser.id) {
            return false
        }

        report.claim(currentUser)

        return true
    }

    fun resolveReport(
        reportId: String,
        currentUser: User
    ): Boolean {
        val report = reportRepository.findById(reportId)
            ?: return false

        if (!canManageReport(report, currentUser)) {
            return false
        }

        report.resolve()

        return true
    }

    private fun canManageReport(
        report: Report,
        currentUser: User
    ): Boolean {
        val isOwner = report.createdBy.id == currentUser.id
        val isAdmin = currentUser.role == Role.ADMIN

        return isOwner || isAdmin
    }
}