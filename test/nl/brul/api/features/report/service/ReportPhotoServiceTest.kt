// test/nl/brul/api/features/report/service/ReportPhotoServiceTest.kt
package nl.brul.api.features.report.service

import java.time.Clock
import java.time.ZoneOffset
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import nl.brul.api.common.error.AppException
import nl.brul.api.features.report.FakePhotoRepository
import nl.brul.api.features.report.FakeReportRepository
import nl.brul.api.features.report.InMemoryPhotoStorage
import nl.brul.api.features.report.PhotoTestData
import nl.brul.api.features.report.model.Location

/** The photo service with fakes: rules, GPS (G1, G2), storage and clean-up. */
class ReportPhotoServiceTest {

    private val owner = 1L
    private val photos = FakePhotoRepository()
    private val storage = InMemoryPhotoStorage()

    private val service = ReportPhotoService(
        reportRepository = FakeReportRepository(
            PhotoTestData.nuisance(id = 1, ownerId = owner),
            PhotoTestData.nuisance(id = 2, ownerId = null)
        ),
        photoRepository = photos,
        storage = storage,
        clock = Clock.fixed(PhotoTestData.NOW, ZoneOffset.UTC)
    )

    private fun upload(bytes: ByteArray = PhotoTestData.jpeg(), appLocation: Location? = null) =
        service.upload(reportId = 1, uploaderId = owner, bytes = bytes, appLocation = appLocation)

    @Test
    fun `G1 the location comes from the photo itself`() {
        val photo = upload(PhotoTestData.jpegWithGps(51.5856, 4.7763))

        assertTrue(abs(assertNotNull(photo.latitude) - 51.5856) < 0.00001)
        assertTrue(abs(assertNotNull(photo.longitude) - 4.7763) < 0.00001)
        assertEquals("PHOTO", photo.locationSource)
    }

    @Test
    fun `G1 the photo wins over the app location`() {
        val photo = upload(PhotoTestData.jpegWithGps(51.5856, 4.7763), appLocation = Location(52.0, 5.0))

        assertEquals("PHOTO", photo.locationSource)
    }

    @Test
    fun `G2 without GPS in the photo the app location is used`() {
        val photo = upload(appLocation = Location(52.0, 5.0))

        assertEquals(52.0, photo.latitude)
        assertEquals(5.0, photo.longitude)
        assertEquals("APP", photo.locationSource)
    }

    @Test
    fun `without any GPS the photo has no location`() {
        val photo = upload(PhotoTestData.png())

        assertNull(photo.latitude)
        assertNull(photo.longitude)
        assertNull(photo.locationSource)
    }

    @Test
    fun `upload stores the file under a random name and sets the time`() {
        val photo = upload()
        val saved = photos.photos.single()

        assertTrue(saved.storageKey.endsWith(".jpg"))
        assertTrue(saved.storageKey in storage.files)
        assertEquals("image/jpeg", photo.contentType)
        assertEquals("2026-11-05T19:30:00Z", photo.createdAt)
        assertEquals("/api/v1/photos/${photo.id}", photo.url)
    }

    @Test
    fun `an unknown report gives 404`() {
        val error = assertFailsWith<AppException.NotFound> {
            service.upload(reportId = 99, uploaderId = owner, bytes = PhotoTestData.jpeg(), appLocation = null)
        }

        assertEquals("REPORT_NOT_FOUND", error.code)
        assertFailsWith<AppException.NotFound> { service.getReportOwnerId(99) }
        assertFailsWith<AppException.NotFound> { service.listByReport(99) }
    }

    @Test
    fun `the owner of a report is known, also when there is none`() {
        assertEquals(owner, service.getReportOwnerId(1))
        assertNull(service.getReportOwnerId(2))
    }

    @Test
    fun `P3 a text file is rejected and nothing is stored`() {
        assertFailsWith<AppException.UnsupportedMediaType> { upload("not an image".toByteArray()) }

        assertTrue(storage.files.isEmpty())
        assertTrue(photos.photos.isEmpty())
    }

    @Test
    fun `P4 the fourth photo is rejected`() {
        repeat(3) { upload() }

        val error = assertFailsWith<AppException.Conflict> { upload() }
        assertEquals("PHOTO_LIMIT_REACHED", error.code)
        assertEquals(3, storage.files.size)
    }

    @Test
    fun `no file is left behind when saving the data fails`() {
        photos.failOnAdd = true

        assertFailsWith<IllegalStateException> { upload() }

        assertTrue(storage.files.isEmpty())
    }

    @Test
    fun `the list shows the photos of one report, oldest first`() {
        val first = upload()
        val second = upload(PhotoTestData.png())

        assertEquals(listOf(first.id, second.id), service.listByReport(1).map { it.id })
    }

    @Test
    fun `the image comes back with its content type`() {
        val bytes = PhotoTestData.png()
        val photo = upload(bytes)

        val image = service.getImage(photo.id)

        assertEquals("image/png", image.contentType)
        assertTrue(bytes.contentEquals(image.bytes))
    }

    @Test
    fun `an unknown photo gives 404`() {
        val error = assertFailsWith<AppException.NotFound> { service.getImage(42) }

        assertEquals("PHOTO_NOT_FOUND", error.code)
        assertFailsWith<AppException.NotFound> { service.getUploaderId(42) }
        assertFailsWith<AppException.NotFound> { service.delete(42) }
    }

    @Test
    fun `delete removes the data and the file`() {
        val photo = upload()

        assertEquals(owner, service.getUploaderId(photo.id))
        service.delete(photo.id)

        assertTrue(storage.files.isEmpty())
        assertFailsWith<AppException.NotFound> { service.getImage(photo.id) }
    }

    @Test
    fun `files of a deleted report can be cleaned up`() {
        upload()
        upload(PhotoTestData.png())

        service.deleteFiles(service.storageKeysOf(1))

        assertTrue(storage.files.isEmpty())
    }
}
