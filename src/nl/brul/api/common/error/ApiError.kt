// src/nl/brul/api/common/error/ApiError.kt
package nl.brul.api.common.error

import kotlinx.serialization.Serializable

/**
 * Error body returned by the API.
 *
 * [code] is a fixed, machine-readable value (for example `TIME_CONFLICT`)
 * so the Android app can show its own message instead of parsing [message].
 * It is left out of the JSON when there is no specific code.
 */
@Serializable
data class ApiError(
    val message: String,
    val code: String? = null
)
