// src/nl/brul/api/features/auth/dto/AuthResponse.kt
package nl.brul.api.features.auth.dto

import kotlinx.serialization.Serializable
import nl.brul.api.features.user.model.User

@Serializable
data class AuthResponse(
    val id: Long,
    val username: String,
    val email: String,
    val createdAt: Long
)

fun User.toAuthResponse(): AuthResponse {
    return AuthResponse(
        id = id,
        username = username,
        email = email,
        createdAt = createdAt
    )
}