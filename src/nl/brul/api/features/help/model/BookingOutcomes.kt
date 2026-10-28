// src/nl/brul/api/features/help/model/BookingOutcomes.kt
package nl.brul.api.features.help.model

/**
 * Results of the booking rules, returned as values instead of exceptions.
 *
 * A sealed type forces every caller to handle each outcome, and the tests
 * can compare outcomes directly. The service turns a rejection into the
 * matching HTTP error.
 */

sealed interface JoinOutcome {
    data object SignedUp : JoinOutcome
    data class Waitlisted(val position: Int) : JoinOutcome
    data class Rejected(val reason: JoinRejection) : JoinOutcome
}

enum class JoinRejection {
    /** R2 */
    OWN_REQUEST,

    /** R3: started, ended or closed. */
    NOT_OPEN,

    /** R4: already active, or a final status such as LATE_CANCELLED. */
    ALREADY_JOINED,

    /** R11 */
    BLOCKED,

    /** R5 */
    TIME_CONFLICT
}

sealed interface LeaveOutcome {
    /** [freedPlace] is true when a SIGNED_UP place became available (R8). */
    data class Left(
        val participation: HelpParticipation,
        val freedPlace: Boolean
    ) : LeaveOutcome

    data class Rejected(val reason: LeaveRejection) : LeaveOutcome
}

enum class LeaveRejection {
    NOT_PARTICIPATING,

    /** R7 */
    ALREADY_STARTED
}

sealed interface AttendanceOutcome {
    data class Marked(val participation: HelpParticipation) : AttendanceOutcome
    data class Rejected(val reason: AttendanceRejection) : AttendanceOutcome
}

enum class AttendanceRejection {
    NOT_OWNER,
    NOT_STARTED,
    NOT_SIGNED_UP
}
