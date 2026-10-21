// test/nl/brul/api/features/report/repository/ReportViewCountTest.kt
package nl.brul.api.features.report.repository

import nl.brul.api.features.report.model.Location
import nl.brul.api.features.report.model.NewReport
import nl.brul.api.features.report.model.ReportCategory
import nl.brul.api.features.report.model.ReportType
import nl.brul.api.features.report.service.ReportSearch
import nl.brul.api.features.report.service.ReportService
import nl.brul.api.features.report.service.ReportSort
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class ReportViewCountTest {

    private fun createRepository(): ExposedReportRepository {
        val database = Database.connect(
            url = "jdbc:h2:mem:views_${UUID.randomUUID()};DB_CLOSE_DELAY=-1",
            driver = "org.h2.Driver",
            user = "sa",
            password = ""
        )

        transaction(database) {
            SchemaUtils.create(ReportsTable)
        }

        return ExposedReportRepository(database)
    }

    private fun newReport(title: String = "Loud music") = NewReport(
        type = ReportType.NUISANCE,
        title = title,
        description = "Music during the night.",
        category = ReportCategory.NOISE,
        location = Location(52.0705, 4.3007)
    )

    @Test
    fun `new report starts with zero views`() {
        val repository = createRepository()
        val created = repository.create(newReport())

        assertEquals(0, created.viewCount)
    }

    @Test
    fun `detail views increment the stored count`() {
        val repository = createRepository()
        val created = repository.create(newReport())

        val firstView = repository.findByIdAndIncrementViews(created.id)
        assertNotNull(firstView)
        assertEquals(1, firstView.viewCount)

        val secondView = repository.findByIdAndIncrementViews(created.id)
        assertNotNull(secondView)
        assertEquals(2, secondView.viewCount)

        val stored = repository.findById(created.id)
        assertNotNull(stored)
        assertEquals(2, stored.viewCount)
    }

    @Test
    fun `ordinary reads do not increment views`() {
        val repository = createRepository()
        val created = repository.create(newReport())

        repository.findById(created.id)
        repository.findAll()
        repository.findById(created.id)

        assertEquals(0, repository.findById(created.id)?.viewCount)
    }

    @Test
    fun `missing report does not create a view`() {
        val repository = createRepository()

        assertNull(repository.findByIdAndIncrementViews(999L))
    }

    @Test
    fun `service detail views increment but normal reads do not`() {
        val repository = createRepository()
        val service = ReportService(repository)
        val created = service.create(newReport())

        service.findById(created.id)
        service.findAll()

        assertEquals(0, service.findById(created.id)?.viewCount)

        val viewed = service.findByIdAndIncrementViews(created.id)
        assertNotNull(viewed)
        assertEquals(1, viewed.viewCount)

        assertEquals(1, service.findById(created.id)?.viewCount)
    }

    @Test
    fun `most viewed sorts using stored view counts`() {
        val repository = createRepository()
        val service = ReportService(repository)

        val first = service.create(newReport("First report"))
        val second = service.create(newReport("Second report"))
        val third = service.create(newReport("Third report"))

        service.findByIdAndIncrementViews(first.id)

        repeat(3) {
            service.findByIdAndIncrementViews(second.id)
        }

        repeat(2) {
            service.findByIdAndIncrementViews(third.id)
        }

        val sorted = service.search(
            ReportSearch(sort = ReportSort.MOST_VIEWED)
        )

        assertEquals(
            listOf(second.id, third.id, first.id),
            sorted.map { it.id }
        )

        assertEquals(
            listOf(3, 2, 1),
            sorted.map { it.viewCount }
        )
    }
}