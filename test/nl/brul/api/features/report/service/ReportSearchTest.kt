// test/nl/brul/api/features/report/service/ReportSearchTest.kt
package nl.brul.api.features.report.service

import nl.brul.api.features.report.model.Location
import nl.brul.api.features.report.model.NuisanceReport
import nl.brul.api.features.report.model.Report
import nl.brul.api.features.report.model.ReportCategory
import nl.brul.api.features.report.model.ReportType
import nl.brul.api.features.report.model.SafetyReport
import nl.brul.api.features.report.model.NewReport
import nl.brul.api.features.report.repository.ReportRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class ReportSearchTest {

    private val denHaag = Location(52.0705, 4.3007)
    private val rotterdam = Location(51.9244, 4.4777)
    private val amsterdam = Location(52.3676, 4.9041)

    private fun createService(): ReportService {
        val reports = listOf(
            SafetyReport(
                id = 1L,
                title = "Unsafe crossing",
                description = "Dangerous traffic near the school.",
                category = ReportCategory.TRAFFIC_SAFETY,
                location = denHaag,
                urgent = true
            ),
            NuisanceReport(
                id = 2L,
                title = "Loud music",
                description = "Noise during the night.",
                category = ReportCategory.NOISE,
                location = rotterdam
            ),
            NuisanceReport(
                id = 3L,
                title = "Street waste",
                description = "Rubbish near the crossing.",
                category = ReportCategory.WASTE,
                location = amsterdam
            )
        )

        return ReportService(FakeSearchRepository(reports))
    }

    private class FakeSearchRepository(
        private val reports: List<Report>
    ) : ReportRepository {

        override fun create(report: NewReport): Report {
            error("Create is not used in these tests.")
        }

        override fun findAll(): List<Report> {
            return reports
        }

        override fun findById(id: Long): Report? {
            return reports.find { it.id == id }
        }

        override fun update(report: Report): Boolean {
            error("Update is not used in these tests.")
        }

        override fun delete(id: Long): Boolean {
            error("Delete is not used in these tests.")
        }
    }

    @Test
    fun `search by title ignores case`() {
        val results = createService().search(
            ReportSearch(query = "LOUD")
        )

        assertEquals(listOf(2L), results.map { it.id })
    }

    @Test
    fun `search also checks description`() {
        val results = createService().search(
            ReportSearch(query = "crossing")
        )

        assertEquals(listOf(1L, 3L), results.map { it.id })
    }

    @Test
    fun `filter by report type`() {
        val results = createService().search(
            ReportSearch(type = ReportType.NUISANCE)
        )

        assertEquals(listOf(2L, 3L), results.map { it.id })
    }

    @Test
    fun `filter by category`() {
        val results = createService().search(
            ReportSearch(category = ReportCategory.NOISE)
        )

        assertEquals(listOf(2L), results.map { it.id })
    }

    @Test
    fun `combine text and type filters`() {
        val results = createService().search(
            ReportSearch(
                query = "crossing",
                type = ReportType.NUISANCE
            )
        )

        assertEquals(listOf(3L), results.map { it.id })
    }

    @Test
    fun `filter by distance`() {
        val results = createService().search(
            ReportSearch(
                location = denHaag,
                radiusKm = 5.0
            )
        )

        assertEquals(listOf(1L), results.map { it.id })
    }

    @Test
    fun `sort by nearest location`() {
        val results = createService().search(
            ReportSearch(
                location = amsterdam,
                sort = ReportSort.NEAREST
            )
        )

        assertEquals(listOf(3L, 1L, 2L), results.map { it.id })

    }

    @Test
    fun `sort by descending id`() {
        val results = createService().search(
            ReportSearch(sort = ReportSort.ID_DESC)
        )

        assertEquals(listOf(3L, 2L, 1L), results.map { it.id })
    }

    @Test
    fun `unknown search text returns empty list`() {
        val results = createService().search(
            ReportSearch(query = "nonexistent")
        )

        assertTrue(results.isEmpty())
    }

    @Test
    fun `blank search text returns all reports`() {
        val results = createService().search(
            ReportSearch(query = "   ")
        )

        assertEquals(3, results.size)
    }

    @Test
    fun `radius requires location`() {
        assertFailsWith<IllegalArgumentException> {
            ReportSearch(radiusKm = 5.0)
        }
    }

    @Test
    fun `radius must be positive`() {
        assertFailsWith<IllegalArgumentException> {
            ReportSearch(
                location = denHaag,
                radiusKm = -5.0
            )
        }
    }

    @Test
    fun `radius cannot be infinity`() {
        assertFailsWith<IllegalArgumentException> {
            ReportSearch(
                location = denHaag,
                radiusKm = Double.POSITIVE_INFINITY
            )
        }
    }

    @Test
    fun `nearest sorting requires location`() {
        assertFailsWith<IllegalArgumentException> {
            ReportSearch(sort = ReportSort.NEAREST)
        }
    }

    @Test
    fun `distance to same location is zero`() {
        assertEquals(0.0, denHaag.distanceTo(denHaag), 0.001)
    }

    @Test
    fun `distance between cities is reasonable`() {
        val distance = denHaag.distanceTo(rotterdam)

        assertTrue(distance > 15.0)
        assertTrue(distance < 30.0)
    }
}