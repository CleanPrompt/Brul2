package nl.brul.domain

/**
 * Thrown by the domain when a user tries an action that the business rules
 * do not allow for that user (for example changing someone else's report).
 */
class NotAllowedException(
    message: String
) : RuntimeException(message)
