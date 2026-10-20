// src/nl/brul/api/features/auth/dto/LoginRequest.kt
package nl.brul.api.features.auth.dto

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(
    val email: String,
    val password: String
)