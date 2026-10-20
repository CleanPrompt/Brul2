// test/nl/brul/api/features/report/repository/ReportMetadataTest.kt
package nl.brul.api.features.report.repository

import nl.brul.api.features.report.model.NewReport
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import nl.brul.api.features.report.model.Location
import nl.brul.api.features.report.model.NuisanceReport
import nl.brul.api.features.report.model.ReportCategory
import nl.brul.api.features.report.model.ReportStatus
import nl.brul.api.features.report.model.ReportType
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.UUID

class ReportMetadataTest {

    private val location = Location(52.0705, 4.3007)

    private fun repository(
        currentTimeMillis: () -> Long = { 123456789L }
    ): ExposedReportRepository {
        val databaseName = "metadata_${UUID.randomUUID()}"

        val database = Database.connect(
            url = "jdbc:h2:mem:$databaseName;DB_CLOSE_DELAY=-1",
            driver = "org.h2.Driver"
        )

        transaction(database) {
            SchemaUtils.create(ReportsTable)
        }

        return ExposedReportRepository(database, currentTimeMillis)
    }

    private fun newReport() = NewReport(
        type = ReportType.NUISANCE,
        title = "Loud music",
        description = "Music during the night",
        category = ReportCategory.NOISE,
        location = location
    )

    @Test
    fun `new report stores creation time`() {
        val repository = repository { 123456789L }

        val created = repository.create(newReport())

        assertEquals(123456789L, created.createdAt)
    }

    @Test
    fun `new report starts with zero views`() {
        val repository = repository()

        val created = repository.create(newReport())

        assertEquals(0, created.viewCount)
    }

    @Test
    fun `metadata is preserved when reading from database`() {
        val repository = repository { 987654321L }

        val created = repository.create(newReport())
        val loaded = repository.findById(created.id)

        assertNotNull(loaded)
        assertEquals(987654321L, loaded.createdAt)
        assertEquals(0, loaded.viewCount)
    }

    @Test
    fun `update preserves original creation time`() {
        val repository = repository { 123456789L }

        val created = repository.create(newReport())

        val updated = NuisanceReport(
            id = created.id,
            title = "Updated title",
            description = created.description,
            category = created.category,
            location = created.location,
            status = ReportStatus.OPEN,
            createdAt = 999999999L,
            viewCount = 50
        )

        assertTrue(repository.update(updated))

        val loaded = repository.findById(created.id)
        assertNotNull(loaded)

        assertEquals(123456789L, loaded.createdAt)
        assertEquals(0, loaded.viewCount)
        assertEquals("Updated title", loaded.title)
    }

    @Test
    fun `negative view count is rejected`() {
        assertFailsWith<IllegalArgumentException> {
            NuisanceReport(
                id = 1,
                title = "Test report",
                description = "Invalid view count",
                category = ReportCategory.NOISE,
                location = location,
                viewCount = -1
            )
        }
    }
}