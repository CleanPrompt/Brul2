// src/nl/brul/api/features/user/dto/UpdateUserRoleRequest.kt
package nl.brul.api.features.user.dto

import kotlinx.serialization.Serializable

@Serializable
data class UpdateUserRoleRequest(
    val role: String
)