// src/nl/brul/api/features/user/service/UserService.kt
package nl.brul.api.features.user.service

import nl.brul.api.features.user.model.User
import nl.brul.api.features.user.model.UserRole
import nl.brul.api.features.user.repository.UserRepository

class UserService(
    private val userRepository: UserRepository
) {

    fun isAdmin(userId: Long): Boolean {
        return userRepository.findById(userId)?.role == UserRole.ADMIN
    }

    fun findAll(): List<User> {
        return userRepository.findAll()
    }

    fun findById(id: Long): User? {
        return userRepository.findById(id)
    }

    fun updateRole(
        targetUserId: Long,
        actingUserId: Long,
        role: String
    ): User? {
        val newRole = UserRole.entries.firstOrNull {
            it.name == role
        } ?: throw IllegalArgumentException("Invalid user role.")

        if (targetUserId == actingUserId && newRole != UserRole.ADMIN) {
            throw IllegalArgumentException(
                "You cannot remove your own admin role."
            )
        }

        return userRepository.updateRole(targetUserId, newRole)
    }
}