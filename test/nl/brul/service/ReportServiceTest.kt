package nl.brul.service

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue
import nl.brul.domain.HelpRequest
import nl.brul.domain.Location
import nl.brul.domain.NuisanceReport
import nl.brul.domain.NotAllowedException
import nl.brul.domain.NuisanceType
import nl.brul.domain.ReportStatus
import nl.brul.domain.ReportType
import nl.brul.domain.Role
import nl.brul.domain.SafetyReport
import nl.brul.domain.Urgency
import nl.brul.domain.User
import nl.brul.persistence.InMemoryReportRepository
import nl.brul.persistence.ReportSeedData

class ReportServiceTest {

    private val owner = User(
        id = "user-1",
        username = "owner",
        role = Role.USER
    )

    private val otherUser = User(
        id = "user-2",
        username = "otherUser",
        role = Role.USER
    )

    private val admin = User(
        id = "admin-1",
        username = "admin",
        role = Role.ADMIN
    )

    private val location = Location(
        latitude = 52.0705,
        longitude = 4.3007
    )

    @Test
    fun `user cannot claim own help request`() {
        val repository =
            InMemoryReportRepository()

        val service =
            ReportService(repository)

        val helpRequest = HelpRequest(
            title = "Need help",
            description = null,
            photo = null,
            location = location,
            createdBy = owner
        )

        repository.save(helpRequest)

        val exception =
            assertFailsWith<IllegalStateException> {
                service.claimHelpRequest(
                    reportId = helpRequest.id,
                    currentUser = owner
                )
            }

        assertEquals(
            "You cannot claim your own HelpRequest.",
            exception.message
        )

        assertEquals(
            ReportStatus.OPEN,
            helpRequest.status
        )

        assertNull(
            helpRequest.claimedBy
        )
    }

    @Test
    fun `another user can claim help request`() {
        val repository =
            InMemoryReportRepository()

        val service =
            ReportService(repository)

        val helpRequest = HelpRequest(
            title = "Need help",
            description = null,
            photo = null,
            location = location,
            createdBy = owner
        )

        repository.save(helpRequest)

        val result =
            service.claimHelpRequest(
                reportId = helpRequest.id,
                currentUser = otherUser
            )

        assertEquals(
            helpRequest.id,
            result.id
        )

        assertEquals(
            otherUser,
            (result as HelpRequest).claimedBy
        )

        assertEquals(
            ReportStatus.CLAIMED,
            result.status
        )
    }

    @Test
    fun `user cannot delete report of another user`() {
        val repository =
            InMemoryReportRepository()

        val service =
            ReportService(repository)

        val report = NuisanceReport(
            title = "Loud music",
            description = null,
            photo = null,
            location = location,
            createdBy = owner,
            nuisanceType = NuisanceType.NOISE
        )

        repository.save(report)

        val exception =
            assertFailsWith<NotAllowedException> {
                service.deleteReport(
                    reportId = report.id,
                    currentUser = otherUser
                )
            }

        assertEquals(
            "You are not allowed to manage this report.",
            exception.message
        )

        assertEquals(
            report,
            repository.findById(report.id)
        )
    }

    @Test
    fun `admin can delete report of another user`() {
        val repository =
            InMemoryReportRepository()

        val service =
            ReportService(repository)

        val report = NuisanceReport(
            title = "Loud music",
            description = null,
            photo = null,
            location = location,
            createdBy = owner,
            nuisanceType = NuisanceType.NOISE
        )

        repository.save(report)

        service.deleteReport(
            reportId = report.id,
            currentUser = admin
        )

        assertNull(
            repository.findById(report.id)
        )
    }

    @Test
    fun `claiming a safety report is a conflict`() {
        val repository =
            InMemoryReportRepository()

        val service =
            ReportService(repository)

        val report = SafetyReport(
            title = "Broken streetlight",
            description = null,
            photo = null,
            location = location,
            createdBy = owner,
            urgency = Urgency.MEDIUM
        )

        repository.save(report)

        assertFailsWith<ConflictException> {
            service.claimHelpRequest(
                reportId = report.id,
                currentUser = otherUser
            )
        }
    }

    @Test
    fun `helper can resolve claimed help request`() {
        val repository =
            InMemoryReportRepository()

        val service =
            ReportService(repository)

        val helpRequest = HelpRequest(
            title = "Need help",
            description = null,
            photo = null,
            location = location,
            createdBy = owner
        )

        repository.save(helpRequest)

        service.claimHelpRequest(
            reportId = helpRequest.id,
            currentUser = otherUser
        )

        val result =
            service.resolveReport(
                reportId = helpRequest.id,
                currentUser = otherUser
            )

        assertEquals(
            ReportStatus.RESOLVED,
            result.status
        )
    }

    @Test
    fun `unknown report is not found`() {
        val service =
            ReportService(InMemoryReportRepository())

        assertFailsWith<ReportNotFoundException> {
            service.getReport("does-not-exist")
        }
    }

    @Test
    fun `repository can start with seed data`() {
        val repository =
            InMemoryReportRepository(ReportSeedData.reports())

        val service =
            ReportService(repository)

        val reports =
            service.getReports()

        assertTrue(
            reports.size >= 3
        )

        assertEquals(
            ReportType.entries.toSet(),
            reports.map { report -> report.type }.toSet()
        )
    }
}
