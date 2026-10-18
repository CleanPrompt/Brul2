// src/nl/brul/api/common/error/ApiError.kt
package nl.brul.api.common.error

import kotlinx.serialization.Serializable

@Serializable
data class ApiError(
    val message: String
)
