// src/nl/brul/api/features/auth/dto/CurrentUserResponse.kt
package nl.brul.api.features.auth.dto

import kotlinx.serialization.Serializable

@Serializable
data class CurrentUserResponse(
    val userId: Long
)