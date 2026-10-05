package nl.brul.service

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import nl.brul.domain.HelpRequest
import nl.brul.domain.Location
import nl.brul.domain.NuisanceReport
import nl.brul.domain.NuisanceType
import nl.brul.domain.ReportStatus
import nl.brul.domain.Role
import nl.brul.domain.User
import nl.brul.persistence.InMemoryReportRepository

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
            assertFailsWith<ConflictException> {
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
            result.claimedBy
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
            assertFailsWith<ForbiddenException> {
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
}