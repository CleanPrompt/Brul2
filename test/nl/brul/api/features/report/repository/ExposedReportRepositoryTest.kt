// test/nl/brul/api/features/report/repository/ExposedReportRepositoryTest.kt
package nl.brul.api.features.report.repository

import nl.brul.api.features.report.model.NewReport
import nl.brul.api.features.report.model.HelpRequest
import nl.brul.api.features.report.model.Location
import nl.brul.api.features.report.model.NuisanceReport
import nl.brul.api.features.report.model.ReportCategory
import nl.brul.api.features.report.model.ReportStatus
import nl.brul.api.features.report.model.ReportType
import nl.brul.api.features.report.model.SafetyReport
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ExposedReportRepositoryTest {

    private fun createRepository(): ExposedReportRepository {
        val database = Database.connect(
            url = "jdbc:h2:mem:reports_${UUID.randomUUID()};DB_CLOSE_DELAY=-1",
            driver = "org.h2.Driver",
            user = "sa",
            password = ""
        )

        transaction(database) {
            SchemaUtils.create(ReportsTable)
        }

        return ExposedReportRepository(database)
    }

    @Test
    fun `create and find safety report`() {
        val repository = createRepository()

        val saved = repository.create(
            NewReport(
                type = ReportType.SAFETY,
                title = "Unsafe crossing",
                description = "Cars are speeding near the school.",
                category = ReportCategory.TRAFFIC_SAFETY,
                location = Location(52.08, 4.31),
                urgent = true
            )
        )

        assertTrue(saved.id > 0)

        val found = repository.findById(saved.id)

        assertNotNull(found)
        assertIs<SafetyReport>(found)
        assertEquals("Unsafe crossing", found.title)
        assertEquals(ReportCategory.TRAFFIC_SAFETY, found.category)
        assertTrue(found.urgent)
        assertEquals(ReportStatus.OPEN, found.status)
    }

    @Test
    fun `create and find all report types`() {
        val repository = createRepository()
        val location = Location(52.08, 4.31)

        repository.create(
            NewReport(
                type = ReportType.SAFETY,
                title = "Unsafe crossing",
                description = "Cars drive too fast.",
                category = ReportCategory.TRAFFIC_SAFETY,
                location = location,
                urgent = false
            )
        )

        repository.create(
            NewReport(
                type = ReportType.NUISANCE,
                title = "Loud music",
                description = "Music during the night.",
                category = ReportCategory.NOISE,
                location = location
            )
        )

        repository.create(
            NewReport(
                type = ReportType.HELP_REQUEST,
                title = "Help with groceries",
                description = "Looking for help with shopping.",
                category = ReportCategory.GROCERIES,
                location = location,
                volunteersNeeded = 2
            )
        )

        val reports = repository.findAll()

        assertEquals(3, reports.size)

        assertIs<SafetyReport>(reports[0])
        assertEquals(ReportCategory.TRAFFIC_SAFETY, reports[0].category)

        assertIs<NuisanceReport>(reports[1])
        assertEquals(ReportCategory.NOISE, reports[1].category)

        assertIs<HelpRequest>(reports[2])
        assertEquals(ReportCategory.GROCERIES, reports[2].category)
    }

    @Test
    fun `update existing report`() {
        val repository = createRepository()

        val saved = repository.create(
            NewReport(
                type = ReportType.SAFETY,
                title = "Broken streetlight",
                description = "Streetlight is not working.",
                category = ReportCategory.TRAFFIC_SAFETY,
                location = Location(52.08, 4.31),
                urgent = true
            )
        )

        val updated = repository.update(
            SafetyReport(
                id = saved.id,
                title = "Streetlight repaired",
                description = "The streetlight has been repaired.",
                category = ReportCategory.TRAFFIC_SAFETY,
                location = saved.location,
                urgent = false,
                status = ReportStatus.RESOLVED
            )
        )

        assertTrue(updated)

        val found = repository.findById(saved.id)

        assertNotNull(found)
        assertIs<SafetyReport>(found)
        assertEquals("Streetlight repaired", found.title)
        assertEquals(ReportCategory.TRAFFIC_SAFETY, found.category)
        assertFalse(found.urgent)
        assertEquals(ReportStatus.RESOLVED, found.status)
    }

    @Test
    fun `delete existing report`() {
        val repository = createRepository()

        val saved = repository.create(
            NewReport(
                type = ReportType.NUISANCE,
                title = "Noise complaint",
                description = "Loud construction noise.",
                category = ReportCategory.NOISE,
                location = Location(52.08, 4.31)
            )
        )

        assertTrue(repository.delete(saved.id))
        assertNull(repository.findById(saved.id))
        assertFalse(repository.delete(saved.id))
    }

    @Test
    fun `find missing report returns null`() {
        val repository = createRepository()

        assertNull(repository.findById(999L))
    }

    @Test
    fun `update cannot change report type`() {
        val repository = createRepository()
        val location = Location(52.08, 4.31)

        val saved = repository.create(
            NewReport(
                type = ReportType.SAFETY,
                title = "Unsafe crossing",
                description = "Cars are speeding.",
                category = ReportCategory.TRAFFIC_SAFETY,
                location = location,
                urgent = true
            )
        )

        val updated = repository.update(
            HelpRequest(
                id = saved.id,
                title = "Help with moving",
                description = "Need help moving furniture.",
                category = ReportCategory.MOVING,
                location = location,
                volunteersNeeded = 2
            )
        )

        assertFalse(updated)

        val found = repository.findById(saved.id)

        assertNotNull(found)
        assertIs<SafetyReport>(found)
        assertEquals("Unsafe crossing", found.title)
        assertEquals(ReportCategory.TRAFFIC_SAFETY, found.category)
    }
}