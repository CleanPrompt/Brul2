// test/nl/brul/api/features/report/service/ReportServiceTest.kt
package nl.brul.api.features.report.service

import nl.brul.api.features.report.model.HelpRequest
import nl.brul.api.features.report.model.Location
import nl.brul.api.features.report.model.NuisanceReport
import nl.brul.api.features.report.model.Report
import nl.brul.api.features.report.model.ReportCategory
import nl.brul.api.features.report.model.ReportStatus
import nl.brul.api.features.report.model.ReportType
import nl.brul.api.features.report.model.SafetyReport
import nl.brul.api.features.report.model.NewReport
import nl.brul.api.features.report.repository.ReportRepository
import nl.brul.api.support.TestSlots
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ReportServiceTest {

    private val location = Location(52.0705, 4.3007)

    private class FakeReportRepository : ReportRepository {

        private val reports = mutableMapOf<Long, Report>()
        private var nextId = 1L

        override fun create(report: NewReport): Report {
            val id = nextId++

            val saved = when (report.type) {
                ReportType.SAFETY -> SafetyReport(
                    id = id,
                    title = report.title,
                    description = report.description,
                    category = report.category,
                    location = report.location,
                    urgent = requireNotNull(report.urgent)
                )

                ReportType.NUISANCE -> NuisanceReport(
                    id = id,
                    title = report.title,
                    description = report.description,
                    category = report.category,
                    location = report.location
                )

                ReportType.HELP_REQUEST -> HelpRequest(
                    id = id,
                    title = report.title,
                    description = report.description,
                    category = report.category,
                    location = report.location,
                    volunteersNeeded = requireNotNull(report.volunteersNeeded),
                    slot = requireNotNull(report.slot)
                )
            }

            reports[id] = saved
            return saved
        }

        override fun findAll(): List<Report> {
            return reports.values.sortedBy { it.id }
        }

        override fun findById(id: Long): Report? {
            return reports[id]
        }

        override fun update(report: Report): Boolean {
            val existing = reports[report.id] ?: return false

            if (existing.type != report.type) {
                return false
            }

            reports[report.id] = report
            return true
        }

        override fun delete(id: Long): Boolean {
            return reports.remove(id) != null
        }
    }

    @Test
    fun `create safety report`() {
        val service = ReportService(FakeReportRepository())

        val saved = service.create(
            NewReport(
                type = ReportType.SAFETY,
                title = "Unsafe crossing",
                description = "Cars are driving too fast.",
                category = ReportCategory.TRAFFIC_SAFETY,
                location = location,
                urgent = true
            )
        )

        assertIs<SafetyReport>(saved)
        assertEquals(1L, saved.id)
        assertEquals(ReportCategory.TRAFFIC_SAFETY, saved.category)
        assertTrue(saved.urgent)
    }

    @Test
    fun `find all reports`() {
        val service = ReportService(FakeReportRepository())

        service.create(
            NewReport(
                type = ReportType.NUISANCE,
                title = "Loud music",
                description = "Music late at night.",
                category = ReportCategory.NOISE,
                location = location
            )
        )

        service.create(
            NewReport(
                type = ReportType.HELP_REQUEST,
                title = "Help with groceries",
                description = "Need help carrying groceries.",
                category = ReportCategory.GROCERIES,
                location = location,
                volunteersNeeded = 2,
                slot = TestSlots.slot
            )
        )

        val reports = service.findAll()

        assertEquals(2, reports.size)
        assertIs<NuisanceReport>(reports[0])
        assertIs<HelpRequest>(reports[1])
    }

    @Test
    fun `find report by id`() {
        val service = ReportService(FakeReportRepository())

        val saved = service.create(
            NewReport(
                type = ReportType.SAFETY,
                title = "Broken streetlight",
                description = "Streetlight is not working.",
                category = ReportCategory.TRAFFIC_SAFETY,
                location = location,
                urgent = false
            )
        )

        val found = service.findById(saved.id)

        assertNotNull(found)
        assertEquals(saved.id, found.id)
        assertEquals("Broken streetlight", found.title)
    }

    @Test
    fun `find missing report returns null`() {
        val service = ReportService(FakeReportRepository())

        assertNull(service.findById(999L))
    }

    @Test
    fun `update existing report`() {
        val service = ReportService(FakeReportRepository())

        val saved = service.create(
            NewReport(
                type = ReportType.SAFETY,
                title = "Broken streetlight",
                description = "Streetlight is not working.",
                category = ReportCategory.TRAFFIC_SAFETY,
                location = location,
                urgent = true
            )
        )

        val updated = service.update(
            SafetyReport(
                id = saved.id,
                title = "Streetlight repaired",
                description = "The streetlight has been repaired.",
                category = ReportCategory.TRAFFIC_SAFETY,
                location = location,
                urgent = false,
                status = ReportStatus.RESOLVED
            )
        )

        assertTrue(updated)

        val found = service.findById(saved.id)

        assertNotNull(found)
        assertEquals("Streetlight repaired", found.title)
        assertEquals(ReportStatus.RESOLVED, found.status)
    }

    @Test
    fun `delete existing report`() {
        val service = ReportService(FakeReportRepository())

        val saved = service.create(
            NewReport(
                type = ReportType.NUISANCE,
                title = "Loud music",
                description = "Music late at night.",
                category = ReportCategory.NOISE,
                location = location
            )
        )

        assertTrue(service.delete(saved.id))
        assertNull(service.findById(saved.id))
        assertFalse(service.delete(saved.id))
    }

    @Test
    fun `find by id rejects non positive id`() {
        val service = ReportService(FakeReportRepository())

        assertFailsWith<IllegalArgumentException> {
            service.findById(0)
        }
    }

    @Test
    fun `delete rejects non positive id`() {
        val service = ReportService(FakeReportRepository())

        assertFailsWith<IllegalArgumentException> {
            service.delete(-1)
        }
    }

    @Test
    fun `update cannot change report type`() {
        val service = ReportService(FakeReportRepository())

        val saved = service.create(
            NewReport(
                type = ReportType.SAFETY,
                title = "Unsafe crossing",
                description = "Cars are driving too fast.",
                category = ReportCategory.TRAFFIC_SAFETY,
                location = location,
                urgent = true
            )
        )

        val updated = service.update(
            HelpRequest(
                id = saved.id,
                title = "Help with moving",
                description = "Need help moving furniture.",
                category = ReportCategory.MOVING,
                location = location,
                volunteersNeeded = 2,
                slot = TestSlots.slot
            )
        )

        assertFalse(updated)
        assertIs<SafetyReport>(service.findById(saved.id))
    }
}