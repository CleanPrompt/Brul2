package nl.brul.service

import nl.brul.domain.HelpRequest
import nl.brul.domain.Location
import nl.brul.domain.NuisanceReport
import nl.brul.domain.NuisanceType
import nl.brul.domain.Report
import nl.brul.domain.Role
import nl.brul.domain.SafetyReport
import nl.brul.domain.Urgency
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

    fun createSafetyReport(
        title: String,
        description: String?,
        photo: String?,
        location: Location,
        urgency: Urgency,
        currentUser: User
    ): SafetyReport {
        val report = SafetyReport(
            title = title,
            description = description,
            photo = photo,
            location = location,
            createdBy = currentUser,
            urgency = urgency
        )

        reportRepository.save(report)

        return report
    }

    fun createNuisanceReport(
        title: String,
        description: String?,
        photo: String?,
        location: Location,
        nuisanceType: NuisanceType,
        currentUser: User
    ): NuisanceReport {
        val report = NuisanceReport(
            title = title,
            description = description,
            photo = photo,
            location = location,
            createdBy = currentUser,
            nuisanceType = nuisanceType
        )

        reportRepository.save(report)

        return report
    }

    fun createHelpRequest(
        title: String,
        description: String?,
        photo: String?,
        location: Location,
        currentUser: User
    ): HelpRequest {
        val report = HelpRequest(
            title = title,
            description = description,
            photo = photo,
            location = location,
            createdBy = currentUser
        )

        reportRepository.save(report)

        return report
    }

    fun updateReport(
        reportId: String,
        title: String,
        description: String?,
        photo: String?,
        location: Location,
        currentUser: User
    ): Report? {
        val report = reportRepository.findById(reportId)
            ?: return null

        if (!canManageReport(report, currentUser)) {
            return null
        }

        report.update(
            title = title,
            description = description,
            photo = photo,
            location = location
        )

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