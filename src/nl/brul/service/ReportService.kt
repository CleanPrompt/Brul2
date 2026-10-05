package nl.brul.service

import nl.brul.domain.HelpRequest
import nl.brul.domain.Location
import nl.brul.domain.NuisanceReport
import nl.brul.domain.NuisanceType
import nl.brul.domain.Report
import nl.brul.domain.ReportStatus
import nl.brul.domain.ReportType
import nl.brul.domain.Role
import nl.brul.domain.SafetyReport
import nl.brul.domain.Urgency
import nl.brul.domain.User
import nl.brul.persistence.ReportRepository

class ReportService(
    private val reportRepository: ReportRepository
) {

    fun getReports(
        search: String? = null,
        type: ReportType? = null,
        status: ReportStatus? = null,
        urgency: Urgency? = null,
        nuisanceType: NuisanceType? = null
    ): List<Report> {

        return reportRepository
            .findAll()
            .asSequence()
            .filter { report ->
                matchesSearch(
                    report = report,
                    search = search
                )
            }
            .filter { report ->
                matchesType(
                    report = report,
                    type = type
                )
            }
            .filter { report ->
                status == null ||
                        report.status == status
            }
            .filter { report ->
                urgency == null ||
                        (
                                report is SafetyReport &&
                                        report.urgency == urgency
                                )
            }
            .filter { report ->
                nuisanceType == null ||
                        (
                                report is NuisanceReport &&
                                        report.nuisanceType == nuisanceType
                                )
            }
            .toList()
    }

    fun getReport(
        reportId: String
    ): Report {
        return findReportOrThrow(reportId)
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
    ): Report {

        val report =
            findReportOrThrow(reportId)

        requireCanManageReport(
            report = report,
            currentUser = currentUser
        )

        report.update(
            title = title,
            description = description,
            photo = photo,
            location = location
        )

        return reportRepository.update(report)
            ?: throw ReportNotFoundException(reportId)
    }

    fun deleteReport(
        reportId: String,
        currentUser: User
    ) {

        val report =
            findReportOrThrow(reportId)

        requireCanManageReport(
            report = report,
            currentUser = currentUser
        )

        val deleted =
            reportRepository.deleteById(reportId)

        if (!deleted) {
            throw ReportNotFoundException(reportId)
        }
    }

    fun claimHelpRequest(
        reportId: String,
        currentUser: User
    ): HelpRequest {

        val report =
            findReportOrThrow(reportId)

        if (report !is HelpRequest) {
            throw ConflictException(
                "Only a HelpRequest can be claimed."
            )
        }

        if (report.createdBy.id == currentUser.id) {
            throw ConflictException(
                "You cannot claim your own HelpRequest."
            )
        }

        try {
            report.claim(currentUser)
        } catch (exception: IllegalStateException) {
            throw ConflictException(
                exception.message
                    ?: "HelpRequest cannot be claimed."
            )
        }

        reportRepository.update(report)

        return report
    }

    fun resolveReport(
        reportId: String,
        currentUser: User
    ): Report {

        val report =
            findReportOrThrow(reportId)

        requireCanManageReport(
            report = report,
            currentUser = currentUser
        )

        try {
            report.resolve()
        } catch (exception: IllegalStateException) {
            throw ConflictException(
                exception.message
                    ?: "Report cannot be resolved."
            )
        }

        reportRepository.update(report)

        return report
    }

    private fun findReportOrThrow(
        reportId: String
    ): Report {

        return reportRepository.findById(reportId)
            ?: throw ReportNotFoundException(reportId)
    }

    private fun requireCanManageReport(
        report: Report,
        currentUser: User
    ) {

        val isOwner =
            report.createdBy.id == currentUser.id

        val isAdmin =
            currentUser.role == Role.ADMIN

        if (!isOwner && !isAdmin) {
            throw ForbiddenException(
                "You are not allowed to manage this report."
            )
        }
    }

    private fun matchesSearch(
        report: Report,
        search: String?
    ): Boolean {

        if (search.isNullOrBlank()) {
            return true
        }

        val searchTerm =
            search.trim()

        val titleMatches =
            report.title.contains(
                other = searchTerm,
                ignoreCase = true
            )

        val descriptionMatches =
            report.description?.contains(
                other = searchTerm,
                ignoreCase = true
            ) ?: false

        return titleMatches ||
                descriptionMatches
    }

    private fun matchesType(
        report: Report,
        type: ReportType?
    ): Boolean {

        if (type == null) {
            return true
        }

        return when (type) {

            ReportType.SAFETY ->
                report is SafetyReport

            ReportType.NUISANCE ->
                report is NuisanceReport

            ReportType.HELP ->
                report is HelpRequest
        }
    }
}