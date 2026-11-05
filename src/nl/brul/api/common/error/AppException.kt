// src/nl/brul/api/common/error/AppException.kt
package nl.brul.api.common.error

/**
 * Expected application errors. `plugins/ErrorHandling.kt` maps each subtype to an HTTP status code
 * and an [ErrorResponse], so services and routes never build error responses themselves.
 *
 * [code] is a stable, machine-readable identifier the app can react to; [message] explains it to a person.
 * Taken from the example base; 413 and 415 were added for photo uploads.
 */
sealed class AppException(
    val code: String,
    override val message: String
) : RuntimeException(message) {

    /** 400: the input is invalid. */
    class BadRequest(message: String, code: String = "INVALID_REQUEST") : AppException(code, message)

    /** 401: the caller is not authenticated. */
    class Unauthorized(message: String, code: String = "UNAUTHORIZED") : AppException(code, message)

    /** 403: the caller is authenticated but not allowed to do this. */
    class Forbidden(message: String, code: String = "FORBIDDEN") : AppException(code, message)

    /** 404: the requested resource does not exist. */
    class NotFound(message: String, code: String) : AppException(code, message)

    /** 409: the request conflicts with the current state, e.g. a limit is reached. */
    class Conflict(message: String, code: String) : AppException(code, message)

    /** 413: the request body is larger than allowed. */
    class PayloadTooLarge(message: String, code: String) : AppException(code, message)

    /** 415: the uploaded file has a type the server does not accept. */
    class UnsupportedMediaType(message: String, code: String) : AppException(code, message)
}

/**
 * Like Kotlin's `require`, but throws [AppException.BadRequest], so the client gets a 400 with this message.
 */
inline fun requireValid(condition: Boolean, message: () -> String) {
    if (!condition) {
        throw AppException.BadRequest(message())
    }
}
