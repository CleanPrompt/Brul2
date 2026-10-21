// src/nl/brul/api/features/user/dto/UserResponse.kt
package nl.brul.api.features.user.dto

import kotlinx.serialization.Serializable
import nl.brul.api.features.user.model.User

@Serializable
data class UserResponse(
    val id: Long,
    val username: String,
    val email: String,
    val createdAt: Long,
    val role: String
)

fun User.toUserResponse(): UserResponse = UserResponse(
    id = id,
    username = username,
    email = email,
    createdAt = createdAt,
    role = role.name
)