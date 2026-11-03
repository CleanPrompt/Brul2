// test/nl/brul/api/features/report/service/PhotoRulesTest.kt
package nl.brul.api.features.report.service

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import nl.brul.api.features.report.model.ImageFormat

/** Pure tests of the photo rules P1-P4. */
class PhotoRulesTest {

    private fun check(
        isOwner: Boolean = true,
        size: Long = 1_000,
        format: ImageFormat? = ImageFormat.JPEG,
        existing: Int = 0
    ) = PhotoRules.check(isOwner, size, format, existing)

    @Test
    fun `owner can add a normal JPEG`() {
        assertNull(check())
    }

    @Test
    fun `P1 someone else cannot add photos`() {
        assertEquals(PhotoRejection.NOT_OWNER, check(isOwner = false))
    }

    @Test
    fun `P2 exactly 5 MB is allowed, one byte more is not`() {
        assertNull(check(size = PhotoRules.MAX_PHOTO_BYTES))
        assertEquals(PhotoRejection.TOO_LARGE, check(size = PhotoRules.MAX_PHOTO_BYTES + 1))
    }

    @Test
    fun `P3 unknown format is rejected`() {
        assertEquals(PhotoRejection.UNSUPPORTED_TYPE, check(format = null))
    }

    @Test
    fun `P4 the fourth photo is rejected`() {
        assertNull(check(existing = 2))
        assertEquals(PhotoRejection.PHOTO_LIMIT, check(existing = 3))
    }

    @Test
    fun `the first broken rule decides`() {
        assertEquals(PhotoRejection.NOT_OWNER, check(isOwner = false, format = null, existing = 3))
    }
}
