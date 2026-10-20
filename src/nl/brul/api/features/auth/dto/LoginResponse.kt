// src/nl/brul/api/features/auth/dto/LoginResponse.kt
package nl.brul.api.features.auth.dto

import kotlinx.serialization.Serializable

@Serializable
data class LoginResponse(
    val id: Long,
    val username: String,
    val email: String,
    val createdAt: Long,
    val accessToken: String,
    val tokenType: String
)