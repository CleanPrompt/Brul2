// src/nl/brul/api/features/user/repository/UserRepository.kt
package nl.brul.api.features.user.repository

import nl.brul.api.features.user.model.NewUser
import nl.brul.api.features.user.model.User
import nl.brul.api.features.user.model.UserRole

interface UserRepository {
    fun create(user: NewUser): User
    fun findById(id: Long): User?
    fun findByEmail(email: String): User?

    fun findAll(): List<User>
    fun updateRole(id: Long, role: UserRole): User?
}