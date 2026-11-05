// test/nl/brul/api/features/report/service/PhotoAccessServiceTest.kt
package nl.brul.api.features.report.service

import kotlin.test.Test
import kotlin.test.assertFailsWith
import nl.brul.api.common.error.AppException
import nl.brul.api.features.report.FakePhotoUserRepository
import nl.brul.api.features.user.model.UserRole

/** P1 and P5: who may add and delete photos. User 3 is an admin in the fake. */
class PhotoAccessServiceTest {

    private val access = PhotoAccessService(FakePhotoUserRepository())

    @Test
    fun `P1 the owner of the report may add photos`() {
        access.requireReportOwner(currentUserId = 1, ownerId = 1)
    }

    @Test
    fun `P1 someone else, even an admin, may not add photos`() {
        assertFailsWith<AppException.Forbidden> { access.requireReportOwner(currentUserId = 2, ownerId = 1) }
        assertFailsWith<AppException.Forbidden> { access.requireReportOwner(currentUserId = 3, ownerId = 1) }
    }

    @Test
    fun `P1 nobody may add photos to a report without an owner`() {
        assertFailsWith<AppException.Forbidden> { access.requireReportOwner(currentUserId = 1, ownerId = null) }
    }

    @Test
    fun `P5 the uploader and an admin may delete, someone else may not`() {
        access.requireUploaderOrAdmin(currentUserId = 1, uploaderId = 1)
        access.requireUploaderOrAdmin(currentUserId = 3, uploaderId = 1)

        assertFailsWith<AppException.Forbidden> { access.requireUploaderOrAdmin(currentUserId = 2, uploaderId = 1) }
    }

    @Test
    fun `P5 the admin role comes from the database, not from the token`() {
        val users = FakePhotoUserRepository()
        val access = PhotoAccessService(users)

        users.updateRole(3, UserRole.USER)

        assertFailsWith<AppException.Forbidden> { access.requireUploaderOrAdmin(currentUserId = 3, uploaderId = 1) }
    }
}
