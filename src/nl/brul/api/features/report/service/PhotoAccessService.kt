// src/nl/brul/api/features/report/service/PhotoAccessService.kt
package nl.brul.api.features.report.service

import nl.brul.api.common.error.AppException
import nl.brul.api.features.user.model.UserRole
import nl.brul.api.features.user.repository.UserRepository

/**
 * Who may change photos, in one place. The routes call one of these checks before they do any work.
 *
 * The role is read from the database on every call, not from the JWT: a user who is no longer an admin loses
 * that right immediately, even with a token issued earlier.
 */
class PhotoAccessService(
    private val userRepository: UserRepository
) {

    /** P1: only the owner of the report adds photos. [ownerId] is `null` for a report without an owner. */
    fun requireReportOwner(currentUserId: Long, ownerId: Long?) {
        if (currentUserId != ownerId) {
            throw AppException.Forbidden("Only the owner of the report can add photos")
        }
    }

    /** P5: the uploader may delete a photo; anyone else must be an admin. */
    fun requireUploaderOrAdmin(currentUserId: Long, uploaderId: Long?) {
        if (currentUserId != uploaderId && !isAdmin(currentUserId)) {
            throw AppException.Forbidden("Only the uploader or an admin can delete this photo")
        }
    }

    private fun isAdmin(userId: Long): Boolean =
        userRepository.findById(userId)?.role == UserRole.ADMIN
}
