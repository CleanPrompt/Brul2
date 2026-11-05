// test/nl/brul/api/features/report/repository/ExposedReportPhotoRepositoryTest.kt
package nl.brul.api.features.report.repository

import java.time.Instant
import java.util.UUID
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import nl.brul.api.features.report.database.ReportPhotosTable
import nl.brul.api.features.report.model.Location
import nl.brul.api.features.report.model.LocationSource
import nl.brul.api.features.report.model.NewReport
import nl.brul.api.features.report.model.NewReportPhoto
import nl.brul.api.features.report.model.PhotoLocation
import nl.brul.api.features.report.model.ReportCategory
import nl.brul.api.features.report.model.ReportType
import nl.brul.api.features.user.model.NewUser
import nl.brul.api.features.user.repository.ExposedUserRepository
import nl.brul.api.features.user.repository.UsersTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

/** The Exposed repository against a real in-memory H2 database, with a fresh database for every test. */
class ExposedReportPhotoRepositoryTest {

    private lateinit var reports: ExposedReportRepository
    private lateinit var photos: ExposedReportPhotoRepository
    private var reportId = 0L

    @BeforeTest
    fun setUp() {
        val database = Database.connect(
            url = "jdbc:h2:mem:report_photos_${UUID.randomUUID()};DB_CLOSE_DELAY=-1",
            driver = "org.h2.Driver",
            user = "sa",
            password = ""
        )

        transaction(database) {
            SchemaUtils.create(UsersTable, ReportsTable, ReportPhotosTable)
        }

        ExposedUserRepository(database).create(
            NewUser(username = "owner", email = "owner@example.com", passwordHash = "test-password-hash")
        )

        reports = ExposedReportRepository(database)
        photos = ExposedReportPhotoRepository(database)
        reportId = reports.create(
            NewReport(
                type = ReportType.NUISANCE,
                title = "Graffiti",
                description = "Graffiti on the bridge",
                category = ReportCategory.VANDALISM,
                location = Location(51.5866, 4.7760),
                ownerId = 1
            )
        ).id
    }

    private fun newPhoto(key: String, location: PhotoLocation? = null) = NewReportPhoto(
        reportId = reportId,
        uploaderId = 1,
        contentType = "image/jpeg",
        sizeBytes = 1_234,
        storageKey = key,
        createdAt = Instant.parse("2026-11-05T19:30:00Z"),
        location = location
    )

    @Test
    fun `a photo with a location is saved and read back unchanged`() {
        val location = PhotoLocation(Location(51.5856, 4.7763), LocationSource.PHOTO)

        val saved = photos.add(newPhoto("a.jpg", location))

        assertEquals(saved, photos.findById(saved.id))
        assertEquals(location, photos.findById(saved.id)?.location)
        assertEquals(Instant.parse("2026-11-05T19:30:00Z"), photos.findById(saved.id)?.createdAt)
    }

    @Test
    fun `a photo without a location reads back without one`() {
        val saved = photos.add(newPhoto("b.jpg"))

        assertNull(photos.findById(saved.id)?.location)
    }

    @Test
    fun `photos of a report are listed oldest first and counted`() {
        val first = photos.add(newPhoto("c.jpg"))
        val second = photos.add(newPhoto("d.jpg"))

        assertEquals(listOf(first.id, second.id), photos.findByReport(reportId).map { it.id })
        assertEquals(2, photos.countByReport(reportId))
        assertEquals(0, photos.countByReport(reportId + 1))
    }

    @Test
    fun `delete removes only that photo`() {
        val kept = photos.add(newPhoto("e.jpg"))
        val removed = photos.add(newPhoto("f.jpg"))

        assertTrue(photos.delete(removed.id))
        assertFalse(photos.delete(removed.id))
        assertEquals(listOf(kept.id), photos.findByReport(reportId).map { it.id })
    }

    @Test
    fun `deleting the report removes its photo rows (CASCADE)`() {
        photos.add(newPhoto("g.jpg"))

        reports.delete(reportId)

        assertEquals(0, photos.countByReport(reportId))
    }
}
