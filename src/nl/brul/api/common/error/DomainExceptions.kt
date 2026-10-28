// src/nl/brul/api/common/error/DomainExceptions.kt
package nl.brul.api.common.error

/**
 * Exceptions that services throw when a business rule is broken.
 *
 * Each type maps to one HTTP status in ErrorHandling.kt. The [code] ends up
 * in the JSON error body, so clients can react to the exact rule that failed.
 */

/** 404: the requested resource does not exist. */
open class DomainNotFoundException(
    val code: String,
    message: String
) : RuntimeException(message)

/** 403: the user is authenticated but not allowed to do this. */
open class DomainForbiddenException(
    val code: String,
    message: String
) : RuntimeException(message)

/** 409: the request is valid, but conflicts with the current state. */
open class DomainConflictException(
    val code: String,
    message: String
) : RuntimeException(message)

/** 413: the uploaded content is larger than allowed. */
class PayloadTooLargeException(
    val code: String,
    message: String
) : RuntimeException(message)

/** 415: the uploaded content has a type we do not accept. */
class UnsupportedMediaTypeException(
    val code: String,
    message: String
) : RuntimeException(message)
