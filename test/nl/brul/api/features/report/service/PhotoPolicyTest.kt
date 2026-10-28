// test/nl/brul/api/features/report/service/PhotoPolicyTest.kt
package nl.brul.api.features.report.service

import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import nl.brul.api.features.help.model.ParticipationStatus
import nl.brul.api.features.report.model.HelpRequest
import nl.brul.api.features.report.model.ImageFormat
import nl.brul.api.features.report.model.Location
import nl.brul.api.features.report.model.NuisanceReport
import nl.brul.api.features.report.model.PhotoKind
import nl.brul.api.features.report.model.Report
import nl.brul.api.features.report.model.ReportCategory
import nl.brul.api.features.report.model.ReportStatus
import nl.brul.api.features.report.model.TimeSlot

/** Pure tests of the photo rules F1-F5 (P1-P3 in the design). */
class PhotoPolicyTest {

    private fun at(time: String): Instant = Instant.parse("2026-11-20T${time}:00Z")

    private val owner = 1L

    private fun nuisance(status: ReportStatus = ReportStatus.OPEN) = NuisanceReport(
        id = 1,
        title = "Graffiti",
        description = "Graffiti on the bridge",
        category = ReportCategory.VANDALISM,
        location = Location(52.0, 4.0),
        status = status,
        ownerId = owner
    )

    private val helpRequest = HelpRequest(
        id = 2,
        title = "Help with groceries",
        description = "Carrying groceries upstairs",
        category = ReportCategory.GROCERIES,
        location = Location(52.0, 4.0),
        volunteersNeeded = 2,
        slot = TimeSlot(at("10:00"), at("12:00")),
        ownerId = owner
    )

    private fun check(
        report: Report = nuisance(),
        uploaderId: Long = owner,
        kind: PhotoKind = PhotoKind.SITUATION,
        sizeBytes: Long = 1_000,
        format: ImageFormat? = ImageFormat.JPEG,
        existing: Int = 0,
        participation: ParticipationStatus? = null,
        now: Instant = at("08:00")
    ): PhotoDecision = PhotoPolicy.check(
        PhotoUploadRequest(report, uploaderId, kind, sizeBytes, format, existing, participation),
        now
    )

    private fun rejected(reason: PhotoRejection) = PhotoDecision.Rejected(reason)

    @Test
    fun `owner can add a situation photo`() {
        assertEquals(PhotoDecision.Allowed, check())
    }

    // P1 (F1)
    @Test
    fun `a PDF disguised as JPEG is not recognised as an image`() {
        val pdf = "%PDF-1.7 fake".toByteArray()

        assertNull(ImageFormat.detect(pdf))
        assertEquals(rejected(PhotoRejection.UNSUPPORTED_TYPE), check(format = ImageFormat.detect(pdf)))
    }

    @Test
    fun `jpeg and png are recognised by their first bytes`() {
        val jpeg = byteArrayOf(0xFF.toByte(), 0xD8.toByte(), 0xFF.toByte(), 0xE0.toByte())
        val png = byteArrayOf(0x89.toByte(), 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A, 0x00)

        assertEquals(ImageFormat.JPEG, ImageFormat.detect(jpeg))
        assertEquals(ImageFormat.PNG, ImageFormat.detect(png))
    }

    @Test
    fun `photo larger than the limit is rejected (F2)`() {
        assertEquals(
            rejected(PhotoRejection.TOO_LARGE),
            check(sizeBytes = PhotoPolicy.MAX_PHOTO_BYTES + 1)
        )
        assertEquals(PhotoDecision.Allowed, check(sizeBytes = PhotoPolicy.MAX_PHOTO_BYTES))
    }

    // P2 (F3)
    @Test
    fun `a fourth photo is rejected`() {
        assertEquals(rejected(PhotoRejection.PHOTO_LIMIT), check(existing = 3))
    }

    @Test
    fun `only the owner adds situation photos (F4)`() {
        assertEquals(rejected(PhotoRejection.NOT_ALLOWED), check(uploaderId = 2))
    }

    @Test
    fun `resolved report takes no new situation photos (F4)`() {
        assertEquals(
            rejected(PhotoRejection.REPORT_RESOLVED),
            check(report = nuisance(ReportStatus.RESOLVED))
        )
    }

    // P3 (F5)
    @Test
    fun `volunteer adds a result photo only once the help has started`() {
        assertEquals(
            rejected(PhotoRejection.NOT_STARTED),
            check(
                report = helpRequest,
                uploaderId = 2,
                kind = PhotoKind.RESULT,
                participation = ParticipationStatus.SIGNED_UP,
                now = at("09:00")
            )
        )

        assertEquals(
            PhotoDecision.Allowed,
            check(
                report = helpRequest,
                uploaderId = 2,
                kind = PhotoKind.RESULT,
                participation = ParticipationStatus.SIGNED_UP,
                now = at("10:30")
            )
        )
    }

    @Test
    fun `waitlisted volunteers cannot add a result photo (F5)`() {
        assertEquals(
            rejected(PhotoRejection.NOT_ALLOWED),
            check(
                report = helpRequest,
                uploaderId = 2,
                kind = PhotoKind.RESULT,
                participation = ParticipationStatus.WAITLISTED,
                now = at("10:30")
            )
        )
    }

    @Test
    fun `owner of a nuisance report adds the result photo`() {
        assertEquals(PhotoDecision.Allowed, check(kind = PhotoKind.RESULT))
        assertEquals(rejected(PhotoRejection.NOT_ALLOWED), check(uploaderId = 2, kind = PhotoKind.RESULT))
    }
}
