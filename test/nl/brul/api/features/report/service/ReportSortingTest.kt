// test/nl/brul/api/features/report/service/ReportSortingTest.kt
package nl.brul.api.features.report.service

import kotlin.test.Test
import kotlin.test.assertEquals
import nl.brul.api.features.report.model.Location
import nl.brul.api.features.report.model.NuisanceReport
import nl.brul.api.features.report.model.Report
import nl.brul.api.features.report.model.ReportCategory
import nl.brul.api.features.report.repository.NewReport
import nl.brul.api.features.report.repository.ReportRepository

class ReportSortingTest {

    private val location = Location(52.0705, 4.3007)

    private fun report(
        id: Long,
        createdAt: Long,
        viewCount: Int
    ): Report = NuisanceReport(
        id = id,
        title = "Test report $id",
        description = "A report for sorting tests",
        category = ReportCategory.NOISE,
        location = location,
        createdAt = createdAt,
        viewCount = viewCount
    )

    private fun service(reports: List<Report>): ReportService {
        val repository = object : ReportRepository {
            override fun create(report: NewReport): Report =
                error("Not needed for this test.")

            override fun findAll(): List<Report> = reports

            override fun findById(id: Long): Report? =
                error("Not needed for this test.")

            override fun update(report: Report): Boolean =
                error("Not needed for this test.")

            override fun delete(id: Long): Boolean =
                error("Not needed for this test.")
        }

        return ReportService(repository)
    }

    @Test
    fun `newest sorts by creation time`() {
        val reports = listOf(
            report(1, 1000, 0),
            report(2, 3000, 0),
            report(3, 2000, 0)
        )

        val result = service(reports).search(
            ReportSearch(sort = ReportSort.NEWEST)
        )

        assertEquals(listOf(2L, 3L, 1L), result.map { it.id })
    }

    @Test
    fun `newest uses highest ID when timestamps match`() {
        val reports = listOf(
            report(1, 1000, 0),
            report(3, 1000, 0),
            report(2, 1000, 0)
        )

        val result = service(reports).search(
            ReportSearch(sort = ReportSort.NEWEST)
        )

        assertEquals(listOf(3L, 2L, 1L), result.map { it.id })
    }

    @Test
    fun `most viewed sorts by view count`() {
        val reports = listOf(
            report(1, 1000, 5),
            report(2, 2000, 20),
            report(3, 3000, 10)
        )

        val result = service(reports).search(
            ReportSearch(sort = ReportSort.MOST_VIEWED)
        )

        assertEquals(listOf(2L, 3L, 1L), result.map { it.id })
    }

    @Test
    fun `most viewed uses creation time when counts match`() {
        val reports = listOf(
            report(1, 1000, 10),
            report(2, 3000, 10),
            report(3, 2000, 10)
        )

        val result = service(reports).search(
            ReportSearch(sort = ReportSort.MOST_VIEWED)
        )

        assertEquals(listOf(2L, 3L, 1L), result.map { it.id })
    }

    @Test
    fun `most viewed uses ID when counts and timestamps match`() {
        val reports = listOf(
            report(1, 1000, 10),
            report(3, 1000, 10),
            report(2, 1000, 10)
        )

        val result = service(reports).search(
            ReportSearch(sort = ReportSort.MOST_VIEWED)
        )

        assertEquals(listOf(3L, 2L, 1L), result.map { it.id })
    }
}