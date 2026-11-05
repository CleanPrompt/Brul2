// src/nl/brul/api/common/error/ErrorResponse.kt
package nl.brul.api.common.error

import kotlinx.serialization.Serializable

/** The JSON body of every [AppException], e.g. `{ "code": "PHOTO_LIMIT_REACHED", "message": "..." }`. */
@Serializable
data class ErrorResponse(
    val code: String,
    val message: String
)
