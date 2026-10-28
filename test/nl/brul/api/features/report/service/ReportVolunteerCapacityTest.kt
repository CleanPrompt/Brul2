
// test/nl/brul/api/features/report/service/ReportVolunteerCapacityTest.kt
package nl.brul.api.features.report.service

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import java.time.Instant
import nl.brul.api.features.help.model.HelpParticipation
import nl.brul.api.features.help.model.ParticipationStatus
import nl.brul.api.features.help.repository.HelpParticipationRepository
import nl.brul.api.features.report.model.HelpRequest
import nl.brul.api.features.report.model.Location
import nl.brul.api.features.report.model.NewReport
import nl.brul.api.features.report.model.Report
import nl.brul.api.features.report.model.ReportCategory
import nl.brul.api.features.report.repository.ReportRepository
import nl.brul.api.support.TestSlots

class ReportVolunteerCapacityTest {

    @Test
    fun `cannot reduce capacity below participant count`() {
        val reportRepository = FakeReportRepository(
            helpRequest(volunteersNeeded = 5)
        )
        val participationRepository = FakeParticipationRepository(
            participantCount = 3
        )

        val service = ReportService(
            repository = reportRepository,
            helpParticipationRepository = participationRepository
        )

        assertFailsWith<IllegalArgumentException> {
            service.update(helpRequest(volunteersNeeded = 2))
        }

        assertEquals(
            5,
            (reportRepository.findById(1L) as HelpRequest)
                .volunteersNeeded
        )
    }

    @Test
    fun `can reduce capacity to participant count`() {
        val reportRepository = FakeReportRepository(
            helpRequest(volunteersNeeded = 5)
        )
        val participationRepository = FakeParticipationRepository(
            participantCount = 3
        )

        val service = ReportService(
            repository = reportRepository,
            helpParticipationRepository = participationRepository
        )

        val updated = service.update(
            helpRequest(volunteersNeeded = 3)
        )

        assertTrue(updated)
        assertEquals(
            3,
            (reportRepository.findById(1L) as HelpRequest)
                .volunteersNeeded
        )
    }

    @Test
    fun `can increase capacity above participant count`() {
        val reportRepository = FakeReportRepository(
            helpRequest(volunteersNeeded = 5)
        )
        val participationRepository = FakeParticipationRepository(
            participantCount = 3
        )

        val service = ReportService(
            repository = reportRepository,
            helpParticipationRepository = participationRepository
        )

        val updated = service.update(
            helpRequest(volunteersNeeded = 6)
        )

        assertTrue(updated)
        assertEquals(
            6,
            (reportRepository.findById(1L) as HelpRequest)
                .volunteersNeeded
        )
    }

    private fun helpRequest(
        volunteersNeeded: Int
    ): HelpRequest = HelpRequest(
        id = 1L,
        title = "Help with groceries",
        description = "I need help carrying groceries.",
        category = ReportCategory.GROCERIES,
        location = Location(
            latitude = 52.0705,
            longitude = 4.3007
        ),
        volunteersNeeded = volunteersNeeded,
        slot = TestSlots.slot
    )

    private class FakeReportRepository(
        private var report: Report
    ) : ReportRepository {

        override fun create(report: NewReport): Report {
            error("Not needed for this test.")
        }

        override fun findAll(): List<Report> =
            listOf(report)

        override fun findById(id: Long): Report? =
            report.takeIf { it.id == id }

        override fun update(report: Report): Boolean {
            if (this.report.id != report.id) {
                return false
            }

            this.report = report
            return true
        }

        override fun delete(id: Long): Boolean {
            if (report.id != id) {
                return false
            }

            return true
        }
    }

    private class FakeParticipationRepository(
        private val participantCount: Int
    ) : HelpParticipationRepository {

        override fun findByHelpRequest(
            helpRequestId: Long
        ): List<HelpParticipation> =
            emptyList()

        override fun findByHelpRequestAndUser(
            helpRequestId: Long,
            userId: Long
        ): HelpParticipation? = null

        override fun findByUser(
            userId: Long
        ): List<HelpParticipation> =
            emptyList()

        override fun add(
            helpRequestId: Long,
            userId: Long,
            status: ParticipationStatus,
            joinedAt: Instant
        ): HelpParticipation {
            error("Not needed for this test.")
        }

        override fun update(
            participation: HelpParticipation
        ): Boolean = false

        override fun countSignedUp(
            helpRequestId: Long
        ): Int = participantCount
    }
}
