// src/nl/brul/api/features/auth/dto/RegisterRequest.kt
package nl.brul.api.features.auth.dto

import kotlinx.serialization.Serializable

@Serializable
data class RegisterRequest(
    val username: String,
    val email: String,
    val password: String
)