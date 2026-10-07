package nl.brul.service

import nl.brul.domain.Claimable
import nl.brul.domain.HelpRequest
import nl.brul.domain.Location
import nl.brul.domain.NuisanceReport
import nl.brul.domain.NuisanceType
import nl.brul.domain.Report
import nl.brul.domain.ReportFilter
import nl.brul.domain.ReportStatus
import nl.brul.domain.ReportType
import nl.brul.domain.SafetyReport
import nl.brul.domain.Urgency
import nl.brul.domain.User
import nl.brul.domain.filterBy
import nl.brul.persistence.ReportRepository

/**
 * Use cases for reports.
 *
 * The service coordinates: it loads a report, lets the domain object apply
 * its business rules, and stores the result. The rules themselves (who may
 * change, resolve or claim a report, and in which status) live in the domain.
 */
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

        val criteria = ReportFilter(
            search = search,
            type = type,
            status = status,
            urgency = urgency,
            nuisanceType = nuisanceType
        )

        return reportRepository
            .findAll()
            .filterBy(criteria)
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

        report.update(
            title = title,
            description = description,
            photo = photo,
            location = location,
            updatedBy = currentUser
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

        report.checkCanBeManagedBy(currentUser)

        val deleted =
            reportRepository.deleteById(reportId)

        if (!deleted) {
            throw ReportNotFoundException(reportId)
        }
    }

    fun claimHelpRequest(
        reportId: String,
        currentUser: User
    ): Report {

        val report =
            findReportOrThrow(reportId)

        if (report !is Claimable) {
            throw ConflictException(
                "Only a HelpRequest can be claimed."
            )
        }

        report.claim(currentUser)

        reportRepository.update(report)

        return report
    }

    fun resolveReport(
        reportId: String,
        currentUser: User
    ): Report {

        val report =
            findReportOrThrow(reportId)

        report.resolve(resolvedBy = currentUser)

        reportRepository.update(report)

        return report
    }

    private fun findReportOrThrow(
        reportId: String
    ): Report {

        return reportRepository.findById(reportId)
            ?: throw ReportNotFoundException(reportId)
    }
}
