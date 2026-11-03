// test/nl/brul/api/features/report/service/ReportPhotoServiceTest.kt
package nl.brul.api.features.report.service

import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import nl.brul.api.features.report.FakePhotoRepository
import nl.brul.api.features.report.FakeReportRepository
import nl.brul.api.features.report.InMemoryPhotoStorage
import nl.brul.api.features.report.PhotoTestData
import nl.brul.api.features.report.model.Location
import nl.brul.api.features.report.model.ReportPhoto

/** The photo service with fakes: rules, GPS (G1, G2), storage and clean-up. */
class ReportPhotoServiceTest {

    private val owner = 1L
    private val photos = FakePhotoRepository()
    private val storage = InMemoryPhotoStorage()

    private val service = ReportPhotoService(
        reportRepository = FakeReportRepository(PhotoTestData.nuisance(id = 1, ownerId = owner)),
        photoRepository = photos,
        storage = storage,
        currentTimeMillis = { 1_000L }
    )

    private fun saved(result: PhotoUploadResult): ReportPhoto =
        assertIs<PhotoUploadResult.Saved>(result).photo

    @Test
    fun `G1 the location comes from the photo itself`() {
        val photo = saved(service.upload(1, owner, PhotoTestData.jpegWithGps(51.5856, 4.7763)))

        val location = assertNotNull(photo.location)
        assertTrue(abs(location.latitude - 51.5856) < 0.00001)
        assertEquals(ReportPhoto.LocationSource.PHOTO, photo.locationSource)
    }

    @Test
    fun `G1 the photo wins over the app location`() {
        val photo = saved(
            service.upload(
                1, owner, PhotoTestData.jpegWithGps(51.5856, 4.7763),
                appLocation = Location(52.0, 5.0)
            )
        )

        assertEquals(ReportPhoto.LocationSource.PHOTO, photo.locationSource)
    }

    @Test
    fun `G2 without GPS in the photo the app location is used`() {
        val photo = saved(service.upload(1, owner, PhotoTestData.jpeg(), Location(52.0, 5.0)))

        assertEquals(Location(52.0, 5.0), photo.location)
        assertEquals(ReportPhoto.LocationSource.APP, photo.locationSource)
    }

    @Test
    fun `without any GPS the photo has no location`() {
        val photo = saved(service.upload(1, owner, PhotoTestData.png()))

        assertNull(photo.location)
        assertNull(photo.locationSource)
    }

    @Test
    fun `upload stores the file under a random name`() {
        val photo = saved(service.upload(1, owner, PhotoTestData.jpeg()))

        assertTrue(photo.storageKey.endsWith(".jpg"))
        assertTrue(photo.storageKey in storage.files)
        assertEquals("image/jpeg", photo.contentType)
        assertEquals(1_000L, photo.createdAt)
    }

    @Test
    fun `missing report is rejected`() {
        val result = service.upload(99, owner, PhotoTestData.jpeg())

        assertEquals(PhotoUploadResult.Rejected(PhotoRejection.REPORT_NOT_FOUND), result)
    }

    @Test
    fun `P1 someone else cannot add a photo`() {
        val result = service.upload(1, uploaderId = 2, bytes = PhotoTestData.jpeg())

        assertEquals(PhotoUploadResult.Rejected(PhotoRejection.NOT_OWNER), result)
        assertTrue(storage.files.isEmpty())
    }

    @Test
    fun `P3 a text file is rejected`() {
        val result = service.upload(1, owner, "not an image".toByteArray())

        assertEquals(PhotoUploadResult.Rejected(PhotoRejection.UNSUPPORTED_TYPE), result)
    }

    @Test
    fun `P4 the fourth photo is rejected`() {
        repeat(3) { saved(service.upload(1, owner, PhotoTestData.jpeg())) }

        val result = service.upload(1, owner, PhotoTestData.jpeg())

        assertEquals(PhotoUploadResult.Rejected(PhotoRejection.PHOTO_LIMIT), result)
    }

    @Test
    fun `no orphan file when saving the data fails`() {
        photos.failOnAdd = true

        assertFailsWith<IllegalStateException> {
            service.upload(1, owner, PhotoTestData.jpeg())
        }

        assertTrue(storage.files.isEmpty())
    }

    @Test
    fun `list of a missing report is null`() {
        assertNull(service.list(99))
    }

    @Test
    fun `load returns the data and the bytes`() {
        val bytes = PhotoTestData.jpeg()
        val photo = saved(service.upload(1, owner, bytes))

        val (loaded, loadedBytes) = assertNotNull(service.load(photo.id))

        assertEquals(photo, loaded)
        assertTrue(bytes.contentEquals(loadedBytes))
    }

    @Test
    fun `uploader can delete their photo and the file is removed`() {
        val photo = saved(service.upload(1, owner, PhotoTestData.jpeg()))

        assertEquals(PhotoDeleteResult.DELETED, service.delete(photo.id, owner, isAdmin = false))
        assertTrue(storage.files.isEmpty())
        assertNull(service.load(photo.id))
    }

    @Test
    fun `someone else cannot delete, an admin can`() {
        val photo = saved(service.upload(1, owner, PhotoTestData.jpeg()))

        assertEquals(PhotoDeleteResult.FORBIDDEN, service.delete(photo.id, 2, isAdmin = false))
        assertEquals(PhotoDeleteResult.DELETED, service.delete(photo.id, 2, isAdmin = true))
    }

    @Test
    fun `deleting a missing photo says not found`() {
        assertEquals(PhotoDeleteResult.NOT_FOUND, service.delete(42, owner, isAdmin = true))
    }

    @Test
    fun `files of a deleted report can be cleaned up`() {
        saved(service.upload(1, owner, PhotoTestData.jpeg()))
        saved(service.upload(1, owner, PhotoTestData.png()))

        service.deleteFiles(service.storageKeysOf(1))

        assertTrue(storage.files.isEmpty())
    }
}
